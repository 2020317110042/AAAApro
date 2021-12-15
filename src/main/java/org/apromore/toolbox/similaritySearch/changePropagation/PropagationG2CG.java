package org.apromore.toolbox.similaritySearch.changePropagation;

import org.apromore.toolbox.similaritySearch.common.EdgePairFromG2CG;
import org.apromore.toolbox.similaritySearch.common.VertexPairFromG2CG;
import org.apromore.toolbox.similaritySearch.graph.Edge;
import org.apromore.toolbox.similaritySearch.graph.Graph;
import org.apromore.toolbox.similaritySearch.graph.Vertex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


public class PropagationG2CG {

    //追加节点的传播(G2CG)
    public static void  AppendNodePropagation2CG (Graph mergedModel, Graph variant, Vertex v_p, Vertex new_v, String label, String edge_id, List<VertexPairFromG2CG> vertexPairFromG2CGs, List<EdgePairFromG2CG> edgePairFromG2CGs){

        //首先将新节点追加到p后, 改变变体
        ChangeOperationOnProcessVariant.AppendNodeToG(variant,new_v,v_p,label,edge_id);

        //得到cg中p对应的点的id
        String right_CG_v_id = findCorrespondingNodeIdInCG(v_p.getID(), vertexPairFromG2CGs);

        //得到cg中s对应的点cg_p
        Vertex right_CG_v = new Vertex(v_p.getType(),new String(""), new String(""));
        List<Vertex> vertexes = mergedModel.getVertices();
        Iterator<Vertex> it = vertexes.iterator();
        while (it.hasNext()){
            Vertex v = it.next();
            if(v.getID().equals(right_CG_v_id)){

                    right_CG_v = v;
                break;
            }
        }


        //判断p是事件还是函数，判断p的前继边是否有多个注释

            /*g获得cg_p的前置节点集*/
        List<Vertex> preset_of_right_CG_v = mergedModel.getPreset(right_CG_v_id);
        Vertex pre_of_right_CG_v = preset_of_right_CG_v.get(0);

            /*得到cg_p和其前继节点集的边注释*/
        Edge e = mergedModel.containsEdge(pre_of_right_CG_v.getID(), right_CG_v_id);
        HashSet<String> label_of_e = e.getLabels();



        if((v_p.getType().toString().equals("function") || v_p.getType().toString().equals("event")) && label_of_e.size() > 1){

            //**create a new auxiliary node 'v'''***//
            Vertex newGw = new Vertex(Vertex.GWType.xor, mergedModel.getIdGenerator().getNextId());
            newGw.setConfigurable(true);

            //**insert the new auxiliary node between 'right_CG_v' and its preceding vertex***********//
            ChangeOperationOnMergedModel.InsertNodeToCG(mergedModel, pre_of_right_CG_v, right_CG_v, newGw);

                 /*get the 'pre-set' of 'v_p*/
            List<Vertex> preset_of_v_p = variant.getPreset(v_p.getID());
            Vertex pre_of_v_p = preset_of_v_p.get(0);

                /*get the label on the edge between 'v_p' and its 'pre-set' in the variant before changing*/
            Edge edge = variant.containsEdge(pre_of_v_p.getID(), v_p.getID());
            HashSet<String> label_of_edge = edge.getLabels();

            /*split the common edge between the newly created auxiliary node and 'right_CG_v'*/
            ChangeOperationOnMergedModel.SplitCommonRegion(mergedModel, newGw, right_CG_v, right_CG_v, label_of_edge);

        }

        //cg中追加节点
        Vertex new_v_in_CG = new Vertex(new_v.getType(), new_v.getLabel(), mergedModel.getIdGenerator().getNextId());
        HashSet<String> label_on_new_added_edge = new HashSet<String>();
        label_on_new_added_edge.add(label);
        ChangeOperationOnMergedModel.AppendNodeToCG(mergedModel, right_CG_v, new_v_in_CG, label_on_new_added_edge,mergedModel.getIdGenerator().getNextId());


            /*****************************************************************************************/
        ///**update the edge mapping between G and CG*,very important*//

        VertexPairFromG2CG vpg2cg = new VertexPairFromG2CG(new_v.getID(), new_v_in_CG.getID(), variant.ID, mergedModel.ID);
        vertexPairFromG2CGs.add(vpg2cg);

        ///**update the edge mapping between G and CG*,very important*//

        Edge right_CG_e = mergedModel.containsEdge(right_CG_v.getID(), new_v_in_CG.getID());
        List<String> right = new ArrayList<String>();
        right.add(right_CG_e.getId());
        Edge left_G_e = variant.containsEdge(v_p.getID(), new_v.getID());
        EdgePairFromG2CG epg2cg = new EdgePairFromG2CG(left_G_e.getId(), right, variant.ID, mergedModel.ID, new Boolean(false) );
        edgePairFromG2CGs.add(epg2cg);
    }

