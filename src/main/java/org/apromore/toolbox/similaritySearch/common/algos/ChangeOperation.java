package org.apromore.toolbox.similaritySearch.common.algos;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fengz2 on 12/09/2014.
 */
public class ChangeOperation {


    private ChangeOperationName changeOperationName;
    private List<Object> changeOperationArgues = new ArrayList<Object>();

    public void setChangeOperationName (ChangeOperationName changeOperationName){
        this.changeOperationName = changeOperationName;
    }

    public ChangeOperationName getChangeOperationName(){
        return changeOperationName;
    }

    public void setChangeOperationArgues(List<Object> changeOperationArgues){
        this.changeOperationArgues = changeOperationArgues;
    }

    public List<Object> getChangeOperationArgues(){
        return changeOperationArgues;
    }
}
