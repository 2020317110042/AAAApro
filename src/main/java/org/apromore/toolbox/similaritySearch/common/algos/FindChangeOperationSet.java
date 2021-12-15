package org.apromore.toolbox.similaritySearch.common.algos;

import org.apache.commons.collections.CollectionUtils;
import org.apromore.toolbox.similaritySearch.common.IdGeneratorHelper;
import org.apromore.toolbox.similaritySearch.graph.Edge;
import org.apromore.toolbox.similaritySearch.graph.Graph;
import org.apromore.toolbox.similaritySearch.graph.Vertex;

import java.io.IOException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class FindChangeOperationSet {

    public static List<ChangeOperation> findGraphEditDistanceInG(Graph sg1, Graph sg2){

        //创建一个列表以记录一系列变更操作
        List<ChangeOperation> changeOperationList = new ArrayList<>();

        IdGeneratorHelper idGenerator = new IdGeneratorHelper();

        //以下为点操作
        List<Vertex> vertexList_sg1 = sg1.getVertices();//获得sg1的节点集
        List<Vertex> vertexList_original_sg1 = new ArrayList<Vertex>();//'vertexList_original_sg1'是sg1节点的复制
        for(Vertex v : vertexList_sg1){
            vertexList_original_sg1.add(v);
        }
        //创建一个数组列表以保存'vertexList_sg1'的ID
        List<String> ids_vertexList_in_G1 = new ArrayList<String>();
        for(Vertex v : vertexList_original_sg1){
            ids_vertexList_in_G1.add(v.getID());
        }


        List<Vertex> vertexList_sg2 = sg2.getVertices();//获得sg2的节点集
        List<Vertex> vertexList_original_sg2 = new ArrayList<Vertex>();//'vertexList_original_sg2'是sg2节点的复制
        for(Vertex v : vertexList_sg2){
            vertexList_original_sg2.add(v);
        }
        //创建一个数组列表以保存'vertexList_sg2'的ID
        List<String> ids_vertexList_in_G2 = new ArrayList<String>();
        for(Vertex v : vertexList_original_sg2){
            ids_vertexList_in_G2.add(v.getID());
        }


        //找到g1和g2中id一致但标签却不一致的点
        List<Vertex> clabel_vertexList = new ArrayList<Vertex>();
        Vertex vv = new Vertex("xor",new String(""));
        for(Vertex v_sg1 : vertexList_original_sg1)
            for(Vertex v_sg2: vertexList_original_sg2)//需要加个判断条件：除了连接器之外的点
                if(!v_sg1.getType().equals(vv.getType()) && !v_sg2.getType().equals(vv.getType()))
                    if(v_sg2.getID().equals( v_sg1.getID() ) && !v_sg2.getLabel().equals( v_sg1.getLabel() ) )
                        clabel_vertexList.add(v_sg2);//将sg1和sg2中id一致但标签却不一致的点添加至clabel_vertexlist


        //获得g1和g2节点的交集，并添加至disjoint_vertexList
        List<Vertex> disjoint_vertexList = new ArrayList<Vertex>();
        for(Vertex v_sg1 : vertexList_original_sg1){
            for(Vertex v_sg2: vertexList_original_sg2){
                if(v_sg2.getID().equals(v_sg1.getID())){
                    disjoint_vertexList.add(v_sg2);//将sg1和sg2共有的点添加至disjoint_vertexList
                }
            }
        }


        //获取应从G1中删除的顶点
        List<Vertex> deleted_vertexList_from_G1;
        Iterator<Vertex> it_sg1 = vertexList_original_sg1.iterator();
        //从g1中删除disjoint_vertexList中的点
        while (it_sg1.hasNext()){
            Vertex v_in_sg1 = it_sg1.next();
            for(Vertex v_disjoint : disjoint_vertexList){
                if(v_in_sg1.getID().equals(v_disjoint.getID())){
                    it_sg1.remove();
                }
            }
        }
        deleted_vertexList_from_G1 = vertexList_original_sg1;//得到g2中没有但g1中有的点 deleted_vertexList_from_G1**********
        List<String> ids_deleted_vertexList_from_G1 = new ArrayList<String>();//用列表保存其id
        for(Vertex v : deleted_vertexList_from_G1){
            ids_deleted_vertexList_from_G1.add(v.getID());
        }


        //获取应添加到G1的节点
        List<Vertex> added_vertexList_to_G1;
        Iterator<Vertex> it_sg2 = vertexList_original_sg2.iterator();
        //从g2中删除disjoint_vertexList中的点
        while(it_sg2.hasNext()){
            Vertex v_in_sg2 = it_sg2.next();
            for(Vertex v_disjoint : disjoint_vertexList){
                if(v_in_sg2.getID().equals(v_disjoint.getID())){
                    it_sg2.remove();
                }
            }
        }
        added_vertexList_to_G1 = vertexList_original_sg2;//得到g2中有但g1中没有的点 added_vertexList_to_G1******************
        List<String> ids_added_vertexList_to_G1 = new ArrayList<String>();//用列表保存其id
        for(Vertex v : added_vertexList_to_G1){
            ids_added_vertexList_to_G1.add(v.getID());
        }


        //以下为边操作
        List<Edge> edgeList_original_sg1 = new ArrayList<Edge>();//存储g1的所有边
        for(Edge e : sg1.getEdges()){
            edgeList_original_sg1.add(e);
        }
        List<String> ids_edgeList_sg1 = new ArrayList<String>();//存储g1的所有边的id
        for(Edge edge : edgeList_original_sg1){
            ids_edgeList_sg1.add(edge.getId());
        }
        List<Edge> edgeList_original_sg2 = new ArrayList<Edge>();//存储g2的所有边
        for(Edge e : sg2.getEdges()){
            edgeList_original_sg2.add(e);
        }
        List<String> ids_edgeList_sg2 = new ArrayList<String>();//存储g2的所有边的id
        for(Edge edge : edgeList_original_sg2){
            ids_edgeList_sg2.add(edge.getId());
        }


        //获得g1边和g2边的交集并存于disjoint_edgeList
        List<Edge> disjoint_edgeList = new ArrayList<Edge>();
        for(Edge e_sg1 : edgeList_original_sg1){
            for(Edge e_sg2: edgeList_original_sg2){
                if(e_sg2.getId().equals(e_sg1.getId())){
                    disjoint_edgeList.add(e_sg2);
                }
            }
        }


        //得到g1中应删除的边
        List<Edge> deleted_edgeList_from_G1;
        Iterator<Edge> it_sg14edge = edgeList_original_sg1.iterator();
        //从g1中删除disjoint_edgeList中的边
        while (it_sg14edge.hasNext()){
            Edge e_in_sg1 = it_sg14edge.next();
            for(Edge e_disjoint : disjoint_edgeList){
                if(e_in_sg1.getId().equals(e_disjoint.getId())){
                    it_sg14edge.remove();
                }
            }
        }
        deleted_edgeList_from_G1 = edgeList_original_sg1;//得到g2中没有但g1中有的边 deleted_edgeList_from_G1****************
        List<String> ids_deleted_edgeList_from_G1 = new ArrayList<String>();//用列表保存其id
        for(Edge e : deleted_edgeList_from_G1){
            ids_deleted_edgeList_from_G1.add(e.getId());
        }


        //获取应添加到G1的边
        List<Edge> added_edgeList_to_G1;
        Iterator<Edge> it_sg24edge = edgeList_original_sg2.iterator();
        //从g2中删除disjoint_edgeList中的边
        while(it_sg24edge.hasNext()){
            Edge e_in_sg2 = it_sg24edge.next();
            for(Edge e_disjoint : disjoint_edgeList){
                if(e_in_sg2.getId().equals(e_disjoint.getId())){
                    it_sg24edge.remove();
                }
            }
        }
        added_edgeList_to_G1 = edgeList_original_sg2;//得到g2中有但g1中没有的边 added_edgeList_to_G1************************
        List<String> ids_added_edgeList_to_G1 = new ArrayList<String>();//用列表保存其id
        for(Edge e : added_edgeList_to_G1){
            ids_added_edgeList_to_G1.add(e.getId());
        }


        /*汇总低级更改原语，即
        1.删除节点；
        2.添加节点；
        3.删除边；
        4.将边缘添加到更多高级更改操作中
        Loop 'added_edgeList_to_G1'
        EdgeObjWithVisitedFlag[] added_edge_array = new EdgeObjWithVisitedFlag[added_edgeList_to_G1.size()]; //set an empty array
        for(int i =0; i< added_edge_array.length; i++){

            EdgeObjWithVisitedFlag edgeObjWithVisitedFlag = new EdgeObjWithVisitedFlag();
            edgeObjWithVisitedFlag.setEdge(added_edgeList_to_G1.get(i));
            edgeObjWithVisitedFlag.setFlag(false); //initially set false, which means edge has not been visited
            added_edge_array[i] = edgeObjWithVisitedFlag;
        }

        EdgeObjWithVisitedFlag[] deleted_edge_array = new EdgeObjWithVisitedFlag[deleted_edgeList_from_G1.size()]; //set an empty array
        for(int i =0; i< deleted_edge_array.length; i++){

            EdgeObjWithVisitedFlag edgeObjWithVisitedFlag = new EdgeObjWithVisitedFlag();
            edgeObjWithVisitedFlag.setEdge(deleted_edgeList_from_G1.get(i));
            edgeObjWithVisitedFlag.setFlag(false); //initially set false, which means edge has not been visited
            deleted_edge_array[i] = edgeObjWithVisitedFlag;
        }*/


        List<EdgeObjWithVisitedFlag> added_edge_array = new ArrayList<EdgeObjWithVisitedFlag>();//added_edge_array用于记录应添加的边是否已被访问过
        Iterator<Edge> added_edgeList_to_G1_it = added_edgeList_to_G1.iterator();
        while(added_edgeList_to_G1_it.hasNext()){
            Edge edge = added_edgeList_to_G1_it.next();
            EdgeObjWithVisitedFlag edgeObjWithVisitedFlag = new EdgeObjWithVisitedFlag();
            edgeObjWithVisitedFlag.setEdge(edge);
            edgeObjWithVisitedFlag.setFlag(false);
            added_edge_array.add(edgeObjWithVisitedFlag);
        }


        List<EdgeObjWithVisitedFlag> deleted_edge_array = new ArrayList<EdgeObjWithVisitedFlag>();//deleted_edge_array用于记录应删除的边是否已被访问过
        Iterator<Edge> deleted_edgeList_from_G1_it = deleted_edgeList_from_G1.iterator();
        while(deleted_edgeList_from_G1_it.hasNext()){
            Edge edge = deleted_edgeList_from_G1_it.next();
            EdgeObjWithVisitedFlag edgeObjWithVisitedFlag = new EdgeObjWithVisitedFlag();
            edgeObjWithVisitedFlag.setEdge(edge);
            edgeObjWithVisitedFlag.setFlag(false);
            deleted_edge_array.add(edgeObjWithVisitedFlag);
        }


        List<NodeObjWithVisitedFlag> added_vertex_array = new ArrayList<NodeObjWithVisitedFlag>();//added_vertex_array用于记录应添加的点是否已被访问过
        Iterator<Vertex> added_vertexList_to_G1_it = added_vertexList_to_G1.iterator();
        while(added_vertexList_to_G1_it.hasNext()){
            Vertex vertex = added_vertexList_to_G1_it.next();
            NodeObjWithVisitedFlag nodeObjWithVisitedFlag = new NodeObjWithVisitedFlag();
            nodeObjWithVisitedFlag.setVertex(vertex);
            nodeObjWithVisitedFlag.setFlag(false);
            added_vertex_array.add(nodeObjWithVisitedFlag);
        }


        List<NodeObjWithVisitedFlag> deleted_vertex_array = new ArrayList<NodeObjWithVisitedFlag>();//deleted_vertex_array用于记录应删除的点是否已被访问过
        Iterator<Vertex> deleted_vertexList_from_G1_it = deleted_vertexList_from_G1.iterator();
        while(deleted_vertexList_from_G1_it.hasNext()){
            Vertex vertex = deleted_vertexList_from_G1_it.next();
            NodeObjWithVisitedFlag nodeObjWithVisitedFlag = new NodeObjWithVisitedFlag();
            nodeObjWithVisitedFlag.setVertex(vertex);
            nodeObjWithVisitedFlag.setFlag(false);
            deleted_vertex_array.add(nodeObjWithVisitedFlag);
        }


        List<NodeObjWithVisitedFlag> clabel_vertex_array = new ArrayList<NodeObjWithVisitedFlag>();//clabel_vertex_array用于记录应更新标签的点是否已被访问过
        Iterator<Vertex> clabel_vertexList_to_G1_it = clabel_vertexList.iterator();
        while(clabel_vertexList_to_G1_it.hasNext()){
            Vertex vertex = clabel_vertexList_to_G1_it.next();
            NodeObjWithVisitedFlag nodeObjWithVisitedFlag = new NodeObjWithVisitedFlag();
            nodeObjWithVisitedFlag.setVertex(vertex);
            nodeObjWithVisitedFlag.setFlag(false);
            clabel_vertex_array.add(nodeObjWithVisitedFlag);
        }

        /**“改变边”的条件判断*/
        if(checkEdgeVisited(added_edge_array) && checkEdgeVisited(deleted_edge_array) && !checkVertexVisited(deleted_vertex_array) && !checkVertexVisited(added_vertex_array)){
            while(checkEdgeVisited(added_edge_array) && checkEdgeVisited(deleted_edge_array)){
                Iterator<EdgeObjWithVisitedFlag> it_del = deleted_edge_array.iterator();
                Iterator<EdgeObjWithVisitedFlag> it_ins = added_edge_array.iterator();
                while(it_del.hasNext() && it_ins.hasNext()){
                    EdgeObjWithVisitedFlag edgeObjWithVisitedFlag_d = it_del.next();//得到当前应删除的边
                    EdgeObjWithVisitedFlag edgeObjWithVisitedFlag_i = it_ins.next();//得到当前应添加的边
                    if(edgeObjWithVisitedFlag_d.getFlag() == false && edgeObjWithVisitedFlag_i.getFlag() == false){
                        Edge e_deleted_from_G1 = edgeObjWithVisitedFlag_d.getEdge();
                        Edge e_added_into_G1 = edgeObjWithVisitedFlag_i.getEdge();
                        String dstarting_point = e_deleted_from_G1.getFromVertex();//应删除边的起点
                        String dending_point = e_deleted_from_G1.getToVertex();//应删除边的终点
                        String istarting_point = e_added_into_G1.getFromVertex();//应添加边的起点
                        String iending_point = e_added_into_G1.getToVertex();//应添加边的终点

                        if(ids_vertexList_in_G1.contains(istarting_point)&&ids_vertexList_in_G1.contains(iending_point)&&ids_vertexList_in_G1.contains(dstarting_point)&&ids_vertexList_in_G1.contains(dending_point)){
                            ChangeOperation op = new ChangeOperation();
                            op.setChangeOperationName(ChangeOperationName.CHANGEEDGEING);

                            List<Object> changeOperationArgument = new ArrayList<Object>();
                            changeOperationArgument.add(sg1.ID);//第一个参数为变体的id
                            changeOperationArgument.add(dstarting_point);//第二个参数为旧边的起点
                            changeOperationArgument.add(dending_point);//第三个参数为旧边的终点
                            changeOperationArgument.add(istarting_point);//第四个参数为新边的起点
                            changeOperationArgument.add(iending_point);//第五个参数为新边的终点
                            op.setChangeOperationArgues(changeOperationArgument);
                            changeOperationList.add(op);//将变更操作添加到列表中

                            edgeObjWithVisitedFlag_d.setFlag(true);//已访问过该边
                            edgeObjWithVisitedFlag_i.setFlag(true);//已访问过该边
                            ids_deleted_edgeList_from_G1.remove(e_deleted_from_G1.getId());//在”待删除边id“中删除该边的id
                            ids_edgeList_sg1.remove(e_deleted_from_G1.getId());//将该边的id从sg1中删除
                            ids_added_edgeList_to_G1.remove(e_added_into_G1.getId());//在”待添加边id“中删除该边的id
                            ids_edgeList_sg1.add(e_added_into_G1.getId());//将该边的id加入sg1
                        }

                    }
                }
            }
        }

        /**“预、追加点”、“插入边”、“添加点”的条件判断*/
        if(checkEdgeVisited(added_edge_array)&&!checkEdgeVisited(deleted_edge_array)) {
            while (checkEdgeVisited(added_edge_array)) {

                //addnode的判断条件
                Iterator<NodeObjWithVisitedFlag> it_add_node = added_vertex_array.iterator();
                while (it_add_node.hasNext()) {

                    NodeObjWithVisitedFlag nodeObjWithVisitedFlag = it_add_node.next();
                    if (nodeObjWithVisitedFlag.getFlag() == false) {

                        Vertex v_add_to_g1 = nodeObjWithVisitedFlag.getVertex();//new_v
                        String v_add_to_g1_id = v_add_to_g1.getID();
                        //找到前置节点
                        List<Vertex> parents_of_new_node = sg2.getPreset(v_add_to_g1_id);
                        Vertex parent_of_new_node = new Vertex(Vertex.Type.function, "#", idGenerator.getNextId());
                        if (parents_of_new_node.size() > 0) {

                            parent_of_new_node = (Vertex) CollectionUtils.get(parents_of_new_node, 0);//here we assume 'starting point' has just one parent

                        }
                        String starting_point = parent_of_new_node.getID();//starting_point
                        //找到后置节点
                        List<Vertex> children_of_new_node = sg2.getPostset(v_add_to_g1_id);
                        Vertex child_of_new_node = new Vertex(Vertex.Type.function, "#", idGenerator.getNextId());
                        if (children_of_new_node.size() > 0) {

                            child_of_new_node = (Vertex) CollectionUtils.get(children_of_new_node, 0);//here we assume 'ending point' has just one child
                        }
                        String ending_point = child_of_new_node.getID();//ending_point

                        //找出add node 的潜在操作，新加的节点的前置和后置节点在ids_vertexList_in_G1中，而新添加的节点在ids_added_vertexList_to_G1中
                        if (ids_vertexList_in_G1.contains(starting_point) && ids_vertexList_in_G1.contains(ending_point) && ids_added_vertexList_to_G1.contains(v_add_to_g1_id)) {

                            ChangeOperation op = new ChangeOperation();
                            op.setChangeOperationName(ChangeOperationName.ADDNODETOG);
                            List<Object> changeOperationArgument = new ArrayList<Object>();
                            //根据更改操作函数的参数顺序保留参数的顺序
                            changeOperationArgument.add(v_add_to_g1);
                            changeOperationArgument.add(starting_point);
                            changeOperationArgument.add(ending_point);
                            //根据更改操作函数的参数顺序保留参数的顺序

                            op.setChangeOperationArgues(changeOperationArgument);
                            changeOperationList.add(op);
                            nodeObjWithVisitedFlag.setFlag(true);

                            ids_added_vertexList_to_G1.remove(v_add_to_g1_id);
                            ids_vertexList_in_G1.add(v_add_to_g1_id);

                            //将加入顶点的标志设置为true，这意味着加入的顶点已被访问
                            Iterator<NodeObjWithVisitedFlag> added_vertex_array_it = added_vertex_array.iterator();
                            while (added_vertex_array_it.hasNext()) {
                                NodeObjWithVisitedFlag vertex_obj_with_flag = added_vertex_array_it.next();
                                if (vertex_obj_with_flag.getVertex().getID().equals(v_add_to_g1_id)) {
                                    vertex_obj_with_flag.setFlag(true);
                                }

                            }


                            Logger log1 = Logger.getLogger("2");
                            try {
                                FileHandler fileHandler = new FileHandler("C:/Users/wang/Desktop/test/addnode.log", true);
                                fileHandler.setLevel(Level.INFO);
                                fileHandler.setFormatter(new java.util.logging.Formatter() {

                                    public String format(LogRecord record) {
                                        return record.getLevel() + ": " + record.getMessage() + "\n";
                                    }
                                });
                                log1.addHandler(fileHandler);
                                log1.info("addnode");


                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }
                    }
                }
                //addnode的判断条件结束


                Iterator<EdgeObjWithVisitedFlag> edge_obj_with_flag_it = added_edge_array.iterator();
                while (edge_obj_with_flag_it.hasNext()) {
                    EdgeObjWithVisitedFlag edgeObjWithVisitedFlag = edge_obj_with_flag_it.next();//得到当前应添加的边
                    if (edgeObjWithVisitedFlag.getFlag() == false) {
                        Edge e_added_into_G1 = edgeObjWithVisitedFlag.getEdge();
                        String starting_point = e_added_into_G1.getFromVertex();
                        String ending_point = e_added_into_G1.getToVertex();

                        //找出“附加节点”的潜在变更操作，判断条件是：新添加的边的起始节点在g1中，而终止节点在“应添加到G1的节点”中
                        if (ids_vertexList_in_G1.contains(starting_point) && ids_added_vertexList_to_G1.contains(ending_point)) {

                            ChangeOperation op = new ChangeOperation();
                            op.setChangeOperationName(ChangeOperationName.APPENDNODETOG);
                            List<Object> changeOperationArgument = new ArrayList<Object>();

                            //根据更改操作函数的参数顺序保留参数的顺序/
                            changeOperationArgument.add(sg1.ID);
                            changeOperationArgument.add(ending_point);  //**new_v
                            changeOperationArgument.add(starting_point);  //**v_p
                            //根据更改操作函数的参数顺序保留参数的顺序/

                            op.setChangeOperationArgues(changeOperationArgument);
                            changeOperationList.add(op);

                            //remove this edge from 'added_edgeList_to_G1' and 'ids_added_edgeList_to_G1'
                            edgeObjWithVisitedFlag.setFlag(true);//set it to be true, which means this edge has been visited.

                            ids_added_edgeList_to_G1.remove(e_added_into_G1.getId());
                            //add this edge to sg1. I.e. 'edgeList_original_G1'
                            //edgeList_original_sg1.add(e_added_into_G1);
                            ids_edgeList_sg1.add(e_added_into_G1.getId());

                            //remove the appended node from 'added_vertexList_to_G1' and 'ids_added_vertexList_to_G1'
//                        Iterator<Vertex> it_2 = added_vertexList_to_G1.iterator();
//                        while (it_2.hasNext()){
//
//                            Vertex v = it_2.next();
//                            if(v.getID().equals(ending_point)){
//
//                                //in the meanwhile, add the appended node to sg1. I.e. 'ids_vertex_G1'
//                                //vertexList_original_sg1.add(v);
//                                ids_vertexList_in_G1.add(v.getID());
//
//                                it_2.remove();
//
//                            }
//                        }

                            ids_vertexList_in_G1.add(ending_point);

                            ids_added_vertexList_to_G1.remove(ending_point);

                            //set the flag of the appended vertex as true, which means the appended vertex has been visited
                            Iterator<NodeObjWithVisitedFlag> added_vertex_array_it = added_vertex_array.iterator();
                            while (added_vertex_array_it.hasNext()) {

                                NodeObjWithVisitedFlag vertex_obj_with_flag = added_vertex_array_it.next();

                                if (vertex_obj_with_flag.getVertex().getID().equals(ending_point)) {

                                    vertex_obj_with_flag.setFlag(true);

                                }

                            }

                        }

                        //找出“预加节点”的潜在更改操作，判断条件是：新添加的边的起始节点在“应添加到G1的节点”中，而终止节点在g1中
                        if (ids_vertexList_in_G1.contains(ending_point) && ids_added_vertexList_to_G1.contains(starting_point)) {
                            ChangeOperation op = new ChangeOperation();
                            op.setChangeOperationName(ChangeOperationName.PREPENDNODETOG);
                            List<Object> changeOperationArgument = new ArrayList<Object>();

                            //根据更改操作函数的参数顺序保留参数的顺序
                            changeOperationArgument.add(sg1.ID);
                            changeOperationArgument.add(starting_point);  //new_v
                            changeOperationArgument.add(ending_point);  //v_s

                            op.setChangeOperationArgues(changeOperationArgument);
                            changeOperationList.add(op);

                            //从“added_edgeList_to_G1”和“ids_added_edgeList_to_G1”中删除此边
                            edgeObjWithVisitedFlag.setFlag(true);
                            //将其设置为true，表示此边缘已被访问过

                            ids_added_edgeList_to_G1.remove(e_added_into_G1.getId());
                            //将该边加入sg1
                            //edgeList_original_sg1.add(e_added_into_G1);
                            ids_edgeList_sg1.add(e_added_into_G1.getId());

                            //从“ added_vertexList_to_G1”和“ ids_added_vertexList_to_G1”中删除附加的节点
//                        Iterator<Vertex> it_2 = added_vertexList_to_G1.iterator();
//                        while (it_2.hasNext()){
//
//                            Vertex v = it_2.next();
//                            if(v.getID().equals(ending_point)){
//
//                                //in the meanwhile, add the appended node to sg1. I.e. 'ids_vertex_G1'
//                                //vertexList_original_sg1.add(v);
//                                ids_vertexList_in_G1.add(v.getID());
//
//                                it_2.remove();
//
//                            }
//                        }

                            ids_vertexList_in_G1.add(starting_point);

                            ids_added_vertexList_to_G1.remove(starting_point);

                            //将附加顶点的标志设置为true，这意味着附加顶点已被访问
                            Iterator<NodeObjWithVisitedFlag> added_vertex_array_it = added_vertex_array.iterator();
                            while (added_vertex_array_it.hasNext()) {
                                NodeObjWithVisitedFlag vertex_obj_with_flag = added_vertex_array_it.next();
                                if (vertex_obj_with_flag.getVertex().getID().equals(starting_point)) {
                                    vertex_obj_with_flag.setFlag(true);
                                }

                            }

                        }

                        //找出“插入边”的潜在更改操作，判断条件是：新添加的边的起始节点在g1中，而终止节点也在g1中
                        if (ids_vertexList_in_G1.contains(starting_point) && ids_vertexList_in_G1.contains(ending_point)) {
                            ChangeOperation op = new ChangeOperation();
                            op.setChangeOperationName(ChangeOperationName.INSERTEDGETOG);

                            List<Object> changeOperationArgument = new ArrayList<Object>();
                            changeOperationArgument.add(starting_point);  //第一个变量为起点
                            changeOperationArgument.add(ending_point);    //第二个变量为终点
                            op.setChangeOperationArgues(changeOperationArgument);
                            changeOperationList.add(op);//将变更操作添加到列表中

                            edgeObjWithVisitedFlag.setFlag(true);//已访问过该边
                            //added_edgeList_to_G1.remove(e_added_into_G1);//在”待添加边“中删除该边
                            ids_added_edgeList_to_G1.remove(e_added_into_G1.getId());//在”待添加边id“中删除该边的id
                            //edgeList_original_sg1.add(e_added_into_G1);//将该边加入sg1
                            ids_edgeList_sg1.add(e_added_into_G1.getId());//将该边的id加入sg1


                            Logger log1 = Logger.getLogger("2");
                            try {
                                FileHandler fileHandler = new FileHandler("C:/Users/wang/Desktop/test/inertedge.log", true);
                                fileHandler.setLevel(Level.INFO);
                                fileHandler.setFormatter(new java.util.logging.Formatter() {

                                    public String format(LogRecord record) {
                                        return record.getLevel() + ": " + record.getMessage() + "\n";
                                    }
                                });
                                log1.addHandler(fileHandler);
                                log1.info("inertedge");


                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
            }
        }

        /**“插入点”的条件判断*/
        while (checkVertexVisited(added_vertex_array)&& checkEdgeVisited(deleted_edge_array)){

            Iterator<NodeObjWithVisitedFlag> it_add_node = added_vertex_array.iterator();
            Iterator<Edge> it_del = deleted_edgeList_from_G1.iterator();
            //Iterator<EdgeObjWithVisitedFlag> edge_obj_with_flag_it = added_edge_array.iterator();
            while (it_add_node.hasNext()&& it_del.hasNext()){

                NodeObjWithVisitedFlag nodeObjWithVisitedFlag=it_add_node.next();
                //EdgeObjWithVisitedFlag edgeObjWithVisitedFlag =edge_obj_with_flag_it.next();
                Edge e_deleted_from_G1 = it_del.next();
                if(nodeObjWithVisitedFlag.getFlag()==false  /*&& edgeObjWithVisitedFlag.getFlag()==false*/){
                    //点的判断的准备
                    Vertex v_add_to_g1 = nodeObjWithVisitedFlag.getVertex();//new_v
                    //找到前置节点
                    List<Vertex> parents_of_new_node =sg2.getPreset(v_add_to_g1.getID());
                    Vertex parent_of_new_node = new Vertex(Vertex.Type.function, "#", idGenerator.getNextId());
                    if(parents_of_new_node.size()>0){

                        parent_of_new_node = (Vertex)CollectionUtils.get(parents_of_new_node,0);//here we assume 'starting point' has just one parent

                    }
                    String starting_point =parent_of_new_node.getID();//starting_point
                    //找到后置节点
                    List<Vertex> children_of_new_node = sg2.getPostset(v_add_to_g1.getID());
                    Vertex child_of_new_node = new Vertex(Vertex.Type.function, "#", idGenerator.getNextId());
                    if(children_of_new_node.size()>0){

                        child_of_new_node = (Vertex)CollectionUtils.get(children_of_new_node,0);//here we assume 'ending point' has just one child
                    }
                    String ending_point = child_of_new_node.getID();//ending_point


                    //边的判断的准备
                    //Edge e_deleted_from_G1 = sg1.containsEdge(starting_point,ending_point);
                    //Edge e_deleted_from_G1 = edgeObjWithVisitedFlag_d.getEdge();


                    //判断条件
                    if(ids_vertexList_in_G1.contains(starting_point)&&ids_vertexList_in_G1.contains(ending_point)&&ids_added_vertexList_to_G1.contains(v_add_to_g1.getID())
                            && ids_deleted_edgeList_from_G1.contains(e_deleted_from_G1.getId())){

                        ChangeOperation op = new ChangeOperation();
                        op.setChangeOperationName(ChangeOperationName.INSERTNODETOG);

                        List<Object> changeOperationArgument = new ArrayList<Object>();
                        //根据更改操作函数的参数顺序保留参数的顺序
                        changeOperationArgument.add(v_add_to_g1);
                        changeOperationArgument.add(starting_point);
                        changeOperationArgument.add(ending_point);
                        //根据更改操作函数的参数顺序保留参数的顺序
                        op.setChangeOperationArgues(changeOperationArgument);
                        changeOperationList.add(op);

                        nodeObjWithVisitedFlag.setFlag(true);
                        //edgeObjWithVisitedFlag_d.setFlag(true);

                        //ids_deleted_edgeList_from_G1.remove(e_deleted_from_G1.getId());//在”待删除边id“中删除该边的id
                        //ids_edgeList_sg1.remove(e_deleted_from_G1.getId());//将该边的id从sg1中删除
                        it_del.remove();
                        ids_deleted_edgeList_from_G1.remove(e_deleted_from_G1.getId());

                        ids_added_vertexList_to_G1.remove(v_add_to_g1.getID());//将该点从增加点集中删除
                        ids_vertexList_in_G1.add(v_add_to_g1.getID());//将该点加入g1点集


                        //将插入顶点的标志设置为true，这意味着插入的顶点已被访问
                        Iterator<NodeObjWithVisitedFlag> added_vertex_array_it = added_vertex_array.iterator();
                        while(added_vertex_array_it.hasNext()){
                            NodeObjWithVisitedFlag vertex_obj_with_flag = added_vertex_array_it.next();
                            if(vertex_obj_with_flag.getVertex().getID().equals(v_add_to_g1.getID())){
                                vertex_obj_with_flag.setFlag(true);
                            }

                        }

                        //将删除边的标志设置为true，这意味着删除边已被访问
                        Iterator<EdgeObjWithVisitedFlag> deleted_edge_array_it = deleted_edge_array.iterator();
                        while(deleted_edge_array_it.hasNext()){

                            EdgeObjWithVisitedFlag edge_obj_with_flag = deleted_edge_array_it.next();

                            if(edge_obj_with_flag.getEdge().getFromVertex().equals(starting_point) && edge_obj_with_flag.getEdge().getToVertex().equals(ending_point)){
                                edge_obj_with_flag.setFlag(true);
                            }
                        }
                    }
                }
            }
        }

        /**“预、追加点”、“插入边”的条件判断*/
        /*while(checkEdgeVisited(added_edge_array)){//检查是否每一条应添加的边都已被访问
            Iterator<EdgeObjWithVisitedFlag> edge_obj_with_flag_it = added_edge_array.iterator();
            while(edge_obj_with_flag_it.hasNext()){
                EdgeObjWithVisitedFlag edgeObjWithVisitedFlag = edge_obj_with_flag_it.next();//得到当前应添加的边
                if(edgeObjWithVisitedFlag.getFlag() == false){
                    Edge e_added_into_G1 = edgeObjWithVisitedFlag.getEdge();
                    String starting_point = e_added_into_G1.getFromVertex();
                    String ending_point = e_added_into_G1.getToVertex();

                    //找出“附加节点”的潜在变更操作，判断条件是：新添加的边的起始节点在g1中，而终止节点在“应添加到G1的节点”中
                    if(ids_vertexList_in_G1.contains(starting_point)&&ids_added_vertexList_to_G1.contains(ending_point)){

                        ChangeOperation op = new ChangeOperation();
                        op.setChangeOperationName(ChangeOperationName.APPENDNODETOG);
                        List<Object> changeOperationArgument = new ArrayList<Object>();

                        changeOperationArgument.add(sg1.ID);
                        changeOperationArgument.add(ending_point);  //**new_v
                        changeOperationArgument.add(starting_point);  //**v_p

                        op.setChangeOperationArgues(changeOperationArgument);
                        changeOperationList.add(op);

                        edgeObjWithVisitedFlag.setFlag(true);//set it to be true, which means this edge has been visited.

                        ids_added_edgeList_to_G1.remove(e_added_into_G1.getId());
                        ids_edgeList_sg1.add(e_added_into_G1.getId());


                        ids_vertexList_in_G1.add(ending_point);

                        ids_added_vertexList_to_G1.remove(ending_point);

                        Iterator<NodeObjWithVisitedFlag> added_vertex_array_it = added_vertex_array.iterator();
                        while(added_vertex_array_it.hasNext()){

                            NodeObjWithVisitedFlag vertex_obj_with_flag = added_vertex_array_it.next();

                            if(vertex_obj_with_flag.getVertex().getID().equals(ending_point)){

                                vertex_obj_with_flag.setFlag(true);

                            }

                        }

                    }

                    //找出“预加节点”的潜在更改操作，判断条件是：新添加的边的起始节点在“应添加到G1的节点”中，而终止节点在g1中
                    if(ids_vertexList_in_G1.contains(ending_point)&&ids_added_vertexList_to_G1.contains(starting_point)){
                        ChangeOperation op = new ChangeOperation();
                        op.setChangeOperationName(ChangeOperationName.PREPENDNODETOG);
                        List<Object> changeOperationArgument = new ArrayList<Object>();

                        changeOperationArgument.add(sg1.ID);
                        changeOperationArgument.add(starting_point);  //new_v
                        changeOperationArgument.add(ending_point);  //v_s

                        op.setChangeOperationArgues(changeOperationArgument);
                        changeOperationList.add(op);

                        //从“added_edgeList_to_G1”和“ids_added_edgeList_to_G1”中删除此边
                        edgeObjWithVisitedFlag.setFlag(true);
                        //将其设置为true，表示此边缘已被访问过

                        ids_added_edgeList_to_G1.remove(e_added_into_G1.getId());
                        ids_edgeList_sg1.add(e_added_into_G1.getId());
                        ids_vertexList_in_G1.add(starting_point);
                        ids_added_vertexList_to_G1.remove(starting_point);

                        //将附加顶点的标志设置为true，这意味着附加顶点已被访问
                        Iterator<NodeObjWithVisitedFlag> added_vertex_array_it = added_vertex_array.iterator();
                        while(added_vertex_array_it.hasNext()){
                            NodeObjWithVisitedFlag vertex_obj_with_flag = added_vertex_array_it.next();
                            if(vertex_obj_with_flag.getVertex().getID().equals(starting_point)){
                                vertex_obj_with_flag.setFlag(true);
                            }

                        }

                    }

                    //找出“插入边”的潜在更改操作，判断条件是：新添加的边的起始节点在g1中，而终止节点也在g1中
                    if(ids_vertexList_in_G1.contains(starting_point)&&ids_vertexList_in_G1.contains(ending_point)){
                        ChangeOperation op = new ChangeOperation();
                        op.setChangeOperationName(ChangeOperationName.INSERTEDGETOG);

                        List<Object> changeOperationArgument = new ArrayList<Object>();
                        changeOperationArgument.add(starting_point);  //第一个变量为起点
                        changeOperationArgument.add(ending_point);    //第二个变量为终点
                        op.setChangeOperationArgues(changeOperationArgument);
                        changeOperationList.add(op);//将变更操作添加到列表中

                        edgeObjWithVisitedFlag.setFlag(true);//已访问过该边
                        //added_edgeList_to_G1.remove(e_added_into_G1);//在”待添加边“中删除该边
                        ids_added_edgeList_to_G1.remove(e_added_into_G1.getId());//在”待添加边id“中删除该边的id
                        //edgeList_original_sg1.add(e_added_into_G1);//将该边加入sg1
                        ids_edgeList_sg1.add(e_added_into_G1.getId());//将该边的id加入sg1
                    }

                }
            }
        }*/

        /**“删除边”的条件判断*/
        while(checkEdgeVisited(deleted_edge_array)){//检查是否每一条应删除的边都已被访问
            Iterator<Edge> it_del = deleted_edgeList_from_G1.iterator();
            while(it_del.hasNext()){

                Edge e_deleted_from_G1 = it_del.next();
                String starting_point = e_deleted_from_G1.getFromVertex();
                String ending_point = e_deleted_from_G1.getToVertex();

                //找出“删除边缘”的潜在更改操作,判断条件是：待删除的边的起始节点在g1中，而终止节点也在g1中
                if(ids_vertexList_in_G1.contains(starting_point)&&ids_vertexList_in_G1.contains(ending_point)){

                    ChangeOperation op = new ChangeOperation();
                    op.setChangeOperationName(ChangeOperationName.DELETEEDGEFROMG);
                    List<Object> changeOperationArgument = new ArrayList<Object>();

                /*keep the order of arguments here in accordance with the order of arguments of change operation function*/
                    changeOperationArgument.add(starting_point);  //**v_p
                    changeOperationArgument.add(ending_point);  //**v_s

                /*keep the order of arguments here in accordance with the order of arguments of change operation function*/

                    op.setChangeOperationArgues(changeOperationArgument);
                    changeOperationList.add(op);

                    //**remove this edge from 'deleted_edgeList_from_G1' and 'ids_deleted_edgeList_from_G1'**//
                    it_del.remove();
                    ids_deleted_edgeList_from_G1.remove(e_deleted_from_G1.getId());

                    /*set the flag of the deleted edge as true, which means the deleted edge has been visited, added on 4 sep 2016*/
                    Iterator<EdgeObjWithVisitedFlag> deleted_edge_array_it = deleted_edge_array.iterator();
                    while(deleted_edge_array_it.hasNext()){

                        EdgeObjWithVisitedFlag edge_obj_with_flag = deleted_edge_array_it.next();

                        if(edge_obj_with_flag.getEdge().getFromVertex().equals(starting_point) && edge_obj_with_flag.getEdge().getToVertex().equals(ending_point)){
                            edge_obj_with_flag.setFlag(true);
                        }
                    }
                }
            }
        }

        /**“更新点注释”的条件判断*/
        while(checkVertexVisited(clabel_vertex_array)){
            Iterator<NodeObjWithVisitedFlag> it_del = clabel_vertex_array.iterator();
            while(it_del.hasNext()){
                NodeObjWithVisitedFlag nodeObjWithVisitedFlag = it_del.next();//得到当前应更新标签的点
                if(nodeObjWithVisitedFlag.getFlag() == false){
                    Vertex nv = nodeObjWithVisitedFlag.getVertex();
                    String id_nv = nv.getID();
                    ChangeOperation op = new ChangeOperation();
                    op.setChangeOperationName(ChangeOperationName.UPDATELABELTOG);

                    List<Object> changeOperationArgument = new ArrayList<Object>();
                    changeOperationArgument.add(id_nv);//将nv的id添加到变量
                    op.setChangeOperationArgues(changeOperationArgument);
                    changeOperationList.add(op);//将变更操作添加到列表中

                    nodeObjWithVisitedFlag.setFlag(true);//已访问过该点
                }
            }
        }

        /**“移除点”的条件判断*/
        while(checkVertexVisited(deleted_vertex_array)){
            Iterator<NodeObjWithVisitedFlag> it_del = deleted_vertex_array.iterator();
            while(it_del.hasNext()){
                NodeObjWithVisitedFlag nodeObjWithVisitedFlag = it_del.next();
                if(nodeObjWithVisitedFlag.getFlag() == false){
                    Vertex nv = nodeObjWithVisitedFlag.getVertex();
                    String id_nv = nv.getID();
                    ChangeOperation op = new ChangeOperation();
                    op.setChangeOperationName(ChangeOperationName.REMOVENODEFROMG);

                    List<Object> changeOperationArgument = new ArrayList<Object>();
                    changeOperationArgument.add(id_nv);//将nv的id添加到变量
                    op.setChangeOperationArgues(changeOperationArgument);
                    changeOperationList.add(op);//将变更操作添加到列表中

                    nodeObjWithVisitedFlag.setFlag(true);//已访问过该点
                }
            }
        }


        return changeOperationList;
}

    public static List<ChangeOperation> findGraphEditDistanceInCG(Graph sg1, Graph sg2){

        IdGeneratorHelper idGenerator = new IdGeneratorHelper();

        //创建一个列表以记录一系列更改操作
        List<ChangeOperation> changeOperationList = new ArrayList<ChangeOperation>();


        /*********************************************1 点操作**********************************************************/
        //用vertexList_original_sg1复制sg1中的节点，并用ids_vertexList_in_CG1保存其id
        List<Vertex> vertexList_sg1 = sg1.getVertices();
        List<Vertex> vertexList_original_sg1 = new ArrayList<Vertex>();
        for(Vertex v : vertexList_sg1){
            vertexList_original_sg1.add(v);
        }
        List<String> ids_vertexList_in_CG1 = new ArrayList<String>();
        for(Vertex v : vertexList_original_sg1){
            ids_vertexList_in_CG1.add(v.getID());
        }


        //用vertexList_original_sg2复制sg2中的节点，并用ids_vertexList_in_CG2保存其id
        List<Vertex> vertexList_sg2 = sg2.getVertices();
        List<Vertex> vertexList_original_sg2 = new ArrayList<Vertex>();
        for(Vertex v : vertexList_sg2){
            vertexList_original_sg2.add(v);
        }
        List<String> ids_vertexList_in_CG2 = new ArrayList<String>();
        for(Vertex v : vertexList_original_sg2){
            ids_vertexList_in_CG2.add(v.getID());
        }


        //得到sg1和sg2节点的交集，存到disjoint_vertexList
        List<Vertex> disjoint_vertexList = new ArrayList<Vertex>();
        for(Vertex v_sg1 : vertexList_original_sg1){
            for(Vertex v_sg2: vertexList_original_sg2){
                if(v_sg2.getID().equals(v_sg1.getID())){
                    disjoint_vertexList.add(v_sg2);
                }
            }
        }


        //获取应从CG1中删除的顶点，即sg1减disjoint_vertexList，存到deleted_vertexList_from_CG1，并用ids_deleted_vertexList_from_CG1存放其id
        List<Vertex> deleted_vertexList_from_CG1;
        Iterator<Vertex> it_sg1 = vertexList_original_sg1.iterator();
        while (it_sg1.hasNext()){
            Vertex v_in_sg1 = it_sg1.next();
            for(Vertex v_disjoint : disjoint_vertexList){
                if(v_in_sg1.getID().equals(v_disjoint.getID())){
                    it_sg1.remove();
                }
            }
        }
        deleted_vertexList_from_CG1 = vertexList_original_sg1;
        List<String> ids_deleted_vertexList_from_CG1 = new ArrayList<String>();
        for(Vertex v : deleted_vertexList_from_CG1){
            ids_deleted_vertexList_from_CG1.add(v.getID());
        }


        //获取应添加到CG1的顶点，即sg2减disjoint_vertexList，存到added_vertexList_to_CG1，并用ids_added_vertexList_to_CG1存放其id
        List<Vertex> added_vertexList_to_CG1;
        Iterator<Vertex> it_sg2 = vertexList_original_sg2.iterator();
        while(it_sg2.hasNext()){
            Vertex v_in_sg2 = it_sg2.next();
            for(Vertex v_disjoint : disjoint_vertexList){
                if(v_in_sg2.getID().equals(v_disjoint.getID())){
                    it_sg2.remove();
                }
            }
        }
        added_vertexList_to_CG1 = vertexList_original_sg2;
        List<String> ids_added_vertexList_to_CG1 = new ArrayList<String>();
        for(Vertex v : added_vertexList_to_CG1){
            ids_added_vertexList_to_CG1.add(v.getID());
        }


        /*********************************************2 边操作**********************************************************/
        //用edgeList_original_sg1复制sg1中的边，并用ids_edgeList_sg1保存其id
        List<Edge> edgeList_original_sg1 = new ArrayList<Edge>();
        for(Edge e : sg1.getEdges()){
            edgeList_original_sg1.add(e);
        }
        List<String> ids_edgeList_sg1 = new ArrayList<String>();
        for(Edge edge : edgeList_original_sg1){
            ids_edgeList_sg1.add(edge.getId());
        }


        //用edgeList_original_sg2复制sg2中的边，并用ids_edgeList_sg2保存其id
        List<Edge> edgeList_original_sg2 = new ArrayList<Edge>();
        for(Edge e : sg2.getEdges()){
            edgeList_original_sg2.add(e);
        }
        List<String> ids_edgeList_sg2 = new ArrayList<String>();
        for(Edge edge : edgeList_original_sg2){
            ids_edgeList_sg2.add(edge.getId());
        }


        //得到sg1和sg2边的交集，存到disjoint_edgeList
        List<Edge> disjoint_edgeList = new ArrayList<Edge>();
        for(Edge e_sg1 : edgeList_original_sg1){
            for(Edge e_sg2: edgeList_original_sg2){
                if(e_sg2.getId().equals(e_sg1.getId())){
                    disjoint_edgeList.add(e_sg2);
                }
            }
        }


        //获取应从CG1中删除的边，即sg1减disjoint_edgeList，存到deleted_edgeList_from_CG1，并用ids_deleted_edgeList_from_CG1存放其id
        List<Edge> deleted_edgeList_from_CG1;
        Iterator<Edge> it_sg14edge = edgeList_original_sg1.iterator();
        while (it_sg14edge.hasNext()){
            Edge e_in_sg1 = it_sg14edge.next();
            for(Edge e_disjoint : disjoint_edgeList){
                if(e_in_sg1.getId().equals(e_disjoint.getId())){
                    it_sg14edge.remove();
                }
            }
        }
        deleted_edgeList_from_CG1 = edgeList_original_sg1;
        List<String> ids_deleted_edgeList_from_CG1 = new ArrayList<String>();
        for(Edge e : deleted_edgeList_from_CG1){
            ids_deleted_edgeList_from_CG1.add(e.getId());
        }


        //获取应添加到CG1的边，即sg2减disjoint_edgeList，存到added_edgeList_to_CG1，并用ids_added_edgeList_to_CG1存放其id
        List<Edge> added_edgeList_to_CG1;
        Iterator<Edge> it_sg24edge = edgeList_original_sg2.iterator();
        while(it_sg24edge.hasNext()){
            Edge e_in_sg2 = it_sg24edge.next();
            for(Edge e_disjoint : disjoint_edgeList){
                if(e_in_sg2.getId().equals(e_disjoint.getId())){
                    it_sg24edge.remove();
                }
            }
        }
        added_edgeList_to_CG1 = edgeList_original_sg2;
        List<String> ids_added_edgeList_to_CG1 = new ArrayList<String>();
        for(Edge e : added_edgeList_to_CG1){
            ids_added_edgeList_to_CG1.add(e.getId());
        }


        /***********************************************3 添加访问标记***************************************************/
        List<EdgeObjWithVisitedFlag> added_edge_array = new ArrayList<EdgeObjWithVisitedFlag>();//added_edge_array表示应添加的边的访问情况
        Iterator<Edge> added_edgeList_to_CG1_it = added_edgeList_to_CG1.iterator();
        while(added_edgeList_to_CG1_it.hasNext()){

            Edge edge = added_edgeList_to_CG1_it.next();
            EdgeObjWithVisitedFlag edgeObjWithVisitedFlag = new EdgeObjWithVisitedFlag();
            edgeObjWithVisitedFlag.setEdge(edge);
            edgeObjWithVisitedFlag.setFlag(false);
            added_edge_array.add(edgeObjWithVisitedFlag);
        }

        List<EdgeObjWithVisitedFlag> deleted_edge_array = new ArrayList<EdgeObjWithVisitedFlag>();//deleted_edge_array表示应删除的边的访问情况
        Iterator<Edge> deleted_edgeList_from_CG1_it = deleted_edgeList_from_CG1.iterator();
        while(deleted_edgeList_from_CG1_it.hasNext()){

            Edge edge = deleted_edgeList_from_CG1_it.next();
            EdgeObjWithVisitedFlag edgeObjWithVisitedFlag = new EdgeObjWithVisitedFlag();
            edgeObjWithVisitedFlag.setEdge(edge);
            edgeObjWithVisitedFlag.setFlag(false);
            deleted_edge_array.add(edgeObjWithVisitedFlag);
        }

        List<EdgeObjWithVisitedFlag> joint_edge_array = new ArrayList<EdgeObjWithVisitedFlag>();//joint_edge_array表示sg1与sg2的公共边的访问情况
        Iterator<Edge> joint_edge_array_it = disjoint_edgeList.iterator();
        while(joint_edge_array_it.hasNext()){

            Edge edge = joint_edge_array_it.next();
            EdgeObjWithVisitedFlag edgeObjWithVisitedFlag = new EdgeObjWithVisitedFlag();
            edgeObjWithVisitedFlag.setEdge(edge);
            edgeObjWithVisitedFlag.setFlag(false);
            joint_edge_array.add(edgeObjWithVisitedFlag);
        }

        List<NodeObjWithVisitedFlag> added_vertex_array = new ArrayList<NodeObjWithVisitedFlag>();//added_vertex_array表示应添加的点的访问情况
        Iterator<Vertex> added_vertexList_to_CG1_it = added_vertexList_to_CG1.iterator();
        while(added_vertexList_to_CG1_it.hasNext()){

            Vertex vertex = added_vertexList_to_CG1_it.next();
            NodeObjWithVisitedFlag nodeObjWithVisitedFlag = new NodeObjWithVisitedFlag();
            nodeObjWithVisitedFlag.setVertex(vertex);
            nodeObjWithVisitedFlag.setFlag(false);
            added_vertex_array.add(nodeObjWithVisitedFlag);

        }

        List<NodeObjWithVisitedFlag> deleted_vertex_array = new ArrayList<NodeObjWithVisitedFlag>();//deleted_vertex_array表示应删除的点的访问情况
        Iterator<Vertex> deleted_vertexList_from_CG1_it = deleted_vertexList_from_CG1.iterator();
        while(deleted_vertexList_from_CG1_it.hasNext()){

            Vertex vertex = deleted_vertexList_from_CG1_it.next();
            NodeObjWithVisitedFlag nodeObjWithVisitedFlag = new NodeObjWithVisitedFlag();
            nodeObjWithVisitedFlag.setVertex(vertex);
            nodeObjWithVisitedFlag.setFlag(false);
            deleted_vertex_array.add(nodeObjWithVisitedFlag);
        }

        List<NodeObjWithVisitedFlag> joint_node_array = new ArrayList<NodeObjWithVisitedFlag>();//joint_node_array表示sg1与sg2的公共点的访问情况
        Iterator<Vertex> joint_node_array_it = disjoint_vertexList.iterator();
        while(joint_node_array_it.hasNext()){

            Vertex vertex = joint_node_array_it.next();
            NodeObjWithVisitedFlag nodeObjWithVisitedFlag = new NodeObjWithVisitedFlag();
            nodeObjWithVisitedFlag.setVertex(vertex);
            nodeObjWithVisitedFlag.setFlag(false);
            joint_node_array.add(nodeObjWithVisitedFlag);
        }
/**********************************************4 操作的条件判断**********************************************************/
        //"添加边注释"、"删除边注释"的判断条件
        Iterator<EdgeObjWithVisitedFlag> joint_edge__it = joint_edge_array.iterator();
        while(joint_edge__it.hasNext()){
            EdgeObjWithVisitedFlag joint_edge_obj_with_flag = joint_edge__it.next();
            if(joint_edge_obj_with_flag.getFlag() == false){

                //获取边缘的起点和终点
                String starting_point = joint_edge_obj_with_flag.getEdge().getFromVertex();
                String ending_point = joint_edge_obj_with_flag.getEdge().getToVertex();

                //获取该边在sg1的注释
                HashSet<String> labels_on_edge_in_sg1 = new HashSet<String>();
                labels_on_edge_in_sg1 = sg1.getEdgeLabels(starting_point,ending_point);
                //去除注释的逗号
                HashSet<String> labels_on_edge_in_sg1_after_trim = new HashSet<String>();
                Iterator<String> labels_on_edge_in_sg1_it = labels_on_edge_in_sg1.iterator();
                while(labels_on_edge_in_sg1_it.hasNext()){

                    String labels_on_edge_in_sg1_before_trim = labels_on_edge_in_sg1_it.next();
                    labels_on_edge_in_sg1_after_trim = convertCommaSeparatedStringToHashset(labels_on_edge_in_sg1_before_trim);
                }

                //获取该边在sg2的注释
                HashSet<String> labels_on_edge_in_sg2 = new HashSet<String>();
                labels_on_edge_in_sg2 = sg2.getEdgeLabels(starting_point,ending_point);

                HashSet<String> labels_on_edge_in_sg2_after_trim = new HashSet<String>();
                Iterator<String> labels_on_edge_in_sg2_it = labels_on_edge_in_sg2.iterator();
                while(labels_on_edge_in_sg2_it.hasNext()){

                    String labels_on_edge_in_sg2_before_trim = labels_on_edge_in_sg2_it.next();
                    labels_on_edge_in_sg2_after_trim = convertCommaSeparatedStringToHashset(labels_on_edge_in_sg2_before_trim);
                }

                //得到应添加到sg1的注释，即'labels_on_edge_in_sg2'减去'labels_on_edge_in_sg1'
                HashSet<String> added_labels_on_edge_in_sg1 = new HashSet<String>();
                Iterator<String> labels_on_edge_in_sg2_after_trim_it = labels_on_edge_in_sg2_after_trim.iterator();
                while(labels_on_edge_in_sg2_after_trim_it.hasNext()){

                    String label_on_edge_in_sg2 = labels_on_edge_in_sg2_after_trim_it.next();
                    added_labels_on_edge_in_sg1.add(label_on_edge_in_sg2);

                }
                added_labels_on_edge_in_sg1.removeAll(labels_on_edge_in_sg1_after_trim);

                //得到应在sg1中删除的注释，即'labels_on_edge_in_sg1'减去'labels_on_edge_in_sg2'
                HashSet<String> removed_labels_on_edge_from_sg1 = new HashSet<String>();
                Iterator<String> labels_on_edge_in_sg1_after_trim_it = labels_on_edge_in_sg1_after_trim.iterator();
                while(labels_on_edge_in_sg1_after_trim_it.hasNext()){

                    String label_on_edge_in_sg1 = labels_on_edge_in_sg1_after_trim_it.next();
                    removed_labels_on_edge_from_sg1.add(label_on_edge_in_sg1);

                }
                removed_labels_on_edge_from_sg1.removeAll(labels_on_edge_in_sg2_after_trim);

                if(added_labels_on_edge_in_sg1.size()>0){
                    ChangeOperation op = new ChangeOperation();
                    op.setChangeOperationName(ChangeOperationName.ADDEDGEANNOTATION);
                    List<Object> changeOperationArgument = new ArrayList<Object>();

                    changeOperationArgument.add(starting_point);//边的起点
                    changeOperationArgument.add(ending_point);  //边的终点
                    changeOperationArgument.add(added_labels_on_edge_in_sg1); //应添加的边注释

                    op.setChangeOperationArgues(changeOperationArgument);
                    changeOperationList.add(op);
                }
                else if(removed_labels_on_edge_from_sg1.size()>0){
                    ChangeOperation op = new ChangeOperation();
                    op.setChangeOperationName(ChangeOperationName.DELETEEDGEANNOTATIONFROMCG);
                    List<Object> changeOperationArgument = new ArrayList<Object>();

                    changeOperationArgument.add(starting_point);
                    changeOperationArgument.add(ending_point);
                    changeOperationArgument.add(removed_labels_on_edge_from_sg1);

                    op.setChangeOperationArgues(changeOperationArgument);
                    changeOperationList.add(op);
                }
            }
            joint_edge_obj_with_flag.setFlag(true);//表示该边已被访问
        }

        //“更新点注释”的判断条件
        Iterator<NodeObjWithVisitedFlag> joint_node__it = joint_node_array.iterator();
        if(!checkEdgeVisited(added_edge_array) && !checkEdgeVisited(deleted_edge_array) && !checkVertexVisited(deleted_vertex_array) && !checkVertexVisited(added_vertex_array))
        while(joint_node__it.hasNext()){
            NodeObjWithVisitedFlag joint_node_obj_with_flag = joint_node__it.next();
            if(joint_node_obj_with_flag.getVertex().getType().toString().equals("gateway"))
                continue;
            else if(joint_node_obj_with_flag.getFlag() == false){

                String v_id=joint_node_obj_with_flag.getVertex().getID();
                //获取该点在sg1的注释
                Vertex CG_v = new Vertex(joint_node_obj_with_flag.getVertex().getType(),new String(""), new String(""));
                List<Vertex> vertexes = sg1.getVertices();
                Iterator<Vertex> it = vertexes.iterator();
                while (it.hasNext()){
                    Vertex v = it.next();
                    if(v.getID().equals(v_id)){
                        CG_v = v;
                        break;
                    }
                }
                String label1=CG_v.getLabel();

                //获取该点在sg2的注释
                Vertex CG_v1 = new Vertex(joint_node_obj_with_flag.getVertex().getType(),new String(""), new String(""));
                List<Vertex> vertexes1 = sg2.getVertices();
                Iterator<Vertex> it1 = vertexes1.iterator();
                while (it1.hasNext()){
                    Vertex v = it1.next();
                    if(v.getID().equals(v_id)){
                        CG_v1 = v;
                        break;
                    }
                }
                String label2=CG_v1.getLabel();

                //若label1与label2不同，则进行更新点注释操作
                if(!label1.equals(label2)){
                    ChangeOperation op = new ChangeOperation();
                    op.setChangeOperationName(ChangeOperationName.UPDATENODEANNOTATION);
                    List<Object> changeOperationArgument = new ArrayList<Object>();

                    changeOperationArgument.add(v_id); //应添加的边注释

                    op.setChangeOperationArgues(changeOperationArgument);
                    changeOperationList.add(op);
                }
            }
            joint_node_obj_with_flag.setFlag(true);//表示该点已被访问
        }

        //"插入边"、“预加/追加节点“等操作的判断条件
        while(checkEdgeVisited(added_edge_array)){
            Iterator<EdgeObjWithVisitedFlag> added_edgeObjWithVisitedFlagIterator = added_edge_array.iterator();
            while(added_edgeObjWithVisitedFlagIterator.hasNext()){
                EdgeObjWithVisitedFlag added_edge_obj_with_flag = added_edgeObjWithVisitedFlagIterator.next();
                if(added_edge_obj_with_flag.getFlag() == false){
                    Edge e_added_into_CG1 = added_edge_obj_with_flag.getEdge();
                    String starting_point = e_added_into_CG1.getFromVertex();
                    String ending_point = e_added_into_CG1.getToVertex();

                    //找到起点的父节点以及终点的子节点，以判断"添加节点"操作
                    List<Vertex> parents_of_starting_point =sg2.getPreset(starting_point);
                    Vertex parent_of_starting_point = new Vertex(Vertex.Type.function, "#", idGenerator.getNextId());
                    if(parents_of_starting_point.size()>0){
                        parent_of_starting_point = (Vertex)CollectionUtils.get(parents_of_starting_point,0);
                    }
                    List<Vertex> children_of_ending_point = sg2.getPostset(ending_point);
                    Vertex child_of_ending_point = new Vertex(Vertex.Type.function, "#", idGenerator.getNextId());
                    if(children_of_ending_point.size()>0){
                        child_of_ending_point = (Vertex)CollectionUtils.get(children_of_ending_point,0);
                    }

                    //修剪边的注释
                    HashSet<String> edgeLabels = new HashSet<String>();
                    Iterator iterator = e_added_into_CG1.getLabels().iterator();
                    while(iterator.hasNext()){
                        String edgeLablesWithComma = (String)iterator.next();
                        edgeLabels = convertCommaSeparatedStringToHashset(edgeLablesWithComma);
                    }

                    //addnode的判断条件：待添加边的起点的父节点在sg1中，且待添加边的起点在待添加节点集中，且待添加边的终点在sg1中。(即当前的待添加边是添加节点的出边)★
                    if(ids_vertexList_in_CG1.contains(parent_of_starting_point.getID()) && ids_added_vertexList_to_CG1.contains(starting_point) &&
                       ids_vertexList_in_CG1.contains(ending_point)){

                        ChangeOperation op = new ChangeOperation();
                        op.setChangeOperationName(ChangeOperationName.ADDNODETOCG);
                        List<Object> changeOperationArgument = new ArrayList<Object>();

                        changeOperationArgument.add(starting_point);//new_v
                        changeOperationArgument.add(parent_of_starting_point.getID());//v_p
                        changeOperationArgument.add(ending_point);//v_s
                        changeOperationArgument.add(edgeLabels);//两条新边的注释

                        op.setChangeOperationArgues(changeOperationArgument);
                        changeOperationList.add(op);
                        added_edge_obj_with_flag.setFlag(true);//当前待添加边已被访问
                        ids_added_edgeList_to_CG1.remove(e_added_into_CG1.getId());//在待添加边的id集中删除该边的id
                        ids_edgeList_sg1.add(e_added_into_CG1.getId());//将该边添加到sg1的边集中

                        //同时要将添加节点的入边也标记为已访问，在待添加边的id集中删除该边的id并添加到sg1的边集中★
                        Iterator<EdgeObjWithVisitedFlag> edge_obj_with_flag_it = added_edge_array.iterator();
                        while(edge_obj_with_flag_it.hasNext()){
                            EdgeObjWithVisitedFlag edgeObjWithVisitedFlag = edge_obj_with_flag_it.next();
                            if(edgeObjWithVisitedFlag.getEdge().getId().equals(sg2.containsEdge(parent_of_starting_point.getID(),starting_point).getId())){
                                edgeObjWithVisitedFlag.setFlag(true);
                            }
                        }
                        ids_added_edgeList_to_CG1.remove(sg2.containsEdge(parent_of_starting_point.getID(),starting_point).getId());
                        ids_edgeList_sg1.add(sg2.containsEdge(parent_of_starting_point.getID(),starting_point).getId());

                        //将添加的节点标记为已访问，并添加到sg1的点id集中，从待添加点id中删除
                        Iterator<NodeObjWithVisitedFlag> added_vertex_array_it = added_vertex_array.iterator();
                        while(added_vertex_array_it.hasNext()){
                            NodeObjWithVisitedFlag vertex_obj_with_flag = added_vertex_array_it.next();
                            if(vertex_obj_with_flag.getVertex().getID().equals(starting_point)){
                                vertex_obj_with_flag.setFlag(true);
                            }
                        }
                        ids_vertexList_in_CG1.add(starting_point);
                        ids_added_vertexList_to_CG1.remove(starting_point);
                    }

                    //addnode的另一个判断条件：待添加边的终点的子节点在sg1中，且待添加边的终点在待添加节点集中，且待添加边的起点在sg1中。(即当前的待添加边是添加节点的入边)★(原理同上)
                    else if(ids_vertexList_in_CG1.contains(starting_point) && ids_added_vertexList_to_CG1.contains(ending_point) &&
                            ids_vertexList_in_CG1.contains(child_of_ending_point.getID())){

                        ChangeOperation op = new ChangeOperation();
                        op.setChangeOperationName(ChangeOperationName.ADDNODETOCG);
                        List<Object> changeOperationArgument = new ArrayList<Object>();

                        changeOperationArgument.add(ending_point);
                        changeOperationArgument.add(starting_point);
                        changeOperationArgument.add(child_of_ending_point.getID());
                        changeOperationArgument.add(edgeLabels);

                        op.setChangeOperationArgues(changeOperationArgument);
                        changeOperationList.add(op);
                        added_edge_obj_with_flag.setFlag(true);
                        ids_added_edgeList_to_CG1.remove(e_added_into_CG1.getId());
                        ids_edgeList_sg1.add(e_added_into_CG1.getId());

                        Iterator<EdgeObjWithVisitedFlag> edge_obj_with_flag_it = added_edge_array.iterator();
                        while(edge_obj_with_flag_it.hasNext()){
                            EdgeObjWithVisitedFlag edgeObjWithVisitedFlag = edge_obj_with_flag_it.next();
                            if(edgeObjWithVisitedFlag.getEdge().getId().equals(sg2.containsEdge(ending_point,child_of_ending_point.getID()).getId())){
                                edgeObjWithVisitedFlag.setFlag(true);
                            }
                        }
                        ids_added_edgeList_to_CG1.remove(sg2.containsEdge(ending_point,child_of_ending_point.getID()).getId());
                        ids_edgeList_sg1.add(sg2.containsEdge(ending_point,child_of_ending_point.getID()).getId());

                        Iterator<NodeObjWithVisitedFlag> added_vertex_array_it = added_vertex_array.iterator();
                        while(added_vertex_array_it.hasNext()){
                            NodeObjWithVisitedFlag vertex_obj_with_flag = added_vertex_array_it.next();
                            if(vertex_obj_with_flag.getVertex().getID().equals(ending_point)){
                                vertex_obj_with_flag.setFlag(true);
                            }
                        }
                        ids_vertexList_in_CG1.add(ending_point);
                        ids_added_vertexList_to_CG1.remove(ending_point);
                    }

                    //预加节点的判断条件：待添加边的起点在添加节点集中，终点在sg1中
                    else if(ids_vertexList_in_CG1.contains(ending_point)&&ids_added_vertexList_to_CG1.contains(starting_point)){

                        ChangeOperation op = new ChangeOperation();
                        op.setChangeOperationName(ChangeOperationName.PREPENDNODETOCG);
                        List<Object> changeOperationArgument = new ArrayList<Object>();

                        changeOperationArgument.add(starting_point);//new_v
                        changeOperationArgument.add(ending_point);//v_s
                        changeOperationArgument.add(edgeLabels);//边注释

                        op.setChangeOperationArgues(changeOperationArgument);
                        changeOperationList.add(op);
                        added_edge_obj_with_flag.setFlag(true);
                        ids_added_edgeList_to_CG1.remove(e_added_into_CG1.getId());
                        ids_edgeList_sg1.add(e_added_into_CG1.getId());

                        //将添加的点标记为已访问，添加到sg1，从待添加中删除
                        Iterator<NodeObjWithVisitedFlag> added_vertex_array_it = added_vertex_array.iterator();
                        while(added_vertex_array_it.hasNext()){
                            NodeObjWithVisitedFlag vertex_obj_with_flag = added_vertex_array_it.next();
                            if(vertex_obj_with_flag.getVertex().getID().equals(starting_point)){
                                vertex_obj_with_flag.setFlag(true);
                            }
                        }
                        ids_vertexList_in_CG1.add(starting_point);
                        ids_added_vertexList_to_CG1.remove(starting_point);
                    }

                    //追加节点的判断条件：待添加边的终点在添加节点集中，起点在sg1中，原理同上
                    else if(ids_vertexList_in_CG1.contains(starting_point)&&ids_added_vertexList_to_CG1.contains(ending_point)){

                        ChangeOperation op = new ChangeOperation();
                        op.setChangeOperationName(ChangeOperationName.APPENDNODETOCG);
                        List<Object> changeOperationArgument = new ArrayList<Object>();

                        changeOperationArgument.add(sg1.ID);
                        changeOperationArgument.add(ending_point);
                        changeOperationArgument.add(starting_point);
                        changeOperationArgument.add(edgeLabels);

                        op.setChangeOperationArgues(changeOperationArgument);
                        changeOperationList.add(op);
                        added_edge_obj_with_flag.setFlag(true);
                        ids_added_edgeList_to_CG1.remove(e_added_into_CG1.getId());
                        ids_edgeList_sg1.add(e_added_into_CG1.getId());

                        //将添加的点标记为已访问，添加到sg1，从待添加中删除
                        Iterator<NodeObjWithVisitedFlag> added_vertex_array_it = added_vertex_array.iterator();
                        while(added_vertex_array_it.hasNext()){
                            NodeObjWithVisitedFlag vertex_obj_with_flag = added_vertex_array_it.next();
                            if(vertex_obj_with_flag.getVertex().getID().equals(ending_point)){
                                vertex_obj_with_flag.setFlag(true);
                            }
                        }
                        ids_vertexList_in_CG1.add(ending_point);
                        ids_added_vertexList_to_CG1.remove(ending_point);
                    }

                    //找出“插入边”的潜在更改操作，判断条件是：新添加的边的起始节点在g1中，而终止节点也在g1中
                    else if(ids_vertexList_in_CG1.contains(starting_point)&&ids_vertexList_in_CG1.contains(ending_point)){
                        ChangeOperation op = new ChangeOperation();
                        op.setChangeOperationName(ChangeOperationName.INSERTEDGETOCG);

                        List<Object> changeOperationArgument = new ArrayList<Object>();
                        changeOperationArgument.add(starting_point);  //第一个变量为起点
                        changeOperationArgument.add(ending_point);    //第二个变量为终点
                        changeOperationArgument.add(edgeLabels); //**label(s) on added edge
                        op.setChangeOperationArgues(changeOperationArgument);
                        changeOperationList.add(op);//将变更操作添加到列表中

                        added_edge_obj_with_flag.setFlag(true);//已访问过该边
                        ids_added_edgeList_to_CG1.remove(e_added_into_CG1.getId());//在”待添加边id“中删除该边的id
                        ids_edgeList_sg1.add(e_added_into_CG1.getId());//将该边的id加入sg1
                    }

                }
            }
        }

        //"删除边"的判断条件
        while(checkEdgeVisited(deleted_edge_array)){//检查是否每一条应删除的边都已被访问
            Iterator<Edge> it_del = deleted_edgeList_from_CG1.iterator();
            while(it_del.hasNext()){

                Edge e_deleted_from_G1 = it_del.next();
                String starting_point = e_deleted_from_G1.getFromVertex();
                String ending_point = e_deleted_from_G1.getToVertex();

                //找出“删除边缘”的潜在更改操作,判断条件是：待删除的边的起始节点在g1中，而终止节点也在g1中
                if(ids_vertexList_in_CG1.contains(starting_point)&&ids_vertexList_in_CG1.contains(ending_point)){

                    ChangeOperation op = new ChangeOperation();
                    op.setChangeOperationName(ChangeOperationName.DELETEEDGEFROMCG);
                    List<Object> changeOperationArgument = new ArrayList<Object>();

                    changeOperationArgument.add(starting_point);  //**v_p
                    changeOperationArgument.add(ending_point);  //**v_s
                    //修剪边的注释
                    HashSet<String> edgeLabels = new HashSet<String>();
                    Iterator iterator = e_deleted_from_G1.getLabels().iterator();
                    while(iterator.hasNext()){
                        String edgeLablesWithComma = (String)iterator.next();
                        edgeLabels = convertCommaSeparatedStringToHashset(edgeLablesWithComma);
                    }
                    changeOperationArgument.add(edgeLabels);

                    op.setChangeOperationArgues(changeOperationArgument);
                    changeOperationList.add(op);

                    it_del.remove();
                    ids_deleted_edgeList_from_CG1.remove(e_deleted_from_G1.getId());

                    Iterator<EdgeObjWithVisitedFlag> deleted_edge_array_it = deleted_edge_array.iterator();
                    while(deleted_edge_array_it.hasNext()){

                        EdgeObjWithVisitedFlag edge_obj_with_flag = deleted_edge_array_it.next();

                        if(edge_obj_with_flag.getEdge().getFromVertex().equals(starting_point) && edge_obj_with_flag.getEdge().getToVertex().equals(ending_point)){
                            edge_obj_with_flag.setFlag(true);
                        }
                    }
                }
            }
        }

        return changeOperationList;
    }

    public static boolean checkEdgeVisited(List<EdgeObjWithVisitedFlag> edge_array){
        boolean unvisited = false;
        for(EdgeObjWithVisitedFlag edgeObjWithVisitedFlag : edge_array){
            if(edgeObjWithVisitedFlag.getFlag() == false){
                unvisited = true;
                break;
            }
            else {
                continue;
            }
        }
        return unvisited;
    }

    public static boolean checkVertexVisited(List<NodeObjWithVisitedFlag> node_array){
        boolean unvisited = false;
        for(NodeObjWithVisitedFlag nodeObjWithVisitedFlag : node_array){
            if(nodeObjWithVisitedFlag.getFlag() == false){
                unvisited = true;
                break;
            }
            else {
                continue;
            }
        }
        return unvisited;
    }

    //将逗号分隔的字符串转为哈希集
    private static HashSet<String> convertCommaSeparatedStringToHashset(String commaSeperatedString){

        HashSet<String> trimedHashSet = new HashSet<String>();

        StringTokenizer st = new StringTokenizer(commaSeperatedString,",");

        while(st.hasMoreTokens()){

                trimedHashSet.add(st.nextToken());
        }



        return trimedHashSet;

    }

}