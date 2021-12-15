package org.apromore.toolbox.similaritySearch.tools;

import org.apromore.service.model.MergeData;
import org.apromore.toolbox.similaritySearch.algorithms.MergeModels;
import org.apromore.toolbox.similaritySearch.algorithms.MergedModels_Result_DO;
import org.apromore.toolbox.similaritySearch.common.*;
import org.apromore.toolbox.similaritySearch.graph.Graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/*Revised by Zaiwen FENG from 1ST July 2014*/

public class MergeProcesses {

    /**
     * Finds the Processes Similarity.
     * @param models    the Canonical Process Type
     * @param removeEnt The Canonical Process Type
     * @param algorithm the search Algorithm
     * @param threshold the search merge threshold
     * @param param     the search parameters
     * @return the similarity between processes
     */
    public static MergeProcesses_Result_DO mergeProcesses(ArrayList<MergeData> models, boolean removeEnt, String algorithm,
        double threshold, double... param) {

        /*Declare the result data object for the class*/
        MergeProcesses_Result_DO mergeProcessesResultDo = new MergeProcesses_Result_DO();

//        ArrayList<CanonicalProcessType> cpt = new ArrayList<>(models.values());
//        ArrayList<ProcessModelVersion> pmv = new ArrayList<>(models.keySet());

        IdGeneratorHelper idGenerator = new IdGeneratorHelper();
        Graph m1 = CPFModelParser.readModel(models.get(0).getCanonicalProcessType(), models.get(0).getNode_id_mapping(), models.get(0).getEdge_id_mapping());
        m1.setIdGenerator(idGenerator);
        m1.ID = String.valueOf(models.get(0).getModel_id());
        m1.removeEmptyNodes();
 //       m1.reorganizeIDs();

        Graph m2 = CPFModelParser.readModel(models.get(1).getCanonicalProcessType(), models.get(1).getNode_id_mapping(), models.get(1).getEdge_id_mapping());
        m2.setIdGenerator(idGenerator);
        m2.ID = String.valueOf(models.get(1).getModel_id());
        m2.removeEmptyNodes();
//        m2.reorganizeIDs();


        m1.addLabelsToUnNamedEdges();
        m2.addLabelsToUnNamedEdges();

//        Graph merged = MergeModels.mergeModels(m1, m2, idGenerator, removeEnt, algorithm, param);
        MergedModels_Result_DO mergeModelsResultDo = MergeModels.mergeModels(m1, m2, idGenerator, removeEnt, algorithm, param);

        if (models.size() > 2) {
            for (int i = 2; i < models.size(); i++) {
                Graph m3 = CPFModelParser.readModel(models.get(i).getCanonicalProcessType(), models.get(i).getNode_id_mapping(), models.get(i).getEdge_id_mapping());
                m3.setIdGenerator(idGenerator);
                m3.ID = String.valueOf(models.get(i).getModel_id());
                m3.removeEmptyNodes();
                //m3.reorganizeIDs();
                m3.addLabelsToUnNamedEdges();

                /*To get the vertex mapping as well as edge mapping when 'm1' and 'm2' are merged. */
                List<VertexPairFromG2CG> vertexPairFromG2CGs_m1_m2_CG = new ArrayList<VertexPairFromG2CG>();
                vertexPairFromG2CGs_m1_m2_CG = mergeModelsResultDo.getVertexMappingGandCG();

                List<EdgePairFromG2CG> edgePairFromG2CGs_m1_m2_CG = new ArrayList<EdgePairFromG2CG>();
                edgePairFromG2CGs_m1_m2_CG = mergeModelsResultDo.getEdgeMappingGandCG();

                mergeModelsResultDo = MergeModels.mergeModels(mergeModelsResultDo.getGraph(), m3, idGenerator, removeEnt, algorithm, param);

                /*To get the vertex mapping and edge mapping between temp merged model (m1&m2 merged model), 'm3'and merged model*/
                List<VertexPairFromG2CG> vertexPairFromG2CGs = new ArrayList<VertexPairFromG2CG>();
                vertexPairFromG2CGs = mergeModelsResultDo.getVertexMappingGandCG();
                List<EdgePairFromG2CG> edgePairFromG2CGs = new ArrayList<EdgePairFromG2CG>();
                edgePairFromG2CGs = mergeModelsResultDo.getEdgeMappingGandCG();

                /*Here, we have to reorganize the vertex mapping between m1,m2,m23 and merged model*/
                /*The basic method that we use is to join two array list, i.e. 'vertexPairFromG2CGs' and 'vertexPairFromG2CG_m1_m2_CG'*/

                /*create a new array list named 'addedVertexPairFromG2CG', which is used to save the mappings between m2, m1 and CG*/
                List<VertexPairFromG2CG> addedVertexPairFromG2CGs = new ArrayList<VertexPairFromG2CG>();

                /*iterate the array list 'vertexPairFromG2CGs'*/
                Iterator<VertexPairFromG2CG> vertexPairFromG2CGIterator = vertexPairFromG2CGs.iterator();
                while(vertexPairFromG2CGIterator.hasNext()){

                    VertexPairFromG2CG vertexPairFromG2CG = vertexPairFromG2CGIterator.next();
                    String temp_merged_or_m3_pid = vertexPairFromG2CG.getPid_G();
                    String vertex_id_in_temp_merged_or_m3 = vertexPairFromG2CG.getLeft_G_v_id();
                    String cg_pid = vertexPairFromG2CG.getPid_CG();
                    String vertex_id_in_CG = vertexPairFromG2CG.getRight_CG_v_id();

                    /* So we iterator the array list 'vertexPairFromG2CG_m1_m2_CG'*/

                    Iterator<VertexPairFromG2CG> vertexPairFromG2CGIterator1 = vertexPairFromG2CGs_m1_m2_CG.iterator();
                    while(vertexPairFromG2CGIterator1.hasNext()){

                        VertexPairFromG2CG vertexPairFromG2CG_m1_m2_CG = vertexPairFromG2CGIterator1.next();
                        String temp_merged_pid = vertexPairFromG2CG_m1_m2_CG.getPid_CG();
                        String vertex_id_in_temp_merged = vertexPairFromG2CG_m1_m2_CG.getRight_CG_v_id();
                        String variant_pid = vertexPairFromG2CG_m1_m2_CG.getPid_G();
                        String vertex_id_in_variant = vertexPairFromG2CG_m1_m2_CG.getLeft_G_v_id();

                    /*check if this item, i.e. 'vertexPairFromG2CG' exists in another array list 'vertexPairFromG2CG_m1_m2_CG'*/
                        if((temp_merged_or_m3_pid.equals(temp_merged_pid))&&(vertex_id_in_temp_merged_or_m3.equals(vertex_id_in_temp_merged))){

                            /*if yes, create new mapping item, and add it into 'vertexPairFromG2CG'*/
                            VertexPairFromG2CG new_added_item = new VertexPairFromG2CG(vertex_id_in_variant, vertex_id_in_CG,variant_pid,cg_pid);

                            addedVertexPairFromG2CGs.add(new_added_item);

                        }

                    }



                }

                /*combine two array lists, namely 'addedVertexPairFromG2CGs' and 'VertexPairFromG2CGs'*/
                vertexPairFromG2CGs.addAll(addedVertexPairFromG2CGs);

                /*define an array list 'deletedVertexPairFromGtoCGs', which is used to save the vertex pair that will be removed from 'VertexPairFromGtoCGs'*/
                List<VertexPairFromG2CG> deletedVertexPairFromGtoCGs = new ArrayList<VertexPairFromG2CG>();

                /*iterate the array list 'vertexPairFromG2CGs' for the next round */
                Iterator<VertexPairFromG2CG> vertexPairFromG2CGIterator_2nd_round = vertexPairFromG2CGs.iterator();
                while(vertexPairFromG2CGIterator_2nd_round.hasNext()){

                    VertexPairFromG2CG vertexPairFromG2CG = vertexPairFromG2CGIterator_2nd_round.next();
                    String temp_merged_or_m3_pid = vertexPairFromG2CG.getPid_G();
                    String vertex_id_in_temp_merged_or_m3 = vertexPairFromG2CG.getLeft_G_v_id();

                    /*iterate the array list 'vertexPairFromG2CGs_m1_m2_CG' for the next round,in this round, remove the temporary vertex mapping between
                        * 'CG' and 'temp merged model of m1 and m2'*/
                    Iterator<VertexPairFromG2CG> vertexPairFromG2CGIterator1_next_round = vertexPairFromG2CGs_m1_m2_CG.iterator();
                    while(vertexPairFromG2CGIterator1_next_round.hasNext()){

                        VertexPairFromG2CG vertexPairFromG2CG_m1_m2_CG = vertexPairFromG2CGIterator1_next_round.next();
                        String temp_merged_pid = vertexPairFromG2CG_m1_m2_CG.getPid_CG();
                        String vertex_id_in_temp_merged = vertexPairFromG2CG_m1_m2_CG.getRight_CG_v_id();

                        /*check if this item, i.e. 'vertexPairFromG2CG' exists in another array list 'vertexPairFromG2CG_m1_m2_CG'*/
                        if((temp_merged_or_m3_pid.equals(temp_merged_pid))&&(vertex_id_in_temp_merged_or_m3.equals(vertex_id_in_temp_merged))){

                                /*if yes, remove this item from 'vertexPairFromG2CG'*/
                            deletedVertexPairFromGtoCGs.add(vertexPairFromG2CG);

                        }

                    }


                }

                /*Subtract 'deletedVertexPairFromGtoCGs' from 'VertexPairFromGtoCGs' */
                vertexPairFromG2CGs.removeAll(deletedVertexPairFromGtoCGs);

                /*create a new array list named 'addedEdgePairFromG2CG', which is used to save the edge mappings between m2, m1 and CG*/
                List<EdgePairFromG2CG> addedEdgePairFromG2CGs = new ArrayList<EdgePairFromG2CG>();


                 /*Here, we have to reorganize the edge mapping between m1,m2,m23 and merged model*/
                /*The basic method that we use is to join two array list, i.e. 'edgePairFromG2CGs' and 'edgePairFromG2CG_m1_m2_CG'*/
                                /*iterate the array list 'edgePairFromG2CGs'*/
                Iterator<EdgePairFromG2CG> edgePairFromG2CGIterator = edgePairFromG2CGs.iterator();
                while(edgePairFromG2CGIterator.hasNext()){

                    EdgePairFromG2CG edgePairFromG2CG = edgePairFromG2CGIterator.next();
                    String temp_merged_pid_ = edgePairFromG2CG.getPid_G();
                    String edge_id_in_temp_merged_ = edgePairFromG2CG.getLeft_G_e_id();//edge in temp merged model or in m3
                    boolean is_part_of_path_ = edgePairFromG2CG.getIs_part_of_mapping();
                    String cg_pid = edgePairFromG2CG.getPid_CG();
                    List<String> edge_ids_in_CG = edgePairFromG2CG.getRight_CG_p_e_id();//path in merged model

                    /* So we iterator the array list 'edgePairFromG2CG_m1_m2_CG'*/
                    Iterator<EdgePairFromG2CG> edgePairFromG2CG_m1_m2_CG_Iterator = edgePairFromG2CGs_m1_m2_CG.iterator();
                    while(edgePairFromG2CG_m1_m2_CG_Iterator.hasNext()){

                        EdgePairFromG2CG edgePairFromG2CG_m1_m2_CG = edgePairFromG2CG_m1_m2_CG_Iterator.next();
                        String temp_merged_pid = edgePairFromG2CG_m1_m2_CG.getPid_CG();
                        List<String> edges_id_in_path_in_temp_merged = edgePairFromG2CG_m1_m2_CG.getRight_CG_p_e_id();
                        String variant_pid = edgePairFromG2CG_m1_m2_CG.getPid_G();
                        String edge_id_in_variant = edgePairFromG2CG_m1_m2_CG.getLeft_G_e_id();
                        boolean is_part_of_path = edgePairFromG2CG_m1_m2_CG.getIs_part_of_mapping();

                          /*check if this item, i.e. 'edgePairFromG2CG' exists in another array list 'edgePairFromG2CG_m1_m2_CG'*/
                        if((temp_merged_pid_.equals(temp_merged_pid))&&(edges_id_in_path_in_temp_merged.contains(edge_id_in_temp_merged_))){

                            boolean is_edge_an_part_of_path;
                            if((is_part_of_path==true)||(is_part_of_path_==true)){
                                is_edge_an_part_of_path = true;
                            }else{
                                is_edge_an_part_of_path = false;
                            }

                            /*if yes, create new item, and add it into 'edgePairFromG2CGs'*/
                            EdgePairFromG2CG new_added_item = new EdgePairFromG2CG(edge_id_in_variant,edge_ids_in_CG,variant_pid,cg_pid,is_edge_an_part_of_path);

                            addedEdgePairFromG2CGs.add(new_added_item);

                        }

                    }

                }
                /*combine two array lists, namely 'addedEdgePairFromG2CGs' and 'EdgePairFromG2CGs'*/
                edgePairFromG2CGs.addAll(addedEdgePairFromG2CGs);



                /*define an array list 'deletedEdgePairFromGtoCGs', which is used to save the edge pair that will be removed from 'EdgePairFromGtoCGs'*/
                List<EdgePairFromG2CG> deletedEdgePairFromGtoCGs = new ArrayList<EdgePairFromG2CG>();

                /*iterate the array list 'edgePairFromG2CGs' for the next round */
                Iterator<EdgePairFromG2CG> edgePairFromG2CGIterator_2nd_round = edgePairFromG2CGs.iterator();
                while(edgePairFromG2CGIterator_2nd_round.hasNext()){

                    EdgePairFromG2CG edgePairFromG2CG = edgePairFromG2CGIterator_2nd_round.next();
                    String temp_merged_or_m3_pid = edgePairFromG2CG.getPid_G();
                    String edge_id_in_temp_merged_or_m3 = edgePairFromG2CG.getLeft_G_e_id();

                    /*iterate the array list 'edgePairFromG2CGs_m1_m2_CG' for the next round,in this round, remove the temporary edge mapping between
                        * 'CG' and 'temp merged model of m1 and m2'*/
                    Iterator<EdgePairFromG2CG> edgePairFromG2CGIterator1_next_round = edgePairFromG2CGs_m1_m2_CG.iterator();
                    while(edgePairFromG2CGIterator1_next_round.hasNext()){

                        EdgePairFromG2CG edgePairFromG2CG_m1_m2_CG = edgePairFromG2CGIterator1_next_round.next();
                        String temp_merged_pid = edgePairFromG2CG_m1_m2_CG.getPid_CG();
                        List<String> edges_id_in_path_in_temp_merged = edgePairFromG2CG_m1_m2_CG.getRight_CG_p_e_id();

                        /*check if this item, i.e. 'vertexPairFromG2CG' exists in another array list 'vertexPairFromG2CG_m1_m2_CG'*/
                        if((temp_merged_or_m3_pid.equals(temp_merged_pid))&&edges_id_in_path_in_temp_merged.contains(edge_id_in_temp_merged_or_m3)){

                            /*if yes, remove this item from 'edgePairFromG2CG'*/
                            deletedEdgePairFromGtoCGs.add(edgePairFromG2CG);

                        }

                    }


                }

                /*Subtract 'deletedEdgePairFromGtoCGs' from 'EdgePairFromGtoCGs' */
                edgePairFromG2CGs.removeAll(deletedEdgePairFromGtoCGs);

                /*Last, to be more exact, we combine some edges to be a path in CG, so we need to deal with 'edgePairFromG2CGs'*/
                List<EdgePairFromG2CG> updatedEdgePairFromG2CGs = findPathFromEdgePairFromG2CGs(edgePairFromG2CGs);

                mergeModelsResultDo.setVertexMappingGandCG(vertexPairFromG2CGs);
                mergeModelsResultDo.setEdgeMappingGandCG(updatedEdgePairFromG2CGs);
            }

        }

        /*Elicits the merged model from result of 'mergeModelsResultDo'*/
        Graph merged = mergeModelsResultDo.getGraph();
        /*Fill the result data object*/
        mergeProcessesResultDo.setVertexPairFromG2CGs(mergeModelsResultDo.getVertexMappingGandCG());
        mergeProcessesResultDo.setEdgePairFromG2CGs(mergeModelsResultDo.getEdgeMappingGandCG());
        mergeProcessesResultDo.setCanonicalProcessType(CPFModelParser.writeModel(merged, idGenerator));
        return mergeProcessesResultDo;
    }


