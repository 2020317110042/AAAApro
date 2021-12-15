package org.apromore.toolbox.similaritySearch.changePropagation;

import org.apromore.toolbox.similaritySearch.common.EdgePairFromG2CG;
import org.apromore.toolbox.similaritySearch.common.VertexPairFromG2CG;
import org.apromore.toolbox.similaritySearch.graph.Edge;
import org.apromore.toolbox.similaritySearch.graph.Graph;
import org.apromore.toolbox.similaritySearch.graph.Vertex;
import org.apromore.util.ChangePropagationUtils;

import java.util.*;


public class CommonFunction4Propagation {

    //找到g中的对应点
    public static String findCorrespondingNodeInG(String vertexCanonicalIdInCG, Graph variant, Graph merged, List<VertexPairFromG2CG> vertexPairFromG2CGs) {

        String left_G_v_id = new String("");

        Iterator<VertexPairFromG2CG> it = vertexPairFromG2CGs.iterator();

        while (it.hasNext()) {

            VertexPairFromG2CG vertexPairFromG2CG = it.next();

            if (vertexPairFromG2CG.getRight_CG_v_id().equals(vertexCanonicalIdInCG) && vertexPairFromG2CG.getPid_G().equals(variant.ID) && vertexPairFromG2CG.getPid_CG().equals(merged.ID)) {

                if (vertexPairFromG2CG.getLeft_G_v_id() != null) {
                    left_G_v_id = vertexPairFromG2CG.getLeft_G_v_id();
                }
            }
        }

        return left_G_v_id;
    }

