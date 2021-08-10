package org.apromore.toolbox.similaritySearch.changePropagation.PropagationCommandFromCG2G;

import org.apromore.toolbox.similaritySearch.changePropagation.*;
import org.apromore.toolbox.similaritySearch.common.EdgePairFromG2CG;
import org.apromore.toolbox.similaritySearch.common.VertexPairFromG2CG;
import org.apromore.toolbox.similaritySearch.graph.Edge;
import org.apromore.toolbox.similaritySearch.graph.Graph;
import org.apromore.toolbox.similaritySearch.graph.Vertex;

import java.util.*;

/**
 * Created by fzw on 5/9/2016.
 */
public class PrependNodePropagationFromCG2GCommand implements Command{

    private Graph mergedModel;
    private Map<String, Graph> variants;
    private Vertex new_v;
    //private Vertex v_p;
    private Vertex v_s;
    private HashSet<String> labels_on_new_edge_in_CG;
    private List<VertexPairFromG2CG> vertexPairFromG2CGs;
    private List<EdgePairFromG2CG> edgePairFromG2CGs;
    private List<String> updated_variants_id;
    private String prepended_edge_id;
    //private String added_rear_edge_id;

    public PrependNodePropagationFromCG2GCommand(Graph mergedModel_ToBeChanged, Map<String, Graph> variants_ToBeChanged,
                                                 Vertex new_v, Vertex v_s, HashSet<String> labels_on_new_edge_in_CG,
                                                 List<VertexPairFromG2CG> vertexPairFromG2CGs, List<EdgePairFromG2CG> edgePairFromG2CGs,
                                                 List<String> updated_variants_id ,String prepended_edge_id){

        this.mergedModel = mergedModel_ToBeChanged;
        this.variants = variants_ToBeChanged;
        this.new_v = new_v;
        //this.v_p = v_p;
        this.v_s = v_s;
        this.labels_on_new_edge_in_CG = labels_on_new_edge_in_CG;
        this.vertexPairFromG2CGs = vertexPairFromG2CGs;
        this.edgePairFromG2CGs = edgePairFromG2CGs;
        this.updated_variants_id = updated_variants_id;
        this.prepended_edge_id = prepended_edge_id;
        //this.added_rear_edge_id = added_rear_edge_id;
    }

    public void execute(){
        PropagationCG2G.PrependNodePropagationFromCG2G(mergedModel, variants, new_v, v_s, labels_on_new_edge_in_CG, vertexPairFromG2CGs, edgePairFromG2CGs, updated_variants_id, prepended_edge_id);
    }
}
