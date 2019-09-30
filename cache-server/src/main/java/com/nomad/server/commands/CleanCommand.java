package com.nomad.server.commands;

import com.nomad.InternalTransactDataStore;
import com.nomad.model.ServerModel;
import com.nomad.server.ModelStore;

public class CleanCommand extends CommonCommand implements Command{

    public CleanCommand(final InternalTransactDataStore store, final ServerModel server) {
        super(store,server);
    }

    @Override
    public String execute(final String[] input, final String sessionId) {
        if (input.length < 2) {
            return "must be: clean modelName ";
        }
        String result="";
        final ModelStore<?> ms = store.getModelStore(input[1]);
        if (ms != null) {
            ms.clean();
            ;
        }else{
            result= "uncnown model: input[1]";
        }
        return result;
    }

}
