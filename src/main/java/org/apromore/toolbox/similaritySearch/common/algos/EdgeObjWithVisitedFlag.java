package org.apromore.toolbox.similaritySearch.common.algos;

import org.apromore.toolbox.similaritySearch.graph.Edge;

/**
 * Created by fengz2 on 7/12/2014.
 */
public class EdgeObjWithVisitedFlag {

    //此类用于记录是否已访问添加的边或删除的边，在'findChangeOperationSet'类中使用了该类
    private boolean flag = false; //1. false为未访问，true为已访问
    private Edge edge;

    public boolean getFlag(){
        return flag;
    }

    public void setFlag(boolean flag){
        this.flag = flag;
    }

    public Edge getEdge(){
        return edge;
    }

    public void setEdge(Edge edge){
        this.edge = edge;
    }

}
