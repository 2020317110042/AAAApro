package org.apromore.toolbox.similaritySearch.changePropagation.PropagationCommandFromCG2G;

import org.apromore.toolbox.similaritySearch.changePropagation.Command;
import org.apromore.toolbox.similaritySearch.changePropagation.PropagationCG2G;
import org.apromore.toolbox.similaritySearch.common.EdgePairFromG2CG;
import org.apromore.toolbox.similaritySearch.common.VertexPairFromG2CG;
import org.apromore.toolbox.similaritySearch.graph.Graph;
import org.apromore.toolbox.similaritySearch.graph.Vertex;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by fzw on 4/27/2016.
 */
public class AddNodePropagationFromCG2GCommand implements Command {

    private Graph mergedModel;
    private Map<String, Graph> variants;
    private Vertex new_v;
    private Vertex v_p;
    private Vertex v_s;
    private HashSet<String> labels_on_new_edge_in_CG;
    private List<VertexPairFromG2CG> vertexPairFromG2CGs;
    private List<EdgePairFromG2CG> edgePairFromG2CGs;
    private List<String> updated_variants_id;
    private String added_front_edge_id;
    private String added_rear_edge_id;

    public AddNodePropagationFromCG2GCommand(Graph mergedModel_ToBeChanged, Map<String, Graph> variants_ToBeChanged,  Vertex new_v, Vertex v_p, Vertex v_s, HashSet<String> labels_on_new_edge_in_CG,
                                                List<VertexPairFromG2CG> vertexPairFromG2CGs, List<EdgePairFromG2CG> edgePairFromG2CGs, List<String> updated_variants_id
    ,String added_front_edge_id, String added_rear_edge_id){

        this.mergedModel = mergedModel_ToBeChanged;
        this.variants = variants_ToBeChanged;
        this.new_v = new_v;
        this.v_p = v_p;
        this.v_s = v_s;
        this.labels_on_new_edge_in_CG = labels_on_new_edge_in_CG;
        this.vertexPairFromG2CGs = vertexPairFromG2CGs;
        this.edgePairFromG2CGs = edgePairFromG2CGs;
        this.updated_variants_id = updated_variants_id;
        this.added_front_edge_id = added_front_edge_id;
        this.added_rear_edge_id = added_rear_edge_id;
    }

    public void execute(){

        PropagationCG2G.AddNodePropagationFromCG2G(mergedModel, variants,new_v, v_p, v_s,  labels_on_new_edge_in_CG, vertexPairFromG2CGs, edgePairFromG2CGs,
                updated_variants_id,added_front_edge_id,added_rear_edge_id);
    }
}
