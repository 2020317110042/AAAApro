package org.apromore.toolbox.similaritySearch.changePropagation;

import org.apromore.toolbox.similaritySearch.algorithms.MergedModels_Result_DO;
import org.apromore.toolbox.similaritySearch.algorithms.Variant_Result_DO;
import org.apromore.toolbox.similaritySearch.changePropagation.PropagationCommandFromCG2G.*;
import org.apromore.toolbox.similaritySearch.changePropagation.PropagationCommandFromG2CG.*;
import org.apromore.toolbox.similaritySearch.common.EdgePairFromG2CG;
import org.apromore.toolbox.similaritySearch.common.VertexPairFromG2CG;
import org.apromore.toolbox.similaritySearch.common.algos.ChangeOperation;
import org.apromore.toolbox.similaritySearch.common.algos.ChangeOperationName;
import org.apromore.toolbox.similaritySearch.common.algos.FindChangeOperationSet;
import org.apromore.toolbox.similaritySearch.common.algos.MappingObjBetweenCanonicalIdAndDatabaseId;
import org.apromore.toolbox.similaritySearch.graph.Edge;
import org.apromore.toolbox.similaritySearch.graph.Graph;
import org.apromore.toolbox.similaritySearch.graph.Vertex;

import java.util.*;

public class PropagationClient {

