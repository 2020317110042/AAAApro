package org.apromore.toolbox.similaritySearch.changePropagation.PropagationCommandFromG2CG;

import org.apromore.toolbox.similaritySearch.changePropagation.Command;
import org.apromore.toolbox.similaritySearch.changePropagation.PropagationG2CG;
import org.apromore.toolbox.similaritySearch.common.EdgePairFromG2CG;
import org.apromore.toolbox.similaritySearch.common.VertexPairFromG2CG;
import org.apromore.toolbox.similaritySearch.graph.Graph;
import org.apromore.toolbox.similaritySearch.graph.Vertex;

import java.util.List;

/**
 * Created by Home on 8/26/2016.
 */
public class DeleteEdgePropagationFromG2CGCommand implements Command {

    private Graph originalMergedModel;
    private Graph variantToBeChanged;
    //private String label;
    //private String edge_id;
    private Vertex v_p;
    private Vertex v_s;
    private List<VertexPairFromG2CG> vertexPairFromG2CGs;
    private List<EdgePairFromG2CG> edgePairFromG2CGs;

    public DeleteEdgePropagationFromG2CGCommand(Graph originalMergedModel, Graph variantToBeChanged,
                                                 Vertex v_p, Vertex v_s, List<VertexPairFromG2CG> vertexPairFromG2CGs,
                                                List<EdgePairFromG2CG> edgePairFromG2CGs){

        this.originalMergedModel = originalMergedModel;
        this.variantToBeChanged = variantToBeChanged;
        //this.label = label;
        //this.edge_id = edge_id;
        this.v_p = v_p;
        this.v_s = v_s;
        this.vertexPairFromG2CGs = vertexPairFromG2CGs;
        this.edgePairFromG2CGs = edgePairFromG2CGs;



    }
     public void execute(){

         PropagationG2CG.DeleteEdgePropagation2CG(originalMergedModel,variantToBeChanged,v_p,v_s,vertexPairFromG2CGs, edgePairFromG2CGs);
     }

}
