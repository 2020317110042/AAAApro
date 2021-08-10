package org.apromore.toolbox.similaritySearch.algorithms;

/**
 * Created by fzw on 3/23/2016.
 *
 /**
 * Describes the data object of results of change propagation from CG to G.
 *
 * @author <a href="mailto:fzwbmw@gmail.com">Zaiwen FENG</a>
 */

import org.apromore.toolbox.similaritySearch.common.EdgePairFromG2CG;
import org.apromore.toolbox.similaritySearch.common.VertexPairFromG2CG;
import org.apromore.toolbox.similaritySearch.graph.Graph;

import java.util.*;


public class Variant_Result_DO {

    /*describe all the variant graphs of the merged model*/
    //private List<Graph> graphs = new ArrayList<Graph>();

    /*K: the original pmv id of variant graph, V: change variant graph*/
    private Map<String, Graph> graphs = new HashMap<String, Graph>();

    /*describe all the vertex mappings between the variant graphs and the merged model*/
    private List<VertexPairFromG2CG> vertexPairFromG2CGs = new ArrayList<VertexPairFromG2CG>();

    /*describe all the edge mappings between the variant graphs and the merged model*/
    private List<EdgePairFromG2CG> edgePairFromG2CGs = new ArrayList<EdgePairFromG2CG>();

    public Map<String, Graph> getGraph(){

        return graphs;
    }

    public void setGraph(Map< String, Graph> graphs){

        this.graphs = graphs;
    }

    public List<VertexPairFromG2CG> getVertexPairFromCG2Gs() {

        return vertexPairFromG2CGs;
    }

    public void setVertexPairFromCG2Gs(List<VertexPairFromG2CG> vertexPairFromG2CGs){

        this.vertexPairFromG2CGs = vertexPairFromG2CGs;
    }

    public List<EdgePairFromG2CG> getEdgePairFromG2CGs(){

        return edgePairFromG2CGs;
    }

    public void setEdgePairFromG2CGs(List<EdgePairFromG2CG> edgePairFromG2CGs){

        this.edgePairFromG2CGs = edgePairFromG2CGs;
    }


}
