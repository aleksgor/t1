package com.nomad.server.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nomad.InternalTransactDataStore;
import com.nomad.core.SessionContainerImpl;
import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.model.Identifier;
import com.nomad.model.ServerModel;

public class RemoveModelFromCacheCommand extends CommonCommand implements Command{

    public RemoveModelFromCacheCommand(final InternalTransactDataStore store, final ServerModel server) {
        super(store,server);
    }

    @Override
    public String execute(final String[] input, final String sessionId) {
        if (input.length < 4) {
            return "must be: get modelName model field1 value1 ,field2 value2 ...";
        }
        String result="";
        final List<String[]> params = new ArrayList<>();
        for (int i = 2; i < input.length; i++) {
            final String[] param = new String[2];
            param[0] = input[i];
            i++;
            if (i < input.length) {
                param[1] = input[i];
            } else {
                param[1] = null;
            }
            params.add(param);
        }
        final Identifier id = getId(input[1], params);
        if (id != null) {

            try {
                store.remove(Collections.singletonList(id), new SessionContainerImpl(sessionId));
                result = "model with id:" + id + " model has been successfully removed";
            } catch (final LogicalException e) {
                result = e.toString();
            } catch (final SystemException e) {
                result = " system sxception :" + e.getMessage();
            }

        } else {
            return " wrong parameter:" + getFullCommand(input);
        }
        return result;
    }

}
