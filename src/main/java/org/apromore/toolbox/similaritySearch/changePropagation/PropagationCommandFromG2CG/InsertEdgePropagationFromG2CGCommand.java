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
public class InsertEdgePropagationFromG2CGCommand implements Command {

    private Graph originalMergedModel;
    private Graph variantToBeChanged;
    private String label;
    private String edge_id;
    private Vertex v_p;
    private Vertex v_s;
    private List<VertexPairFromG2CG> vertexPairFromG2CGs;
    private List<EdgePairFromG2CG> edgePairFromG2CGs;

    public InsertEdgePropagationFromG2CGCommand(Graph originalMergedModel, Graph variantToBeChanged,
                                                 Vertex v_p, Vertex v_s, String label, String edge_id, List<VertexPairFromG2CG> vertexPairFromG2CGs,
                                                List<EdgePairFromG2CG> edgePairFromG2CGs){

        this.originalMergedModel = originalMergedModel;
        this.variantToBeChanged = variantToBeChanged;
        this.label = label;
        this.edge_id = edge_id;
        this.v_p = v_p;
        this.v_s = v_s;
        this.vertexPairFromG2CGs = vertexPairFromG2CGs;
        this.edgePairFromG2CGs = edgePairFromG2CGs;



    }
     public void execute(){

         PropagationG2CG.InsertEdgePropagation2CG(originalMergedModel,variantToBeChanged,v_p,v_s,label,edge_id,vertexPairFromG2CGs, edgePairFromG2CGs);
     }

}
