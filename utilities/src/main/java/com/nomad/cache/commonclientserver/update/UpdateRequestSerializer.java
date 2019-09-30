package com.nomad.cache.commonclientserver.update;

import java.io.IOException;
import java.util.Collection;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.model.update.UpdateItem;
import com.nomad.model.update.UpdateRequestImpl;
import com.nomad.serializer.Serializer;

public class UpdateRequestSerializer implements Serializer<UpdateRequestImpl> {

    @Override
    public void write(final MessageOutputStream out, final UpdateRequestImpl data) throws IOException, SystemException {
        out.writeString(data.getModelName());
        out.writeList(data.getUpdateItems());
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public UpdateRequestImpl read(final MessageInputStream input) throws IOException, SystemException {
        final UpdateRequestImpl result = new UpdateRequestImpl();
        result.setModelName(input.readString());
        result.getUpdateItems().addAll((Collection<UpdateItem>) input.readList());
        return result;
    }

}
