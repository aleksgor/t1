package com.nomad.server.commands;



import com.nomad.InternalTransactDataStore;
import com.nomad.model.ServerModel;

public class GetBlockedIdCommand extends CommonCommand implements Command{

    public GetBlockedIdCommand(final InternalTransactDataStore store, final ServerModel server) {
        super(store,server);
    }

    @Override
    public String execute(final String[] input, final String sessionId) {

        //TODO
        final String result="";
        //		Map<Identifier, String> blocks=store.getBlockedId();
        //		Set<Entry<Identifier,String>> entries=blocks.entrySet();
        //		for (Entry<Identifier, String> entry : entries) {
        //			result+=" id:"+entry.getKey()+" session:"+entry.getValue();
        //		}
        return result;
    }

}
