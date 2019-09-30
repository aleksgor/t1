package com.nomad;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.nomad.exception.SystemException;
import com.nomad.exception.UnsupportedModelException;
import com.nomad.model.Criteria;
import com.nomad.model.DataStore;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.StoreModel;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.server.ModelStore;
import com.nomad.server.SaveService;
import com.nomad.server.ServerContext;
import com.nomad.utility.DataInvokerPool;

public interface InternalDataStore extends DataStore {

    void registerModel(StoreModel model, DataInvokerPool dataInvoker, SaveService saveService, ServerContext context) throws SystemException;

    ModelStore<?> getModelStore(String modelName);

    void cleanSession(int timeout);

    boolean checkSession(String sessionId);

    int getDataCount();

    void clear();

    Map<String, Integer> getObjectsCount();

    Set<Identifier> getIdentifiers(String modelName);

    <T extends Model> Collection<T> getList(Criteria<T> criteria) throws UnsupportedModelException, SystemException;

    <T extends Model>  StatisticResult<T> getIdentifiers(Criteria<T> criteria) throws UnsupportedModelException, SystemException;

    int removeOutstandingModels(int removeCount);

}
