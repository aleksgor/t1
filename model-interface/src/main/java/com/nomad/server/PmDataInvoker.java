package com.nomad.server;

import com.nomad.model.DataInvoker;
import com.nomad.utility.PooledObject;

public interface PmDataInvoker  extends DataInvoker {
    DataDefinitionService getDataDefinitionService();

    void setConnectPool(ConnectionPool<? extends PooledObject> connectPool);

    void setDataDefinitionService(DataDefinitionService dataDefinition);

}
