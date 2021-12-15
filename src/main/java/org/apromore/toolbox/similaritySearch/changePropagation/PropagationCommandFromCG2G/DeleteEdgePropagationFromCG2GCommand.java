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


public class DeleteEdgePropagationFromCG2GCommand implements Command {

    private Graph mergedModel;
    private Map<String, Graph> variants;
    private Vertex v_p;
    private Vertex v_s;
    private HashSet<String> removed_labels_on_existing_edge_in_CG;
    private List<VertexPairFromG2CG> vertexPairFromG2CGs;
    private List<EdgePairFromG2CG> edgePairFromG2CGs;
    private List<String> updated_variants_id;

    public DeleteEdgePropagationFromCG2GCommand(Graph mergedModel, Map<String, Graph> variants, Vertex v_p, Vertex v_s,
                                                HashSet<String> removed_labels_on_existing_edge_in_CG,
                                                List<VertexPairFromG2CG> vertexPairFromG2CGs, List<EdgePairFromG2CG> edgePairFromG2CGs,
                                                List<String> updated_variants_id){

        this.mergedModel= mergedModel;
        this.variants = variants;
        this.v_p = v_p;
        this.v_s = v_s;
        this.removed_labels_on_existing_edge_in_CG = removed_labels_on_existing_edge_in_CG;
        this.vertexPairFromG2CGs = vertexPairFromG2CGs;
        this.edgePairFromG2CGs = edgePairFromG2CGs;
        this.updated_variants_id = updated_variants_id;



    }
    public void execute(){

        PropagationCG2G.DeleteEdgePropagationFromCGToG(mergedModel,variants,v_p,v_s,removed_labels_on_existing_edge_in_CG,
                vertexPairFromG2CGs,edgePairFromG2CGs,updated_variants_id);
    }
}
