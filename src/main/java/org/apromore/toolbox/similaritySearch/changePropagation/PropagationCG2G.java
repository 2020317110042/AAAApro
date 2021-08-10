package org.apromore.toolbox.similaritySearch.changePropagation;

import org.apromore.toolbox.similaritySearch.common.EdgePairFromG2CG;
import org.apromore.toolbox.similaritySearch.common.VertexPairFromG2CG;
import org.apromore.toolbox.similaritySearch.graph.Edge;
import org.apromore.toolbox.similaritySearch.graph.Graph;
import org.apromore.toolbox.similaritySearch.graph.Vertex;

import java.util.*;


public class PropagationCG2G {

    //删除边注释操作(CG2G)F
    public static void RemoveEdgeAnnotationPropagationFromCGToG(Graph mergedModel, Map<String, Graph> variants, Vertex v_p, Vertex v_s,
                                                                HashSet<String> removed_labels_on_edge_from_CG, List<VertexPairFromG2CG> vertexPairFromG2CGs,
                                                                List<EdgePairFromG2CG> edgePairFromG2CGs, List<String> updated_variants_id) {

        //从CG移除边缘,起点是“ v_p”，终点是“ v_s”
        ChangeOperationOnMergedModel.DeleteEdgeAnnotationFromCG(mergedModel, v_p.getID(), v_s.getID(), removed_labels_on_edge_from_CG);

        // *接下来，清除'updated_variants_id'的数组列表
        updated_variants_id.clear();

        //循环删除的标签和变体标签，找相同
        Iterator<String> iterator = removed_labels_on_edge_from_CG.iterator();
        while (iterator.hasNext()) {
            String removed_label_on_edge_from_CG = iterator.next();
            Set<String> keySet = variants.keySet();
            Iterator<String> graphIterator = keySet.iterator();
            while (graphIterator.hasNext()) {
                String variant_original_pmv_id = graphIterator.next();
                Graph variant = variants.get(variant_original_pmv_id);
                Set<String> all_edge_labels = variant.getGraphLabels();
                String edge_label_on_variant = new String("");
                Iterator itr = all_edge_labels.iterator();
                while(itr.hasNext()){

                    edge_label_on_variant = itr.next().toString();
                }

                //若有相同的标签，则对该变体进行操作
                if (edge_label_on_variant.equals(removed_label_on_edge_from_CG)) {

                    /*在这里，首先我们尝试找到与合并模型中的“ v_p”和“ v_s”对应的变体中的映射节点*/
                    String v_p_id_in_G = CommonFunction4Propagation.findCorrespondingNodeInG(v_p.getID(), variant, mergedModel, vertexPairFromG2CGs);
                    String v_s_id_in_G = CommonFunction4Propagation.findCorrespondingNodeInG(v_s.getID(), variant, mergedModel, vertexPairFromG2CGs);

                    /*如果没有CG的“ v_p”的对应节点，则表示“ v_p”是辅助顶点，应创建新网关并将其映射到“ v_p”*/
                    Vertex v_p_in_G = new Vertex(v_p.getType(), new String(""), new String(""));
                    if (v_p_id_in_G.equals("")) {
                        v_p_in_G = CommonFunction4Propagation.createAuxNodeMappingInG(v_p,vertexPairFromG2CGs,variant,mergedModel,edgePairFromG2CGs);
                        v_p_id_in_G=v_p_in_G.getID();
                    }
                    /*如果CG的“ v_s”没有对应的节点，则表示“ v_s”是辅助顶点，应创建新网关并映射到“ v_s”*/
                    Vertex v_s_in_G = new Vertex(v_s.getType(), new String(""), new String(""));
                    if (v_s_id_in_G.equals("")) {
                        v_s_in_G = CommonFunction4Propagation.createAuxNodeMappingInG(v_s,vertexPairFromG2CGs,variant,mergedModel,edgePairFromG2CGs);
                        v_s_id_in_G=v_s_in_G.getID();
                    }

                    //移除G边缘的边缘注释
                    ChangeOperationOnProcessVariant.DeleteEdgeAnnotationFromG(variant, v_p_id_in_G, v_s_id_in_G);
                    //记录发生更改的变体的ID
                    updated_variants_id.add(variant.ID);
                }
            }
        }
    }

