package org.apromore.toolbox.similaritySearch.changePropagation.PropagationCommandFromCG2G;

import org.apromore.toolbox.similaritySearch.changePropagation.Command;
import org.apromore.toolbox.similaritySearch.changePropagation.PropagationCG2G;
import org.apromore.toolbox.similaritySearch.common.EdgePairFromG2CG;
import org.apromore.toolbox.similaritySearch.common.VertexPairFromG2CG;
import org.apromore.toolbox.similaritySearch.graph.Graph;
import org.apromore.toolbox.similaritySearch.graph.Vertex;

import java.util.List;
import java.util.Map;


public class UpdateNodeAnnotationFromCG2GCommand implements Command{

    private Graph mergedModel;
    private Map<String, Graph> variants;
    private Vertex v;
    private String new_label;
    private List<VertexPairFromG2CG> vertexPairFromG2CGs;
    private List<EdgePairFromG2CG> edgePairFromG2CGs;
    private List<String> updated_variants_id;

    public UpdateNodeAnnotationFromCG2GCommand(Graph mergedModel_ToBeChanged, Map<String, Graph> variants_ToBeChanged, Vertex v, String new_label,
                                               List<VertexPairFromG2CG> vertexPairFromG2CGs, List<EdgePairFromG2CG> edgePairFromG2CGs, List<String> updated_variants_id){

        this.mergedModel = mergedModel_ToBeChanged;
        this.variants = variants_ToBeChanged;
        this.v = v;
        this.new_label=new_label;
        this.vertexPairFromG2CGs = vertexPairFromG2CGs;
        this.edgePairFromG2CGs = edgePairFromG2CGs;
        this.updated_variants_id = updated_variants_id;
    }

    public void execute(){

        PropagationCG2G.UpdateNodeAnnotationPropagationFromCG2G(mergedModel,variants,v,new_label,vertexPairFromG2CGs,edgePairFromG2CGs,updated_variants_id);
    }
}
