package com.nomad.store;


import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.nomad.InternalDataStore;
import com.nomad.InternalTransactDataStore;
import com.nomad.exception.BlockException;
import com.nomad.exception.ErrorCodes;
import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.exception.UnsupportedModelException;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.MessageSenderReceiverImpl;
import com.nomad.model.Criteria;
import com.nomad.model.DataSourceModel;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.StoreModel;
import com.nomad.model.core.SessionContainer;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.model.update.UpdateItem;
import com.nomad.model.update.UpdateRequest;
import com.nomad.server.DataDefinitionService;
import com.nomad.server.ModelStore;
import com.nomad.server.SaveService;
import com.nomad.server.ServerContext;
import com.nomad.server.BlockService.BlockLevel;
import com.nomad.server.ServerContext.ServiceName;
import com.nomad.utility.DataInvokerPool;
import com.nomad.utility.MessageUtil;

public class SimpleStore implements InternalTransactDataStore{

    private volatile InternalDataStore store;
    private volatile ServerContext context;
    private final DataDefinitionService dataDefinition;

    public SimpleStore(final InternalDataStore store, final ServerContext context) throws SystemException {
        this.store = store;
        this.context = context;
        this.dataDefinition = context.getDataDefinitionService(null);

    }

    @Override
    public Collection<Model> get(final Collection< Identifier> ids, final SessionContainer sessions) throws UnsupportedModelException, SystemException {
        return store.get(ids);
    }

    @Override
    public Collection<Model> put(final Collection<Model> data, final SessionContainer sessions) throws UnsupportedModelException, SystemException {
        return store.put(data,sessions.getSessionId());
    }

    @Override
    public Collection<Identifier> remove(final Collection<Identifier> ids, final SessionContainer sessions) throws UnsupportedModelException, SystemException {
        store.remove(ids,sessions.getSessionId());
        return ids;
    }



    @Override
    public ModelStore<?> getModelStore(final String modelName) {
        return store.getModelStore(modelName);
    }

    @Override
    public long cleanOldData(final int percent) throws SystemException {
        return 0;
    }

    @Override
    public Map<String, Integer> getObjectsCount() {
        return new HashMap<>();
    }

    @Override
    public Set<Identifier> getIdentifiers(final String modelName) {

        return store.getIdentifiers(modelName);
    }



    @Override
    public void registerModel(final StoreModel model, final DataSourceModel dataSource) throws SystemException {
        final DataInvokerPool dataInvoker = (DataInvokerPool) context.getDataInvoker(model.getDataSource());
        if (dataInvoker != null) {
            dataInvoker.incrementPoolSize(dataSource.getThreads());
        }
        final SaveService saveService = (SaveService) context.get(ServiceName.SAVE_SERVICE);

        store.registerModel(model, dataInvoker, saveService, context);
    }


    @Override
    public <T extends Model> StatisticResult<T> getIdentifiers(final Criteria<T> criteria) throws UnsupportedModelException, SystemException {
        return store.getIdentifiers(criteria);
    }


    @Override
    public void start() throws SystemException {

    }

    @Override
    public void stop() {

    }

    @Override
  public void commit(final SessionContainer sessions) throws SystemException {

    }

    @Override
    public void rollback(SessionContainer sessions) {

    }

    @Override
    public void closeSession(SessionContainer sessions) throws SystemException {


    }

    @Override
    public Collection<Identifier> contains(final Collection<Identifier> id) throws UnsupportedModelException, SystemException {

        return null;
    }

    @Override
    public Collection<Model> getFromCache(final Collection<Identifier> id,  SessionContainer sessions)  throws UnsupportedModelException, SystemException {

        return null;
    }

    @Override
    public void commitPhase2(SessionContainer sessions) throws SystemException {


    }

    @Override
    public Collection<Identifier> block(final Collection<Identifier> ids, SessionContainer sessions, BlockLevel blockLevel) throws SystemException {

        return null;
    }

    @Override
    public void unblock(SessionContainer sessions) throws SystemException {


    }

    @Override
    public void update(Collection<UpdateRequest> updateRequests, Collection<Identifier> ids, SessionContainer sessions) throws UnsupportedModelException, SystemException, BlockException, LogicalException {
        MessageSenderReceiver msr = new MessageSenderReceiverImpl(dataDefinition);
        Collection<Model> models = get(ids, sessions);
            for (Model t : models) {
                for (UpdateRequest updateRequest :  updateRequests) {
                    updateModel(t, updateRequest, msr,sessions);
                    
                }
            }
    }

    private Model updateModel(Model t, UpdateRequest updateRequest, MessageSenderReceiver msr, SessionContainer sessions) throws LogicalException, SystemException {
        

        Model result = MessageUtil.clone(t, msr);
        for (UpdateItem updateItem : updateRequest.getUpdateItems()) {
            updateField(result, updateItem);
        }
        return result;
    }

    private void updateField(Model model, UpdateItem updateItem) throws LogicalException {
        String soperation = updateItem.getValue();
        if (soperation == null) {
            soperation = "0";
        }
        double opernad = Double.parseDouble(soperation);

        Object data;
        try {
            Method getter = new PropertyDescriptor(updateItem.getFieldName(), model.getClass()).getReadMethod();
            data = getter.invoke(model);
            if (data == null) {
                data = 0.0;
            }
        } catch (IntrospectionException e) {
            throw new LogicalException(ErrorCodes.Update.ERROR_UPDATE_INVALID_PROPERTY_NAME, model.getModelName(), updateItem.getFieldName());
        } catch (IllegalAccessException e) {
            throw new LogicalException(ErrorCodes.Update.ERROR_UPDATE_INVALID_PROPERTY_ACCESS, model.getModelName(), updateItem.getFieldName());
        } catch (Exception e) {
            throw new LogicalException(ErrorCodes.Update.ERROR_UPDATE_INVALID_PROPRTY, model.getModelName(), updateItem.getFieldName());
        }
        if (data instanceof Number) {
            Number number = (Number) data;
            switch (updateItem.getOperation()) {
            case DECREMENT:
                number = number.doubleValue() - opernad;
                break;
            case DECREMENT_BY:
                number = opernad - number.doubleValue();
                break;
            case DIVIDE:
                if (opernad == 0) {
                    throw new LogicalException(ErrorCodes.Update.ERROR_UPDATE_INVALID_OPERNAD, "" + opernad);
                }
                number = number.doubleValue() / opernad;
                break;
            case DIVIDE_BY:
                if (number.doubleValue() == 0) {
                    throw new LogicalException(ErrorCodes.Update.ERROR_UPDATE_INVALID_OPERNAD, "" + number.doubleValue());
                }
                number = opernad / number.doubleValue();
                break;
            case INCREMENT:
                number = number.doubleValue() + opernad;
                break;
            case MULTIPLY:
                number = number.doubleValue() * opernad;
                break;
            }
            try {
                Method setter = new PropertyDescriptor(updateItem.getFieldName(), model.getClass()).getWriteMethod();
                setter.invoke(model, number);
            } catch (IntrospectionException e) {
                throw new LogicalException(ErrorCodes.Update.ERROR_UPDATE_INVALID_PROPERTY_NAME, model.getModelName(), updateItem.getFieldName());
            } catch (IllegalAccessException e) {
                throw new LogicalException(ErrorCodes.Update.ERROR_UPDATE_INVALID_PROPERTY_ACCESS, model.getModelName(), updateItem.getFieldName());
            } catch (Exception e) {
                throw new LogicalException(ErrorCodes.Update.ERROR_UPDATE_INVALID_PROPRTY, model.getModelName(), updateItem.getFieldName());
            }
        }

    }



}
