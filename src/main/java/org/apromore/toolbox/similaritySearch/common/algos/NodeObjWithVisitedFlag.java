package org.apromore.toolbox.similaritySearch.common.algos;

import org.apromore.toolbox.similaritySearch.graph.Vertex;

/**
 * Created by fzw on 4/27/2016.
 */
public class NodeObjWithVisitedFlag {

    //this class help to record if an added node or a deleted node has been visited or not, it's used in 'findChangeOperationSet' class
    private boolean flag = false; //1. false means the edge has not been visited. 2. true means it has been visited
    private Vertex vertex;

    public void setFlag(boolean flag){

        this.flag = flag;
    }

    public boolean getFlag(){

        return flag;
    }

    public void setVertex(Vertex vertex){

        this.vertex = vertex;
    }

    public Vertex getVertex(){

        return vertex;
    }

}
