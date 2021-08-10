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
public class PrependNodePropagationFromG2CGCommand implements Command {


    private Graph mergedModel;
    private Graph variantToBeChanged;
    private Vertex new_v;
    private Vertex v_s;
    private String label;
    private String edge_id;
    private List<VertexPairFromG2CG> vertexPairFromG2CGs;
    private List<EdgePairFromG2CG> edgePairFromG2CGs;

    public PrependNodePropagationFromG2CGCommand(Graph originalMergedModel, Graph variantToBeChanged,
                                                String label, String edge_id, Vertex new_v, Vertex v_s, List<VertexPairFromG2CG> vertexPairFromG2CGs,
    List<EdgePairFromG2CG> edgePairFromG2CGs){

        this.mergedModel = originalMergedModel;
        this.variantToBeChanged = variantToBeChanged;
        this.v_s = v_s;
        this.new_v = new_v;
        this.label = label;
        this.edge_id = edge_id;
        this.vertexPairFromG2CGs = vertexPairFromG2CGs;
        this.edgePairFromG2CGs = edgePairFromG2CGs;

    }

    public void execute(){

        PropagationG2CG.PrependNodePropagation2CG(mergedModel, variantToBeChanged, new_v, v_s, label, edge_id, vertexPairFromG2CGs, edgePairFromG2CGs);
    }

}
