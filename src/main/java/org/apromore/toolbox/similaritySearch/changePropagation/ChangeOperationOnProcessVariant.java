package org.apromore.toolbox.similaritySearch.changePropagation;

import org.apromore.toolbox.similaritySearch.graph.Edge;
import org.apromore.toolbox.similaritySearch.graph.Graph;
import org.apromore.toolbox.similaritySearch.graph.Vertex;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by fengz2 on 18/09/2014.
 G‘s change operation    */
public class ChangeOperationOnProcessVariant {

    //添加边
    public static void InsertEdgeToG (Graph processVariant, Vertex v_p, Vertex v_s, String label, String edge_id){


        /*set up a new edge between v_p and v_s*/
        Edge e = processVariant.connectVertices(v_p, v_s);//连接两个点
        e.setId(edge_id);//设置边的标识符

        /**add label to the edge**/
        HashSet<String> labels = new HashSet<String>();
        labels.add(label);
        //e.addLabels(labels);
        e.setLabels(labels);

    }

    //删除边
    public static void DeleteEdgeFromG (Graph processVariant, Vertex v_p, Vertex v_s){


        //*judge if the edge (v_p, v_s) is contained in the process variant*//
        Edge e = processVariant.containsEdge(v_p.getID(), v_s.getID());//判断变体中是否有这个边
        if(e != null){

            processVariant.removeEdge(v_p.getID(), v_s.getID());//删除边操作
        }

        //**remove the orphan node in the changed model, cleaning operation**//
        processVariant.removeEmptyNodes();//执行清洁操作，消除孤立点


    }

    //预加节点
    public static void PrependNodeToG (Graph processVariant, Vertex new_v, Vertex v_s, String label, String edge_id ){


        processVariant.addVertex(new_v);//将新节点添加到节点集
        InsertEdgeToG(processVariant, new_v, v_s, label, edge_id);//调用插入边操作
    }

    //追加节点
    public static void AppendNodeToG (Graph processVariant, Vertex new_v, Vertex v_p, String label, String edge_id ){


        processVariant.addVertex(new_v);
        InsertEdgeToG(processVariant, v_p, new_v, label, edge_id);


    }

    /*add a node to the 'graph'  添加节点* note that here we assume the label of the front edge and the rear edge are the same  假设前边的标签和后边相同*/
    public static void AddNodeToG(Graph processVariant, Vertex new_v, Vertex v_p, Vertex v_s, String label, String front_edge_id, String rear_edge_id){

        processVariant.addVertex(new_v);//将新节点添加到节点集
        InsertEdgeToG(processVariant,v_p,new_v,label,front_edge_id);
        InsertEdgeToG(processVariant,new_v,v_s,label,rear_edge_id);//调用两次插入边操作

    }

    //该函数为：先保存待移除边的id和label，然后移除边，之后直接调用AddNodeToG操作
    public static String InsertNodeToG (Graph processVariant, Vertex new_v, Vertex v_p, Vertex v_s, String front_edge_id, String rear_edge_id){

        //processVariant.addVertex(new_v);
        //InsertEdgeToG(processVariant, v_p, new_v, edgeId_1);
        //InsertEdgeToG(processVariant, new_v, v_s, edgeId_2);

        String removed_edge_id = new String("");
        HashSet<String> label_on_edge = new HashSet<String>();
        String commaDelimitedString = new String("");
        /*remove the edge (v_p, v_s) if it does exist*/
        Edge e = processVariant.containsEdge(v_p.getID(), v_s.getID());//得到要移除的边
        if(e != null){
            removed_edge_id = e.getId();//得到边的id
            label_on_edge = e.getLabels();//得到边的标签
            //将集合变量label转换为以逗号为分隔的字符串
            commaDelimitedString = StringUtils.collectionToCommaDelimitedString(label_on_edge);
            processVariant.removeEdge(v_p.getID(), v_s.getID());//移除边
        }

        AddNodeToG(processVariant,new_v,v_p,v_s,commaDelimitedString, front_edge_id,rear_edge_id);
        return removed_edge_id;//返回已删除边的id
    }

    /*this is not a change primitive/operation. However, it may be used in change propagation from CG to G*/
    public static void DeleteEdgeAnnotationFromG (Graph variant, String fromVertex, String toVertex){


        HashSet<String> labels = variant.containsEdge(fromVertex, toVertex).getLabels();

        Iterator<String> it = labels.iterator();

        while(it.hasNext()){

            String label = it.next();

            if(!label.equals("")){

                it.remove();
            }
        }

        labels.add(new String(""));

    }


}
