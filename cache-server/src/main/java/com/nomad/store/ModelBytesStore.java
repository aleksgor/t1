package com.nomad.store;

import com.nomad.exception.SystemException;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.MessageSenderReceiverImpl;
import com.nomad.model.Model;
import com.nomad.model.StoreModel;
import com.nomad.server.DataDefinitionService;
import com.nomad.server.ModelStore;
import com.nomad.server.SaveService;
import com.nomad.server.ServerContext;
import com.nomad.utility.DataInvokerPool;
import com.nomad.utility.MessageUtil;

public class ModelBytesStore extends AbstractModelStore< byte[]> implements ModelStore< byte[]> {

    private final DataDefinitionService dataDefinition;
    private final MessageSenderReceiver msr;

    public ModelBytesStore(final boolean readThrough, final boolean writeThrough, final DataInvokerPool dataInvoker, final SaveService saveService,
            final ServerContext context, final StoreModel modelStore) throws SystemException {
        super(readThrough, writeThrough, dataInvoker, saveService, context, modelStore);

        dataDefinition = context.getDataDefinitionService(null);
        msr = new MessageSenderReceiverImpl(dataDefinition);
    }

    @Override
    protected Model getModelFromBytes(final byte[] input) throws SystemException {
        return (Model) MessageUtil.getModelFromBytes(input, msr);
    }

    @Override
    protected byte[] getBytesFromModel(final Model data) throws SystemException {
        return MessageUtil.getBytesFromModel(data, msr);
    }

}