    //预加节点的传播(G2CG)
    public static void PrependNodePropagation2CG (Graph mergedModel, Graph variant, Vertex new_v, Vertex v_s, String label, String edge_id, List<VertexPairFromG2CG> vertexPairFromG2CGs, List<EdgePairFromG2CG> edgePairFromG2CGs){

        //首先将新节点预加到s上, 改变变体
        ChangeOperationOnProcessVariant.PrependNodeToG(variant,new_v,v_s,label, edge_id);

        //得到cg中s对应的点的id
        String right_CG_v_id = findCorrespondingNodeIdInCG(v_s.getID(), vertexPairFromG2CGs);

        //得到cg中s对应的点cg_s
        Vertex right_CG_v = new Vertex(v_s.getType(),new String(""), new String(""));
        List<Vertex> vertexes = mergedModel.getVertices();
        Iterator<Vertex> it = vertexes.iterator();
        while (it.hasNext()){
            Vertex v = it.next();
            if(v.getID().equals(right_CG_v_id)){

                right_CG_v = v;
                break;
            }
        }

        //得到cg_s的后续节点集
        List<Vertex> postset_of_right_CG_v = mergedModel.getPostset(right_CG_v_id);
        Vertex post_of_right_CG_v = postset_of_right_CG_v.get(0);

        //得到cg_s和其后续节点集的边注释
        Edge e = mergedModel.containsEdge(right_CG_v_id, post_of_right_CG_v.getID());
        HashSet<String> label_of_e = e.getLabels();

        /*判断s是事件还是函数，判断s的后续边是否有多个注释*/
        if((v_s.getType().toString().equals("function") || v_s.getType().toString().equals("event")) && label_of_e.size() > 1){

            //创建一个新的辅助节点v''
            Vertex newGw = new Vertex(Vertex.GWType.xor, mergedModel.getIdGenerator().getNextId());
            newGw.setConfigurable(true);

            //将辅助节点插入cg_s与其后续节点之间
            ChangeOperationOnMergedModel.InsertNodeToCG(mergedModel, right_CG_v, post_of_right_CG_v, newGw);

            //得到s的后续节点集
            List<Vertex> postset_of_v_s = variant.getPostset(v_s.getID());
            Vertex post_of_v_s = postset_of_v_s.get(0);

            //得到s和其后续节点集的边注释
            Edge edge = variant.containsEdge(v_s.getID(),post_of_v_s.getID());
            HashSet<String> label_of_edge = edge.getLabels();

            //在辅助节点和cg_s间划分公共边
            ChangeOperationOnMergedModel.SplitCommonRegion(mergedModel, right_CG_v, newGw, right_CG_v, label_of_edge);
        }

        //cg中预加节点
        Vertex new_v_in_CG = new Vertex(new_v.getType(), new_v.getLabel(), mergedModel.getIdGenerator().getNextId());
        HashSet<String> label_on_new_added_edge = new HashSet<String>();
        label_on_new_added_edge.add(label);
        ChangeOperationOnMergedModel.PrependNodeToCG(mergedModel, new_v_in_CG, right_CG_v, label_on_new_added_edge,mergedModel.getIdGenerator().getNextId());

        //更新g和cg的节点映射
        VertexPairFromG2CG vpg2g = new VertexPairFromG2CG(new_v.getID(), new_v_in_CG.getID(), variant.ID, mergedModel.ID);
        vertexPairFromG2CGs.add(vpg2g);

        //更新g和cg的边映射
        Edge right_CG_e = mergedModel.containsEdge(new_v_in_CG.getID(),right_CG_v.getID());
        List<String> right = new ArrayList<String>();
        right.add(right_CG_e.getId());
        Edge left_G_e = variant.containsEdge(new_v.getID(),v_s.getID());
        EdgePairFromG2CG epg2cg = new EdgePairFromG2CG(left_G_e.getId(), right, variant.ID, mergedModel.ID, new Boolean(false) );
        edgePairFromG2CGs.add(epg2cg);
    }

    //插入边的传播(G2CG)
    public static void InsertEdgePropagation2CG (Graph originalMergedModel, Graph variantToBeChanged, Vertex v_p, Vertex v_s, String label, String edge_id, List<VertexPairFromG2CG> vertexPairFromG2CGs,List<EdgePairFromG2CG> edgePairFromG2CGs){

        //首先对变体进行插入边操作
        ChangeOperationOnProcessVariant.InsertEdgeToG(variantToBeChanged,v_p,v_s,label,edge_id);

        //找到p,s在cg中对应点的id
        String id_cg_p = findCorrespondingNodeIdInCG(v_p.getID(),vertexPairFromG2CGs);
        String id_cg_s = findCorrespondingNodeIdInCG(v_s.getID(),vertexPairFromG2CGs);

        //在cg中找到上述id对应的点，得到cg_p
        Vertex cg_p = new Vertex(v_p.getType(),new String(""), new String(""));
        List<Vertex> vertexes = originalMergedModel.getVertices();
        Iterator<Vertex> it = vertexes.iterator();
        while (it.hasNext()){
            Vertex v = it.next();
            if(v.getID().equals(id_cg_p)){
                cg_p = v;
                break;
            }
        }

        //得到cg_s
        Vertex cg_s = new Vertex(v_s.getType(),new String(""), new String(""));
        List<Vertex> vertexes1 = originalMergedModel.getVertices();
        Iterator<Vertex> it1 = vertexes1.iterator();
        while (it1.hasNext()){
            Vertex v1 = it1.next();
            if(v1.getID().equals(id_cg_s)){
                cg_s = v1;
                break;
            }
        }

        //得到变体的边的注释并插入cg的对应边,若没有对应的边则需要插入边
        HashSet<String> edge_label = variantToBeChanged.getGraphLabels();//得到g的标签
        ChangeOperationOnMergedModel.InsertEdgeToCG(originalMergedModel, cg_p, cg_s, originalMergedModel.getIdGenerator().getNextId());
        ChangeOperationOnMergedModel.InsertEdgeAnnotationToCG (originalMergedModel, cg_p, cg_s, edge_label);//在边上插入标签

        //更新边的映射
        Edge right_CG_e = originalMergedModel.containsEdge(cg_p.getID(), cg_s.getID());
        List<String> right = new ArrayList<String>();
        right.add(right_CG_e.getId());
        Edge left_G_e = variantToBeChanged.containsEdge(v_p.getID(), v_s.getID());
        EdgePairFromG2CG epg2cg = new EdgePairFromG2CG(left_G_e.getId(), right, variantToBeChanged.ID, originalMergedModel.ID, new Boolean(false) );
        edgePairFromG2CGs.add(epg2cg);

    }