    /*Here, to be more exact, we combine some edges to be a path in CG, so we need to deal with 'edgePairFromG2CGs'*/
    public static List<EdgePairFromG2CG> findPathFromEdgePairFromG2CGs(List<EdgePairFromG2CG> originalEdgePairFromG2CGs){

        List<EdgePairFromG2CG> combinedEdgePairFromG2CGs = new ArrayList<EdgePairFromG2CG>();//important

        List<EdgePairFromG2CG> clonedEdgePairFromG2CGs = new ArrayList<EdgePairFromG2CG>();

        /*copy 'originalEdgePairFromG2CG' to 'combinedEdgePairFromG2CG'*/
        Iterator<EdgePairFromG2CG> iterator = originalEdgePairFromG2CGs.iterator();
        while(iterator.hasNext()){

            EdgePairFromG2CG edgePairFromG2CG = iterator.next();

            clonedEdgePairFromG2CGs.add(edgePairFromG2CG);

        }



        Iterator<EdgePairFromG2CG> it = originalEdgePairFromG2CGs.iterator();
        while(it.hasNext()){

            EdgePairFromG2CG edgePairFromG2CG = it.next();

            String left_G_e_id = edgePairFromG2CG.getLeft_G_e_id();
            String pid_G = edgePairFromG2CG.getPid_G();
            String pid_CG = edgePairFromG2CG.getPid_CG();

            /*define a array list to save 'edgePairFromG2CG' with the same 'left_G_e_id', 'pid_CG' and 'pid_G'*/
            List<String> right_CG_p_e_id_updated = new ArrayList<String>();

            List<EdgePairFromG2CG> deletedEdgePairFromG2CGs = new ArrayList<EdgePairFromG2CG>();
            /*loop the 'edgePairFromG2CGs' again, to find the potential path*/
            Iterator<EdgePairFromG2CG> its = clonedEdgePairFromG2CGs.iterator();
            while (its.hasNext()){
                EdgePairFromG2CG edgePairFromG2CG1 = its.next();

                String left_G_e_id_cloned = edgePairFromG2CG1.getLeft_G_e_id();
                List<String> right_CG_p_e_id_cloned = edgePairFromG2CG1.getRight_CG_p_e_id();
                String pid_G1_cloned = edgePairFromG2CG1.getPid_G();
                String pid_CG_cloned = edgePairFromG2CG1.getPid_CG();

                if((left_G_e_id_cloned.equals(left_G_e_id)) && (pid_CG_cloned.equals(pid_CG)) && (pid_G1_cloned.equals(pid_G))){

                    /*update 'right_CG_p_e_id'*/
                    right_CG_p_e_id_updated.addAll(right_CG_p_e_id_cloned);

                    /*remove 'edgePairFromG2CG1' from 'clonedEdgePairFromG2CGs'*/
                    deletedEdgePairFromG2CGs.add(edgePairFromG2CG1);

                }

            }
            /*define a new 'EdgePairFromG2CG'*/
            EdgePairFromG2CG new_item = new EdgePairFromG2CG(new String(""),new ArrayList<String>(),new String(""),new String(""),false);

            if(right_CG_p_e_id_updated.size()>1){

                /*create a new EdgePairFromG2CG object, with updated 'right_CG_p_e_id', which is path*/
                 new_item = new EdgePairFromG2CG(left_G_e_id,right_CG_p_e_id_updated,pid_G,pid_CG,true);
                            /*add 'new_item' to 'combined'*/
                combinedEdgePairFromG2CGs.add(new_item);
                            /*remove 'edgePairFromG2CG' with the same 'left_G_e_id', 'pid_CG' and 'pid_G' from 'clonedEdgePairFromG2CGs'*/
                clonedEdgePairFromG2CGs.removeAll(deletedEdgePairFromG2CGs);

            }else if(right_CG_p_e_id_updated.size()==1) {
                new_item = new EdgePairFromG2CG(left_G_e_id,right_CG_p_e_id_updated,pid_G,pid_CG,false);
                            /*add 'new_item' to 'combined'*/
                combinedEdgePairFromG2CGs.add(new_item);
                            /*remove 'edgePairFromG2CG' with the same 'left_G_e_id', 'pid_CG' and 'pid_G' from 'clonedEdgePairFromG2CGs'*/
                clonedEdgePairFromG2CGs.removeAll(deletedEdgePairFromG2CGs);

            }else if(right_CG_p_e_id_updated.size()==0){

                continue;
            }




        }




        return combinedEdgePairFromG2CGs;
    }
}