    //判断应怎样在G中创建CG中辅助节点的对应点，并返回该点，目前只能创建父、子节点都在映射中的点
    public static Vertex createAuxNodeMappingInG (Vertex auxInCG, List<VertexPairFromG2CG> vertexPairFromG2CGs, Graph variant, Graph mergedModel, List<EdgePairFromG2CG> edgePairFromG2CGs){

        String aux_mapped_id_in_G = new String("");//初始化要插入的点和其ID
        Vertex aux_mapped = new Vertex(auxInCG.getType(), new String(""), new String(""));

        HashSet<String> label_on_variant_set = variant.getGraphLabels();//得到变体的标签
        String label_on_variant = new String("");
        Iterator<String> iterator = label_on_variant_set.iterator();
        while(iterator.hasNext()){
            label_on_variant = iterator.next();
        }

        List<VertexPairFromG2CG> vertexPairFromG2CGs_clone = new ArrayList<VertexPairFromG2CG> ();//复制点映射集
        for(int i = 0; i < vertexPairFromG2CGs.size(); i++ ){
            VertexPairFromG2CG vertexPairFromG2CG = new VertexPairFromG2CG(new String(""),new String(""),new String(""),new String(""));
            vertexPairFromG2CG.setLeft_G_v_id(vertexPairFromG2CGs.get(i).getLeft_G_v_id());
            vertexPairFromG2CG.setRight_CG_v_id(vertexPairFromG2CGs.get(i).getRight_CG_v_id());
            vertexPairFromG2CG.setPid_G(vertexPairFromG2CGs.get(i).getPid_G());
            vertexPairFromG2CG.setPid_CG(vertexPairFromG2CGs.get(i).getPid_CG());
            vertexPairFromG2CGs_clone.add(vertexPairFromG2CG);
        }

        //判断是合并节点还是拆分节点
        LinkedList<Vertex> aux_parents = auxInCG.getParentsList();
        LinkedList<Vertex> aux_children = auxInCG.getChildrenList();
        //如果是拆分节点
        if((aux_parents.size()==1) && (aux_children.size()>1)) {
            Boolean mapped_is_found = false;
            Vertex aux_parent = aux_parents.getFirst();//在CG中找到aux的父节点
            Iterator<Vertex> aux_children_it = aux_children.iterator();//遍历aux的每一个子节点
            while (aux_children_it.hasNext()) {
                Vertex aux_child = aux_children_it.next();//获取当前子节点aux_child
                Iterator it_vertex_mappings = vertexPairFromG2CGs_clone.iterator();//接下来遍历点映射集，查找是否存在aux_child的映射
                while(it_vertex_mappings.hasNext()){
                    VertexPairFromG2CG vertexPairFromG2CG = (VertexPairFromG2CG) it_vertex_mappings.next();
                    String left_G_v_id = vertexPairFromG2CG.getLeft_G_v_id();
                    String right_CG_v_id = vertexPairFromG2CG.getRight_CG_v_id();
                    String pid_G = vertexPairFromG2CG.getPid_G();
                    String pid_CG = vertexPairFromG2CG.getPid_CG();

                    //如果顶点aux_child的映射已经存在，则检查这两个点之间的边上有没有当前变体的标签
                    if((right_CG_v_id.equals(aux_child.getID())&&(pid_G.equals(variant.ID))&&(pid_CG.equals(mergedModel.ID))&&(!left_G_v_id.equals("null")))){
                        Edge e = mergedModel.containsEdge(auxInCG.getID(),aux_child.getID());
                        //若边上有当前变体的标签，则表示可以创建aux的对应节点
                        if(ChangePropagationUtils.convertCommaSeparatedStringToHashset((String)e.getLabels().toArray()[0]).contains(label_on_variant)){
                            //在G中创建aux的对应连接器
                            aux_mapped_id_in_G = createCorrespondingNodeInG(auxInCG.getID(), aux_parent, aux_child, vertexPairFromG2CGs, variant, mergedModel, edgePairFromG2CGs, new LinkedList<Vertex>(), 1);
                            mapped_is_found = true;
                            break;//映射已找到，跳出映射集的遍历
                        }
                    }
                }//结束节点集遍历
                if(mapped_is_found == true)//若已经创建aux的映射节点，则可以直接跳出循环
                    break;
            }

            //若aux 与其 所有有映射的子节点 的边上都没有该变体的标签，则说明可能aux是一串连接器的起点
            if(mapped_is_found == false){
                LinkedList<Vertex> chain_of_conf_GW_from_auxInCG = mergedModel.getChildConfGWChain(auxInCG);//从aux开始获得其后面一系列的连接器(包括自身)，保存至chain_of_conf_GW_from_auxInCG
                //若aux后还有连接器
                if(chain_of_conf_GW_from_auxInCG.size()>1){
                    Vertex last_conf_GW = chain_of_conf_GW_from_auxInCG.getLast();//得到'chain_of_conf_GW_from_auxInCG'的最后一个元素
                    LinkedList<Vertex> last_conf_GW_children = last_conf_GW.getChildrenList();//得到最后一个元素的所有子节点
                    Iterator<Vertex> it = last_conf_GW_children.iterator();//遍历其子节点
                    while(it.hasNext()){
                        Vertex last_conf_GW_child = it.next();//得到当前子节点
                        //检查最后一个连接器和其子节点之间的边上是否包含当前变体的标签
                        Edge e = mergedModel.containsEdge(last_conf_GW.getID(),last_conf_GW_child.getID());
                        if(ChangePropagationUtils.convertCommaSeparatedStringToHashset((String)e.getLabels().toArray()[0]).contains(label_on_variant)){
                            //在G中创建aux的对应连接器
                            aux_mapped_id_in_G = createCorrespondingNodeInG(auxInCG.getID(), aux_parent, last_conf_GW_child, vertexPairFromG2CGs, variant, mergedModel, edgePairFromG2CGs, chain_of_conf_GW_from_auxInCG, 2);
                            break;
                        }
                    }
                }
            }
        }
        //如果是合并节点
        else if((aux_parents.size()>1) && (aux_children.size()==1)){
            Boolean mapped_is_found = false;
            Vertex aux_child = aux_children.getFirst();//在CG中找到aux的子节点
            Iterator<Vertex> aux_parents_it = aux_parents.iterator();//遍历aux的每一个父节点
            while(aux_parents_it.hasNext()){
                Vertex aux_parent = aux_parents_it.next();//获取当前父节点aux_parent
                Iterator it_vertex_mappings = vertexPairFromG2CGs_clone.iterator();//接下来遍历点映射集，查找是否存在aux_parent的映射
                while(it_vertex_mappings.hasNext()){
                    VertexPairFromG2CG vertexPairFromG2CG = (VertexPairFromG2CG) it_vertex_mappings.next();
                    String left_G_v_id = vertexPairFromG2CG.getLeft_G_v_id();
                    String right_CG_v_id = vertexPairFromG2CG.getRight_CG_v_id();
                    String pid_G = vertexPairFromG2CG.getPid_G();
                    String pid_CG = vertexPairFromG2CG.getPid_CG();

                    //如果aux_parent的映射已经存在，则检查这两个点之间的边上有没有当前变体的标签
                    if((right_CG_v_id.equals(aux_parent.getID())&&(pid_G.equals(variant.ID))&&(pid_CG.equals(mergedModel.ID))&&(!left_G_v_id.equals("null")))){
                        Edge e = mergedModel.containsEdge(aux_parent.getID(),auxInCG.getID());
                        //若边上有当前变体的标签，则表示可以创建aux的对应节点
                        if(ChangePropagationUtils.convertCommaSeparatedStringToHashset((String)e.getLabels().toArray()[0]).contains(label_on_variant)){

                            //在G中创建aux的对应连接器
                            aux_mapped_id_in_G = createCorrespondingNodeInG(auxInCG.getID(), aux_parent, aux_child, vertexPairFromG2CGs, variant, mergedModel, edgePairFromG2CGs, new LinkedList<Vertex>(), 1);
                            /*//找到'v_p_in_CG' & 'v_s_in_CG'在G中的对应点的ID，并得到相应的点
                            String v_p_id_in_G = new String("");
                            String v_s_id_in_G = new String("");
                            Iterator<VertexPairFromG2CG> iterator1 = vertexPairFromG2CGs.iterator();
                            while (iterator1.hasNext()) {//得到ID
                                VertexPairFromG2CG vertexPairFromG2CG1 = iterator1.next();
                                if (vertexPairFromG2CG1.getRight_CG_v_id().equals(aux_parent.getID()) && vertexPairFromG2CG1.getPid_G().equals(variant.ID))
                                    v_p_id_in_G = vertexPairFromG2CG1.getLeft_G_v_id();
                                else if (vertexPairFromG2CG1.getRight_CG_v_id().equals(aux_child.getID()) && vertexPairFromG2CG1.getPid_G().equals(variant.ID))
                                    v_s_id_in_G = vertexPairFromG2CG1.getLeft_G_v_id();
                            }
                            List<Vertex> vertex_list_G = variant.getVertices();
                            Vertex v_p_in_G = new Vertex(aux_parent.getType(),new String(""), new String(""));
                            Vertex v_s_in_G = new Vertex(aux_child.getType(),new String(""), new String(""));
                            Iterator<Vertex> it = vertex_list_G.iterator();
                            while(it.hasNext()){//得到点
                                Vertex v = it.next();
                                if(v.getID().equals(v_p_id_in_G))
                                    v_p_in_G = v;
                                else if(v.getID().equals(v_s_id_in_G))
                                    v_s_in_G = v;
                            }
                            Vertex newGw = new Vertex("xor", variant.getIdGenerator().getNextId());//创建一个新的xor连接器，然后insert
                            String front_edge_id = variant.getIdGenerator().getNextId();
                            String rear_edge_id = variant.getIdGenerator().getNextId();
                            String removed_edge_id = ChangeOperationOnProcessVariant.InsertNodeToG(variant, newGw, v_p_in_G, v_s_in_G, front_edge_id, rear_edge_id);
                            aux_mapped_id_in_G=newGw.getID();
                            //更新点映射
                            VertexPairFromG2CG vertexPairFromG2CG_added = new VertexPairFromG2CG(newGw.getID(), auxInCG.getID(), variant.ID, mergedModel.ID);
                            vertexPairFromG2CGs.add(vertexPairFromG2CG_added);
                            //更新边映射，先删除insertnode中相关边的映射关系，然后加入两个新边的映射
                            Iterator<EdgePairFromG2CG> it_1 = edgePairFromG2CGs.iterator();
                            while (it_1.hasNext()) {
                                EdgePairFromG2CG edgePairFromG2CG = it_1.next();
                                if (edgePairFromG2CG.getLeft_G_e_id().equals(removed_edge_id))
                                    it_1.remove();
                            }
                            List<String> right_rear_e_id = new ArrayList<String>();
                            List<String> right_front_e_id = new ArrayList<String>();
                            String rear_edge_id_in_CG = mergedModel.containsEdge(auxInCG.getID(), aux_child.getID()).getId();
                            right_rear_e_id.add(rear_edge_id_in_CG);
                            EdgePairFromG2CG edgePairFromG2CG_rear_part = new EdgePairFromG2CG(rear_edge_id, right_rear_e_id, variant.ID, mergedModel.ID, false);
                            String front_edge_id_in_CG = mergedModel.containsEdge(aux_parent.getID(),auxInCG.getID()).getId();
                            right_front_e_id.add(front_edge_id_in_CG);
                            EdgePairFromG2CG edgePairFromG2CG_rear_part1 = new EdgePairFromG2CG(front_edge_id, right_front_e_id, variant.ID, mergedModel.ID, false);
                            edgePairFromG2CGs.add(edgePairFromG2CG_rear_part);
                            edgePairFromG2CGs.add(edgePairFromG2CG_rear_part1);*/
                            mapped_is_found = true;
                            break;//映射已找到，跳出映射集的遍历
                        }
                    }
                }
                if(mapped_is_found == true)//若已经创建aux的映射节点，则可以直接跳出循环
                    break;
            }

            //若aux 与其 所有有映射的父节点 的边上都没有该变体的标签，则说明可能aux是一串连接器的终点
            if(mapped_is_found == false){
                LinkedList<Vertex> chain_of_conf_GW_ended_with_auxInCG = mergedModel.getParentConfGWChain(auxInCG);//从aux开始获得其前面一系列的连接器(包括自身)，保存至chain_of_conf_GW_ended_with_auxInCG
                //若aux前还有连接器
                if(chain_of_conf_GW_ended_with_auxInCG.size()>1){
                    Vertex first_conf_GW = chain_of_conf_GW_ended_with_auxInCG.getFirst();//得到'chain_of_conf_GW_ended_with_auxInCG'的第一个元素
                    LinkedList<Vertex> first_conf_GW_parents = first_conf_GW.getParentsList();//得到最后一个元素的所有父节点
                    Iterator<Vertex> it = first_conf_GW_parents.iterator();//遍历其父节点
                    while(it.hasNext()){
                        Vertex first_conf_GW_parent = it.next();//得到当前父节点
                        //检查第一个连接器和其父节点之间的边上是否包含当前变体的标签
                        Edge e = mergedModel.containsEdge(first_conf_GW_parent.getID(),first_conf_GW.getID());
                        if(ChangePropagationUtils.convertCommaSeparatedStringToHashset((String)e.getLabels().toArray()[0]).contains(label_on_variant)){
                            //在G中创建aux的对应连接器
                            aux_mapped_id_in_G = createCorrespondingNodeInG(auxInCG.getID(), first_conf_GW_parent, aux_child, vertexPairFromG2CGs, variant, mergedModel, edgePairFromG2CGs, chain_of_conf_GW_ended_with_auxInCG, 3);
                            break;
                        }
                    }
                }
            }
        }

        List<Vertex> vertexes = variant.getVertices();//找到变体中aux_mapped_id_in_G对应的点，并返回该点
        Iterator<Vertex> it = vertexes.iterator();
        while (it.hasNext()) {
            Vertex v = it.next();
            if (v.getID().equals(aux_mapped_id_in_G)) {
                aux_mapped = v;
                break;
            }
        }
        return aux_mapped;
    }

