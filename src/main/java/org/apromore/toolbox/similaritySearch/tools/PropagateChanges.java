package org.apromore.toolbox.similaritySearch.tools;

import org.apromore.cpf.CanonicalProcessType;
import org.apromore.dao.model.EdgeMappingGandCG;
import org.apromore.dao.model.NodeMappingGandCG;
import org.apromore.service.model.MergeData;
import org.apromore.toolbox.similaritySearch.algorithms.MergedModels_Result_DO;
import org.apromore.toolbox.similaritySearch.algorithms.Variant_Result_DO;
import org.apromore.toolbox.similaritySearch.changePropagation.PropagationClient;
import org.apromore.toolbox.similaritySearch.common.CPFModelParser;
import org.apromore.toolbox.similaritySearch.common.EdgePairFromG2CG;
import org.apromore.toolbox.similaritySearch.common.IdGeneratorHelper;
import org.apromore.toolbox.similaritySearch.common.VertexPairFromG2CG;
import org.apromore.toolbox.similaritySearch.graph.Graph;

import java.io.IOException;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;

/**
 * Created by fengz2 on 2/10/2014.
 */
public class PropagateChanges {

    /*传播从变体到合并模型的更改
     * 参数1：一组规范模型，其中包括原始流程变体，更改的变体和原始合并的模型
     * 参数2：G和CG之间的节点映射，引用数据库节点
     * 参数3：G和CG之间的边映射，引用数据库边
     * 返回：更新的合并规范模型，其中包括已更改的规范化合并模型，更新的顶点和边映射*/
    public static MergeProcesses_Result_DO propagateChangesFromG2CG(ArrayList<MergeData> models, List<NodeMappingGandCG> nodeMappingGandCGs, List<EdgeMappingGandCG> edgeMappingGandCGs ){
        long start = System.nanoTime();

                /*Declare the result data object for the class*/
        MergeProcesses_Result_DO changedMergedCanonicalModel = new MergeProcesses_Result_DO();

        IdGeneratorHelper idGenerator = new IdGeneratorHelper();

        //note: 节点和边的ID为规范ID
//        Graph originalVariantGraph = CPFModelParser.readModel(models.get(0).getCanonicalProcessType(),models.get(0).getNode_id_mapping(),models.get(0).getEdge_id_mapping());
        Graph originalVariantGraph = CPFModelParser.readModel(models.get(0).getCanonicalProcessType());//with canonical ids
        originalVariantGraph.setIdGenerator(idGenerator);
        originalVariantGraph.ID = String.valueOf(models.get(0).getModel_id());
        originalVariantGraph.removeEmptyNodes();

//        Graph changedVariantGraph = CPFModelParser.readModel(models.get(1).getCanonicalProcessType(),models.get(1).getNode_id_mapping(),models.get(1).getEdge_id_mapping());
        /*in this case, if canonical net are disconnected nets, invoke 'readModels'. Otherwise, invoke 'readModel'*/
        Graph changedVariantGraph;
        if(models.get(1).getCanonicalProcessType().getNet().size()>1){
            changedVariantGraph = CPFModelParser.readModels2SingleGraph(models.get(1).getCanonicalProcessType());
        }else {
            changedVariantGraph = CPFModelParser.readModel(models.get(1).getCanonicalProcessType());//with canonical ids
        }
        changedVariantGraph.setIdGenerator(idGenerator);
        changedVariantGraph.ID = String.valueOf(models.get(1).getModel_id());
        //changedVariantGraph.removeEmptyNodes();/*here, we move this code in order to avoid removing process fragment after deleting some edge*/

//        Graph originalMergedGraph = CPFModelParser.readModel(models.get(2).getCanonicalProcessType(),models.get(2).getNode_id_mapping(),models.get(2).getEdge_id_mapping());
        Graph originalMergedGraph = CPFModelParser.readModel(models.get(2).getCanonicalProcessType()); //with canonical ids
        originalMergedGraph.setIdGenerator(idGenerator);
        originalMergedGraph.ID = String.valueOf(models.get(2).getModel_id());
        originalMergedGraph.removeEmptyNodes();

        /////***here we should get the mappings between canonical id and databased id for nodes and edges....
        originalVariantGraph.setNode_id_mapping(models.get(0).getNode_id_mapping());
        originalVariantGraph.setEdge_id_mapping(models.get(0).getEdge_id_mapping());

        changedVariantGraph.setNode_id_mapping(models.get(1).getNode_id_mapping());
        changedVariantGraph.setEdge_id_mapping(models.get(1).getEdge_id_mapping());

        originalMergedGraph.setNode_id_mapping(models.get(2).getNode_id_mapping());
        originalMergedGraph.setEdge_id_mapping(models.get(2).getEdge_id_mapping());

        //above. we should get the mappings between canonical id and databased id for nodes and edges..

        originalVariantGraph.addLabelsToUnNamedEdges();
        changedVariantGraph.addLabelsToUnNamedEdges();
        originalMergedGraph.addLabelsToUnNamedEdges();


                    /*将节点映射bean转换为节点对*/
        List<VertexPairFromG2CG> vertexPairFromG2CGs= convertNodeMappingToVertexPair(nodeMappingGandCGs);

                /*将边映射bean转换为节点对*/
        List<EdgePairFromG2CG> edgePairFromG2CGs = convertEdgeMappingToEdgePair(edgeMappingGandCGs);

        MergedModels_Result_DO changedMergedGraphModel = PropagationClient.start(originalVariantGraph, changedVariantGraph, originalMergedGraph,
                vertexPairFromG2CGs, edgePairFromG2CGs);

                /*Elicits the merged model from result of 'mergeModelsResultDo'*/
        Graph changedMergedGraph = changedMergedGraphModel.getGraph();
        //changedMergedGraph.cleanGraph();
        /*Fill the result data object*/
        changedMergedCanonicalModel.setVertexPairFromG2CGs(changedMergedGraphModel.getVertexMappingGandCG());
        changedMergedCanonicalModel.setEdgePairFromG2CGs(changedMergedGraphModel.getEdgeMappingGandCG());
        changedMergedCanonicalModel.setCanonicalProcessType(CPFModelParser.writeModel(changedMergedGraph, idGenerator));
        long end = System.nanoTime();

        long timeElapsed = end-start;
        Logger log = Logger.getLogger("111");
        try {
            FileHandler fileHandler = new FileHandler("C:/Users/lly/Desktop/test/lly.log");
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
        return changedMergedCanonicalModel;


    }

    /*将更改从合并模型传播到变量集
     * 参数1：一组规范模型，包括：1）原始合并模型，2）更改后的合并模型，以及 3）所有原始流程变体，在当前版本中，有两个原始流程变体。
     * 参数2：G和CG之间的节点映射，该节点引用数据库节点
     * 参数3：G和CG之间的边缘映射，边缘是指数据库边缘
     * 返回：更新的变体规范模型，其中包括更新的规范变体模型，以及所有变体和合并图之间的顶点和边映射*/
    public static PropagateChangesCG2G_Result_DO propagateChangesFromCG2G(ArrayList<MergeData> models, List<NodeMappingGandCG> nodeMappingGandCGs, List<EdgeMappingGandCG> edgeMappingGandCGs ){


        //声明该类的返回值
        PropagateChangesCG2G_Result_DO propagateChangesCG2G_result_do = new PropagateChangesCG2G_Result_DO();
        IdGeneratorHelper idGenerator = new IdGeneratorHelper();

        //边和点的id是规范id
        Graph originalMergedGraph = CPFModelParser.readModel(models.get(0).getCanonicalProcessType());//第一个数据为原总体
        originalMergedGraph.setIdGenerator(idGenerator);
        originalMergedGraph.ID = String.valueOf(models.get(0).getModel_id());
        originalMergedGraph.removeEmptyNodes();

        Graph changedMergedGraph = CPFModelParser.readModel(models.get(1).getCanonicalProcessType());//第二个数据为改变后的总体
        changedMergedGraph.setIdGenerator(idGenerator);
        changedMergedGraph.ID = String.valueOf(models.get(1).getModel_id());
        changedMergedGraph.removeEmptyNodes();

        //第三个数据为变体1
        Graph originalVariantGraph_1 = CPFModelParser.readModel(models.get(2).getCanonicalProcessType());
        originalVariantGraph_1.setIdGenerator(idGenerator);
        originalVariantGraph_1.ID = String.valueOf(models.get(2).getModel_id());
        originalVariantGraph_1.removeEmptyNodes();

        //第四个数据为变体2
        Graph originalVariantGraph_2 = CPFModelParser.readModel(models.get(3).getCanonicalProcessType());
        originalVariantGraph_2.setIdGenerator(idGenerator);
        originalVariantGraph_2.ID = String.valueOf(models.get(3).getModel_id());
        originalVariantGraph_2.removeEmptyNodes();

        /*//第五个数据为变体3
        Graph originalVariantGraph_3 = CPFModelParser.readModel(models.get(4).getCanonicalProcessType());
        originalVariantGraph_3.setIdGenerator(idGenerator);
        originalVariantGraph_3.ID = String.valueOf(models.get(4).getModel_id());
        originalVariantGraph_3.removeEmptyNodes();*/

        //在这里，我们应该获取节点和边的规范ID与数据库ID之间的映射
        originalMergedGraph.setNode_id_mapping(models.get(0).getNode_id_mapping());
        originalMergedGraph.setEdge_id_mapping(models.get(0).getEdge_id_mapping());

        changedMergedGraph.setNode_id_mapping(models.get(1).getNode_id_mapping());
        changedMergedGraph.setEdge_id_mapping(models.get(1).getEdge_id_mapping());

        originalVariantGraph_1.setNode_id_mapping(models.get(2).getNode_id_mapping());
        originalVariantGraph_1.setEdge_id_mapping(models.get(2).getEdge_id_mapping());

        originalVariantGraph_2.setNode_id_mapping(models.get(3).getNode_id_mapping());
        originalVariantGraph_2.setEdge_id_mapping(models.get(3).getEdge_id_mapping());

        //originalVariantGraph_3.setNode_id_mapping(models.get(4).getNode_id_mapping());
        //originalVariantGraph_3.setEdge_id_mapping(models.get(4).getEdge_id_mapping());

        List<Graph> original_graphs = new ArrayList<Graph>();
        original_graphs.add(originalVariantGraph_1);
        original_graphs.add(originalVariantGraph_2);
        //original_graphs.add(originalVariantGraph_3);

        originalMergedGraph.addLabelsToUnNamedEdges();
        changedMergedGraph.addLabelsToUnNamedEdges();
        originalVariantGraph_1.addLabelsToUnNamedEdges();
        originalVariantGraph_2.addLabelsToUnNamedEdges();
        //originalVariantGraph_3.addLabelsToUnNamedEdges();

        //将节点映射转化为节点对
        List<VertexPairFromG2CG> vertexPairFromG2CGs= convertNodeMappingToVertexPair(nodeMappingGandCGs);

        //将边映射转化为边对
        List<EdgePairFromG2CG> edgePairFromG2CGs = convertEdgeMappingToEdgePair(edgeMappingGandCGs);

        //保存应更新的变体id
        List<String> updated_variants_id = new ArrayList<String>();

        Variant_Result_DO variant_result_do = PropagationClient.start_CG2G(originalMergedGraph, changedMergedGraph, original_graphs,
                vertexPairFromG2CGs, edgePairFromG2CGs, updated_variants_id);

        //*从'variant_result_do'的结果中获取合并的模型，映射关系为id：变化的变体
        Map<String,Graph> changedVariantGraphs = variant_result_do.getGraph();
        Map<String, CanonicalProcessType> canonicalProcessTypes = new HashMap<String, CanonicalProcessType>();

        //循环'changedVariantGraphs'这一映射
        Set<String> keyset = changedVariantGraphs.keySet();
        Iterator<String> keySetIterator = keyset.iterator();
        while(keySetIterator.hasNext()){

            String key = keySetIterator.next();
            Graph changedVariantGraph = changedVariantGraphs.get(key);

            //在变体已更新的情况下，我们创建它的cpt，'key'意味着'changedVariantGraph'的初始id
            if(updated_variants_id.contains(key)){
                CanonicalProcessType canonicalProcessType = CPFModelParser.writeModel(changedVariantGraph, idGenerator);
                canonicalProcessTypes.put(key,canonicalProcessType);
            }
        }
        //填充返回值
        propagateChangesCG2G_result_do.setCanonicalProcessTypes(canonicalProcessTypes);
        propagateChangesCG2G_result_do.setEdgePairFromG2CGs(variant_result_do.getEdgePairFromG2CGs());
        propagateChangesCG2G_result_do.setVertexPairFromG2CGs(variant_result_do.getVertexPairFromCG2Gs());

        return propagateChangesCG2G_result_do;

    }

    //将节点映射转化为g2cg的节点对
    private static List<VertexPairFromG2CG> convertNodeMappingToVertexPair(List<NodeMappingGandCG> nodeMappingGandCGs){
        List<VertexPairFromG2CG> vertexPairFromG2CGs = new ArrayList<VertexPairFromG2CG>();

        for(NodeMappingGandCG nodeMappingGandCG : nodeMappingGandCGs){

            String left_G_v_id = nodeMappingGandCG.getId_G_n();
            String right_CG_v_id = nodeMappingGandCG.getId_CG_n();
            String pid_G = nodeMappingGandCG.getPID_G();
            String pid_CG = nodeMappingGandCG.getPID_CG();

            VertexPairFromG2CG vertexPairFromG2CG = new VertexPairFromG2CG(left_G_v_id, right_CG_v_id, pid_G, pid_CG);
            vertexPairFromG2CGs.add(vertexPairFromG2CG);
        }
        return vertexPairFromG2CGs;
    }

    //将边映射转化为g2cg的边对
    private static List<EdgePairFromG2CG> convertEdgeMappingToEdgePair(List<EdgeMappingGandCG> edgeMappingGandCGs){

        List<EdgePairFromG2CG> edgePairFromG2CGs = new ArrayList<EdgePairFromG2CG>();
        /*covert the list 'edgeMappingGandCGs' to an array*/
        EdgeMappingGandCG[] edgeMappingGandCGsArray = edgeMappingGandCGs.toArray(new EdgeMappingGandCG[edgeMappingGandCGs.size()]);

        /*loop the the list of 'edgeMappingGandCGs'*/
        for(int i=0; i<edgeMappingGandCGsArray.length; i++){

            EdgeMappingGandCG edgeMappingGandCG = edgeMappingGandCGsArray[i];

            Integer id = edgeMappingGandCG.getId();
            String id_CG_e = edgeMappingGandCG.getId_CG_e();
            String id_G_e = edgeMappingGandCG.getId_G_e();
            String pid_CG = edgeMappingGandCG.getPID_CG();
            String pid_G = edgeMappingGandCG.getPID_G();
            boolean part_of_mapping = edgeMappingGandCG.getPart_of_mapping();


            if(!id_G_e.equals("visited")){

                List<String> right_CG_p_e_id = new ArrayList<String>();
                //Normally, the right_cg_p_e just contains only one corresponding edge in CG
                right_CG_p_e_id.add(id_CG_e);

                        /*loop the list of 'edgeMappingGandCGs' again to merge multiple mapping, i.e. one edge in G
            * is mapped to multiple edges in CG*/
                for(int j=0; j<edgeMappingGandCGsArray.length; j++){

                    String id_CG_e_cp = edgeMappingGandCGsArray[j].getId_CG_e();
                    String id_G_e_cp = edgeMappingGandCGsArray[j].getId_G_e();

                    //if both edge id in G and edge id in CG are same as, set the element already 'visited'
                    if(id_G_e.equals(id_G_e_cp) && id_CG_e.equals(id_CG_e_cp)){

                        EdgeMappingGandCG visited = new EdgeMappingGandCG();
                        visited.setId(0);
                        visited.setId_G_e(new String("visited"));
                        visited.setId_CG_e(new String("visited"));
                        visited.setPID_G(new String("visited"));
                        visited.setPID_CG(new String("visited"));
                        visited.setPart_of_mapping(false);
                        edgeMappingGandCGsArray[j] = visited;

                    }else if( id_G_e.equals(id_G_e_cp) && !id_CG_e.equals(id_CG_e_cp)){

                    /*merge multiple mapping between G and CG.
                    NOTE: the edges has not sorted  so far, maybe it should be sorted ....*/
                        right_CG_p_e_id.add(id_CG_e_cp);
                        /*Then, set the element 'already visited' to avoid more comparison*/
                        EdgeMappingGandCG visited = new EdgeMappingGandCG();
                        visited.setId(0);
                        visited.setId_G_e(new String("visited"));
                        visited.setId_CG_e(new String("visited"));
                        visited.setPID_G(new String("visited"));
                        visited.setPID_CG(new String("visited"));
                        visited.setPart_of_mapping(false);
                        edgeMappingGandCGsArray[j] = visited;
                    }

                }

                EdgePairFromG2CG edgePairFromG2CG = new EdgePairFromG2CG(id_G_e, right_CG_p_e_id, pid_G, pid_CG, part_of_mapping);
                edgePairFromG2CGs.add(edgePairFromG2CG);
            }


        }
        return edgePairFromG2CGs;
    }

}
