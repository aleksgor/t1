package com.nomad.server.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.nomad.InternalTransactDataStore;
import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.model.Identifier;
import com.nomad.model.ServerModel;

public class ContainsCommand extends CommonCommand implements Command{

    public ContainsCommand(final InternalTransactDataStore store, final ServerModel server) {
        super(store,server);
    }

    @Override
    public String execute(final String[] input, final String sessionId) {
        if (input.length < 4) {
            return "must be: contains modelName model field1 value1 ,field2 value2 ...";
        }
        final List<String[]> parameters = new ArrayList<>();
        for (int i = 2; i < input.length; i++) {
            final String[] parameter = new String[2];
            parameter[0] = input[i];
            i++;
            if (i < input.length) {
                parameter[1] = input[i];
            } else {
                parameter[1] = null;
            }
            parameters.add(parameter);
        }
        final Identifier id = getId(input[1], parameters);
        if (id != null) {
            try {
                final Collection<Identifier> result = store.contains(Arrays.asList(id));
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