    //用于对比两个变体的改变，并将变更传播到总体上
    public static MergedModels_Result_DO start(Graph g1, Graph g2, Graph original_cg, List<VertexPairFromG2CG> vertexPairFromG2CGs, List<EdgePairFromG2CG> edgePairFromG2CGs){

        //首先，区分“ g1”和“ g2”以获得从g1到g2的更改操作集
        List<ChangeOperation> changeOperationList = FindChangeOperationSet.findGraphEditDistanceInG(g1, g2);

        //接下来，将具有“ pmv” ID的vertexPairs和edgePairs转换为具有规范ID的对，这是更改变体和更改的合并模型之间的INITIAL顶点映射列表
        List<VertexPairFromG2CG> vertexPairFromG2CGs_canonicalId = convertVertexPairsFromPmvIdToCanonicalId(vertexPairFromG2CGs,g1,original_cg);
        List<EdgePairFromG2CG> edgePairFromG2CGs_canonicalId = convertEdgePairsFromPmvIdToCanonicalId(edgePairFromG2CGs, g1,original_cg);

        //基于changeOperationList给出的信息，开始构造Command类
        PropagationInvoker invoker = new PropagationInvoker();
        Iterator<ChangeOperation> it = changeOperationList.iterator();
        while (it.hasNext()){
            ChangeOperation op = it.next();
            ChangeOperationName changeOperationName = op.getChangeOperationName();
            List<Object> changeOperationArgues = op.getChangeOperationArgues();

            //将命令添加到列表，等待添加-—————————————————————————————————————————————————————————————————————————————————————
            if(changeOperationName.equals(ChangeOperationName.APPENDNODETOG)){

                ///*NOTE: 在此保持参数顺序与更改操作函数的参数顺序一致*/
                String id_Of_G = (String)changeOperationArgues.get(0);//get(0)是g的id
                String ending_vertex_id = (String)changeOperationArgues.get(1);//get(1)是新节点的id
                Vertex new_v_to_g = g2.getVertexMap().get(ending_vertex_id);
                //“ new_v_to_g”是附加到“ g1”的节点。 我们将使用它来建立新的节点映射关系，但是“ new_v_to_g”也是“ g2”上的节点


                String starting_vertex_id = (String)changeOperationArgues.get(2);//get(2)是原本就有的被加点的节点id
                Vertex v_p = g1.getVertexMap().get(starting_vertex_id); //'v_p'是'g1'上的老节点，我们将使用它来查找CG中的相应节点，这非常重要
                /*在此保持参数顺序与更改操作函数的参数顺序一致*/

                //在这里，我们还必须获取“ v_p_in_g2”和“ new_v_to_g”之间的边缘的标识符。 此边缘应位于“ g2”上
                Vertex v_p_in_g2 = g2.getVertexMap().get(starting_vertex_id); //“ v_p_in_g2”是“ g2”上的节点，对应于“ g1”中的“ v_p”,两个点的区别在于g1的后面没加点，g2的加了
                Edge newEdge = g2.connectVertices(v_p_in_g2, new_v_to_g);//新边为g2中新加入的边
                HashSet<String> label_in_hashSet = newEdge.getLabels();//获得新边的注释
                String label = convertedHashSetToString(label_in_hashSet);//将Hashset转化为string
                String edge_id = newEdge.getId();//获得新边的id

                //构造命令-_-  o(╥﹏╥)o
                AppendNodePropagationFromG2CGCommand appendNodePropagationFromG2CGCommand = new AppendNodePropagationFromG2CGCommand(original_cg, g1,
                        label, edge_id, v_p, new_v_to_g, vertexPairFromG2CGs_canonicalId, edgePairFromG2CGs_canonicalId);

                /*将此命令添加到命令列表中*/
                invoker.loadCommand(appendNodePropagationFromG2CGCommand);
            }
            else if(changeOperationName.equals(ChangeOperationName.DELETEEDGEFROMG)){

                //*NOTE: 在此保持参数顺序与更改操作函数的参数顺序一致
                String starting_point = (String)changeOperationArgues.get(0);/*获取已删除边缘的起点“ v_p”*/
                Vertex v_p = g1.getVertexMap().get(starting_point);

                String ending_point = (String)changeOperationArgues.get(1);/*获取已删除边缘的终点“ v_s”*/
                Vertex v_s = g1.getVertexMap().get(ending_point);

                /*构造命令*/
                DeleteEdgePropagationFromG2CGCommand deleteEdgePropagationFromG2CGCommand = new
                        DeleteEdgePropagationFromG2CGCommand(original_cg,g1,v_p,v_s,vertexPairFromG2CGs_canonicalId, edgePairFromG2CGs_canonicalId);

                /*将此命令添加到命令列表中*/
                invoker.loadCommand(deleteEdgePropagationFromG2CGCommand);
            }
            else if(changeOperationName.equals(ChangeOperationName.PREPENDNODETOG)){

                String id_Of_G = (String)changeOperationArgues.get(0);//get(0)是g的id
                String starting_vertex_id = (String)changeOperationArgues.get(1);//get(1)是新节点的id
                Vertex new_v_to_g = g2.getVertexMap().get(starting_vertex_id);
                //“new_v_to_g”是预加到“ g1”的节点。 我们将使用它来建立新的节点映射关系，但是“ new_v_to_g”也是“ g2”上的节点

                String ending_vertex_id = (String)changeOperationArgues.get(2);//get(2)是原本就有的被预加点的节点id
                Vertex v_s = g1.getVertexMap().get(ending_vertex_id); //'v_s'是'g1'上的老节点

                //在这里，我们还必须获取“ v_s_in_g2”和“new_v_to_g”之间的边缘的标识符。 此边缘应位于“ g2”上
                Vertex v_s_in_g2 = g2.getVertexMap().get(ending_vertex_id); //“ v_s_in_g2”是“ g2”上的节点，对应于“ g1”中的“ v_s”,两个点的区别在于g1的后面没加点，g2的加了
                Edge newEdge = g2.connectVertices(new_v_to_g, v_s_in_g2);//新边为g2中新加入的边
                HashSet<String> label_in_hashSet = newEdge.getLabels();//获得新边的注释
                String label = convertedHashSetToString(label_in_hashSet);//将Hashset转化为string
                String edge_id = newEdge.getId();//获得新边的id

                /*构造命令*/
                PrependNodePropagationFromG2CGCommand prependNodePropagationFromG2CGCommand = new PrependNodePropagationFromG2CGCommand(original_cg, g1,
                        label, edge_id, new_v_to_g, v_s, vertexPairFromG2CGs_canonicalId, edgePairFromG2CGs_canonicalId);

                /*将此命令添加到命令列表中*/
                invoker.loadCommand(prependNodePropagationFromG2CGCommand);
            }
            else if(changeOperationName.equals(ChangeOperationName.INSERTEDGETOG)){

                //*NOTE: 在此保持参数顺序与更改操作函数的参数顺序一致
                String starting_point = (String)changeOperationArgues.get(0);//获取插入边缘的起点“v_p”
                Vertex v_p = g1.getVertexMap().get(starting_point);

                String ending_point = (String)changeOperationArgues.get(1);//获取插入边缘的终点“v_s”
                Vertex v_s = g1.getVertexMap().get(ending_point);

                Edge newEdge = g2.connectVertices(v_p, v_s);//获得g2中新加入的边
                HashSet<String> label_in_hashSet = newEdge.getLabels();//获得新边的注释
                String label = convertedHashSetToString(label_in_hashSet);//将Hashset转化为string
                String edge_id = newEdge.getId();//获得新边的id

                /*构造命令*/
                InsertEdgePropagationFromG2CGCommand insertEdgePropagationFromG2CGCommand = new
                        InsertEdgePropagationFromG2CGCommand(original_cg,g1,v_p,v_s,label,edge_id,vertexPairFromG2CGs_canonicalId,edgePairFromG2CGs_canonicalId);

                /*将此命令添加到命令列表中*/
                invoker.loadCommand(insertEdgePropagationFromG2CGCommand);
            }
            else if(changeOperationName.equals(ChangeOperationName.UPDATELABELTOG)){

                //*NOTE: 在此保持参数顺序与更改操作函数的参数顺序一致
                String change_point = (String)changeOperationArgues.get(0);
                Vertex v_p = g1.getVertexMap().get(change_point);//获取g1中的点v_p
                Vertex v_p2 = g2.getVertexMap().get(change_point);//获取g2中的点v_p2，其中标签已变化

                String label = v_p2.getLabel();//获得新的的注释

                /*构造命令*/
                UpdateLabelPropagationFromG2CGCommand updateLabelPropagationFromG2CGCommand = new
                        UpdateLabelPropagationFromG2CGCommand(original_cg,g1,label,v_p,vertexPairFromG2CGs_canonicalId,edgePairFromG2CGs_canonicalId);

                /*将此命令添加到命令列表中*/
                invoker.loadCommand(updateLabelPropagationFromG2CGCommand);
            }
            else if(changeOperationName.equals(ChangeOperationName.CHANGEEDGEING)){

                String id_Of_G = (String)changeOperationArgues.get(0);//get(0)是g的id
                String starting_vertex_id = (String)changeOperationArgues.get(1);//get(1)是旧边起点的id
                Vertex v_p = g1.getVertexMap().get(starting_vertex_id);
                String ending_vertex_id = (String)changeOperationArgues.get(2);//get(2)是旧边终点的id
                Vertex v_s = g1.getVertexMap().get(ending_vertex_id);

                String nstarting_vertex_id = (String)changeOperationArgues.get(3);//get(3)是新边起点的id
                Vertex v_p1 = g2.getVertexMap().get(nstarting_vertex_id);
                String nending_vertex_id = (String)changeOperationArgues.get(4);//get(4)是新边终点的id
                Vertex v_s1 = g2.getVertexMap().get(nending_vertex_id);

                Edge newEdge = g2.connectVertices(v_p1, v_s1);//获得g2中新加入的边
                //HashSet<String> label_in_hashSet = newEdge.getLabels();//获得新边的注释
                // label = convertedHashSetToString(label_in_hashSet);//将Hashset转化为string
                String edge_id = newEdge.getId();//获得新边的id

                /*构造命令*/
                ChangeEdgePropagationFromG2CGCommand changeEdgePropagationFromG2CGCommand = new
                        ChangeEdgePropagationFromG2CGCommand(original_cg,g1,v_p,v_s,v_p1,v_s1,id_Of_G,edge_id,vertexPairFromG2CGs_canonicalId,edgePairFromG2CGs_canonicalId);

                /*将此命令添加到命令列表中*/
                invoker.loadCommand(changeEdgePropagationFromG2CGCommand);
            }
            else if(changeOperationName.equals(ChangeOperationName.REMOVENODEFROMG)){

                String change_point = (String)changeOperationArgues.get(0);
                Vertex v = g1.getVertexMap().get(change_point);//获取g1中的点v_p

                /*构造命令*/
                RemoveNodePropagationFromG2CGCommand removeNodePropagationFromG2CGCommand = new
                        RemoveNodePropagationFromG2CGCommand(original_cg,g1,v,vertexPairFromG2CGs_canonicalId,edgePairFromG2CGs_canonicalId);

                /*将此命令添加到命令列表中*/
                invoker.loadCommand(removeNodePropagationFromG2CGCommand);
            }
            else if(changeOperationName.equals(ChangeOperationName.ADDNODETOG)){

                /*NOTE: keep the order of arguments here in accordance with the order of arguments of change operation function*/
                //String new_v_to_g_id = (String)changeOperationArgues.get(0); //**new_v
                //Vertex new_v_to_g = g2.getVertexMap().get(new_v_to_g_id);//'new_v_to_g' is the node added to 'g1'. We will use it to set up new node mapping relationship, but 'new_v_to_g' is the node on 'g2' as well
                Vertex new_v_to_g = (Vertex) changeOperationArgues.get(0);
                String new_v_to_g_id = new_v_to_g.getID();

                String proceeding_vertex_id = (String)changeOperationArgues.get(1);  //**v_p
                Vertex v_p = g1.getVertexMap().get(proceeding_vertex_id); //'v_p' is the node on the 'g1', we will use it to find the corresponding node in CG, it's very important

                String successive_vertex_id = (String)changeOperationArgues.get(2); //**v_s
                Vertex v_s = g1.getVertexMap().get(successive_vertex_id);//'v_s' is the node on the 'g1', we will use it to find the corresponding node in CG,
                //获取label
                Vertex v_p_in_g2 = g2.getVertexMap().get(proceeding_vertex_id);
                Edge newEdge = g2.connectVertices(v_p_in_g2,new_v_to_g);
                HashSet<String> label_in_hash = newEdge.getLabels();
                String label = convertedHashSetToString(label_in_hash);//将Hashset转化为string

                /*get the id of added front edge*/
                String added_front_edge_id = g2.getEdgeId(proceeding_vertex_id,new_v_to_g_id);

                /*get the id of added rear edge*/
                String added_rear_edge_id = g2.getEdgeId(new_v_to_g_id,successive_vertex_id);

                /*construct the command--*/
                AddNodePropagationFromG2CGCommand addNodePropagationFromG2CGCommand = new AddNodePropagationFromG2CGCommand(original_cg,g1,label,added_front_edge_id,added_rear_edge_id,v_p,
                        v_s,new_v_to_g,vertexPairFromG2CGs_canonicalId, edgePairFromG2CGs_canonicalId);

                /*add this command to the list of commands*/
                invoker.loadCommand(addNodePropagationFromG2CGCommand);

            }
            else if(changeOperationName.equals(ChangeOperationName.INSERTNODETOG)){

                /*NOTE: keep the order of arguments here in accordance with the order of arguments of change operation function*/
                //String new_v_to_g_id = (String)changeOperationArgues.get(0); //**new_v
                //Vertex new_v_to_g = g2.getVertexMap().get(new_v_to_g_id);//'new_v_to_g' is the node added to 'g1'. We will use it to set up new node mapping relationship, but 'new_v_to_g' is the node on 'g2' as well
                Vertex new_v_to_g = (Vertex) changeOperationArgues.get(0);
                String new_v_to_g_id = new_v_to_g.getID();

                String proceeding_vertex_id = (String)changeOperationArgues.get(1);  //**v_p
                Vertex v_p = g1.getVertexMap().get(proceeding_vertex_id); //'v_p' is the node on the 'g1', we will use it to find the corresponding node in CG, it's very important

                String successive_vertex_id = (String)changeOperationArgues.get(2); //**v_s
                Vertex v_s = g1.getVertexMap().get(successive_vertex_id);//'v_p' is the node on the 'g1', we will use it to find the corresponding node in CG

                /*get the id of added front edge*/
                String added_front_edge_id = g2.getEdgeId(proceeding_vertex_id,new_v_to_g_id);

                /*get the id of added rear edge*/
                String added_rear_edge_id = g2.getEdgeId(new_v_to_g_id,successive_vertex_id);

                InsertNodePropagationFromG2CGCommand insertNodePropagationFromG2CGCommand = new InsertNodePropagationFromG2CGCommand(original_cg,g1,added_front_edge_id,added_rear_edge_id,v_p,
                        v_s,new_v_to_g,vertexPairFromG2CGs_canonicalId, edgePairFromG2CGs_canonicalId);

                invoker.loadCommand(insertNodePropagationFromG2CGCommand);

            }

            //执行命令-————————————————————————————————————————————————————————————————————————————————————-——————————————
            invoker.invokeCommands();
        }

        MergedModels_Result_DO changedMergedModel = new MergedModels_Result_DO();
        changedMergedModel.setGraph(original_cg);
        changedMergedModel.setVertexMappingGandCG(vertexPairFromG2CGs_canonicalId);
        changedMergedModel.setEdgeMappingGandCG(edgePairFromG2CGs_canonicalId);
        return changedMergedModel;
    }

