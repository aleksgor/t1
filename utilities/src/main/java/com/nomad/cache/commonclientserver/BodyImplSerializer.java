package com.nomad.cache.commonclientserver;

import java.io.IOException;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.message.Request;
import com.nomad.model.Model;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.serializer.Serializer;

public class BodyImplSerializer implements Serializer<BodyImpl> {

    @Override
    public void write(final MessageOutputStream out, final BodyImpl data) throws IOException,SystemException {
        out.writeObject(data.getRequest());
        out.writeObject(data.getResponse());
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public BodyImpl read(final MessageInputStream input) throws IOException,SystemException {
        final BodyImpl result = new BodyImpl();
        result.setRequest((Request) input.readObject());
        result.setResponse((StatisticResult<? extends Model>) input.readObject());
        return result;
    }

}