    //添加边注释操作(CG2G)
    public static void AddEdgeAnnotationPropagationFromCGToG(Graph mergedModel, Map<String, Graph> variants, Vertex v_p, Vertex v_s,
                                                                HashSet<String> add_labels_on_existing_edge_in_CG, List<VertexPairFromG2CG> vertexPairFromG2CGs,
                                                                List<EdgePairFromG2CG> edgePairFromG2CGs, List<String> updated_variants_id) {

        // *接下来，清除'updated_variants_id'的数组列表
        updated_variants_id.clear();

        //循环删除的标签和变体标签，找相同
        Iterator<String> iterator = add_labels_on_existing_edge_in_CG.iterator();
        while (iterator.hasNext()) {
            String add_label_on_edge_from_CG = iterator.next();
            Set<String> keySet = variants.keySet();
            Iterator<String> graphIterator = keySet.iterator();
            while (graphIterator.hasNext()) {
                String variant_original_pmv_id = graphIterator.next();
                Graph variant = variants.get(variant_original_pmv_id);
                Set<String> all_edge_labels = variant.getGraphLabels();
                String edge_label_on_variant = new String("");
                Iterator itr = all_edge_labels.iterator();
                while(itr.hasNext()){
                    edge_label_on_variant = itr.next().toString();
                }

                //若有相同的标签，则对该变体进行操作
                if (edge_label_on_variant.equals(add_label_on_edge_from_CG)) {

                    //*在这里，首先我们尝试找到与合并模型中的“ v_p”和“ v_s”对应的变体中的映射节点
                    String v_p_id_in_G = CommonFunction4Propagation.findCorrespondingNodeInG(v_p.getID(), variant, mergedModel, vertexPairFromG2CGs);
                    String v_s_id_in_G = CommonFunction4Propagation.findCorrespondingNodeInG(v_s.getID(), variant, mergedModel, vertexPairFromG2CGs);

                    //若v_p_id_in_G或v_s_id_in_G为空，则复制一个vp/vs插入g中，或者创建对应的辅助连接器
                    Vertex v_p_in_G = new Vertex(v_p.getType(), v_p.getLabel(), variant.getIdGenerator().getNextId());
                    if (v_p_id_in_G.equals("")) {
                        if(!v_p.getType().toString().equals("gateway")){//若为空，且不是连接器
                            variant.addVertex(v_p_in_G);
                            VertexPairFromG2CG vpg2cg = new VertexPairFromG2CG(v_p_in_G.getID(), v_p.getID(), variant.ID, mergedModel.ID);
                            vertexPairFromG2CGs.add(vpg2cg);
                        }
                        else if(v_p.getType().toString().equals("gateway"))
                            v_p_in_G = CommonFunction4Propagation.createAuxNodeMappingInG(v_p,vertexPairFromG2CGs,variant,mergedModel,edgePairFromG2CGs);
                        v_p_id_in_G=v_p_in_G.getID();
                    }
                    else if(!v_p_id_in_G.equals("")){
                        HashMap<String, Vertex> vertexHashMap = variant.getVertexMap();
                        v_p_in_G = vertexHashMap.get(v_p_id_in_G);
                    }

                    Vertex v_s_in_G = new Vertex(v_s.getType(), v_s.getLabel(), variant.getIdGenerator().getNextId());
                    if (v_s_id_in_G.equals("")) {
                        if(!v_s.getType().toString().equals("gateway")){//若为空，且不是连接器
                            variant.addVertex(v_s_in_G);
                            VertexPairFromG2CG vpg2cg1 = new VertexPairFromG2CG(v_s_in_G.getID(), v_s.getID(), variant.ID, mergedModel.ID);
                            vertexPairFromG2CGs.add(vpg2cg1);
                        }
                        else if(v_s.getType().toString().equals("gateway"))
                            v_s_in_G = CommonFunction4Propagation.createAuxNodeMappingInG(v_s,vertexPairFromG2CGs,variant,mergedModel,edgePairFromG2CGs);
                        v_s_id_in_G=v_s_in_G.getID();
                    }
                    else if(!v_s_id_in_G.equals("")){
                        HashMap<String, Vertex> vertexHashMap = variant.getVertexMap();
                        v_s_in_G = vertexHashMap.get(v_s_id_in_G);
                    }

                    //进行插入边操作
                    ChangeOperationOnProcessVariant.InsertEdgeToG(variant,v_p_in_G,v_s_in_G,edge_label_on_variant,variant.getIdGenerator().getNextId());
                    //然后将注释插入cg中
                    ChangeOperationOnMergedModel.InsertEdgeAnnotationToCG(mergedModel, v_p, v_s, add_labels_on_existing_edge_in_CG);

                    //更新点、边映射
                    Edge right_CG_e = mergedModel.containsEdge(v_p.getID(), v_s.getID());
                    Edge left_G_e = variant.containsEdge(v_p_id_in_G, v_s_id_in_G);
                    List<String> right_CG_p_e_id = new ArrayList<String>();
                    right_CG_p_e_id.add(right_CG_e.getId());
                    EdgePairFromG2CG epg2cg = new EdgePairFromG2CG(left_G_e.getId(), right_CG_p_e_id, variant.ID, mergedModel.ID, new Boolean(true));
                    //此处'is_part_of_mapping'的定义不严格，因为从CG到G的默认边关系为1：1
                    edgePairFromG2CGs.add(epg2cg);

                    //记录发生更改的变体的ID
                    updated_variants_id.add(variant.ID);
                }
            }
        }
    }