    //用于对比两个总体的改变，并将变更传播到对应的几个变体上
    public static Variant_Result_DO start_CG2G(Graph cg1, Graph cg2, List<Graph> original_graphs, List<VertexPairFromG2CG> vertexPairFromG2CGs_before_prop,
                                               List<EdgePairFromG2CG> edgePairFromG2CGs_before_prop, List<String> updated_variants_id){

        //建立original_graphs_map这一映射保存original_graphs中的变体及其ID
        Map<String, Graph> original_graphs_map = new HashMap<String, Graph>();
        Iterator<Graph> it_original_graphs = original_graphs.iterator();
        while(it_original_graphs.hasNext()){

            Graph graph = it_original_graphs.next();
            original_graphs_map.put(graph.ID, graph);
        }

        //得到从cg1变化到cg2的操作集
        List<ChangeOperation> changeOperationList = FindChangeOperationSet.findGraphEditDistanceInCG(cg1, cg2);

        //将具有"pmv"ID的节点对、边对，转换为具有规范ID的对
        List<VertexPairFromG2CG> vertexPairFromG2CGs_canonicalId = convertVertexPairsFromPmvIdToCanonicalId_from_CG_to_G(vertexPairFromG2CGs_before_prop,original_graphs,cg1);
        List<EdgePairFromG2CG> edgePairFromCG2Gs_canonicalId = convertEdgePairsFromPmvIdToCanonicalId_from_CG_to_G(edgePairFromG2CGs_before_prop, original_graphs, cg1);

        //基于changeOperationList给出的信息，开始构造Command类
        PropagationInvoker invoker = new PropagationInvoker();
        Iterator<ChangeOperation> it = changeOperationList.iterator();
        while (it.hasNext()){
            ChangeOperation op = it.next();
            ChangeOperationName changeOperationName = op.getChangeOperationName();
            List<Object> changeOperationArgues = op.getChangeOperationArgues();

            //将命令添加到列表，等待添加-—————————————————————————————————————————————————————————————————————————————————————
            if(changeOperationName.equals(ChangeOperationName.PREPENDNODETOCG)){

                String starting_vertex_id = (String)changeOperationArgues.get(0);//第一个值为new_v的id
                Vertex new_v_to_cg = cg2.getVertexMap().get(starting_vertex_id);//new_v_to_cg是将要预加到cg1的节点，可以在cg2中找到
                String ending_vertex_id = (String)changeOperationArgues.get(1); //第二个值为v_s的id
                Vertex v_s = cg1.getVertexMap().get(ending_vertex_id); //v_s可以在cg1中找到
                HashSet<String> labels_on_added_edge = (HashSet<String>)changeOperationArgues.get(2); //第三个值为新边上的(复数个)标签
                //得到cg2中新边的id
                String prepended_edge_id = cg2.getEdgeId(starting_vertex_id,ending_vertex_id);

                /*构造命令*/
                PrependNodePropagationFromCG2GCommand prependNodePropagationFromCG2GCommand = new PrependNodePropagationFromCG2GCommand(cg1, original_graphs_map,
                        new_v_to_cg, v_s, labels_on_added_edge, vertexPairFromG2CGs_canonicalId, edgePairFromCG2Gs_canonicalId, updated_variants_id, prepended_edge_id);

                /*将此命令添加到命令列表中*/
                invoker.loadCommand(prependNodePropagationFromCG2GCommand);
            }
            else if(changeOperationName.equals(ChangeOperationName.APPENDNODETOCG)){

                ///*NOTE: keep the order of arguments here in accordance with the order of arguments of change operation function*/
                String id_Of_CG = (String)changeOperationArgues.get(0);

                String ending_vertex_id = (String)changeOperationArgues.get(1); //**new_v
                Vertex new_v_to_cg = cg2.getVertexMap().get(ending_vertex_id);//'new_v_to_cg' is the node appended to 'cg1'. We will use it to set up new node mapping relationship, but 'new_v_to_cg' is the node on 'cg2' as well

                String starting_vertex_id = (String)changeOperationArgues.get(2);  //**v_p
                Vertex v_p = cg1.getVertexMap().get(starting_vertex_id); //'v_p' is the node on the 'cg1', we will use it to find the corresponding node in G, it's very important

                HashSet<String> labels_on_added_edge = (HashSet<String>)changeOperationArgues.get(3); //**label(s) on added edge
                /*keep the order of arguments here in accordance with the order of arguments of change operation function*/

                /*get the id of appended edge*/
                String appended_edge_id = cg2.getEdgeId(starting_vertex_id,ending_vertex_id);

                /*construct the command--*/
                AppendNodePropagationFromCG2GCommand appendNodePropagationFromCG2GCommand = new AppendNodePropagationFromCG2GCommand(cg1, original_graphs_map,
                        v_p, new_v_to_cg, labels_on_added_edge, vertexPairFromG2CGs_canonicalId, edgePairFromCG2Gs_canonicalId, updated_variants_id, appended_edge_id);

                /*add this command to the list of commands*/
                invoker.loadCommand(appendNodePropagationFromCG2GCommand);

            }
            else if(changeOperationName.equals(ChangeOperationName.ADDNODETOCG)){

                /*NOTE: keep the order of arguments here in accordance with the order of arguments of change operation function*/
                String new_v_to_cg_id = (String)changeOperationArgues.get(0); //**new_v
                Vertex new_v_to_cg = cg2.getVertexMap().get(new_v_to_cg_id);//'new_v_to_cg' is the node appended to 'cg1'. We will use it to set up new node mapping relationship, but 'new_v_to_cg' is the node on 'cg2' as well

                String proceeding_vertex_id = (String)changeOperationArgues.get(1);  //**v_p
                Vertex v_p = cg1.getVertexMap().get(proceeding_vertex_id); //'v_p' is the node on the 'cg1', we will use it to find the corresponding node in G, it's very important

                String successive_vertex_id = (String)changeOperationArgues.get(2);
                Vertex v_s = cg1.getVertexMap().get(successive_vertex_id);//'v_p' is the node on the 'cg1', we will use it to find the corresponding node in G,

                HashSet<String> labels_on_added_edge = (HashSet<String>)changeOperationArgues.get(3); //**label(s) on added edge
                /*keep the order of arguments here in accordance with the order of arguments of change operation function*/

                /*get the id of added front edge*/
                String added_front_edge_id = cg2.getEdgeId(proceeding_vertex_id,new_v_to_cg_id);

                /*get the id of added rear edge*/
                String added_rear_edge_id = cg2.getEdgeId(new_v_to_cg_id,successive_vertex_id);

                /*construct the command--*/
                AddNodePropagationFromCG2GCommand addNodePropagationFromCG2GCommand = new AddNodePropagationFromCG2GCommand(cg1, original_graphs_map,
                        new_v_to_cg, v_p, v_s, labels_on_added_edge, vertexPairFromG2CGs_canonicalId, edgePairFromCG2Gs_canonicalId, updated_variants_id,
                        added_front_edge_id,added_rear_edge_id);

                /*add this command to the list of commands*/
                invoker.loadCommand(addNodePropagationFromCG2GCommand);

            }
            else if(changeOperationName.equals(ChangeOperationName.DELETEEDGEANNOTATIONFROMCG)){

                /*NOTE: keep the order of arguments here in accordance with the order of arguments of change operation function*/
                String starting_point_id = (String)changeOperationArgues.get(0); //**v_p
                Vertex starting_point = cg1.getVertexMap().get(starting_point_id);

                String ending_point_id = (String)changeOperationArgues.get(1);  //**v_s
                Vertex ending_point = cg1.getVertexMap().get(ending_point_id); //

                HashSet<String> removed_labels_on_edge_from_sg1 = new HashSet<String>();
                removed_labels_on_edge_from_sg1 = (HashSet<String>)changeOperationArgues.get(2);
                /*keep the order of arguments here in accordance with the order of arguments of change operation function*/

                /*construct the command*/
                RemoveEdgeAnnotationFromCG2GCommand removeEdgeAnnotationFromCG2GCommand = new RemoveEdgeAnnotationFromCG2GCommand(cg1,original_graphs_map,
                        starting_point,ending_point,removed_labels_on_edge_from_sg1,vertexPairFromG2CGs_canonicalId,edgePairFromCG2Gs_canonicalId,updated_variants_id);

                /*add this command to the list of commands*/
                invoker.loadCommand(removeEdgeAnnotationFromCG2GCommand);
            }
            else if(changeOperationName.equals(ChangeOperationName.ADDEDGEANNOTATION)){

                String starting_point_id = (String)changeOperationArgues.get(0);
                Vertex starting_point = cg1.getVertexMap().get(starting_point_id);//起点

                String ending_point_id = (String)changeOperationArgues.get(1);
                Vertex ending_point = cg1.getVertexMap().get(ending_point_id);//终点

                HashSet<String> add_labels_on_edge_from_sg1 = new HashSet<String>();
                add_labels_on_edge_from_sg1 = (HashSet<String>)changeOperationArgues.get(2);//应添加到边的注释

                AddEdgeAnnotationFromCG2GCommand addEdgeAnnotationFromCG2GCommand = new AddEdgeAnnotationFromCG2GCommand(cg1,original_graphs_map,
                        starting_point,ending_point,add_labels_on_edge_from_sg1,vertexPairFromG2CGs_canonicalId,edgePairFromCG2Gs_canonicalId,updated_variants_id);

                invoker.loadCommand(addEdgeAnnotationFromCG2GCommand);
            }
            else if(changeOperationName.equals(ChangeOperationName.UPDATENODEANNOTATION)){

                String change_point = (String)changeOperationArgues.get(0);
                Vertex v_p = cg1.getVertexMap().get(change_point);//获取g1中的点v_p
                Vertex v_p2 = cg2.getVertexMap().get(change_point);//获取g2中的点v_p2，其中标签已变化

                String label = v_p2.getLabel();//获得新的的注释

                /*构造命令*/
                UpdateNodeAnnotationFromCG2GCommand updateNodeAnnotationFromCG2GCommand = new
                        UpdateNodeAnnotationFromCG2GCommand(cg1,original_graphs_map,v_p,label,vertexPairFromG2CGs_canonicalId,edgePairFromCG2Gs_canonicalId,updated_variants_id);

                /*将此命令添加到命令列表中*/
                invoker.loadCommand(updateNodeAnnotationFromCG2GCommand);
            }
            else if(changeOperationName.equals(ChangeOperationName.DELETEEDGEFROMCG)){

                String starting_point_id = (String)changeOperationArgues.get(0); //**v_p
                Vertex starting_point = cg1.getVertexMap().get(starting_point_id);

                String ending_point_id = (String)changeOperationArgues.get(1);  //**v_s
                Vertex ending_point = cg1.getVertexMap().get(ending_point_id); //

                HashSet<String> removed_labels_on_edge_from_sg1 = new HashSet<String>();
                removed_labels_on_edge_from_sg1 = (HashSet<String>)changeOperationArgues.get(2);

                /*执行命令*/
                DeleteEdgePropagationFromCG2GCommand deleteEdgePropagationFromCG2GCommand = new DeleteEdgePropagationFromCG2GCommand(cg1,original_graphs_map,
                        starting_point,ending_point,removed_labels_on_edge_from_sg1,vertexPairFromG2CGs_canonicalId,edgePairFromCG2Gs_canonicalId,updated_variants_id);

                /*将此命令加入到命令集中*/
                invoker.loadCommand(deleteEdgePropagationFromCG2GCommand);


            }
            else if (changeOperationName.equals(ChangeOperationName.INSERTEDGETOCG)){

                String starting_point_id = (String)changeOperationArgues.get(0); //**v_p
                Vertex starting_point = cg1.getVertexMap().get(starting_point_id);

                String ending_point_id = (String)changeOperationArgues.get(1);  //**v_s
                Vertex ending_point = cg1.getVertexMap().get(ending_point_id); //

                HashSet<String> labels_on_added_edge = (HashSet<String>)changeOperationArgues.get(2); //**label(s) on added edge


                String edge_id = cg2.getEdgeId(starting_point_id,ending_point_id);

                InsertEdgePropagationFromCG2GCommand insertEdgePropagationFromCG2GCommand = new InsertEdgePropagationFromCG2GCommand(cg1,original_graphs_map,labels_on_added_edge,starting_point,ending_point,
                        edge_id,vertexPairFromG2CGs_canonicalId,edgePairFromCG2Gs_canonicalId,updated_variants_id);

                invoker.loadCommand(insertEdgePropagationFromCG2GCommand);


            }

            //执行命令-————————————————————————————————————————————————————————————————————————————————————-——————————————
            invoker.invokeCommands();
        }

        Variant_Result_DO changedVariants = new Variant_Result_DO();
        changedVariants.setGraph(original_graphs_map);
        changedVariants.setVertexPairFromCG2Gs(vertexPairFromG2CGs_canonicalId);
        changedVariants.setEdgePairFromG2CGs(edgePairFromCG2Gs_canonicalId);
        return changedVariants;
    }