    //删除边的传播(G2CG)
    public static void DeleteEdgePropagation2CG (Graph originalMergedModel, Graph variantToBeChanged, Vertex v_p, Vertex v_s, List<VertexPairFromG2CG> vertexPairFromG2CGs,List<EdgePairFromG2CG> edgePairFromG2CGs){

        //e为变体中对应的边
        Edge e = variantToBeChanged.containsEdge(v_p.getID(), v_s.getID());

        //对变体执行删除边操作
        ChangeOperationOnProcessVariant.DeleteEdgeFromG(variantToBeChanged, v_p, v_s);

        //用right_CG_e_ids存放cg中与e对应的边的id
        List<String> right_CG_e_ids = findCorrespondingEdgeSetIdInCG(e.getId(), edgePairFromG2CGs);

        //得到cg中所有边的id并用right_CG_p存放cg中与e相对应的边(不唯一)
        List<Edge> right_CG_all_edge = originalMergedModel.getEdges();
        List<Edge> right_CG_p = new ArrayList<Edge>();
        Iterator<Edge> it = right_CG_all_edge.iterator();
        while (it.hasNext()){
            Edge edge = it.next();
            if(right_CG_e_ids.contains(edge.getId())){
                right_CG_p.add(edge);
            }
        }

        // 用edge_annotation_hash_set存放该变体的标签
        HashSet<String> edge_annotation_hash_set = variantToBeChanged.getGraphLabels();
        //删除cg中对应边的相关标签
        Iterator<Edge> ite = right_CG_p.iterator();
        while(ite.hasNext()){//循环对right_CG_p中存放的边进行删除注释操作，删除其对应g中（vp，vs）的注释
            Edge edge = ite.next();
            //对于每个边缘，删除其上的边缘注释“ pid（variant）”
            ChangeOperationOnMergedModel.DeleteEdgeAnnotationFromCG(originalMergedModel, edge.getFromVertex(), edge.getToVertex(),
                    edge_annotation_hash_set);
        }

        //消除孤立节点？？
        //更新映射
        Iterator<EdgePairFromG2CG> iterator = edgePairFromG2CGs.iterator();
        while(iterator.hasNext()){
            EdgePairFromG2CG edgePairFromG2CG = iterator.next();
            //删除部分g与cg的边缘映射，因为变体中的某些边已被删除
            if (edgePairFromG2CG.getLeft_G_e_id().equals(e.getId())){
                iterator.remove();
            }
        }
    }

