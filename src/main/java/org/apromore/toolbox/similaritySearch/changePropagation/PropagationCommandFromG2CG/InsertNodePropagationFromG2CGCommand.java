package org.apromore.toolbox.similaritySearch.changePropagation.PropagationCommandFromG2CG;

import org.apromore.toolbox.similaritySearch.changePropagation.Command;
import org.apromore.toolbox.similaritySearch.changePropagation.PropagationG2CG;
import org.apromore.toolbox.similaritySearch.common.EdgePairFromG2CG;
import org.apromore.toolbox.similaritySearch.common.VertexPairFromG2CG;
import org.apromore.toolbox.similaritySearch.graph.Graph;
import org.apromore.toolbox.similaritySearch.graph.Vertex;

import java.util.List;
/**
 * @author TengHuiWang
 *
 */
public class InsertNodePropagationFromG2CGCommand implements Command {


    private Graph mergedModel;
    private Graph variantToBeChanged;
    private Vertex v_p;
    private Vertex v_s;
    private Vertex new_v;
    private String front_edge_id;
    private String rear_edge_id;
    private List<VertexPairFromG2CG> vertexPairFromG2CGs;
    private List<EdgePairFromG2CG> edgePairFromG2CGs;

    public InsertNodePropagationFromG2CGCommand(Graph originalMergedModel, Graph variantToBeChanged,
                                                String front_edge_id, String rear_edge_id, Vertex v_p, Vertex v_s, Vertex new_v, List<VertexPairFromG2CG> vertexPairFromG2CGs,
                                                List<EdgePairFromG2CG> edgePairFromG2CGs){
        this.mergedModel = originalMergedModel;
        this.variantToBeChanged = variantToBeChanged;
        this.v_p = v_p;
        this.v_s=v_s;
        this.new_v = new_v;
        this.front_edge_id = front_edge_id;
        this.rear_edge_id = rear_edge_id;
        this.vertexPairFromG2CGs = vertexPairFromG2CGs;
        this.edgePairFromG2CGs = edgePairFromG2CGs;
    }
    public void execute(){
        PropagationG2CG.InsertNodePropagation2CG(mergedModel, variantToBeChanged, v_p, v_s, new_v, front_edge_id, rear_edge_id, vertexPairFromG2CGs, edgePairFromG2CGs);
    }

}