    //将具有'pmv'id的vertexPairs转换为具有规范id的对，以便从G传播到CG
    public static List<VertexPairFromG2CG> convertVertexPairsFromPmvIdToCanonicalId(List<VertexPairFromG2CG> vertexPairFromG2CGs, Graph g, Graph cg){
        List<VertexPairFromG2CG> vertexPairFromG2CGs_canonicalId = new ArrayList<VertexPairFromG2CG>();

        MappingObjBetweenCanonicalIdAndDatabaseId[] nodeMappingBetweenCanonicalIdAndDatabaseIds_inG = convertMapToObjArray(g.getNode_id_mapping());
        MappingObjBetweenCanonicalIdAndDatabaseId[] nodeMappingBetweenCanonicalIdAndDatabaseIds_inCG = convertMapToObjArray(cg.getNode_id_mapping());

        //loop the 'vertexPairFromG2CGs'
        Iterator<VertexPairFromG2CG> it = vertexPairFromG2CGs.iterator();

        while (it.hasNext()){
            VertexPairFromG2CG vertexPairFromG2CG = it.next();

            String left_pmv_id = vertexPairFromG2CG.getLeft_G_v_id();
            String right_pmv_id = vertexPairFromG2CG.getRight_CG_v_id();
            String pid_g = vertexPairFromG2CG.getPid_G();
            String pid_cg = vertexPairFromG2CG.getPid_CG();

            String left_canonical_id = new String("");
            String right_canonical_id = new String("");

            //convert 'left_pmv_id' to 'left_canonical_id'
            for(MappingObjBetweenCanonicalIdAndDatabaseId mappingObjBetweenCanonicalIdAndDatabaseId : nodeMappingBetweenCanonicalIdAndDatabaseIds_inG){

                if(mappingObjBetweenCanonicalIdAndDatabaseId.getDatabase_id().equals(left_pmv_id)){
                    left_canonical_id = mappingObjBetweenCanonicalIdAndDatabaseId.getCanonical_id();
                }

            }

            //convert 'right_pmv_id' to 'right_canonical_id'
            for(MappingObjBetweenCanonicalIdAndDatabaseId mappingObjBetweenCanonicalIdAndDatabaseId : nodeMappingBetweenCanonicalIdAndDatabaseIds_inCG){

                if(mappingObjBetweenCanonicalIdAndDatabaseId.getDatabase_id().equals(right_pmv_id)){
                    right_canonical_id = mappingObjBetweenCanonicalIdAndDatabaseId.getCanonical_id();
                }
            }

            VertexPairFromG2CG vertexPairFromG2CG_canonicalId = new VertexPairFromG2CG(left_canonical_id, right_canonical_id, pid_g, pid_cg);
            vertexPairFromG2CGs_canonicalId.add(vertexPairFromG2CG_canonicalId);

        }



        return vertexPairFromG2CGs_canonicalId;
    }

