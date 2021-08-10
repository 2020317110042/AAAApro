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

public class InsertEdgePropagationFromCG2GCommand implements Command {
    private Graph mergedModel;
    private Map<String, Graph> variants;
    private Vertex v_p;
    private Vertex v_s;
    private String edge_id;
    private List<VertexPairFromG2CG> vertexPairFromG2CGs;
    private List<EdgePairFromG2CG> edgePairFromG2CGs;
    private List<String> updated_variants_id;
    private HashSet<String> labels_on_new_edge_in_CG;

    public InsertEdgePropagationFromCG2GCommand(Graph originalMergedModel, Map<String, Graph> variants, HashSet<String> labels_on_new_edge_in_CG,
                                                Vertex v_p, Vertex v_s,  String edge_id, List<VertexPairFromG2CG> vertexPairFromG2CGs,
                                                List<EdgePairFromG2CG> edgePairFromG2CGs, List<String> updated_variants_id){

        this.mergedModel=originalMergedModel;
        this.variants=variants;
        this.v_p=v_p;
        this.v_s = v_s;
        this.edge_id = edge_id;
        this.vertexPairFromG2CGs = vertexPairFromG2CGs;
        this.edgePairFromG2CGs = edgePairFromG2CGs;
        this.updated_variants_id=updated_variants_id;
        this.labels_on_new_edge_in_CG=labels_on_new_edge_in_CG;

    }
    public void execute(){

        PropagationCG2G.InsertEdgePropagationFromCGToG(mergedModel,variants,labels_on_new_edge_in_CG,v_p,v_s,edge_id,vertexPairFromG2CGs, edgePairFromG2CGs,updated_variants_id);
    }
}
