package com.nomad.server.commands;

import java.util.HashMap;
import java.util.Map;

import com.nomad.InternalTransactDataStore;
import com.nomad.model.ServerModel;

public class ListCommand extends CommonCommand implements Command{

    public ListCommand(final InternalTransactDataStore store, final ServerModel server) {
        super(store,server);
    }

    @Override
    public String execute(final String[] input, final String sessionId) {
        if (input.length < 2) {
            return "must be: list modelName ";
        }
        String result="";
        store.getModelStore(input[1]);
        final Map<? extends Object, ? extends Object> map = new HashMap<>(); // TODO
        // ms.getStore();
        if(map!=null){
            for (final Object model : map.values()) {
                result+=model.toString()+"\n";
            }
        }
        return result;
    }

}