    //将具有'pmv'id的vertexPairs转换为具有规范id的对，以便从CG传播到G
    public static List<VertexPairFromG2CG> convertVertexPairsFromPmvIdToCanonicalId_from_CG_to_G(List<VertexPairFromG2CG> vertexPairFromG2CGs, List<Graph> original_graphs, Graph cg){
        List<VertexPairFromG2CG> vertexPairFromG2CGs_canonicalId = new ArrayList<VertexPairFromG2CG>();

        //MappingObjBetweenCanonicalIdAndDatabaseId[] nodeMappingBetweenCanonicalIdAndDatabaseIds_inG = convertMapToObjArray(g.getNode_id_mapping());
        MappingObjBetweenCanonicalIdAndDatabaseId[] nodeMappingBetweenCanonicalIdAndDatabaseIds_inCG = convertMapToObjArray(cg.getNode_id_mapping());
        Map<String, MappingObjBetweenCanonicalIdAndDatabaseId[]> variants_vertex_CanonicalIdAndDatabaseId_map =
                new HashMap<String, MappingObjBetweenCanonicalIdAndDatabaseId[]>();

        for(Graph graph : original_graphs){

            variants_vertex_CanonicalIdAndDatabaseId_map.put(graph.ID, convertMapToObjArray(graph.getNode_id_mapping()));
        }

        //loop the 'vertexPairFromG2CGs'
        Iterator<VertexPairFromG2CG> it = vertexPairFromG2CGs.iterator();

        while (it.hasNext()){
            VertexPairFromG2CG vertexPairFromG2CG = it.next();

            String left_pmv_id = vertexPairFromG2CG.getLeft_G_v_id();
            String right_pmv_id = vertexPairFromG2CG.getRight_CG_v_id();
            String pid_g = vertexPairFromG2CG.getPid_G();
            String pid_cg = vertexPairFromG2CG.getPid_CG();

            String left_canonical_id = new String("null");//in some case, 'left_canonical_id' should be set 'null', which maps to auxiliary node of CG.
            String right_canonical_id = new String("");


            for(Map.Entry<String, MappingObjBetweenCanonicalIdAndDatabaseId[]> entry : variants_vertex_CanonicalIdAndDatabaseId_map.entrySet()){

                if(entry.getKey().equals(pid_g)){

                    MappingObjBetweenCanonicalIdAndDatabaseId[] nodeMappingBetweenCanonicalIdAndDatabaseIds_in_G = entry.getValue();
                    //convert 'left_pmv_id' to 'left_canonical_id'
                    for(MappingObjBetweenCanonicalIdAndDatabaseId mappingObjBetweenCanonicalIdAndDatabaseId : nodeMappingBetweenCanonicalIdAndDatabaseIds_in_G){

                        if(mappingObjBetweenCanonicalIdAndDatabaseId.getDatabase_id().equals(left_pmv_id)){
                            left_canonical_id = mappingObjBetweenCanonicalIdAndDatabaseId.getCanonical_id();
                        }

                    }

                }
            }


            //convert 'right_pmv_id' to 'right_canonical_id'
            for(MappingObjBetweenCanonicalIdAndDatabaseId mappingObjBetweenCanonicalIdAndDatabaseId : nodeMappingBetweenCanonicalIdAndDatabaseIds_inCG){

                if(mappingObjBetweenCanonicalIdAndDatabaseId.getDatabase_id().equals(right_pmv_id)){
                    right_canonical_id = mappingObjBetweenCanonicalIdAndDatabaseId.getCanonical_id();
                }
            }

            VertexPairFromG2CG vertexPairFromG2CG_canonicalId = new VertexPairFromG2CG(left_canonical_id, right_canonical_id, pid_g, pid_cg);
            vertexPairFromG2CGs_canonicalId.add(vertexPairFromG2CG_canonicalId);

        }



        return vertexPairFromG2CGs_canonicalId;
    }

