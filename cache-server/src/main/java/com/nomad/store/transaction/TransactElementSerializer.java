package com.nomad.store.transaction;

import java.io.IOException;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.serializer.Serializer;
import com.nomad.server.transaction.TransactElement.Operation;

public class TransactElementSerializer<T extends Model> implements Serializer<TransactElementImpl> {

    @Override
    public void write(final MessageOutputStream out, final TransactElementImpl data) throws IOException, SystemException {

        out.writeString(data.getSessionId());
        out.writeObject(data.getIdentifier());
        out.writeObject(data.getNewModel());
        String ob = null;
        if (data.getOperation() != null) {
            ob = data.getOperation().toString();
        }
        out.writeString(ob);

    }

    @SuppressWarnings("unchecked")
    @Override
    public TransactElementImpl read(final MessageInputStream input) throws IOException, SystemException {
        final String sessionId = input.readString();
        final Identifier id = (Identifier) input.readObject();
        final T model = (T) input.readObject();
        final String operation = input.readString();
        Operation op = null;
        if (operation != null) {
            op = Operation.valueOf(operation);
        }
        final TransactElementImpl result = new TransactElementImpl(id, op, sessionId);
        result.setNewModel(model);
        return result;
    }

}