    //添加点操作(CG2G)F
    public static void AddNodePropagationFromCG2G(Graph mergedModel, Map<String, Graph> variants, Vertex new_v, Vertex v_p, Vertex v_s,
                                                  HashSet<String> labels_on_new_edge_in_CG, List<VertexPairFromG2CG> vertexPairFromG2CGs,
                                                  List<EdgePairFromG2CG> edgePairFromG2CGs, List<String> updated_variants_id, String front_edge_id, String rear_edge_id) {

        //首先对总体进行添加点操作
        ChangeOperationOnMergedModel.AddNodeToCG(mergedModel, v_p, v_s, new_v, labels_on_new_edge_in_CG, front_edge_id, rear_edge_id);
        //清空修改变体列表
        updated_variants_id.clear();

        //循环新边上的标签
        Iterator<String> edge_labels_on_merged_model = labels_on_new_edge_in_CG.iterator();
        while (edge_labels_on_merged_model.hasNext()) {
            String edge_label_on_merged_model = edge_labels_on_merged_model.next();

            Set<String> keySet = variants.keySet();
            Iterator<String> graphIterator = keySet.iterator();
            while (graphIterator.hasNext()) {
                String variant_original_pmv_id = graphIterator.next();
                Graph variant = variants.get(variant_original_pmv_id);
                Set<String> all_edge_labels = variant.getGraphLabels();
                String edge_label_on_variant = new String("");
                Iterator itr = all_edge_labels.iterator();
                while(itr.hasNext()){
                    edge_label_on_variant = itr.next().toString();//得到当前变体的标签
                }

                //若新边上的标签中和变体标签中有同样的元素
                if (edge_label_on_variant.equals(edge_label_on_merged_model)) {
                    //得到vp和vs在G中的对应点，若没有则代表其是辅助连接器
                    String v_p_id_in_G = CommonFunction4Propagation.findCorrespondingNodeInG(v_p.getID(), variant, mergedModel, vertexPairFromG2CGs);
                    String v_s_id_in_G = CommonFunction4Propagation.findCorrespondingNodeInG(v_s.getID(), variant, mergedModel, vertexPairFromG2CGs);
                    //若没有，则在G中创建对应的连接器
                    Vertex v_p_in_G = null;
                    if (v_p_id_in_G.equals("")) {
                        v_p_in_G = CommonFunction4Propagation.createAuxNodeMappingInG(v_p,vertexPairFromG2CGs,variant,mergedModel,edgePairFromG2CGs);
                        v_p_id_in_G = v_p_in_G.getID();
                    }
                    else if(!v_p_id_in_G.equals("")){
                        HashMap<String, Vertex> vertexHashMap = variant.getVertexMap();
                        v_p_in_G = vertexHashMap.get(v_p_id_in_G);
                    }

                    Vertex v_s_in_G = null;
                    if (v_s_id_in_G.equals("")) {
                        v_s_in_G = CommonFunction4Propagation.createAuxNodeMappingInG(v_s,vertexPairFromG2CGs,variant,mergedModel,edgePairFromG2CGs);
                        v_s_id_in_G = v_s_in_G.getID();
                    }
                    else if(!v_s_id_in_G.equals("")){
                        HashMap<String, Vertex> vertexHashMap = variant.getVertexMap();
                        v_s_in_G = vertexHashMap.get(v_s_id_in_G);
                    }

                    //创建新节点并add
                    Vertex new_v_in_G = new Vertex(new_v.getType(), new_v.getLabel(), variant.getIdGenerator().getNextId());
                    ChangeOperationOnProcessVariant.AddNodeToG(variant, new_v_in_G, v_p_in_G, v_s_in_G, edge_label_on_variant, new String("e").concat(variant.getIdGenerator().getNextId()), new String("e").concat(variant.getIdGenerator().getNextId()));

                    //更新点映射
                    VertexPairFromG2CG vpg2cg = new VertexPairFromG2CG(new_v_in_G.getID(), new_v.getID(), variant.ID, mergedModel.ID);
                    vertexPairFromG2CGs.add(vpg2cg);

                    //更新边映射
                    Edge front_edge_before_new_v_in_CG = mergedModel.containsEdge(v_p.getID(), new_v.getID());
                    Edge front_edge_before_new_v_in_G = variant.containsEdge(v_p_id_in_G, new_v_in_G.getID());
                    List<String> front_path_before_new_v_in_CG = new ArrayList<String>();
                    front_path_before_new_v_in_CG.add(front_edge_before_new_v_in_CG.getId());

                    Edge rear_edge_after_new_v_in_CG = mergedModel.containsEdge(new_v.getID(), v_s.getID());
                    Edge rear_edge_after_new_v_in_G = variant.containsEdge(new_v_in_G.getID(), v_s_id_in_G);
                    List<String> rear_path_after_new_v_in_CG = new ArrayList<String>();
                    rear_path_after_new_v_in_CG.add(rear_edge_after_new_v_in_CG.getId());

                    EdgePairFromG2CG epg2cg_front_part = new EdgePairFromG2CG(front_edge_before_new_v_in_G.getId(), front_path_before_new_v_in_CG, variant.ID, mergedModel.ID, new Boolean(true));
                    EdgePairFromG2CG epg2cg_rear_part = new EdgePairFromG2CG(rear_edge_after_new_v_in_G.getId(), rear_path_after_new_v_in_CG, variant.ID, mergedModel.ID, new Boolean(true));

                    edgePairFromG2CGs.add(epg2cg_front_part);
                    edgePairFromG2CGs.add(epg2cg_rear_part);
                    updated_variants_id.add(variant.ID);
                }
            }//结束变体注释循环
        }//结束边标签循环
    }

