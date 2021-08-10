package org.apromore.toolbox.similaritySearch.algorithms;


import org.apromore.toolbox.similaritySearch.common.EdgePairFromG2CG;
import org.apromore.toolbox.similaritySearch.common.IdGeneratorHelper;
import org.apromore.toolbox.similaritySearch.common.VertexPair;
import org.apromore.toolbox.similaritySearch.common.VertexPairFromG2CG;
import org.apromore.toolbox.similaritySearch.common.algos.GraphEditDistanceGreedy;
import org.apromore.toolbox.similaritySearch.common.algos.TwoVertices;
import org.apromore.toolbox.similaritySearch.common.similarity.AssingmentProblem;
import org.apromore.toolbox.similaritySearch.graph.*;
import org.apromore.toolbox.similaritySearch.graph.Vertex.GWType;
import org.apromore.toolbox.similaritySearch.graph.Vertex.Type;
import org.apromore.toolbox.similaritySearch.planarGraphMathing.PlanarGraphMathing.MappingRegions;

import java.io.IOException;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;

/**
 *Substantial Merging Algorithm
 *
 * Partly implemented by:
 *
 * @author <a href="mailto:fzwbmw@gmail.com">Zaiwen FENG</a>
 */

public class MergeModels {

    private static final Logger LOGGER = Logger.getLogger(MergeModels.class.getCanonicalName());