    //更新节点标签的传播(G2CG)
    public static void UpdateLabelPropagation2CG (Graph originalMergedModel, Graph variantToBeChanged, String nlabel, Vertex nv, List<VertexPairFromG2CG> vertexPairFromG2CGs,List<EdgePairFromG2CG> edgePairFromG2CGs){
        //首先对变体的节点更新标签
        nv.setLabel(nlabel);

        //找到cg中nv对应的点cg_nv
        String id_cg_nv = findCorrespondingNodeIdInCG(nv.getID(),vertexPairFromG2CGs);
        Vertex cg_nv = new Vertex(nv.getType(),new String(""), new String(""));
        List<Vertex> vertexes = originalMergedModel.getVertices();
        Iterator<Vertex> it = vertexes.iterator();
        while (it.hasNext()){
            Vertex v = it.next();
            if(v.getID().equals(id_cg_nv)){
                cg_nv = v;
                break;
            }
        }

        //得到cg_nv的后续节点集
        List<Vertex> postset_of_id_cg_nv = originalMergedModel.getPostset(id_cg_nv);
        HashSet<String> label_of_e = new HashSet<>();
        String slabel_of_e="";
        Vertex post_of_id_cg_nv=null;
        if(!postset_of_id_cg_nv.isEmpty()){
            post_of_id_cg_nv = postset_of_id_cg_nv.get(0);
            //得到cg_nv和其后续节点集的边(出边)注释
            Edge e = originalMergedModel.containsEdge(id_cg_nv, post_of_id_cg_nv.getID());
            label_of_e = e.getLabels();
            slabel_of_e = label_of_e.toString();
        }


        //得到cg_nv的前继节点集
        List<Vertex> preset_of_id_cg_nv = originalMergedModel.getPreset(id_cg_nv);
        HashSet<String> label_of_e1 = new HashSet<>();
        String slabel_of_e1 ="";
        Vertex pre_of_id_cg_nv = null;
        if(!preset_of_id_cg_nv.isEmpty()){
            pre_of_id_cg_nv = preset_of_id_cg_nv.get(0);
            //得到cg_nv和其前继节点集的边(入边)注释
            Edge e1 = originalMergedModel.containsEdge(pre_of_id_cg_nv.getID(), id_cg_nv);
            label_of_e1 = e1.getLabels();
            slabel_of_e1 = label_of_e1.toString();
        }


        //根据cg_nv的出边和入边的注释数判断如何更新cg_nv的注释-------------------------------------------------------------------------------------
        if(!slabel_of_e.contains(",") && !slabel_of_e1.contains(",")){//若出边和入边的注释有一个大小为1
            cg_nv.setLabel(nlabel);//直接更新点注释即可
            //无需更新点边映射
        }
        else if(slabel_of_e1.contains(",") && !slabel_of_e.contains(",") ){//若入边的注释大于1且无出边
            Vertex m = new Vertex("xor",originalMergedModel.getIdGenerator().getNextId());//定义一个新的xor连接器m
            ChangeOperationOnMergedModel.InsertNodeToCG(originalMergedModel,pre_of_id_cg_nv,cg_nv,m);//插入到cg_nv及其前继节点之间
            Vertex v2 = new Vertex(nv.getType(),nlabel,originalMergedModel.getIdGenerator().getNextId());//生成新节点v2
            //在m后面追加v2
            ChangeOperationOnMergedModel.AppendNodeToCG(originalMergedModel,m,v2,variantToBeChanged.getGraphLabels(),originalMergedModel.getIdGenerator().getNextId());
            //删除边(m,cg_nv)上的相关图的注释
            ChangeOperationOnMergedModel.DeleteEdgeAnnotationFromCG(originalMergedModel,m.getID(),cg_nv.getID(),variantToBeChanged.getGraphLabels());

            //更新点映射
            Iterator<VertexPairFromG2CG> iterator = vertexPairFromG2CGs.iterator();//首先删除原本的nv的映射
            while(iterator.hasNext()){
                VertexPairFromG2CG vertexPairFromG2CG = iterator.next();
                if (vertexPairFromG2CG.getLeft_G_v_id().equals(nv.getID())){
                    iterator.remove();
                }
            }
            VertexPairFromG2CG vpg2g = new VertexPairFromG2CG(nv.getID(), v2.getID(), variantToBeChanged.ID, originalMergedModel.ID);//添加新的nv和v2的映射
            vertexPairFromG2CGs.add(vpg2g);

            //更新边映射
            List<Vertex> preset_of_nv = variantToBeChanged.getPreset(nv.getID());
            Vertex pre_of_nv = preset_of_nv.get(0);
            Edge ee = variantToBeChanged.containsEdge(pre_of_nv.getID(), nv.getID());//得到nv的入边ee
            Iterator<EdgePairFromG2CG> iterator1 = edgePairFromG2CGs.iterator();//删除ee原本的边映射
            while(iterator1.hasNext()){
                EdgePairFromG2CG edgePairFromG2CG = iterator1.next();
                if (edgePairFromG2CG.getLeft_G_e_id().equals(ee.getId())){
                    iterator1.remove();
                }
            }
            Edge right_CG_e = originalMergedModel.containsEdge(pre_of_id_cg_nv.getID(),m.getID());
            Edge right_CG_e1 = originalMergedModel.containsEdge(m.getID(),v2.getID());
            List<String> right = new ArrayList<String>();
            right.add(right_CG_e.getId());
            right.add(right_CG_e1.getId());
            EdgePairFromG2CG epg2cg = new EdgePairFromG2CG(ee.getId(), right, variantToBeChanged.ID, originalMergedModel.ID, new Boolean(false) );
            edgePairFromG2CGs.add(epg2cg);//将经过m的一段路径与边ee相映射
        }
        else if(slabel_of_e.contains(",") && !slabel_of_e1.contains(",") ){//若出边的注释大于1且无入边
            Vertex n = new Vertex("xor",originalMergedModel.getIdGenerator().getNextId());//定义一个新的xor连接器n
            ChangeOperationOnMergedModel.InsertNodeToCG(originalMergedModel,cg_nv,post_of_id_cg_nv,n);//插入到cg_nv及其后续节点之间
            Vertex v22 = new Vertex(nv.getType(),nlabel,originalMergedModel.getIdGenerator().getNextId());//生成新节点v22
            //在n前面预加v22
            ChangeOperationOnMergedModel.PrependNodeToCG(originalMergedModel,v22,n,variantToBeChanged.getGraphLabels(),originalMergedModel.getIdGenerator().getNextId());
            //删除边(cg_nv,n)上的相关图的注释
            ChangeOperationOnMergedModel.DeleteEdgeAnnotationFromCG(originalMergedModel,cg_nv.getID(),n.getID(),variantToBeChanged.getGraphLabels());

            //更新点映射
            Iterator<VertexPairFromG2CG> iterator = vertexPairFromG2CGs.iterator();//首先删除原本的nv的映射
            while(iterator.hasNext()){
                VertexPairFromG2CG vertexPairFromG2CG = iterator.next();
                if (vertexPairFromG2CG.getLeft_G_v_id().equals(nv.getID())){
                    iterator.remove();
                }
            }
            VertexPairFromG2CG vpg2g = new VertexPairFromG2CG(nv.getID(), v22.getID(), variantToBeChanged.ID, originalMergedModel.ID);//添加新的nv和v2的映射
            vertexPairFromG2CGs.add(vpg2g);

            //更新边映射
            List<Vertex> postset_of_nv = variantToBeChanged.getPostset(nv.getID());
            Vertex post_of_nv = postset_of_nv.get(0);
            Edge ee = variantToBeChanged.containsEdge(nv.getID(),post_of_nv.getID());//得到nv的出边ee
            Iterator<EdgePairFromG2CG> iterator1 = edgePairFromG2CGs.iterator();//删除ee原本的边映射
            while(iterator1.hasNext()){
                EdgePairFromG2CG edgePairFromG2CG = iterator1.next();
                if (edgePairFromG2CG.getLeft_G_e_id().equals(ee.getId())){
                    iterator1.remove();
                }
            }
            Edge right_CG_e = originalMergedModel.containsEdge(v22.getID(),n.getID());
            Edge right_CG_e1 = originalMergedModel.containsEdge(n.getID(),post_of_id_cg_nv.getID());
            List<String> right = new ArrayList<String>();
            right.add(right_CG_e.getId());
            right.add(right_CG_e1.getId());
            EdgePairFromG2CG epg2cg = new EdgePairFromG2CG(ee.getId(), right, variantToBeChanged.ID, originalMergedModel.ID, new Boolean(false) );
            edgePairFromG2CGs.add(epg2cg);//将经过m的一段路径与边ee相映射
        }
        else if(slabel_of_e1.contains(",") && slabel_of_e.contains(",")){//若出边和入边的注释都大于1
            Vertex mm = new Vertex("xor",originalMergedModel.getIdGenerator().getNextId());//定义一个新的xor连接器mm
            Vertex nn = new Vertex("xor",originalMergedModel.getIdGenerator().getNextId());//定义一个新的xor连接器nn
            ChangeOperationOnMergedModel.InsertNodeToCG(originalMergedModel,pre_of_id_cg_nv,cg_nv,mm);//将mm插入到cg_nv及其前继节点之间
            ChangeOperationOnMergedModel.InsertNodeToCG(originalMergedModel,cg_nv,post_of_id_cg_nv,nn);//将nn插入到cg_nv及其后续节点之间

            //为新边插入标签

            Vertex v222 = new Vertex(nv.getType(),nlabel,originalMergedModel.getIdGenerator().getNextId());//生成新节点v222
            //在mm和nn之间添加节点v222
            ChangeOperationOnMergedModel.AddNodeToCG(originalMergedModel,mm,nn,v222,variantToBeChanged.getGraphLabels(),
                    originalMergedModel.getIdGenerator().getNextId(),originalMergedModel.getIdGenerator().getNextId());
            //删除边(mm,cg_nv)、(cg_nv,nn)上的相关图的注释
            ChangeOperationOnMergedModel.DeleteEdgeAnnotationFromCG(originalMergedModel,mm.getID(),cg_nv.getID(),variantToBeChanged.getGraphLabels());
            ChangeOperationOnMergedModel.DeleteEdgeAnnotationFromCG(originalMergedModel,cg_nv.getID(),nn.getID(),variantToBeChanged.getGraphLabels());

            //更新点映射
            Iterator<VertexPairFromG2CG> iterator = vertexPairFromG2CGs.iterator();//首先删除原本的nv的映射
            while(iterator.hasNext()){
                VertexPairFromG2CG vertexPairFromG2CG = iterator.next();
                if (vertexPairFromG2CG.getLeft_G_v_id().equals(nv.getID())){
                    iterator.remove();
                }
            }
            VertexPairFromG2CG vpg2g = new VertexPairFromG2CG(nv.getID(), v222.getID(), variantToBeChanged.ID, originalMergedModel.ID);//添加新的nv和v2的映射
            vertexPairFromG2CGs.add(vpg2g);

            //更新边映射
            List<Vertex> preset_of_nv = variantToBeChanged.getPreset(nv.getID());
            Vertex pre_of_nv = preset_of_nv.get(0);
            Edge ee = variantToBeChanged.containsEdge(pre_of_nv.getID(), nv.getID());//得到nv的入边ee

            List<Vertex> postset_of_nv = variantToBeChanged.getPostset(nv.getID());
            Vertex post_of_nv = postset_of_nv.get(0);
            Edge eee = variantToBeChanged.containsEdge(nv.getID(),post_of_nv.getID());//得到nv的出边eee

            Iterator<EdgePairFromG2CG> iterator1 = edgePairFromG2CGs.iterator();//删除ee和eee原本的边映射
            while(iterator1.hasNext()){
                EdgePairFromG2CG edgePairFromG2CG = iterator1.next();
                if (edgePairFromG2CG.getLeft_G_e_id().equals(ee.getId())){
                    iterator1.remove();
                }
                if (edgePairFromG2CG.getLeft_G_e_id().equals(eee.getId())){
                    iterator1.remove();
                }
            }

            Edge right_CG_e = originalMergedModel.containsEdge(pre_of_id_cg_nv.getID(),mm.getID());
            Edge right_CG_e1 = originalMergedModel.containsEdge(mm.getID(),v222.getID());
            List<String> right = new ArrayList<String>();
            right.add(right_CG_e.getId());
            right.add(right_CG_e1.getId());
            EdgePairFromG2CG epg2cg = new EdgePairFromG2CG(ee.getId(), right, variantToBeChanged.ID, originalMergedModel.ID, new Boolean(false) );
            edgePairFromG2CGs.add(epg2cg);//将经过mm的一段路径与边ee相映射

            Edge right_CG_e0 = originalMergedModel.containsEdge(v222.getID(),nn.getID());
            Edge right_CG_e10 = originalMergedModel.containsEdge(nn.getID(),post_of_id_cg_nv.getID());
            List<String> right1 = new ArrayList<String>();
            right.add(right_CG_e0.getId());
            right.add(right_CG_e10.getId());
            EdgePairFromG2CG epg2cg1 = new EdgePairFromG2CG(eee.getId(), right1, variantToBeChanged.ID, originalMergedModel.ID, new Boolean(false) );
            edgePairFromG2CGs.add(epg2cg1);//将经过nn的一段路径与边eee相映射
        }
    }