    //追加点操作(CG2G)F
    public static void AppendNodePropagationFromCG2G(Graph mergedModel, Map<String, Graph> variants, Vertex v_p, Vertex new_v, HashSet<String> labels_on_new_edge_in_CG,
                                                     List<VertexPairFromG2CG> vertexPairFromG2CGs, List<EdgePairFromG2CG> edgePairFromG2CGs, List<String> updated_variants_id,
                                                     String appended_edge_id_in_CG) {

        //首先在合并图中追加节点new_v
        ChangeOperationOnMergedModel.AppendNodeToCG(mergedModel, v_p, new_v, labels_on_new_edge_in_CG, appended_edge_id_in_CG);

        //清除'updated_variants_id'中的数据
        updated_variants_id.clear();

        Iterator<String> edge_labels_on_merged_model = labels_on_new_edge_in_CG.iterator();
        while (edge_labels_on_merged_model.hasNext()) {//对labels_on_new_edge_in_CG的每一个标签进行循环
            String edge_label_on_merged_model = edge_labels_on_merged_model.next();//得到cg新边上的当前标签★

            Set<String> keySet = variants.keySet();//得到所有变体的标签
            Iterator<String> graphIterator = keySet.iterator();
            while (graphIterator.hasNext()) {//对每一个变体的标签进行循环
                String variant_original_pmv_id = graphIterator.next();
                Graph variant = variants.get(variant_original_pmv_id);
                Set<String> all_edge_labels = variant.getGraphLabels();
                String edge_label_on_variant = new String("");
                Iterator itr = all_edge_labels.iterator();
                while(itr.hasNext()){
                    edge_label_on_variant = itr.next().toString();//获取当前变体的标签★
                }

                //实际上循环了cg上新边的所有标签，以及两个变体的所有标签，看他们之间有多少相似的
                if (edge_label_on_variant.equals(edge_label_on_merged_model)) {//若当前边标签与当前变体标签相同，说明该变更应传播到当前变体上★

                    String v_p_id_in_G = CommonFunction4Propagation.findCorrespondingNodeInG(v_p.getID(), variant, mergedModel, vertexPairFromG2CGs);//首先尝试寻找与cg中v_p对应的变体中的节点
                    Vertex v_p_in_G = null;

                    //若v_p_id_in_G不为空，则v_p_in_G为其对应的顶点
                    if(!v_p_id_in_G.equals("")){
                        HashMap<String, Vertex> vertexHashMap = variant.getVertexMap();
                        v_p_in_G = vertexHashMap.get(v_p_id_in_G);
                    }

                    //若v_p_id_in_G为空，则v_p_in_G为新创建的网关，并将其映射到v_p*
                    else if (v_p_id_in_G.equals("")) {
                        v_p_in_G = CommonFunction4Propagation.createAuxNodeMappingInG(v_p,vertexPairFromG2CGs,variant,mergedModel,edgePairFromG2CGs);//在变体中创建新的点，并建立映射
                        v_p_id_in_G = v_p_in_G.getID();
                    }

                    //创建新点并将其追加到g中
                    Vertex new_v_in_G = new Vertex(new_v.getType(), new_v.getLabel(), variant.getIdGenerator().getNextId());
                    ChangeOperationOnProcessVariant.AppendNodeToG(variant, new_v_in_G, v_p_in_G, edge_label_on_variant, new String("e").concat(variant.getIdGenerator().getNextId()));

                    //更新点映射
                    VertexPairFromG2CG vpg2cg = new VertexPairFromG2CG(new_v_in_G.getID(), new_v.getID(), variant.ID, mergedModel.ID);
                    vertexPairFromG2CGs.add(vpg2cg);

                    //更新边映射
                    Edge right_CG_e = mergedModel.containsEdge(v_p.getID(), new_v.getID());
                    Edge left_G_e = variant.containsEdge(v_p_id_in_G, new_v_in_G.getID());
                    List<String> right_CG_p_e_id = new ArrayList<String>();
                    right_CG_p_e_id.add(right_CG_e.getId());
                    EdgePairFromG2CG epg2cg = new EdgePairFromG2CG(left_G_e.getId(), right_CG_p_e_id, variant.ID, mergedModel.ID, new Boolean(true));
                    //此处'is_part_of_mapping'的定义不严格，因为从CG到G的默认边关系为1：1
                    edgePairFromG2CGs.add(epg2cg);
                    //记录进行了变更操作的变体的ID
                    updated_variants_id.add(variant.ID);
                }
            }//结束变体循环
        }//结束标签循环
    }

