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
import java.util.Set;

/**
 * Created by fzw on 3/28/2016.
 */
public class AppendNodePropagationFromCG2GCommand implements Command{

    private Graph mergedModel;
    private Map<String, Graph> variants;
    private Vertex v_p;
    private Vertex new_v;
    private HashSet<String> labels_on_new_edge_in_CG;
    private List<VertexPairFromG2CG> vertexPairFromG2CGs;
    private List<EdgePairFromG2CG> edgePairFromG2CGs;
    private List<String> updated_variants_id;
    private String appended_edge_id;

    public AppendNodePropagationFromCG2GCommand(Graph mergedModel_ToBeChanged, Map<String, Graph> variants_ToBeChanged, Vertex v_p, Vertex new_v, HashSet<String> labels_on_new_edge_in_CG,
                                                List<VertexPairFromG2CG> vertexPairFromG2CGs, List<EdgePairFromG2CG> edgePairFromG2CGs,
                                                List<String> updated_variants_id, String appended_edge_id){

        this.mergedModel = mergedModel_ToBeChanged;
        this.variants = variants_ToBeChanged;
        this.v_p = v_p;
        this.new_v = new_v;
        this.labels_on_new_edge_in_CG = labels_on_new_edge_in_CG;
        this.vertexPairFromG2CGs = vertexPairFromG2CGs;
        this.edgePairFromG2CGs = edgePairFromG2CGs;
        this.updated_variants_id = updated_variants_id;
        this.appended_edge_id = appended_edge_id;
    }

    public void execute(){

        PropagationCG2G.AppendNodePropagationFromCG2G(mergedModel,variants,v_p,new_v,labels_on_new_edge_in_CG,vertexPairFromG2CGs,edgePairFromG2CGs,
                updated_variants_id,appended_edge_id);
    }


}