    //改变边的传播(G2CG)
    public static void ChangeEdgePropagation2CG (Graph originalMergedModel, Graph variantToBeChanged, Vertex v_p, Vertex v_s, Vertex v_p1, Vertex v_s1, String label, String edge_id, List<VertexPairFromG2CG> vertexPairFromG2CGs,List<EdgePairFromG2CG> edgePairFromG2CGs){

        //对v_p1和v_s1之间进行插入边操作
        InsertEdgePropagation2CG(originalMergedModel,variantToBeChanged,v_p1,v_s1,label,edge_id,vertexPairFromG2CGs,edgePairFromG2CGs);

        //对v_p和v_s之间进行删除边操作
        DeleteEdgePropagation2CG(originalMergedModel,variantToBeChanged,v_p,v_s,vertexPairFromG2CGs,edgePairFromG2CGs);

        //接下来要进行删除冗余边注释操作并运行清洁操作，日后完善
    }

    //删除点的传播(G2CG)
    public static void removeNodePropagationFromG2CGCommand (Graph originalMergedModel, Graph variantToBeChanged, Vertex v, List<VertexPairFromG2CG> vertexPairFromG2CGs,List<EdgePairFromG2CG> edgePairFromG2CGs){

        //在G中找到VV并将其删除
        //Vertex V = variantToBeChanged.getVertexMap().get(v.getID());
        variantToBeChanged.removeVertex(v.getID());

        //在CG中找到对应的点VV并将其删除
        String v_id = findCorrespondingNodeIdInCG(v.getID(), vertexPairFromG2CGs);
        Vertex VV = new Vertex(v.getType(),new String(""), new String(""));
        List<Vertex> vertexes = originalMergedModel.getVertices();
        Iterator<Vertex> it = vertexes.iterator();
        while (it.hasNext()){
            Vertex vv = it.next();
            if(vv.getID().equals(v_id)){
                VV = vv;
                break;
            }
        }
        originalMergedModel.removeVertex(VV.getID());

        //更新点映射
        Iterator<VertexPairFromG2CG> iterator = vertexPairFromG2CGs.iterator();//删除原本的nv的映射
        while(iterator.hasNext()){
            VertexPairFromG2CG vertexPairFromG2CG = iterator.next();
            if (vertexPairFromG2CG.getLeft_G_v_id().equals(v.getID())){
                iterator.remove();
            }
        }
    }