    //预加点操作(CG2G)F
    public static void PrependNodePropagationFromCG2G(Graph mergedModel, Map<String, Graph> variants, Vertex new_v, Vertex v_s, HashSet<String> labels_on_new_edge_in_CG,
                                                      List<VertexPairFromG2CG> vertexPairFromG2CGs, List<EdgePairFromG2CG> edgePairFromG2CGs, List<String> updated_variants_id,
                                                      String appended_edge_id_in_CG) {

        //首先对CG进行预加节点new_v
        ChangeOperationOnMergedModel.PrependNodeToCG(mergedModel, new_v, v_s, labels_on_new_edge_in_CG, appended_edge_id_in_CG);

        //清除'updated_variants_id'中的数据
        updated_variants_id.clear();

        Iterator<String> edge_labels_on_merged_model = labels_on_new_edge_in_CG.iterator();
        while (edge_labels_on_merged_model.hasNext()) {//对labels_on_new_edge_in_CG的每一个标签进行循环
            String edge_label_on_merged_model = edge_labels_on_merged_model.next();//得到cg新边上的当前标签★

            Set<String> keySet = variants.keySet();//得到所有变体的标签
            Iterator<String> graphIterator = keySet.iterator();
            while (graphIterator.hasNext()) {//对每一个变体的标签进行循环
                String variant_original_pmv_id = graphIterator.next();
                Graph variant = variants.get(variant_original_pmv_id);
                Set<String> all_edge_labels = variant.getGraphLabels();
                String edge_label_on_variant = new String("");
                Iterator itr = all_edge_labels.iterator();
                while(itr.hasNext()){
                    edge_label_on_variant = itr.next().toString();//获取当前变体的标签★
                }

                //实际上循环了cg上新边的所有标签，以及两个变体的所有标签，看他们之间有多少相似的
                if (edge_label_on_variant.equals(edge_label_on_merged_model)) {//若当前边标签与当前变体标签相同，说明该变更应传播到当前变体上★

                    String v_s_id_in_G = CommonFunction4Propagation.findCorrespondingNodeInG(v_s.getID(), variant, mergedModel, vertexPairFromG2CGs);
                    Vertex v_s_in_G = null;

                    //若v_s_id_in_G不为空，则v_s_in_G为其对应的顶点
                    if(!v_s_id_in_G.equals("")){
                        HashMap<String, Vertex> vertexHashMap = variant.getVertexMap();
                        v_s_in_G = vertexHashMap.get(v_s_id_in_G);

                    }

                    //若v_s_id_in_G为空，则v_s_in_G为新创建的网关，并将其映射到v_s*
                    else if (v_s_id_in_G.equals("")) {
                        v_s_in_G = CommonFunction4Propagation.createAuxNodeMappingInG(v_s,vertexPairFromG2CGs,variant,mergedModel,edgePairFromG2CGs);//在变体中创建新的点，并建立映射★
                        v_s_id_in_G = v_s_in_G.getID();
                    }

                    //创建新点并将其预加到g中
                    Vertex new_v_in_G = new Vertex(new_v.getType(), new_v.getLabel(), variant.getIdGenerator().getNextId());
                    ChangeOperationOnProcessVariant.PrependNodeToG(variant, new_v_in_G, v_s_in_G, edge_label_on_variant, new String("e").concat(variant.getIdGenerator().getNextId()));

                    //更新点映射
                    VertexPairFromG2CG vpg2cg = new VertexPairFromG2CG(new_v_in_G.getID(), new_v.getID(), variant.ID, mergedModel.ID);
                    vertexPairFromG2CGs.add(vpg2cg);

                    //更新边映射
                    Edge right_CG_e = mergedModel.containsEdge(new_v.getID(), v_s.getID());
                    Edge left_G_e = variant.containsEdge(new_v_in_G.getID(), v_s_id_in_G);
                    List<String> right_CG_p_e_id = new ArrayList<String>();
                    right_CG_p_e_id.add(right_CG_e.getId());
                    EdgePairFromG2CG epg2cg = new EdgePairFromG2CG(left_G_e.getId(), right_CG_p_e_id, variant.ID, mergedModel.ID, new Boolean(true));
                    //此处'is_part_of_mapping'的定义不严格，因为从CG到G的默认边关系为1：1
                    edgePairFromG2CGs.add(epg2cg);
                    //记录进行了变更操作的变体的ID
                    updated_variants_id.add(variant.ID);
                }
            }//结束变体循环
        }//结束标签循环
    }