    //在G中创建CG的对应点，返回值为该点ID，需要注意的是index有三个值：0，1，2，分别对应单个连接器、连接器链的起点、连接器链的终点
    public static String createCorrespondingNodeInG(String vertexCanonicalIdInCG, Vertex v_p_in_CG, Vertex v_s_in_CG, List<VertexPairFromG2CG> vertexPairFromG2CGs,
                                                  Graph variant, Graph merged, List<EdgePairFromG2CG> edgePairFromG2CGs, LinkedList<Vertex> chain_of_conf_GW, int index) {

        //初始化G中三个点的ID，分别为：新连接器v、v的父节点、v的子节点
        String left_G_v_id = new String("");
        String v_p_id_in_G = new String("");
        String v_s_id_in_G = new String("");

        //找到'v_p_in_CG' & 'v_s_in_CG'在G中的对应点的ID，并得到相应的点
        Iterator<VertexPairFromG2CG> iterator = vertexPairFromG2CGs.iterator();
        while (iterator.hasNext()) {//得到ID
            VertexPairFromG2CG vertexPairFromG2CG = iterator.next();
            if (vertexPairFromG2CG.getRight_CG_v_id().equals(v_p_in_CG.getID()) && vertexPairFromG2CG.getPid_G().equals(variant.ID))
                v_p_id_in_G = vertexPairFromG2CG.getLeft_G_v_id();
            else if (vertexPairFromG2CG.getRight_CG_v_id().equals(v_s_in_CG.getID()) && vertexPairFromG2CG.getPid_G().equals(variant.ID))
                v_s_id_in_G = vertexPairFromG2CG.getLeft_G_v_id();
        }
        List<Vertex> vertex_list_G = variant.getVertices();
        Vertex v_p_in_G = new Vertex(v_p_in_CG.getType(),new String(""), new String(""));
        Vertex v_s_in_G = new Vertex(v_s_in_CG.getType(),new String(""), new String(""));
        Iterator<Vertex> it = vertex_list_G.iterator();
        while(it.hasNext()){//得到点
            Vertex v = it.next();
            if(v.getID().equals(v_p_id_in_G))
                v_p_in_G = v;
            else if(v.getID().equals(v_s_id_in_G))
                v_s_in_G = v;
        }

        List<Vertex> vertexes_in_CG = merged.getVertices();
        Iterator<Vertex> its = vertexes_in_CG.iterator();
        while (its.hasNext()) {
            Vertex vertex_in_CG = its.next();
            //在CG中找到vertexCanonicalIdInCG
            if (vertex_in_CG.getID().equals(vertexCanonicalIdInCG)) {

                //创建一个新的xor连接器，然后insert
                Vertex newGw = new Vertex("xor", variant.getIdGenerator().getNextId());
                newGw.setVertexType(Vertex.Type.gateway);
                newGw.setConfigurable(false);
                newGw.setAddedGW(true);//不是初始连接器
                left_G_v_id = newGw.getID();
                //创建出边和入边的id
                String front_edge_id = variant.getIdGenerator().getNextId();
                String rear_edge_id = variant.getIdGenerator().getNextId();
                String removed_edge_id = ChangeOperationOnProcessVariant.InsertNodeToG(variant, newGw, v_p_in_G, v_s_in_G, front_edge_id, rear_edge_id);
                //更新点映射
                VertexPairFromG2CG vertexPairFromG2CG_added = new VertexPairFromG2CG(newGw.getID(), vertexCanonicalIdInCG, variant.ID, merged.ID);
                vertexPairFromG2CGs.add(vertexPairFromG2CG_added);
                //更新边映射，先删除insertnode中相关边的映射关系
                Iterator<EdgePairFromG2CG> it_1 = edgePairFromG2CGs.iterator();
                while (it_1.hasNext()) {
                    EdgePairFromG2CG edgePairFromG2CG = it_1.next();
                    if (edgePairFromG2CG.getLeft_G_e_id().equals(removed_edge_id))
                        it_1.remove();
                }

                //分三种情况更新两个图的边映射:aux为孤立连接器、连接器链的头、连接器链的尾
                if(index == 1){
                    List<String> right_rear_e_id = new ArrayList<String>();
                    List<String> right_front_e_id = new ArrayList<String>();
                    //创建出边的映射
                    String rear_edge_id_in_CG = merged.containsEdge(vertexCanonicalIdInCG, v_s_in_CG.getID()).getId();
                    right_rear_e_id.add(rear_edge_id_in_CG);
                    EdgePairFromG2CG edgePairFromG2CG_rear_part = new EdgePairFromG2CG(rear_edge_id, right_rear_e_id, variant.ID, merged.ID, false);
                    //创建入边的映射
                    String front_edge_id_in_CG = merged.containsEdge(v_p_in_CG.getID(),vertexCanonicalIdInCG).getId();
                    right_front_e_id.add(front_edge_id_in_CG);
                    EdgePairFromG2CG edgePairFromG2CG_rear_part1 = new EdgePairFromG2CG(front_edge_id, right_front_e_id, variant.ID, merged.ID, false);
                    //将上面俩映射加入总映射
                    edgePairFromG2CGs.add(edgePairFromG2CG_rear_part);
                    edgePairFromG2CGs.add(edgePairFromG2CG_rear_part1);
                }//若为孤立节点，则添加insertnode的两段边与CG的映射

                //先不管了😄😄😄😄😄😄😄😄
                /*case 2: 'aux' is the starting vertex of the connector chain.*/
                else if(index == 2){

                    /*add some new edge mapping relationships between CG and G*/
                    String front_edge_id_in_CG = merged.containsEdge(v_p_in_CG.getID(), vertexCanonicalIdInCG).getId();
                    List<String> right_front_e_id = new ArrayList<String>();
                    right_front_e_id.add(front_edge_id_in_CG);
                    EdgePairFromG2CG edgePairFromG2CG_front_part = new EdgePairFromG2CG(front_edge_id, right_front_e_id, variant.ID, merged.ID, false);
                    edgePairFromG2CGs.add(edgePairFromG2CG_front_part);

                    List<String> right_rear_e_id = new ArrayList<String>();

                    /*converting Link LIST named 'chain_of_connectors_array' to array*/
                    Vertex[] chain_of_connectors_array = chain_of_conf_GW.toArray(new Vertex[chain_of_conf_GW.size()]);

                    /*Getting the rear part of edge in CG between 'v_p' and 'vertexCanonicalIDCG' */
                    String rear_edge_id_within_chain_in_CG = null;
                    for(int i = 0; i < chain_of_conf_GW.size()-1; i++){

                        rear_edge_id_within_chain_in_CG = merged.containsEdge(chain_of_connectors_array[i].getID(),chain_of_connectors_array[i+1].getID()).getId();

                    }

                    right_rear_e_id.add(rear_edge_id_within_chain_in_CG);

                    String rear_edge_id_after_chain_in_CG = merged.containsEdge(chain_of_connectors_array[chain_of_connectors_array.length-1].getID(),
                            v_s_in_CG.getID()).getId();

                    right_rear_e_id.add(rear_edge_id_after_chain_in_CG);

                    EdgePairFromG2CG edgePairFromG2CG_rear_part = new EdgePairFromG2CG(rear_edge_id, right_rear_e_id, variant.ID, merged.ID, false);
                    edgePairFromG2CGs.add(edgePairFromG2CG_rear_part);

                }
                /*case 3: 'aux' is the ending vertex of the connector chain*/
                else if (index == 3){

                    /*add some new edge mapping relationships between CG and G*/
                    String rear_part_edge_id_in_CG = merged.containsEdge(vertexCanonicalIdInCG, v_s_in_CG.getID()).getId();
                    List<String> right_rear_part_e_id = new ArrayList<>();
                    right_rear_part_e_id.add(rear_part_edge_id_in_CG);
                    EdgePairFromG2CG edgePairFromG2CG_rear_part = new EdgePairFromG2CG(rear_edge_id, right_rear_part_e_id, variant.ID, merged.ID, false);
                    edgePairFromG2CGs.add(edgePairFromG2CG_rear_part);

                    List<String> right_front_e_id = new ArrayList<String>();

                    /*converting Link List named 'chain_of_connectors_array' to array*/
                    Vertex[] chain_of_connectors_array = chain_of_conf_GW.toArray(new Vertex[chain_of_conf_GW.size()]);

                    /*Getting the front part of edge in CG between 'v_p' and 'vertexCanonicalIdInCG'*/
                    String front_edge_id_within_chain_in_CG = null;
                    for(int i = 0; i < chain_of_conf_GW.size()-1; i++){

                        front_edge_id_within_chain_in_CG = merged.containsEdge(chain_of_connectors_array[i].getID(),chain_of_connectors_array[i+1].getID()).getId();

                    }


                    String rear_edge_id_before_chain_in_CG = merged.containsEdge(v_p_in_CG.getID(),chain_of_connectors_array[0].getID()).getId();

                    right_front_e_id.add(rear_edge_id_before_chain_in_CG);

                    right_front_e_id.add(front_edge_id_within_chain_in_CG);

                    EdgePairFromG2CG edgePairFromG2CG_front_part = new EdgePairFromG2CG(rear_edge_id, right_front_e_id, variant.ID, merged.ID, false);
                    edgePairFromG2CGs.add(edgePairFromG2CG_front_part);
                }

            }

        }
        return left_G_v_id;
    }

}