    //将具有“ pmv” ID的edgePairs转换为具有规范ID的对，以从G传播到CG
    public static List<EdgePairFromG2CG> convertEdgePairsFromPmvIdToCanonicalId(List<EdgePairFromG2CG> edgePairFromG2CGs, Graph g, Graph cg){
        List<EdgePairFromG2CG> edgePairFromG2CGs_canonicalId = new ArrayList<EdgePairFromG2CG>();

        MappingObjBetweenCanonicalIdAndDatabaseId[] edgeMappingBetweenCanonicalIdAndDatabaseIds_inG = convertMapToObjArray(g.getEdge_id_mapping());
        MappingObjBetweenCanonicalIdAndDatabaseId[] edgeMappingBetweenCanonicalIdAndDatabaseIds_inCG = convertMapToObjArray(cg.getEdge_id_mapping());

        //loop the 'vertexPairFromG2CGs'
        Iterator<EdgePairFromG2CG> it = edgePairFromG2CGs.iterator();

        while (it.hasNext()){
            EdgePairFromG2CG edgePairFromG2CG = it.next();

            String left_pmv_id = edgePairFromG2CG.getLeft_G_e_id();
            List<String> right_pmv_ids = edgePairFromG2CG.getRight_CG_p_e_id();
            String pid_g = edgePairFromG2CG.getPid_G();
            String pid_cg = edgePairFromG2CG.getPid_CG();
            Boolean is_multiple_mapping = edgePairFromG2CG.getIs_part_of_mapping();

            String left_canonical_id = new String("");
            List<String> right_canonical_ids = new ArrayList<String>();

            //convert 'left_pmv_id' to 'left_canonical_id' for edges
            for(MappingObjBetweenCanonicalIdAndDatabaseId mappingObjBetweenCanonicalIdAndDatabaseId : edgeMappingBetweenCanonicalIdAndDatabaseIds_inG){

                if(mappingObjBetweenCanonicalIdAndDatabaseId.getDatabase_id().equals(left_pmv_id)){
                    left_canonical_id = mappingObjBetweenCanonicalIdAndDatabaseId.getCanonical_id();
                }

            }

            //convert a list 'right_pmv_ids' to 'right_canonical_id' for edges
                //loop
            Iterator<String> ite = right_pmv_ids.iterator();
            while(ite.hasNext()){

                String right_pmv_id = ite.next();
                String right_canonical_id = new String("");

                for(MappingObjBetweenCanonicalIdAndDatabaseId mappingObjBetweenCanonicalIdAndDatabaseId : edgeMappingBetweenCanonicalIdAndDatabaseIds_inCG){


                    if(mappingObjBetweenCanonicalIdAndDatabaseId.getDatabase_id().equals(right_pmv_id)){
                        right_canonical_id = mappingObjBetweenCanonicalIdAndDatabaseId.getCanonical_id();
                    }
                }
                right_canonical_ids.add(right_canonical_id);
            }



            EdgePairFromG2CG edgePairFromG2CG_canonicalId = new EdgePairFromG2CG(left_canonical_id, right_canonical_ids, pid_g, pid_cg, is_multiple_mapping);
            edgePairFromG2CGs_canonicalId.add(edgePairFromG2CG_canonicalId);

        }



        return edgePairFromG2CGs_canonicalId;
    }