    //更新点注释操作(CG2G)
    public static void UpdateNodeAnnotationPropagationFromCG2G(Graph mergedModel, Map<String, Graph> variants, Vertex v, String new_label, List<VertexPairFromG2CG> vertexPairFromG2CGs,
                                                               List<EdgePairFromG2CG> edgePairFromG2CGs, List<String> updated_variants_id) {

        v.setLabel(new_label);
        updated_variants_id.clear();

        //得到cg_nv的后续节点集
        List<Vertex> postset_of_id_cg_nv = mergedModel.getPostset(v.getID());
        HashSet<String> label_of_e=new HashSet<>();
        String slabel_of_e="";
        Vertex post_of_id_cg_nv=null;
        if(!postset_of_id_cg_nv.isEmpty()){
            post_of_id_cg_nv = postset_of_id_cg_nv.get(0);
            //得到cg_nv和其后续节点集的边(出边)注释
            Edge e = mergedModel.containsEdge(v.getID(), post_of_id_cg_nv.getID());
            label_of_e = e.getLabels();
            slabel_of_e = convertedHashSetToString(label_of_e);
        }
        //得到cg_nv的前继节点集
        List<Vertex> preset_of_id_cg_nv = mergedModel.getPreset(v.getID());
        HashSet<String> label_of_e1=new HashSet<>();;
        String slabel_of_e1 ="";
        Vertex pre_of_id_cg_nv = null;
        if(!preset_of_id_cg_nv.isEmpty()){
            pre_of_id_cg_nv = preset_of_id_cg_nv.get(0);
            //得到cg_nv和其前继节点集的边(入边)注释
            Edge e1 = mergedModel.containsEdge(pre_of_id_cg_nv.getID(), v.getID());
            label_of_e1 = e1.getLabels();
            slabel_of_e1 = convertedHashSetToString(label_of_e1);
        }

        Iterator<String> edge_labels_on_merged_model ;
        if(!slabel_of_e.equals(""))
         edge_labels_on_merged_model = label_of_e.iterator();
        else  edge_labels_on_merged_model = label_of_e1.iterator();
        while (edge_labels_on_merged_model.hasNext()) {//对labels_on_new_edge_in_CG的每一个标签进行循环
            String edge_label_on_merged_model = edge_labels_on_merged_model.next();//得到cg新边上的当前标签★

            Set<String> keySet = variants.keySet();//得到所有变体的标签
            Iterator<String> graphIterator = keySet.iterator();
            while (graphIterator.hasNext()) {//对每一个变体的标签进行循环
                String variant_original_pmv_id = graphIterator.next();
                Graph variant = variants.get(variant_original_pmv_id);
                Set<String> all_edge_labels = variant.getGraphLabels();
                String edge_label_on_variant = new String("");
                Iterator itr = all_edge_labels.iterator();
                while(itr.hasNext()){
                    edge_label_on_variant = itr.next().toString();//获取当前变体的标签★
                }

                if (edge_label_on_merged_model.contains(edge_label_on_variant)) {//若当前边标签与当前变体标签相同，说明该变更应传播到当前变体上★

                    String v_s_id_in_G = CommonFunction4Propagation.findCorrespondingNodeInG(v.getID(), variant, mergedModel, vertexPairFromG2CGs);
                    Vertex v_s_in_G = new Vertex("xor","");
                    HashMap<String, Vertex> vertexHashMap = variant.getVertexMap();
                    v_s_in_G = vertexHashMap.get(v_s_id_in_G);
                    if(!v_s_id_in_G.equals("")){
                        v_s_in_G.setLabel(new_label);

                        updated_variants_id.add(variant.ID);
                    }
                }

            }
        }
    }

