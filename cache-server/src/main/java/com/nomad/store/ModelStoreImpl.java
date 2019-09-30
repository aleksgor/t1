package com.nomad.store;

import com.nomad.exception.SystemException;
import com.nomad.model.Model;
import com.nomad.model.StoreModel;
import com.nomad.server.ModelStore;
import com.nomad.server.SaveService;
import com.nomad.server.ServerContext;
import com.nomad.utility.DataInvokerPool;

public class ModelStoreImpl extends AbstractModelStore<Model> implements ModelStore<Model> {

    public ModelStoreImpl(final boolean readThrough, final boolean writeThrough, final DataInvokerPool dataInvoker, final SaveService saveService,
            final ServerContext context, final StoreModel modelStore) throws SystemException {
        super(readThrough, writeThrough, dataInvoker, saveService, context, modelStore);
    }

    @Override
    protected Model getModelFromBytes(final Model input) throws SystemException {
        return (Model) input;
    }

    @Override
    protected Model getBytesFromModel(final Model input) throws SystemException {
        return (Model) input;
    }

}