    //将具有“ pmv” ID的edgePairs转换为具有规范ID的对，以从CG传播到G
    public static List<EdgePairFromG2CG> convertEdgePairsFromPmvIdToCanonicalId_from_CG_to_G(List<EdgePairFromG2CG> edgePairFromG2CGs, List<Graph> original_graphs, Graph cg){
        List<EdgePairFromG2CG> edgePairFromG2CGs_canonicalId = new ArrayList<EdgePairFromG2CG>();

        //MappingObjBetweenCanonicalIdAndDatabaseId[] edgeMappingBetweenCanonicalIdAndDatabaseIds_inG = convertMapToObjArray(g.getEdge_id_mapping());
        MappingObjBetweenCanonicalIdAndDatabaseId[] edgeMappingBetweenCanonicalIdAndDatabaseIds_in_CG = convertMapToObjArray(cg.getEdge_id_mapping());
        Map<String, MappingObjBetweenCanonicalIdAndDatabaseId[]> variants_edge_CanonicalIdAndDatabaseId_map =
                new HashMap<String, MappingObjBetweenCanonicalIdAndDatabaseId[]>();

        for(Graph graph : original_graphs){

            variants_edge_CanonicalIdAndDatabaseId_map.put(graph.ID, convertMapToObjArray(graph.getEdge_id_mapping()));
        }

        //loop the 'vertexPairFromG2CGs'
        Iterator<EdgePairFromG2CG> it = edgePairFromG2CGs.iterator();

        while (it.hasNext()){
            EdgePairFromG2CG edgePairFromG2CG = it.next();

            String left_pmv_id = edgePairFromG2CG.getLeft_G_e_id();
            List<String> right_pmv_ids = edgePairFromG2CG.getRight_CG_p_e_id();
            String pid_g = edgePairFromG2CG.getPid_G();
            String pid_cg = edgePairFromG2CG.getPid_CG();
            Boolean is_multiple_mapping = edgePairFromG2CG.getIs_part_of_mapping();

            String left_canonical_id = new String("");
            List<String> right_canonical_ids = new ArrayList<String>();

            //convert 'left_pmv_id' to 'left_canonical_id' for edges
            for(Map.Entry<String, MappingObjBetweenCanonicalIdAndDatabaseId[]> entry : variants_edge_CanonicalIdAndDatabaseId_map.entrySet()){

                if(entry.getKey().equals(pid_g)){

                    MappingObjBetweenCanonicalIdAndDatabaseId[] edgeMappingBetweenCanonicalIdAndDatabaseIds_in_G = entry.getValue();
                    //convert 'left_pmv_id' to 'left_canonical_id'
                    for(MappingObjBetweenCanonicalIdAndDatabaseId mappingObjBetweenCanonicalIdAndDatabaseId : edgeMappingBetweenCanonicalIdAndDatabaseIds_in_G){

                        if(mappingObjBetweenCanonicalIdAndDatabaseId.getDatabase_id().equals(left_pmv_id)){
                            left_canonical_id = mappingObjBetweenCanonicalIdAndDatabaseId.getCanonical_id();
                        }
                    }

                }
            }

            //convert a list 'right_pmv_ids' to 'right_canonical_id' for edges
            //loop
            Iterator<String> ite = right_pmv_ids.iterator();
            while(ite.hasNext()){

                String right_pmv_id = ite.next();
                String right_canonical_id = new String("");

                for(MappingObjBetweenCanonicalIdAndDatabaseId mappingObjBetweenCanonicalIdAndDatabaseId : edgeMappingBetweenCanonicalIdAndDatabaseIds_in_CG){


                    if(mappingObjBetweenCanonicalIdAndDatabaseId.getDatabase_id().equals(right_pmv_id)){
                        right_canonical_id = mappingObjBetweenCanonicalIdAndDatabaseId.getCanonical_id();
                    }
                }
                right_canonical_ids.add(right_canonical_id);
            }



            EdgePairFromG2CG edgePairFromG2CG_canonicalId = new EdgePairFromG2CG(left_canonical_id, right_canonical_ids, pid_g, pid_cg, is_multiple_mapping);
            edgePairFromG2CGs_canonicalId.add(edgePairFromG2CG_canonicalId);

        }

        return edgePairFromG2CGs_canonicalId;
    }