    //删除边操作(CG2G)
    public static void DeleteEdgePropagationFromCGToG(Graph mergedModel, Map<String, Graph> variants, Vertex v_p, Vertex v_s, HashSet<String> removed_labels_on_edge_from_CG,
                                                      List<VertexPairFromG2CG> vertexPairFromG2CGs, List<EdgePairFromG2CG> edgePairFromG2CGs, List<String> updated_variants_id) {
        //首先在从cg中删除一条边
        ChangeOperationOnMergedModel.DeleteEdgeFromCG(mergedModel,v_p,v_s);

        updated_variants_id.clear();

        Iterator<String> iterator = removed_labels_on_edge_from_CG.iterator();
        while (iterator.hasNext()) {
            String removed_label_on_edge_from_CG = iterator.next();

            Set<String> keySet = variants.keySet();
            Iterator<String> graphIterator = keySet.iterator();
            while (graphIterator.hasNext()) {
                String variant_original_pmv_id = graphIterator.next();
                Graph variant = variants.get(variant_original_pmv_id);
                Set<String> all_edge_labels = variant.getGraphLabels();
                String edge_label_on_variant = new String("");
                Iterator itr = all_edge_labels.iterator();
                while (itr.hasNext()) {
                    edge_label_on_variant = itr.next().toString();
                }
                if (edge_label_on_variant.equals(removed_label_on_edge_from_CG)){

                    String v_p_id_in_G = CommonFunction4Propagation.findCorrespondingNodeInG(v_p.getID(), variant, mergedModel, vertexPairFromG2CGs);
                    String v_s_id_in_G = CommonFunction4Propagation.findCorrespondingNodeInG(v_s.getID(), variant, mergedModel, vertexPairFromG2CGs);

                    Vertex v_p_in_G = new Vertex(v_p.getType(), new String(""), new String(""));
                    //如果g中没有vp的对应点，则创造相应辅助节点
                    if (v_p_id_in_G.equals("")) {
                        v_p_in_G = CommonFunction4Propagation.createAuxNodeMappingInG(v_p,vertexPairFromG2CGs,variant,mergedModel,edgePairFromG2CGs);
                    }
                    else{
                        HashMap<String, Vertex> vertexHashMap = variant.getVertexMap();
                        v_p_in_G = vertexHashMap.get(v_p_id_in_G);
                    }

                    Vertex v_s_in_G = new Vertex(v_s.getType(), new String(""), new String(""));
                    //如果g中没有vs的对应点，则创造相应辅助节点
                    if (v_s_id_in_G.equals("")) {
                        v_s_in_G = CommonFunction4Propagation.createAuxNodeMappingInG(v_s,vertexPairFromG2CGs,variant,mergedModel,edgePairFromG2CGs);
                    }
                    else{
                        HashMap<String, Vertex> vertexHashMap = variant.getVertexMap();
                        v_s_in_G = vertexHashMap.get(v_s_id_in_G);
                    }

                    ChangeOperationOnProcessVariant.DeleteEdgeFromG(variant, v_p_in_G, v_s_in_G);
                    //更新映射
                    Iterator<EdgePairFromG2CG> iterator1 = edgePairFromG2CGs.iterator();
                    Edge ee =mergedModel.containsEdge(v_p.getID(),v_s.getID());
                    while(iterator1.hasNext()){
                        EdgePairFromG2CG edgePairFromG2CG = iterator1.next();
                        //删除部分g与cg的边缘映射，因为变体中的某些边已被删除
                        if (edgePairFromG2CG.getRight_CG_p_e_id().equals(ee.getId())){
                            iterator.remove();
                        }
                    }

                    updated_variants_id.add(variant.ID);

                }



            }//end loop variant.
        }

    }

