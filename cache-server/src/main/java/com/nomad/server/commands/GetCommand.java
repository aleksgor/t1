package com.nomad.server.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.nomad.InternalTransactDataStore;
import com.nomad.core.SessionContainerImpl;
import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.ServerModel;

public class GetCommand extends CommonCommand implements Command{

    public GetCommand(final InternalTransactDataStore store, final ServerModel server) {
        super(store,server);
    }

    @Override
    public String execute(final String[] input, final String sessionId) {
        if (input.length < 4) {
            return "must be: get modelName model field1 value1 ,field2 value2 ...";
        }
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
                final Collection<? extends Model> result = store.get(Arrays.asList(id),new SessionContainerImpl(sessionId));
                if (result != null && result.size()>0) {
                    return result.iterator().next().toString();
                } else {
                    return "model: " + id + " not found";
                }
            } catch (final LogicalException e) {
                return e.toString();
            } catch (final SystemException e) {
                return e.toString();
            }
        } else {
            return " wrong parameter:" + getFullCommand(input);
        }
    }

}
