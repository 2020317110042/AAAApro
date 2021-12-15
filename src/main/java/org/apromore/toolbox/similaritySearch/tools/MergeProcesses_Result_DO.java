package org.apromore.toolbox.similaritySearch.tools;

import org.apromore.cpf.CanonicalProcessType;
import org.apromore.toolbox.similaritySearch.common.EdgePairFromG2CG;
import org.apromore.toolbox.similaritySearch.common.VertexPairFromG2CG;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes 1) the data object of results of change propagation from G to CG
 *
 * and 2) the data object of results of process consolidation./*
 * *
 * Created by Zaiwen FENG on 1/07/2014.
 */
public class MergeProcesses_Result_DO {

    private CanonicalProcessType canonicalProcessType = new CanonicalProcessType();
    private List<VertexPairFromG2CG> vertexPairFromG2CGs = new ArrayList<VertexPairFromG2CG>();
    private List<EdgePairFromG2CG> edgePairFromG2CGs = new ArrayList<EdgePairFromG2CG>();

    public CanonicalProcessType getCanonicalProcessType(){

        return canonicalProcessType;
    }

    public void setCanonicalProcessType(CanonicalProcessType canonicalProcessType){

        this.canonicalProcessType = canonicalProcessType;
    }

    public List<VertexPairFromG2CG> getVertexPairFromG2CGs(){

        return vertexPairFromG2CGs;
    }

    public void setVertexPairFromG2CGs(List<VertexPairFromG2CG> vertexPairFromG2CGs){

        this.vertexPairFromG2CGs = vertexPairFromG2CGs;
    }

    public List<EdgePairFromG2CG> getEdgePairFromG2CGs(){

        return edgePairFromG2CGs;
    }

    public void setEdgePairFromG2CGs (List<EdgePairFromG2CG> edgePairFromG2CGs){

        this.edgePairFromG2CGs = edgePairFromG2CGs;
    }
}
