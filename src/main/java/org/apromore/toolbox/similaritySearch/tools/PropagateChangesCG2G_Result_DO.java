package org.apromore.toolbox.similaritySearch.tools;

import org.apromore.cpf.CanonicalProcessType;
import org.apromore.dao.model.ProcessModelVersion;
import org.apromore.toolbox.similaritySearch.common.EdgePairFromG2CG;
import org.apromore.toolbox.similaritySearch.common.VertexPairFromG2CG;

import java.util.*;

/**
 * Created by fzw on 4/6/2016.
 *
 * * Describes  the data object of results of change propagation from CG to G
 */
public class PropagateChangesCG2G_Result_DO {

    //here we just return the changed variant cpf.;
    //private List<CanonicalProcessType> changed_variants_cpt = new ArrayList<CanonicalProcessType>();

    /*K: the pmv id of the original pmv id of variant; V: the changed variant cpf*/
    private Map<String, CanonicalProcessType> changed_variants_cpt = new HashMap<String, CanonicalProcessType>();

    //we return the vertex mapping relationships between merged model and all the variants, no matter it evolves or not
    private List<VertexPairFromG2CG> vertexPairFromG2CGs = new ArrayList<VertexPairFromG2CG>();

    //we return the edge mapping relationships between merged model and all the variants, no matter it evolves or not
    private List<EdgePairFromG2CG> edgePairFromG2CGs = new ArrayList<EdgePairFromG2CG>();

    public void setCanonicalProcessTypes(Map<String, CanonicalProcessType> changed_variants_cpt){

        this.changed_variants_cpt = changed_variants_cpt;

    }

    public Map<String, CanonicalProcessType> getCanonicalProcessTypes(){

        return changed_variants_cpt;
    }

    public void setVertexPairFromG2CGs(List<VertexPairFromG2CG> vertexPairFromG2CGs){

        this.vertexPairFromG2CGs = vertexPairFromG2CGs;
    }

    public List<VertexPairFromG2CG> getVertexPairFromG2CGs(){

        return vertexPairFromG2CGs;
    }

    public void setEdgePairFromG2CGs(List<EdgePairFromG2CG> edgePairFromG2CGs){

        this.edgePairFromG2CGs = edgePairFromG2CGs;
    }

    public List<EdgePairFromG2CG> getEdgePairFromG2CGs(){

        return edgePairFromG2CGs;
    }
}