    //插入点的传播(G2CG)
    public static void InsertNodePropagation2CG (Graph mergedModel, Graph variant, Vertex v_p, Vertex v_s, Vertex new_v, String front_edge_id, String rear_edge_id, List<VertexPairFromG2CG> vertexPairFromG2CGs, List<EdgePairFromG2CG> edgePairFromG2CGs){

        //在vp和vs的边上插入一个新节点
        ChangeOperationOnProcessVariant.InsertNodeToG(variant, new_v, v_p, v_s, front_edge_id, rear_edge_id);


        Vertex new_v_in_CG= new Vertex(new_v.getType(), new_v.getLabel(), mergedModel.getIdGenerator().getNextId());

        //得到CG中v_p对应的点的id
        String right_CG_v_p_id = findCorrespondingNodeIdInCG(v_p.getID(), vertexPairFromG2CGs);
        //得到CG中v_p对应的点CG_p
        Vertex right_CG_v_p = new Vertex(v_p.getType(),new String(""), new String(""));
        List<Vertex> vertexes_p = mergedModel.getVertices();
        Iterator<Vertex> it_v_p = vertexes_p.iterator();
        while (it_v_p.hasNext()){
            Vertex v = it_v_p.next();
            if(v.getID().equals(right_CG_v_p_id)){

                right_CG_v_p = v;
                break;
            }
        }

        //得到CG中v_s对应的点的id
        String right_CG_v_s_id = findCorrespondingNodeIdInCG(v_s.getID(), vertexPairFromG2CGs);
        //得到CG中v_s对应的点CG_p
        Vertex right_CG_v_s = new Vertex(v_s.getType(),new String(""), new String(""));
        List<Vertex> vertexes_s = mergedModel.getVertices();
        Iterator<Vertex> it_v_s = vertexes_s.iterator();
        while (it_v_s.hasNext()){
            Vertex v = it_v_s.next();
            if(v.getID().equals(right_CG_v_s_id)){

                right_CG_v_s = v;
                break;
            }
        }

        //得到cg_p的后续节点集
        List<Vertex> postset_of_right_CG_v_p = mergedModel.getPostset(right_CG_v_p_id);

        Vertex post_of_right_CG_v_p = postset_of_right_CG_v_p.get(0);

        if(!post_of_right_CG_v_p.getType().toString().equals("event")&&!post_of_right_CG_v_p.getType().toString().equals("function")){
            right_CG_v_s=post_of_right_CG_v_p;
        }




        //得到cg_p的前任节点集
        List<Vertex> preset_of_right_CG_v_s = mergedModel.getPreset(right_CG_v_s.getID());

        Vertex pre_of_right_CG_v_s = preset_of_right_CG_v_s.get(0);

        if(!pre_of_right_CG_v_s.getType().toString().equals("event")&&!pre_of_right_CG_v_s.getType().toString().equals("function")){

            right_CG_v_p=pre_of_right_CG_v_s;
        }

        Edge e1=mergedModel.containsEdge(right_CG_v_p.getID(),right_CG_v_s.getID());
        String lable_of_e1=e1.getLabels().toString();


        if(lable_of_e1.contains(",")){

            Vertex x1 = new Vertex("xor",mergedModel.getIdGenerator().getNextId());//定义一个新的连接器x1

            Vertex x2 = new Vertex("xor",mergedModel.getIdGenerator().getNextId());//定义一个新的连接器x2

            //得到变体的标签
            HashSet<String> g_edge_label = variant.getGraphLabels();
            //插入所需要的节点
            ChangeOperationOnMergedModel.InsertNodeToCG(mergedModel,right_CG_v_p,right_CG_v_s,x1);
            ChangeOperationOnMergedModel.InsertNodeToCG(mergedModel,x1,right_CG_v_s,x2);

            ChangeOperationOnMergedModel.AddNodeToCG(mergedModel,x1,x2,new_v_in_CG,g_edge_label,front_edge_id, rear_edge_id);
            ChangeOperationOnMergedModel.DeleteEdgeAnnotationFromCG(mergedModel,x1.getID(),x2.getID(), g_edge_label);

            //更新g和cg的节点映射
            VertexPairFromG2CG vpg2g = new VertexPairFromG2CG(new_v.getID(), new_v_in_CG.getID(), variant.ID, mergedModel.ID);
            vertexPairFromG2CGs.add(vpg2g);

            //更新g和cg的边映射
            Edge right_CG_e_1 = mergedModel.containsEdge(x1.getID(),new_v_in_CG.getID());
            Edge right_CG_e_1_2 = mergedModel.containsEdge(right_CG_v_p.getID(),x1.getID());
            List<String> right_1 = new ArrayList<String>();
            right_1.add(right_CG_e_1.getId());
            right_1.add(right_CG_e_1_2.getId());
            Edge left_G_e_1 = variant.containsEdge(v_p.getID(),new_v.getID());
            EdgePairFromG2CG epg2cg_1 = new EdgePairFromG2CG(left_G_e_1.getId(), right_1, variant.ID, mergedModel.ID, new Boolean(false) );
            edgePairFromG2CGs.add(epg2cg_1);

            Edge right_CG_e_2 = mergedModel.containsEdge(new_v_in_CG.getID(),x2.getID());
            Edge right_CG_e_2_2 = mergedModel.containsEdge(x2.getID(),right_CG_v_s.getID());
            List<String> right_2 = new ArrayList<String>();
            right_2.add(right_CG_e_2.getId());
            right_2.add(right_CG_e_2_2.getId());
            Edge left_G_e_2 = variant.containsEdge(new_v.getID(),v_s.getID());
            EdgePairFromG2CG epg2cg_2 = new EdgePairFromG2CG(left_G_e_2.getId(), right_2, variant.ID, mergedModel.ID, new Boolean(false) );
            edgePairFromG2CGs.add(epg2cg_2);

        }
        else{
            ChangeOperationOnMergedModel.InsertNodeToCG(mergedModel,right_CG_v_p,right_CG_v_s,new_v_in_CG);

            //更新g和cg的节点映射

            VertexPairFromG2CG vpg2g = new VertexPairFromG2CG(new_v.getID(), new_v_in_CG.getID(), variant.ID, mergedModel.ID);
            vertexPairFromG2CGs.add(vpg2g);

            //更新g和cg的边映射

            Edge right_CG_e_1 = mergedModel.containsEdge(right_CG_v_p.getID(),new_v_in_CG.getID());
            List<String> right_1 = new ArrayList<String>();
            right_1.add(right_CG_e_1.getId());
            Edge left_G_e_1 = variant.containsEdge(v_p.getID(),new_v.getID());
            EdgePairFromG2CG epg2cg_1 = new EdgePairFromG2CG(left_G_e_1.getId(), right_1, variant.ID, mergedModel.ID, new Boolean(false) );
            edgePairFromG2CGs.add(epg2cg_1);

            Edge right_CG_e_2 = mergedModel.containsEdge(new_v_in_CG.getID(),right_CG_v_s.getID());
            List<String> right_2 = new ArrayList<String>();
            right_2.add(right_CG_e_2.getId());
            Edge left_G_e_2 = variant.containsEdge(new_v.getID(),v_s.getID());
            EdgePairFromG2CG epg2cg_2 = new EdgePairFromG2CG(left_G_e_2.getId(), right_2, variant.ID, mergedModel.ID, new Boolean(false) );
            edgePairFromG2CGs.add(epg2cg_2);

        }

    }

