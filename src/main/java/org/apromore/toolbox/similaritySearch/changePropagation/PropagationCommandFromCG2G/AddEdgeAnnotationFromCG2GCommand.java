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

public class AddEdgeAnnotationFromCG2GCommand implements Command {

    private Graph mergedModel_ToBeChanged;
    private Map<String, Graph> variants_ToBeChanged;
    private Vertex v_p;
    private Vertex v_s;
    private HashSet<String> add_labels_on_existing_edge_in_CG;
    private List<VertexPairFromG2CG> vertexPairFromG2CGs;
    private List<EdgePairFromG2CG> edgePairFromG2CGs;
    private List<String> updated_variants_id;

    public AddEdgeAnnotationFromCG2GCommand(Graph mergedModel_ToBeChanged, Map<String, Graph> variants_ToBeChanged,  Vertex v_p, Vertex v_s,
                                               HashSet<String> add_labels_on_existing_edge_in_CG,
                                               List<VertexPairFromG2CG> vertexPairFromG2CGs, List<EdgePairFromG2CG> edgePairFromG2CGs,
                                               List<String> updated_variants_id){

        this.mergedModel_ToBeChanged = mergedModel_ToBeChanged;
        this.variants_ToBeChanged = variants_ToBeChanged;
        this.v_p = v_p;
        this.v_s = v_s;
        this.add_labels_on_existing_edge_in_CG = add_labels_on_existing_edge_in_CG;
        this.vertexPairFromG2CGs = vertexPairFromG2CGs;
        this.edgePairFromG2CGs = edgePairFromG2CGs;
        this.updated_variants_id = updated_variants_id;
    }

    public void execute(){

        PropagationCG2G.AddEdgeAnnotationPropagationFromCGToG(mergedModel_ToBeChanged,variants_ToBeChanged,v_p,v_s,add_labels_on_existing_edge_in_CG,
                vertexPairFromG2CGs,edgePairFromG2CGs,updated_variants_id);
    }

}