    public static MergedModels_Result_DO mergeModels(Graph g1, Graph g2, IdGeneratorHelper idGenerator, boolean removeEnt, String algortithm, double... param) {
        HashMap<String, String> objectresourceIDMap = new HashMap<String, String>();
        long start = System.nanoTime();

        /*set up the result data object*/
        MergedModels_Result_DO mergeModelsResultDo = new MergedModels_Result_DO();

        /*set up a Graph structure*/
        Graph merged = new Graph();
        merged.setIdGenerator(idGenerator);
        long startTime = System.currentTimeMillis();

        merged.ID = String.valueOf(idGenerator.getNextId());

        //set up a data structure to record the vertex mapping relation between variants and merged model
        LinkedList<VertexPairFromG2CG> mappingfromg2cg = new LinkedList<VertexPairFromG2CG>();

        /*set up a data structure to record the edge mapping relation between variants and merged model*/
        List<EdgePairFromG2CG> edgemappingfromg2cg = new ArrayList<EdgePairFromG2CG>();

        merged.addVertices(g1.getVertices());
        merged.addEdges(g1.getEdges());

        /*set up the vertex mappings between g1 and merged model and put them into a special list as well */
        for (Vertex g1_v : g1.getVertices()){

            mappingfromg2cg.add(new VertexPairFromG2CG(g1_v.getID(), g1_v.getID(), g1.ID, merged.ID));

        }

        /*set up the edge mappings between g1 and merged model and put them into a special list as well*/

        for (Edge g1_e : g1.getEdges()){

            List<String> right = new ArrayList<String> ();

            right.add(g1_e.getId());
            edgemappingfromg2cg.add(new EdgePairFromG2CG(g1_e.getId(), right, g1.ID, merged.ID, false));

        }

        merged.addVertices(g2.getVertices());
        merged.addEdges(g2.getEdges());

        /*set up the vertex mappings between g2 and merged model and put them into a special list as well*/

        for (Vertex g2_v : g2.getVertices()){

            mappingfromg2cg.add(new VertexPairFromG2CG(g2_v.getID(), g2_v.getID(), g2.ID, merged.ID));
        }

        /*set up the edge mappings between g2 and merged model and put them into a special list as well*/

        for (Edge g2_e : g2.getEdges()){

            List<String> right = new ArrayList<String> ();

            right.add(g2_e.getId());
            edgemappingfromg2cg.add(new EdgePairFromG2CG(g2_e.getId(), right, g2.ID, merged.ID, false));
        }

        // add all resources from the first models
        merged.getResources().putAll(g1.getResources());
        // and then look if something represent the same thing
        // do not try to merge objects in one modelass
        mergeResources(g2.getResources().values(), objectresourceIDMap, merged);
        merged.getObjects().putAll(g1.getObjects());
        mergeObjects(g2.getObjects().values(), objectresourceIDMap, merged);

        //set up a data structure to record the mapping relation between variants
        LinkedList<VertexPair> mapping = new LinkedList<VertexPair>();



        if (algortithm.equals("Greedy")) {
            GraphEditDistanceGreedy gedepc = new GraphEditDistanceGreedy();
            Object weights[] = {"ledcutoff", param[0],
                    "cedcutoff", param[1],
                    "vweight", param[2],
                    "sweight", param[3],
                    "eweight", param[4]};

            gedepc.setWeight(weights);

            for (TwoVertices pair : gedepc.compute(g1, g2)) {
                Vertex v1 = g1.getVertexMap().get(pair.v1);
                Vertex v2 = g2.getVertexMap().get(pair.v2);
                if (v1.getType().equals(v2.getType())) {
                    mapping.add(new VertexPair(v1, v2, pair.weight));
                }
            }
        } else if (algortithm.equals("Hungarian")) {
            mapping = AssingmentProblem.getMappingsVetrexUsingNodeMapping(g1, g2, param[0], param[1]);
        }

        // clean mappings from mappings that conflict
        // TODO uncomment
//		removeNonDominanceMappings(mapping);

        if (removeEnt) {
            g1.fillDominanceRelations();
            g2.fillDominanceRelations();
            removeNonDominanceMappings2(mapping);
        }

        MappingRegions mappingRegions = findMaximumCommonRegions(g1, g2, mapping);

        for (LinkedList<VertexPair> region : mappingRegions.getRegions()) {
            for (VertexPair vp : region) {

                /*Check the children of vp.right if existed in the mcr. If yes, it will be added into the queue named 'nodesToProcess'*/
                LinkedList<Vertex> nodesToProcess = new LinkedList<Vertex>();
                for (Vertex c : vp.getRight().getChildren()) {
                    // the child is also part of the mapping
                    // remove the edge from the merged modelass
                    if (containsVertex(region, c)) {
                        nodesToProcess.add(c);
                    }
                }


                for (Vertex c : nodesToProcess) {


                    /*get the Id of the edge to be removed*/
                    String id4edge = merged.getEdgeId(vp.getRight().getID(), c.getID());

                    /*the following three lines of code are to delete edge from vp.getRight() to c in the process variant
                    * and get the label (NOTE: it's note id of the edge) of the edge*/

                    HashSet<String> labels = merged.removeEdge(vp.getRight().getID(), c.getID());
                    vp.getRight().removeChild(c.getID());
                    c.removeParent(vp.getRight().getID());

                    /*the following line of code is to remove the edge mapping between the process variant and the merged model*/
                    EdgePairFromG2CG edgePairFromG2CG = removeEdgeMappingFromG2CG(id4edge,edgemappingfromg2cg );

                    /*the following five lines of codes are to add labels to the edge which is from vp.getLeft() to c.Left*/
                    Vertex cLeft = getMappingPair(mapping, c);
                    Edge e = merged.containsEdge(vp.getLeft().getID(), cLeft.getID());
                    if (e != null) {
                        e.addLabels(labels);
                    }

                    /*the following line of code is to add the new edge mapping between the process variant and the merged model*/
                    addEdgeMappingFromG2CG(edgePairFromG2CG,e.getId(), edgemappingfromg2cg );

                }
            }
            // add annotations for the labels
            for (VertexPair vp : region) {
                Vertex mappingRight = vp.getRight();



                vp.getLeft().addAnnotations(mappingRight.getAnnotationMap());

                // merge object references
                for (VertexObjectRef o : mappingRight.objectRefs) {
                    boolean mergedO = false;
                    for (VertexObjectRef vo : vp.getLeft().objectRefs) {
                        if ((vo.getObjectID().equals(o.getObjectID()) ||
                                objectresourceIDMap.get(o.getObjectID()) != null &&
                                        objectresourceIDMap.get(o.getObjectID()).equals(vo.getObjectID())) &&
                                o.canMerge(vo)) {
                            vo.addModels(o.getModels());
                            mergedO = true;
                            break;
                        }
                    }
                    if (!mergedO) {
                        vp.getLeft().objectRefs.add(o);
                    }
                }

                // merge resource references
                for (VertexResourceRef o : mappingRight.resourceRefs) {
                    boolean mergedO = false;
                    for (VertexResourceRef vo : vp.getLeft().resourceRefs) {
                        if ((vo.getResourceID().equals(o.getResourceID()) ||
                                objectresourceIDMap.get(o.getResourceID()) != null &&
                                        objectresourceIDMap.get(o.getResourceID()).equals(vo.getResourceID())) &&
                                o.canMerge(vo)) {
                            vo.addModels(o.getModels());
                            mergedO = true;
                            break;
                        }
                    }
                    if (!mergedO) {
                        vp.getLeft().resourceRefs.add(o);
                    }
                }
            }
        }

        LinkedList<Vertex> toRemove = new LinkedList<Vertex>();
        // check if some vertices must be removed
        for (Vertex v : merged.getVertices()) {
            if (v.getParents().size() == 0 && v.getChildren().size() == 0) {
                toRemove.add(v);
            }
        }

        for (Vertex v : toRemove) {

            merged.removeVertex(v.getID());

            updateMappingFromG2CG (v, mappingRegions, mappingfromg2cg);

            LOGGER.info("THIS IS TESTING REDEPLOY WITHOUT RESTARTING VIRGO");

        }

        for (LinkedList<VertexPair> region : mappingRegions.getRegions()) {
            for (VertexPair vp : region) {
                boolean addgw = true;
                boolean addgwr = true;

                for (Vertex p : vp.getLeft().getParents()) {
                    if (containsVertex(region, p)) {
                        addgw = false;
                        break;
                    }
                }
                // check parents from second modelass
                // maybe the nodes are concurrent in one modelass but not in the other
                for (Vertex p : vp.getRight().getParents()) {
                    if (containsVertex(region, p)) {
                        addgwr = false;
                        break;
                    }
                }
                if ((addgw || addgwr) && vp.getLeft().getParents().size() == 1 &&
                        vp.getRight().getParents().size() == 1) {

                    String id_body = idGenerator.getNextId();
                    String id_title = new String ("n");
                    String id = id_title.concat(id_body);
                    Vertex newGw = new Vertex(GWType.xor, id);
                    newGw.setConfigurable(true);
                    newGw.setLabel(id);
                    merged.addVertex(newGw);

                    /*update the mapping between g and cg*/
                    //                  VertexPairFromG2CG vpg2cg_auxiliary_g1 = new VertexPairFromG2CG(new String ("null"),newGw.getID(), g1.ID, merged.ID );
                    VertexPairFromG2CG vpg2cg_auxiliary = new VertexPairFromG2CG(new String ("null"),newGw.getID(), new String("null"), merged.ID );
                    //                  VertexPairFromG2CG vpg2cg_auxiliary_g2 = new VertexPairFromG2CG(new String ("null"),newGw.getID(), g2.ID, merged.ID );
                    //VertexPairFromG2CG vpg2cg_auxiliary_g2 = new VertexPairFromG2CG(new String ("null"),newGw.getID(), new String("null"), merged.ID );
                    mappingfromg2cg.add(vpg2cg_auxiliary);
                    //mappingfromg2cg.add(vpg2cg_auxiliary_g2);


                    Vertex v1 = vp.getLeft().getParents().get(0);
                    String s1_id = merged.getEdgeId(v1.getID(),vp.getLeft().getID());
                    HashSet<String> s1 = merged.removeEdge(v1.getID(), vp.getLeft().getID());
                    v1.removeChild(vp.getLeft().getID());
                    vp.getLeft().removeParent(v1.getID());
                    Edge auxiliary_e1 = merged.connectVertices(v1, newGw, s1);
                    //String id_title_e1 = new String("e");
                    //String auxiliary_e1_id = id_title_e1.concat(auxiliary_e1.getId());
                    String auxiliary_e1_id = auxiliary_e1.getId();
                    //auxiliary_e1.setId(auxiliary_e1_id);
                    /*remove the outdated edge mapping for the edge 's1' in the process variant g1*/
                    EdgePairFromG2CG edgePairFromG2CG4s1 =  removeEdgeMappingFromG2CG (s1_id,  edgemappingfromg2cg);

                    Vertex v2 = vp.getRight().getParents().get(0);
                    String s2_id = merged.getEdgeId(v2.getID(), vp.getRight().getID());
                    HashSet<String> s2 = merged.removeEdge(v2.getID(), vp.getRight().getID());
                    v2.removeChild(vp.getRight().getID());
                    vp.getRight().removeParent(v2.getID());
                    Edge auxiliary_e2 = merged.connectVertices(v2, newGw, s2);
                    //String id_title_e2 = new String("e");
                    //String auxiliary_e2_id = id_title_e2.concat(auxiliary_e2.getId());
                    String auxiliary_e2_id = auxiliary_e2.getId();
                    //auxiliary_e2.setId(auxiliary_e2_id);

                    /*remove the outdated edge mapping for the edge 's2' in the process variant g2*/
                    EdgePairFromG2CG edgePairFromG2CG4s2 = removeEdgeMappingFromG2CG(s2_id, edgemappingfromg2cg);

                    HashSet<String> s3 = new HashSet<String>(s1);
                    s3.addAll(s2);
                    Edge auxiliary_e3 = merged.connectVertices(newGw, vp.getLeft(), s3);
                    newGw.addAnnotationsForGw(s3);
                    //String id_title_e3 = new String("e");
                    //String auxiliary_e3_id = id_title_e3.concat(auxiliary_e3.getId());
                    //auxiliary_e3.setId(auxiliary_e3_id);
                    String auxiliary_e3_id = auxiliary_e3.getId();

                    /*update the new edge mapping for the edge 's1' in the process variant g1*/
                    List<String> updated_ids_s1_s3 = new ArrayList<String>();
                    updated_ids_s1_s3.add(auxiliary_e1_id);
                    updated_ids_s1_s3.add(auxiliary_e3_id);
                    addEdgeMappingFromG2CG (edgePairFromG2CG4s1, updated_ids_s1_s3, edgemappingfromg2cg, s1_id);

                    /*update the new edge mapping for the edge 's2' in the process variant g2*/
                    List<String> updated_ids_s2_s3 = new ArrayList<String>();
                    updated_ids_s2_s3.add(auxiliary_e2_id);
                    updated_ids_s2_s3.add(auxiliary_e3_id);
                    addEdgeMappingFromG2CG(edgePairFromG2CG4s2, updated_ids_s2_s3, edgemappingfromg2cg, s2_id);
                }
            }
        }
        for (LinkedList<VertexPair> region : mappingRegions.getRegions()) {
            for (VertexPair vp : region) {
                boolean addgw = true;
                boolean addgwr = true;
                for (Vertex ch : vp.getLeft().getChildren()) {
                    if (containsVertex(region, ch)) {
                        addgw = false;
                        break;
                    }
                }

                // check parents from second modelass
                // maybe the nodes are concurrent in one modelass but not in the other
                for (Vertex ch : vp.getRight().getChildren()) {
                    if (containsVertex(region, ch)) {
                        addgwr = false;
                        break;
                    }
                }
                if ((addgw || addgwr) && vp.getLeft().getChildren().size() == 1 &&
                        vp.getRight().getChildren().size() == 1) {

                    String id_body = idGenerator.getNextId();
                    String id_title = new String("n");
                    String id = id_title.concat(id_body);
                    Vertex newGw = new Vertex(GWType.xor, id);
                    newGw.setLabel(id);
                    newGw.setConfigurable(true);
                    merged.addVertex(newGw);

                    /*update the mapping between g and cg*/
//                    VertexPairFromG2CG vpg2cg_auxiliary_g1 = new VertexPairFromG2CG(new String ("null"),newGw.getID(), g1.ID, merged.ID );
                    VertexPairFromG2CG vpg2cg_auxiliary = new VertexPairFromG2CG(new String ("null"),newGw.getID(), new String("null"), merged.ID );
//                    VertexPairFromG2CG vpg2cg_auxiliary_g2 = new VertexPairFromG2CG(new String ("null"),newGw.getID(), g2.ID, merged.ID );
                    //VertexPairFromG2CG vpg2cg_auxiliary_g2 = new VertexPairFromG2CG(new String ("null"),newGw.getID(), new String("null"), merged.ID );
                    mappingfromg2cg.add(vpg2cg_auxiliary);
                    //mappingfromg2cg.add(vpg2cg_auxiliary_g2);


                    Vertex v1 = vp.getLeft().getChildren().get(0);
                    String s1_id = merged.getEdgeId(vp.getLeft().getID(), v1.getID());
                    HashSet<String> s1 = merged.removeEdge(vp.getLeft().getID(), v1.getID());
                    vp.getLeft().removeChild(v1.getID());
                    v1.removeParent(vp.getLeft().getID());
                    Edge auxiliary_e1 = merged.connectVertices(newGw, v1, s1);
                    //String id_title_e1 = new String("e");
                    //String auxiliary_e1_id = id_title_e1.concat(auxiliary_e1.getId());
                    //auxiliary_e1.setId(auxiliary_e1_id);
                    String auxiliary_e1_id = auxiliary_e1.getId();
                     /*remove the outdated edge mapping for the edge 's1' in the process variant g1*/
                    EdgePairFromG2CG edgePairFromG2CG4s1 =  removeEdgeMappingFromG2CG (s1_id,  edgemappingfromg2cg);

                    Vertex v2 = vp.getRight().getChildren().get(0);
                    String s2_id = merged.getEdgeId(vp.getRight().getID(), v2.getID());
                    HashSet<String> s2 = merged.removeEdge(vp.getRight().getID(), v2.getID());
                    vp.getRight().removeChild(v2.getID());
                    v2.removeParent(vp.getRight().getID());
                    Edge auxiliary_e2 = merged.connectVertices(newGw, v2, s2);
                    //String id_title_e2 = new String("e");
                    //String auxiliary_e2_id = id_title_e2.concat(auxiliary_e2.getId());
                    //auxiliary_e2.setId(auxiliary_e2_id);
                    String auxiliary_e2_id = auxiliary_e2.getId();

                    /*remove the outdated edge mapping for the edge 's2' in the process variant g2*/
                    EdgePairFromG2CG edgePairFromG2CG4s2 = removeEdgeMappingFromG2CG(s2_id, edgemappingfromg2cg);

                    HashSet<String> s3 = new HashSet<String>(s1);
                    s3.addAll(s2);
                    Edge auxiliary_e3 = merged.connectVertices(vp.getLeft(), newGw, s3);
                    //String id_title_e3 = new String("e");
                    //String auxiliary_e3_id = id_title_e3.concat(auxiliary_e3.getId());
                    //auxiliary_e3.setId(auxiliary_e3_id);
                    String auxiliary_e3_id = auxiliary_e3.getId();
                    newGw.addAnnotationsForGw(s3);

                    /*update the new edge mapping for the edge 's1' in the process variant g1*/
                    List<String> updated_ids_s1_s3 = new ArrayList<String>();
                    updated_ids_s1_s3.add(auxiliary_e1_id);
                    updated_ids_s1_s3.add(auxiliary_e3_id);
                    addEdgeMappingFromG2CG (edgePairFromG2CG4s1, updated_ids_s1_s3, edgemappingfromg2cg, s1_id);

                    /*update the new edge mapping for the edge 's2' in the process variant g2*/
                    List<String> updated_ids_s2_s3 = new ArrayList<String>();
                    updated_ids_s2_s3.add(auxiliary_e2_id);
                    updated_ids_s2_s3.add(auxiliary_e3_id);
                    addEdgeMappingFromG2CG(edgePairFromG2CG4s2, updated_ids_s2_s3, edgemappingfromg2cg, s2_id);
                }
            }
        }

        mergeConnectors(mappingRegions, merged, mapping);

        toRemove = new LinkedList<Vertex>();
        // check if some vertices must be removed
        for (Vertex v : merged.getVertices()) {
            if (v.getParents().size() == 0 && v.getChildren().size() == 0) {
                toRemove.add(v);
            }
        }

        for (Vertex v : toRemove) {
            merged.removeVertex(v.getID());
             /*update the mapping between g and cg*/
            updateMappingFromG2CG (v, mappingRegions, mappingfromg2cg);
        }

        int[] gwInf = merged.getNrOfConfigGWs();

        long mergeTime = System.currentTimeMillis();

        /*update some mapping from variants to merged model during invoking cleanGraph()*/
        merged.cleanGraph(mappingfromg2cg, edgemappingfromg2cg);

        gwInf = merged.getNrOfConfigGWs();

        // labels for all edges should be added to the modelass
        for (Edge e : merged.getEdges()) {
            e.addLabelToModel();
        }

        long cleanTime = System.currentTimeMillis();

        merged.mergetime = mergeTime - startTime;
        merged.cleanTime = cleanTime - startTime;

        merged.name = ",";

//        for (String l : merged.getEdgeLabels()) {
//            merged.name += l + ",";
//        }

        /*just test*/
        int length_of = merged.name.length();


        merged.name = merged.name.substring(0, merged.name.length() - 1);
//        merged.ID = String.valueOf(idGenerator.getNextId());

        /*Fill the result data object*/
        mergeModelsResultDo.setGraph(merged);
        mergeModelsResultDo.setVertexMappingGandCG(runTransformations(mappingfromg2cg));
        mergeModelsResultDo.setEdgeMappingGandCG(edgemappingfromg2cg);
        long end = System.nanoTime();
        long timeElapsed = end-start;
        Logger log = Logger.getLogger("111");
        try {
            FileHandler fileHandler = new FileHandler("C:/Users/lly/Desktop/test/mer.log");
            fileHandler.setLevel(Level.INFO);
            fileHandler.setFormatter(new Formatter() {

                public String format(LogRecord record) {
                    return record.getLevel() + ": " + record.getMessage() + "\n";
                }
            });
            log.addHandler(fileHandler);
            log.info(timeElapsed +"ms");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mergeModelsResultDo;
    }

    private static void mergeResources(Collection<VertexResource> existing,
                                       HashMap<String, String> objectresourceIDMap, Graph merged) {
        // add resources and objects
        for (VertexResource v : existing) {
            boolean mergedResource = false;
            for (VertexResource mv : merged.getResources().values()) {
                if (mv.canMerge(v)) {
                    objectresourceIDMap.put(v.getId(), mv.getId());

                    if (v.isConfigurable()) {
                        mv.setConfigurable(true);
                    }
                    mv.addModels(v.getModels());
                    mergedResource = true;
                    break;
                }
            }
            // this resource must be added
            if (!mergedResource) {
                merged.getResources().put(v.getId(), v);
            }
        }
    }

    private static void mergeObjects(Collection<VertexObject> existing,
                                     HashMap<String, String> objectresourceIDMap, Graph merged) {
        // add resources and objects
        for (VertexObject v : existing) {
            boolean mergedResource = false;
            for (VertexObject mv : merged.getObjects().values()) {
                if (mv.canMerge(v)) {
                    objectresourceIDMap.put(v.getId(), mv.getId());

                    if (v.isConfigurable()) {
                        mv.setConfigurable(true);
                    }
                    mv.addModels(v.getModels());
                    mergedResource = true;
                    break;
                }
            }
            // this resource must be added
            if (!mergedResource) {
                merged.getObjects().put(v.getId(), v);
            }
        }
    }

    @SuppressWarnings("unused")
    private void removeNonDominanceMappings(LinkedList<VertexPair> mapping) {

        LinkedList<VertexPair> removeList = new LinkedList<VertexPair>();
        int i = 0;

        for (VertexPair vp : mapping) {
            i++;
            // the mapping is already in removed list
            if (removeList.contains(vp)) {
                continue;
            }

            for (int j = i; j < mapping.size(); j++) {
                VertexPair vp1 = mapping.get(j);
                if (vp.getLeft().getID() == vp1.getLeft().getID() ||
                        vp.getRight().getID() == vp1.getRight().getID()) {
                    continue;
                }
                boolean dominanceInG1 = containsInDownwardsPath(vp.getLeft(), vp1.getLeft());
                boolean dominanceInG2 = containsInDownwardsPath(vp.getRight(), vp1.getRight());

                // dominance rule is broken
                if (dominanceInG1 && !dominanceInG2 || !dominanceInG1 && dominanceInG2) {
                    // remove 2 pairs from the pairs list and start with the new pair
                    removeList.add(vp);
                    removeList.add(vp1);
                    break;
                }
            }
        }

        // remove conflicting mappings
        for (VertexPair vp : removeList) {
            mapping.remove(vp);
        }
    }

    @SuppressWarnings("unused")
    private void removeNonDominanceMappings1(LinkedList<VertexPair> mapping) {

        LinkedList<VertexPair> removeList = new LinkedList<VertexPair>();
        int i = 0;

        for (VertexPair vp : mapping) {
            i++;
            // the mapping is already in removed list
            if (removeList.contains(vp)) {
                continue;
            }

            // TODO - if there exists path where A dominances B, then this dominances B
            // even when this is a cycle
            for (int j = i; j < mapping.size(); j++) {
                VertexPair vp1 = mapping.get(j);
                if (vp.getLeft().getID() == vp1.getLeft().getID() ||
                        vp.getRight().getID() == vp1.getRight().getID()) {
                    continue;
                }

                // dominance rule is broken
                if (vp.getLeft().dominance.contains(vp1.getLeft().getID())
                        && vp1.getRight().dominance.contains(vp.getRight().getID())
                        || vp1.getLeft().dominance.contains(vp.getLeft().getID())
                        && vp.getRight().dominance.contains(vp1.getRight().getID())) {
                    // remove 2 pairs from the pairs list and start with the new pair
                    removeList.add(vp);
                    removeList.add(vp1);
                    break;
                }
            }
        }

        // remove conflicting mappings
        for (VertexPair vp : removeList) {
            mapping.remove(vp);
        }
    }

    // implementation of Marlon new dominance mapping relation
    private static void removeNonDominanceMappings2(LinkedList<VertexPair> mapping) {

        LinkedList<VertexPair> removeList = new LinkedList<VertexPair>();
        int i = 0;

        for (VertexPair vp : mapping) {
            i++;
            // the mapping is already in removed list
            if (removeList.contains(vp)) {
                continue;
            }

            for (int j = i; j < mapping.size(); j++) {

                VertexPair vp1 = mapping.get(j);

                // the mapping is already in removed list
                if (removeList.contains(vp1)) {
                    continue;
                }

                // same starting or ending point of models
                if (vp.getLeft().getID() == vp1.getLeft().getID() ||
                        vp.getRight().getID() == vp1.getRight().getID()) {
                    continue;
                }

                // dominance rule is broken
                if ((vp.getLeft().dominance.contains(vp1.getLeft().getID())
                        && vp1.getRight().dominance.contains(vp.getRight().getID())
                        && !(vp1.getLeft().dominance.contains(vp.getLeft().getID())
                        || vp.getRight().dominance.contains(vp1.getRight().getID())))
                        || (vp1.getLeft().dominance.contains(vp.getLeft().getID())
                        && vp.getRight().dominance.contains(vp1.getRight().getID())
                        && !(vp.getLeft().dominance.contains(vp1.getLeft().getID())
                        || vp1.getRight().dominance.contains(vp.getRight().getID())))) {
                    // remove 2 pairs from the pairs list and start with the new pair
                    removeList.add(vp);
                    removeList.add(vp1);
                    break;
                }
            }
        }

        // remove conflicting mappings
        for (VertexPair vp : removeList) {
            mapping.remove(vp);
        }
    }

    private boolean containsInDownwardsPath(Vertex v1, Vertex v2) {

        LinkedList<Vertex> toProcess = new LinkedList<Vertex>();
        toProcess.addAll(v1.getChildren());

        while (toProcess.size() > 0) {
            Vertex process = toProcess.removeFirst();
            if (process.getID() == v2.getID()) {
                return true;
            }
            toProcess.addAll(process.getChildren());
        }
        return false;
    }

    private static void mergeConnectors(MappingRegions mappingRegions, Graph merged, LinkedList<VertexPair> mapping) {
        for (LinkedList<VertexPair> region : mappingRegions.getRegions()) {
            for (VertexPair vp : region) {
                if (vp.getLeft().getType().equals(Type.gateway)) {
                    boolean makeConf = false;
                    LinkedList<Vertex> toProcess = new LinkedList<Vertex>();
                    for (Vertex p : vp.getRight().getParents()) {
                        if (!containsVertex(region, p)) {
                            toProcess.add(p);
                        }
                    }

                    for (Vertex p : toProcess) {
                        makeConf = true;
                        HashSet<String> l = merged.removeEdge(p.getID(), vp.getRight().getID());
                        p.removeChild(vp.getRight().getID());
                        vp.getRight().removeParent(p.getID());
                        merged.connectVertices(p, vp.getLeft(), l);
                    }
                    toProcess = new LinkedList<Vertex>();

                    for (Vertex p : vp.getRight().getChildren()) {
                        if (!containsVertex(region, p)) {
                            toProcess.add(p);
                        }
                    }

                    for (Vertex p : toProcess) {
                        makeConf = true;
                        HashSet<String> l = merged.removeEdge(vp.getRight().getID(), p.getID());
                        p.removeParent(vp.getRight().getID());
                        vp.getRight().removeChild(p.getID());
                        merged.connectVertices(vp.getLeft(), p, l);
                    }
                    if (makeConf) {
                        vp.getLeft().setConfigurable(true);
                    }
                    if (!vp.getLeft().getGWType().equals(vp.getRight().getGWType())) {
                        vp.getLeft().setGWType(GWType.or);
                    }
                }
            }
        }
    }


    private static VertexPair findNextVertexToProcess(LinkedList<VertexPair> mapping, LinkedList<VertexPair> visited) {
        for (VertexPair vp : mapping) {
            VertexPair process = containsMapping(visited, vp.getLeft(), vp.getRight());
            if (process == null) {
                return vp;
            }
        }
        return null;
    }

    private static VertexPair containsMapping(LinkedList<VertexPair> mapping, Vertex left, Vertex right) {
        for (VertexPair vp : mapping) {
            if (vp.getLeft().getID() == left.getID() &&
                    vp.getRight().getID() == right.getID()) {
                return vp;
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    private static boolean containsMapping(LinkedList<VertexPair> mapping, VertexPair v) {
        for (VertexPair vp : mapping) {
            if (vp.getLeft().getID() == v.getLeft().getID() &&
                    vp.getRight().getID() == v.getRight().getID()) {
                return true;
            }
        }
        return false;
    }

    public static MappingRegions findMaximumCommonRegions(Graph g1, Graph g2, LinkedList<VertexPair> mapping) {
        MappingRegions map = new MappingRegions();
        LinkedList<VertexPair> visited = new LinkedList<VertexPair>();

        while (true) {
            VertexPair c = findNextVertexToProcess(mapping, visited);
            if (c == null) {
                break;
            }
            LinkedList<VertexPair> toVisit = new LinkedList<VertexPair>();
            LinkedList<VertexPair> mapRegion = new LinkedList<VertexPair>();

            toVisit.add(c);
            while (toVisit.size() > 0) {
                c = toVisit.removeFirst();
                mapRegion.add(c);

                visited.add(c);
                for (Vertex pLeft : c.getLeft().getParents()) {
                    for (Vertex pRight : c.getRight().getParents()) {
                        VertexPair pairMap = containsMapping(mapping, pLeft, pRight);
                        VertexPair containsMap = containsMapping(visited, pLeft, pRight);
                        VertexPair containsMap1 = containsMapping(toVisit, pLeft, pRight);
                        if (pairMap != null && containsMap == null && containsMap1 == null) {
                            toVisit.add(pairMap);
                        }
                    }
                }

                for (Vertex pLeft : c.getLeft().getChildren()) {
                    for (Vertex pRight : c.getRight().getChildren()) {
                        VertexPair pairMap = containsMapping(mapping, pLeft, pRight);
                        VertexPair containsMap = containsMapping(visited, pLeft, pRight);
                        VertexPair containsMap1 = containsMapping(toVisit, pLeft, pRight);
                        if (pairMap != null && containsMap == null && containsMap1 == null) {
                            toVisit.add(pairMap);
                        }
                    }
                }

            }
            if (mapRegion.size() > 0) {
                map.addRegion(mapRegion);
            }
        }

        return map;
    }

    public static boolean containsVertex(LinkedList<VertexPair> mapping, Vertex v) {
        for (VertexPair vp : mapping) {
            if (vp.getLeft().getID() == v.getID() || vp.getRight().getID() == v.getID()) {

                return true;
            }
        }
        return false;
    }

    public static Vertex getMappingPair(LinkedList<VertexPair> mapping, Vertex v) {
        for (VertexPair vp : mapping) {
            if (vp.getLeft().getID() == v.getID()) {
                return vp.getRight();
            } else if (vp.getRight().getID() == v.getID()) {
                return vp.getLeft();
            }
        }
        return null;
    }


    /*this function is to update the vertex mapping from G to CG*/
    public static void updateMappingFromG2CG (Vertex v_to_be_deleted_from_CG, MappingRegions mappingRegions, LinkedList<VertexPairFromG2CG> mappingfromg2cg){

        VertexPairFromG2CG vpg2cg_del= new VertexPairFromG2CG (new String(), new String(), new String(), new String());

        /*get an iterator*/
        Iterator<VertexPairFromG2CG> ite = mappingfromg2cg.iterator();

        while(ite.hasNext()){
            VertexPairFromG2CG vpg2cg = ite.next();
            if(vpg2cg.getRight_CG_v_id().equals(v_to_be_deleted_from_CG.getID())){
                vpg2cg_del = vpg2cg;
                ite.remove();

            }
        }



         /*replace an element in the special list where the element are deleted from it*/
        for(LinkedList<VertexPair> region : mappingRegions.getRegions()){
            for(VertexPair vp : region){

                if (v_to_be_deleted_from_CG.getID().equals(vp.getLeft().getID())){
                    Vertex right_corresponding = vp.getRight();
                    String right_corresponding_id = right_corresponding.getID();

                    vpg2cg_del.setRight_CG_v_id(right_corresponding_id);

                    mappingfromg2cg.add(vpg2cg_del);

                }else if (v_to_be_deleted_from_CG.getID().equals(vp.getRight().getID())){

                    Vertex left_corresponding = vp.getLeft();
                    String left_corresponding_id = left_corresponding.getID();

                    vpg2cg_del.setRight_CG_v_id(left_corresponding_id);

                    /**upload the updated mapping to the vertexpairfromg2cg list*/
                    mappingfromg2cg.add(vpg2cg_del);
                }
            }
        }
    }

    /*this function is remove the redundant edge mapping in the merged model*/
    public static EdgePairFromG2CG removeEdgeMappingFromG2CG (String id4e_to_be_deleted_from_CG, List<EdgePairFromG2CG> edgemappingfromg2cg){
        EdgePairFromG2CG epg2cg_del = new EdgePairFromG2CG(new String(), new ArrayList<String>(), new String(), new String(), false);

        /*get an iterator*/
        Iterator<EdgePairFromG2CG> ite = edgemappingfromg2cg.iterator();

        /*traverse the list 'edgemappingfromg2cg' in order to remove the mapping item which contains the deleted edge in G*/
        while(ite.hasNext()){

            EdgePairFromG2CG epg2cg = ite.next();

            /*for an edge mapping from G to CG, if it's an edge in merged model, remove this mapping from  'EdgePair'
            *  if it's a path in merged model and the path contains the edge to be removed, also remove the mapping */

            if(epg2cg.getRight_CG_p_e_id().size() == 1){
                if(epg2cg.getRight_CG_p_e_id().get(0).equals(id4e_to_be_deleted_from_CG)){
                    epg2cg_del = epg2cg;
                    ite.remove();
                }
            }
            else{
                /*get an iterator*/
                List<String> right_cg_p_e_id = new ArrayList<String>();
                right_cg_p_e_id = epg2cg.getRight_CG_p_e_id();
                Iterator<String> it = right_cg_p_e_id.iterator();

                while(it.hasNext()){

                    String cg_p_e_id = it.next();
                    if(cg_p_e_id.equals(id4e_to_be_deleted_from_CG)){
                        epg2cg_del = epg2cg;
                        ite.remove();
                    }

                }
            }
        }

        return epg2cg_del;

    }
    /*this function is to add the edge mapping in the merged model*/
    public static void addEdgeMappingFromG2CG (EdgePairFromG2CG edgePairFromG2CG_del, String updated_id4e_in_CG, List<EdgePairFromG2CG> edgemappingfromg2cg){

        EdgePairFromG2CG edgePairFromG2CG_added = new EdgePairFromG2CG(new String(), new ArrayList<String>(), new String(), new String(), false);
        edgePairFromG2CG_added.setPid_CG(edgePairFromG2CG_del.getPid_CG());
        edgePairFromG2CG_added.setIs_part_of_mapping(false);
        edgePairFromG2CG_added.setLeft_G_e_id(edgePairFromG2CG_del.getLeft_G_e_id());
        edgePairFromG2CG_added.setPid_G(edgePairFromG2CG_del.getPid_G());
        List<String> updated_id = new ArrayList<String>();
        updated_id.add(updated_id4e_in_CG);
        edgePairFromG2CG_added.setRight_CG_p_e_id(updated_id);

        edgemappingfromg2cg.add(edgePairFromG2CG_added);
    }

    /*this function is another one to add the edge mapping in the merged model
    * the difference is that the replaced is a string list*/
    public static void addEdgeMappingFromG2CG (EdgePairFromG2CG edgePairFromG2CG_del, List<String> updated_ids, List<EdgePairFromG2CG> edgemappingfromg2cg,
                                               String id4e_previously_deleted_from_CG){


        if(edgePairFromG2CG_del.getRight_CG_p_e_id().size() == 1){
            EdgePairFromG2CG edgePairFromG2CG_added = new EdgePairFromG2CG(new String(), new ArrayList<String>(), new String(), new String(), false);
            edgePairFromG2CG_added.setPid_CG(edgePairFromG2CG_del.getPid_CG());
            edgePairFromG2CG_added.setIs_part_of_mapping(false);
            edgePairFromG2CG_added.setLeft_G_e_id(edgePairFromG2CG_del.getLeft_G_e_id());
            edgePairFromG2CG_added.setPid_G(edgePairFromG2CG_del.getPid_G());
            edgePairFromG2CG_added.setRight_CG_p_e_id(updated_ids);

            edgemappingfromg2cg.add(edgePairFromG2CG_added);
        }else{
            EdgePairFromG2CG edgePairFromG2CG_added = new EdgePairFromG2CG(new String(), new ArrayList<String>(), new String(), new String(), false);
            edgePairFromG2CG_added.setPid_CG(edgePairFromG2CG_del.getPid_CG());
            edgePairFromG2CG_added.setIs_part_of_mapping(true);
            edgePairFromG2CG_added.setLeft_G_e_id(edgePairFromG2CG_del.getLeft_G_e_id());
            edgePairFromG2CG_added.setPid_G(edgePairFromG2CG_del.getPid_G());

            List<String> right_p_e_id = new ArrayList<String>();
            right_p_e_id = edgePairFromG2CG_del.getRight_CG_p_e_id();

            /*delete 'id4e_previously_deleted_from_CG' from 'right_p_e_id'*/
            Iterator<String> it = right_p_e_id.iterator();
            while(it.hasNext()){

                String right_e_id = it.next();
                if(right_e_id.equals(id4e_previously_deleted_from_CG)){
                    it.remove();
                }
            }

            /*combine 'updated_ids' with 'right_p_e_id'*/
            Iterator<String> it_second_time = right_p_e_id.iterator();
            while (it_second_time.hasNext()){
                String right_e_id = it_second_time.next();
                updated_ids.add(right_e_id);
            }

            edgePairFromG2CG_added.setRight_CG_p_e_id(updated_ids);

            edgemappingfromg2cg.add(edgePairFromG2CG_added);
        }
    }

    private static List<VertexPairFromG2CG> runTransformations (LinkedList<VertexPairFromG2CG> mappingfromg2cg){

        /*declare an array list*/
        List<VertexPairFromG2CG> vertexPairFromG2CGs = new ArrayList<VertexPairFromG2CG>();


        /*get an iterator*/
        Iterator<VertexPairFromG2CG> ite = mappingfromg2cg.iterator();

        /*transform vertex pairs linked list into vertex pairs array list*/
        while(ite.hasNext()){
            VertexPairFromG2CG vpg2cg = ite.next();

            VertexPairFromG2CG vp = new VertexPairFromG2CG(new String(""), new String(""), new String(""), new String(""));

            vp.setPid_G(vpg2cg.getPid_G());
            vp.setLeft_G_v_id(vpg2cg.getLeft_G_v_id());
            vp.setPid_CG(vpg2cg.getPid_CG());
            vp.setRight_CG_v_id(vpg2cg.getRight_CG_v_id());

            vertexPairFromG2CGs.add(vp);
        }

        return vertexPairFromG2CGs;
    }

}