    //添加点的传播(G2CG)
    public static void AddNodePropagation2CG(Graph mergedModel, Graph variant, Vertex v_p, Vertex v_s, Vertex new_v, String lable, String front_edge_id, String rear_edge_id, List<VertexPairFromG2CG> vertexPairFromG2CGs, List<EdgePairFromG2CG> edgePairFromG2CGs){

        HashSet<String> edge_annotation_hash_set = variant.getGraphLabels();

        ChangeOperationOnProcessVariant.AddNodeToG(variant,new_v,v_p,v_s,lable,front_edge_id,rear_edge_id);

        //得到CG中v_p对应的点的id
        String right_CG_v_p_id = findCorrespondingNodeIdInCG(v_p.getID(), vertexPairFromG2CGs);
        //得到CG中v_p对应的点CG_p
        Vertex right_CG_v_p = new Vertex(v_p.getType(),new String(""), new String(""));
        List<Vertex> vertexes_p = mergedModel.getVertices();
        Iterator<Vertex> it_v_p = vertexes_p.iterator();
        while (it_v_p.hasNext()){
            Vertex v = it_v_p.next();
            if(v.getID().equals(right_CG_v_p_id)){

                right_CG_v_p = v;
                break;
            }
        }
        //得到CG中v_s对应的点的id
        String right_CG_v_s_id = findCorrespondingNodeIdInCG(v_s.getID(), vertexPairFromG2CGs);
        //得到CG中v_s对应的点CG_s
        Vertex right_CG_v_s = new Vertex(v_s.getType(),new String(""), new String(""));
        List<Vertex> vertexes_s = mergedModel.getVertices();
        Iterator<Vertex> it_v_s = vertexes_s.iterator();
        while (it_v_s.hasNext()){
            Vertex v = it_v_s.next();
            if(v.getID().equals(right_CG_v_s_id)){

                right_CG_v_s = v;
                break;
            }
        }

        Vertex new_v_in_CG = new Vertex(new_v.getType(), new_v.getLabel(), mergedModel.getIdGenerator().getNextId());

        ChangeOperationOnMergedModel.AddNodeToCG(mergedModel,right_CG_v_p,right_CG_v_s,new_v_in_CG,edge_annotation_hash_set,front_edge_id,rear_edge_id);

        //更新g和cg的节点映射

        VertexPairFromG2CG vpg2g = new VertexPairFromG2CG(new_v.getID(), new_v_in_CG.getID(), variant.ID, mergedModel.ID);
        vertexPairFromG2CGs.add(vpg2g);

        //更新g和cg的边映射

        Edge right_CG_e_1 = mergedModel.containsEdge(right_CG_v_p.getID(),new_v_in_CG.getID());
        List<String> right_1 = new ArrayList<String>();
        right_1.add(right_CG_e_1.getId());
        Edge left_G_e_1 = variant.containsEdge(v_p.getID(),new_v.getID());
        EdgePairFromG2CG epg2cg_1 = new EdgePairFromG2CG(left_G_e_1.getId(), right_1, variant.ID, mergedModel.ID, new Boolean(false) );
        edgePairFromG2CGs.add(epg2cg_1);

        Edge right_CG_e_2 = mergedModel.containsEdge(new_v_in_CG.getID(),right_CG_v_s.getID());
        List<String> right_2 = new ArrayList<String>();
        right_2.add(right_CG_e_2.getId());
        Edge left_G_e_2 = variant.containsEdge(new_v.getID(),v_s.getID());
        EdgePairFromG2CG epg2cg_2 = new EdgePairFromG2CG(left_G_e_2.getId(), right_2, variant.ID, mergedModel.ID, new Boolean(false) );
        edgePairFromG2CGs.add(epg2cg_2);


    }

