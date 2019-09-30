package com.nomad.store;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.model.Model;
import com.nomad.model.StoreModel;
import com.nomad.server.ModelStore;
import com.nomad.server.SaveService;
import com.nomad.server.ServerContext;
import com.nomad.utility.DataInvokerPool;

public class ZippedModelBytesStore extends AbstractModelStore< byte[]> implements ModelStore< byte[]>  {



    public ZippedModelBytesStore(final boolean readThrough, final boolean writeThrough, final DataInvokerPool dataInvoker, final SaveService saveservice,
            final ServerContext context, final StoreModel modelStore) throws SystemException  {
        super(readThrough, writeThrough, dataInvoker, saveservice,context, modelStore);
    }

    @Override
    protected Model getModelFromBytes(final byte[] input) throws SystemException {
        if (input == null) {
            return null;
        }
        try {
            @SuppressWarnings("resource")
            final MessageInputStream inputStream = new MessageInputStream(new GZIPInputStream(new ByteArrayInputStream(input)),dataDefinition);
            return (Model) inputStream.readObject();
        } catch (final IOException e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    @Override
    protected byte[] getBytesFromModel(final Model input) throws SystemException {
        if (input == null) {
            return null;
        }
        try {
            final ByteArrayOutputStream data = new ByteArrayOutputStream();

            MessageOutputStream secondOut = null;
            final GZIPOutputStream zip = new GZIPOutputStream(data);

            secondOut = new MessageOutputStream(zip,dataDefinition);
            secondOut.writeObject(input);
            zip.finish();
            secondOut.close();
            zip.close();

            return data.toByteArray();
        } catch (final IOException e) {
            throw new SystemException(e.getMessage(), e);
        }
    }
}