    //将映射转换为对象数组以进行边缘映射和节点映射，输入为一个映射，用于保存边缘和节点的规范ID与数据库ID之间的关系。
    public static MappingObjBetweenCanonicalIdAndDatabaseId[] convertMapToObjArray(Map<String, String> map){

        MappingObjBetweenCanonicalIdAndDatabaseId[] mappingArray = new MappingObjBetweenCanonicalIdAndDatabaseId[map.size()];

        //'i'是指向数组的指针
        int i = 0;
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()){

            Map.Entry pairs  = (Map.Entry)it.next();
            String canonical_id = (String)pairs.getKey();
            String database_id = (String)pairs.getValue();

            //create obj
            MappingObjBetweenCanonicalIdAndDatabaseId mappingObj = new MappingObjBetweenCanonicalIdAndDatabaseId();
            mappingObj.setCanonical_id(canonical_id);
            mappingObj.setDatabase_id(database_id);

            mappingArray[i] = mappingObj;
            i++;
            it.remove();
        }
        return mappingArray;
    }

    //将哈希集转换为逗号分隔的字符串
    public static String convertedHashSetToString(HashSet<String> label_in_hashSet){

        String separator = ",";
        int total = label_in_hashSet.size()*separator.length();
        for(String s : label_in_hashSet){
            total += s.length();
        }

        StringBuilder sb = new StringBuilder(total);
        for(String s : label_in_hashSet){

            sb.append(separator).append(s);
        }

        String result = sb.substring(separator.length());//remove the leading separator

        return result;
    }

}




