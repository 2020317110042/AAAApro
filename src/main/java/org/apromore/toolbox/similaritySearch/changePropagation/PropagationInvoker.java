package org.apromore.toolbox.similaritySearch.changePropagation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fengz2 on 16/09/2014.
 */
public class PropagationInvoker {

    private List<Command> commandList = new ArrayList<Command>();

    public void loadCommand(Command command){

        commandList.add(command);
    }

    public void invokeCommands(){

        for(Command command : commandList){

            command.execute();
        }
        commandList.clear();
    }

}
