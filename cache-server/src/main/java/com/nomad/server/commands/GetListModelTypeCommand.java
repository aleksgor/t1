package com.nomad.server.commands;

import java.util.List;

import com.nomad.InternalTransactDataStore;
import com.nomad.model.ServerModel;
import com.nomad.model.StoreModel;


public class GetListModelTypeCommand extends CommonCommand implements Command {


    public GetListModelTypeCommand(final InternalTransactDataStore store, final ServerModel server) {
        super(store,server);
    }

    @Override
    public String execute(final String[] input, final String sessionId) {

        boolean first = true;
        String result = "";

        final List<StoreModel> models = server.getStoreModels();
        for (final StoreModel storeData : models) {
            if (!first) {
                result += ", ";
            }
            result += storeData.getModel();
            first = false;
        }

        return result;
    }

}
