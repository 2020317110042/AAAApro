package org.apromore.service.model;

import org.apromore.cpf.CanonicalProcessType;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Used as an input  for 'MergeProcesses' to conduct merging models.
 *
 *  @author <a href="mailto:fzwbmw@gmail.com">Zaiwen FENG</a>
 */
public class MergeData {

    private Integer model_id = new Integer(0);
    private Map<String, String> node_id_mapping = new HashMap<String, String>(); //map canonical id of database prepended 'n'
    private Map<String, String> edge_id_mapping = new HashMap<String, String>(); ////map canonical id to edge id of database prepended 'e'
    private CanonicalProcessType canonicalProcess;

    public Integer getModel_id(){
        return model_id;
    }

    public void setModel_id(Integer model_id){

        this.model_id = model_id;
    }

    public Map<String, String> getNode_id_mapping(){

        return node_id_mapping;
    }

    public void setNode_id_mapping(Map<String, String> node_id_mapping){

        this.node_id_mapping = node_id_mapping;
    }

    public Map<String, String> getEdge_id_mapping(){

        return edge_id_mapping;
    }

    public void setEdge_id_mapping(Map<String, String> edge_id_mapping){

        this.edge_id_mapping = edge_id_mapping;
    }

    public CanonicalProcessType getCanonicalProcessType(){

        return canonicalProcess;
    }

    public void setCanonicalProcessType (CanonicalProcessType canonicalProcess){

        this.canonicalProcess = canonicalProcess;
    }


}
