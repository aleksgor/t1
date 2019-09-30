package com.nomad;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.nomad.exception.BlockException;
import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.exception.UnsupportedModelException;
import com.nomad.model.Criteria;
import com.nomad.model.DataSourceModel;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.StoreModel;
import com.nomad.model.TransactDataStore;
import com.nomad.model.core.SessionContainer;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.model.update.UpdateRequest;
import com.nomad.server.ModelStore;
import com.nomad.server.BlockService.BlockLevel;

public interface InternalTransactDataStore extends TransactDataStore {

    void registerModel(StoreModel model, DataSourceModel dataSource) throws SystemException;

    ModelStore<?> getModelStore(String modelName);

    long cleanOldData(int percent) throws SystemException;

    Map<String, Integer> getObjectsCount();

    Set<Identifier> getIdentifiers(String modelName);

    <T extends Model> StatisticResult<T> getIdentifiers(Criteria<T> criteria) throws UnsupportedModelException, SystemException;

    Collection<Identifier> block(Collection<Identifier> ids, SessionContainer sessions, BlockLevel blockLevel) throws SystemException;

    void unblock( SessionContainer sessions) throws SystemException;

    void update(Collection<UpdateRequest> updateRequests, Collection<Identifier> ids, SessionContainer sessions ) throws UnsupportedModelException, SystemException, BlockException, LogicalException;

}