    /** 相关函数：寻找CG中的对应点的id */
    public static String findCorrespondingNodeIdInCG(String vertexCanonicalIdInG, List<VertexPairFromG2CG> vertexPairFromG2CGs){

        String vertex_id_in_CG = new String("");

        //loop
        Iterator<VertexPairFromG2CG> it = vertexPairFromG2CGs.iterator();

        while (it.hasNext()){

            VertexPairFromG2CG vertexPairFromG2CG = it.next();

            if(vertexPairFromG2CG.getLeft_G_v_id().equals(vertexCanonicalIdInG)){
                vertex_id_in_CG = vertexPairFromG2CG.getRight_CG_v_id();
            }
        }



        return vertex_id_in_CG;

    }

    /** 相关函数：寻找CG中的对应边的id(可能不唯一) */
    public static List<String> findCorrespondingEdgeSetIdInCG(String edgeCanonicalIdInG, List<EdgePairFromG2CG> edgePairFromG2CGs){

        List<String> edge_ids_in_CG = new ArrayList<String>();

        //loop
        Iterator<EdgePairFromG2CG> it = edgePairFromG2CGs.iterator();

        while(it.hasNext()){

            EdgePairFromG2CG edgePairFromG2CG = it.next();

            if(edgePairFromG2CG.getLeft_G_e_id().equals(edgeCanonicalIdInG)){

                edge_ids_in_CG = edgePairFromG2CG.getRight_CG_p_e_id();

            }

        }

        return edge_ids_in_CG;
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
