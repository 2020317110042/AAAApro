package org.apromore.toolbox.similaritySearch.algorithms;

import org.apromore.toolbox.similaritySearch.common.EdgePairFromG2CG;
import org.apromore.toolbox.similaritySearch.common.VertexPairFromG2CG;
import org.apromore.toolbox.similaritySearch.graph.Graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes 1) the data object of results of change propagation from G to CG
 *
 * and 2) the data object of results of process consolidation.
 *
 * @author <a href="mailto:fzwbmw@gmail.com">Zaiwen FENG</a>
 */

public class MergedModels_Result_DO {

    private Graph graph = new Graph();

    private List<VertexPairFromG2CG> vertexPairFromG2CGs = new ArrayList<VertexPairFromG2CG>();

    private List<EdgePairFromG2CG> edgePairFromG2CGs = new ArrayList<EdgePairFromG2CG>();

    public Graph getGraph(){

        return graph;
    }

    public void setGraph (Graph graph){

        this.graph = graph;
    }

    public List<VertexPairFromG2CG> getVertexMappingGandCG(){

        return vertexPairFromG2CGs;
    }

    public void setVertexMappingGandCG(List<VertexPairFromG2CG> vertexPairFromG2CGs){

        this.vertexPairFromG2CGs = vertexPairFromG2CGs;
    }

    public List<EdgePairFromG2CG> getEdgeMappingGandCG (){

        return edgePairFromG2CGs;
    }

    public void setEdgeMappingGandCG (List<EdgePairFromG2CG> edgePairFromG2CGs){

        this.edgePairFromG2CGs = edgePairFromG2CGs;
    }
}
