package com.nomad.configuration;

import java.util.List;

import com.nomad.InternalDataStore;
import com.nomad.InternalTransactDataStore;
import com.nomad.exception.SystemException;
import com.nomad.model.DataSourceModel;
import com.nomad.model.StoreModel;
import com.nomad.server.ServerContext;
import com.nomad.server.ServerContext.ServiceName;
import com.nomad.server.StoreModelService;
import com.nomad.server.service.storemodelservice.StoreModelServiceImpl;
import com.nomad.store.SimpleStore;
import com.nomad.store.Store;
import com.nomad.store.TransactStore;

public class StoreFactory {

    public static String dataInvokerPoolsName = "dataInvokerPoolsName";

    public enum StoreType {
        SIMPLE, TRANSACT, BLOCKED
    }

    private InternalDataStore store = null;
    private volatile ServerContext context;

    private InternalDataStore getDataStore() {
        if (store == null) {
            store = new Store(context);
        }
        return store;
    }

    public StoreFactory(final ServerContext context) {
        this.context = context;
        store = new Store(context);

    }

    public InternalTransactDataStore getStore(final StoreType storeType) throws SystemException  {
        switch (storeType) {
        case SIMPLE:
            return getSimpleStore();
        case TRANSACT:
            return getTransactStore();

        default:
            return null;

        }
    }

    private InternalTransactDataStore getSimpleStore() throws SystemException  {
        final InternalTransactDataStore store = new SimpleStore(getDataStore(), context);

        registerInStore(store);
        return store;

    }

    private void registerInStore(final InternalTransactDataStore store) throws SystemException  {
        final StoreModelService server = (StoreModelServiceImpl) context.get(ServiceName.STORE_MODEL_SERVICE);
        final List<StoreModel> models = server.getServerModel().getStoreModels();
        for (final StoreModel storeModel : models) {
            if (storeModel.isCache()) {
                if (storeModel.getDataSource() != null) {
                    final DataSourceModel dataSourceModel = server.getServerModel().getDataSources(storeModel.getDataSource());
                    store.registerModel(storeModel, dataSourceModel);
                }
            }
        }
    }

    private InternalTransactDataStore getTransactStore() throws SystemException  {

        final InternalTransactDataStore store = new TransactStore(getDataStore(), context);
        registerInStore(store);
        return store;

    }

}
