package com.nomad.cache.commonclientserver;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.model.Criteria;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.update.UpdateRequest;
import com.nomad.serializer.Serializer;

public class RequestImplSerializer implements Serializer<RequestImpl> {

    @Override
    public void write(final MessageOutputStream out, final RequestImpl data) throws IOException, SystemException {
        out.writeObject(data.getCriteria());
        out.writeList(data.getIdentifiers());
        out.writeList(data.getModels());
        out.writeList(data.getUpdateRequest());
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public RequestImpl read(final MessageInputStream input) throws IOException, SystemException {
        final RequestImpl result = new RequestImpl();
        result.setCriteria((Criteria<? extends Model>) input.readObject());
        result.setIdentifiers((List<Identifier>) input.readList());
        result.setModels((List< Model>) input.readList());
        result.setUpdateRequest((Collection<UpdateRequest>) input.readList());
        return result;
    }

}