    //添加边操作(CG2G)
    public static void InsertEdgePropagationFromCGToG(Graph originalMergedModel, Map<String, Graph> variants,HashSet<String> labels_on_new_edge_in_CG,
                                                      Vertex v_p, Vertex v_s,  String edge_id, List<VertexPairFromG2CG> vertexPairFromG2CGs,
                                                      List<EdgePairFromG2CG> edgePairFromG2CGs,List<String> updated_variants_id){

        Edge ee=ChangeOperationOnMergedModel.InsertEdgeToCG(originalMergedModel,v_p,v_s,edge_id);
        ee.setLabels(labels_on_new_edge_in_CG);
        updated_variants_id.clear();

        Iterator<String> edge_labels_on_merged_model = labels_on_new_edge_in_CG.iterator();
        while (edge_labels_on_merged_model.hasNext()) {
            String edge_label_on_merged_model = edge_labels_on_merged_model.next();

            Set<String> keySet = variants.keySet();
            Iterator<String> graphIterator = keySet.iterator();
            while (graphIterator.hasNext()) {
                String variant_original_pmv_id = graphIterator.next();
                Graph variant = variants.get(variant_original_pmv_id);
                Set<String> all_edge_labels = variant.getGraphLabels();
                String edge_label_on_variant = new String("");
                Iterator itr = all_edge_labels.iterator();
                while (itr.hasNext()) {
                    edge_label_on_variant = itr.next().toString();
                }
                if (edge_label_on_variant.equals(edge_label_on_merged_model)){

                    String v_p_id_in_G = CommonFunction4Propagation.findCorrespondingNodeInG(v_p.getID(), variant, originalMergedModel, vertexPairFromG2CGs);
                    String v_s_id_in_G = CommonFunction4Propagation.findCorrespondingNodeInG(v_s.getID(), variant, originalMergedModel, vertexPairFromG2CGs);

                    Vertex v_p_in_G ;
                    if (v_p_id_in_G.equals("")) {
                        v_p_in_G = CommonFunction4Propagation.createAuxNodeMappingInG(v_p,vertexPairFromG2CGs,variant,originalMergedModel,edgePairFromG2CGs);
                    }
                    else{
                        HashMap<String, Vertex> vertexHashMap = variant.getVertexMap();
                        v_p_in_G = vertexHashMap.get(v_p_id_in_G);
                    }

                    Vertex v_s_in_G;
                    if (v_s_id_in_G.equals("")) {
                        v_s_in_G = CommonFunction4Propagation.createAuxNodeMappingInG(v_s,vertexPairFromG2CGs,variant,originalMergedModel,edgePairFromG2CGs);
                    }
                    else{
                        HashMap<String, Vertex> vertexHashMap = variant.getVertexMap();
                        v_s_in_G = vertexHashMap.get(v_s_id_in_G);
                    }

                    ChangeOperationOnProcessVariant.InsertEdgeToG(variant,v_p_in_G,v_s_in_G,edge_label_on_variant,new String("e").concat(variant.getIdGenerator().getNextId()));

                    //更新边映射
                    Edge right_CG_e = originalMergedModel.containsEdge(v_p.getID(), v_s.getID());
                    Edge left_G_e = variant.containsEdge(v_p_in_G.getID(), v_s_in_G.getID());
                    List<String> right_CG_p_e_id = new ArrayList<String>();
                    right_CG_p_e_id.add(right_CG_e.getId());
                    EdgePairFromG2CG epg2cg = new EdgePairFromG2CG(left_G_e.getId(), right_CG_p_e_id, variant.ID, originalMergedModel.ID, true);
                    //此处'is_part_of_mapping'的定义不严格，因为从CG到G的默认边关系为1：1
                    edgePairFromG2CGs.add(epg2cg);

                    updated_variants_id.add(variant.ID);
                }
            }
        }

    }

    /** 相关函数：将哈希集转换为字符串 */
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
