package org.apromore.toolbox.similaritySearch.changePropagation.PropagationCommandFromG2CG;

import org.apromore.toolbox.similaritySearch.changePropagation.Command;
import org.apromore.toolbox.similaritySearch.changePropagation.PropagationG2CG;
import org.apromore.toolbox.similaritySearch.common.EdgePairFromG2CG;
import org.apromore.toolbox.similaritySearch.common.VertexPairFromG2CG;
import org.apromore.toolbox.similaritySearch.graph.Graph;
import org.apromore.toolbox.similaritySearch.graph.Vertex;

import java.util.List;

/**
 * Created by LLY on 2021/3.
 */
public class RemoveNodePropagationFromG2CGCommand implements Command {


    private Graph mergedModel;
    private Graph variantToBeChanged;
    private Vertex v;
    private List<VertexPairFromG2CG> vertexPairFromG2CGs;
    private List<EdgePairFromG2CG> edgePairFromG2CGs;

    public RemoveNodePropagationFromG2CGCommand(Graph originalMergedModel, Graph variantToBeChanged, Vertex v, List<VertexPairFromG2CG> vertexPairFromG2CGs, List<EdgePairFromG2CG> edgePairFromG2CGs){

        this.mergedModel = originalMergedModel;
        this.variantToBeChanged = variantToBeChanged;
        this.v = v;
        this.vertexPairFromG2CGs = vertexPairFromG2CGs;
        this.edgePairFromG2CGs = edgePairFromG2CGs;

    }

    public void execute(){

        PropagationG2CG.removeNodePropagationFromG2CGCommand(mergedModel, variantToBeChanged, v, vertexPairFromG2CGs, edgePairFromG2CGs);
    }

}
