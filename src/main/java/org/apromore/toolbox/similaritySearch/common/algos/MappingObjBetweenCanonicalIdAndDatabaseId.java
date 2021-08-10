package org.apromore.toolbox.similaritySearch.common.algos;

/**
 * Created by fengz2 on 10/11/2014.
 */
public class MappingObjBetweenCanonicalIdAndDatabaseId {

    //*This class records the object representing the mapping relationship between canonical id and database id*//
    private String canonical_id;
    private String database_id;

    public String getCanonical_id(){
        return canonical_id;
    }

    public void setCanonical_id(String canonical_id){

        this.canonical_id = canonical_id;
    }

    public String getDatabase_id(){
        return database_id;
    }

    public void setDatabase_id(String database_id){

        this.database_id = database_id;
    }

}
