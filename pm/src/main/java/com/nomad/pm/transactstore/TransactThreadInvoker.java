package com.nomad.pm.transactstore;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.ModelNotExistException;
import com.nomad.exception.SystemException;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.MessageSenderReceiverImpl;
import com.nomad.model.Criteria;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.TransactInvoker;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.pm.transact.SessionAssociation;
import com.nomad.pm.transact.TransactElement;
import com.nomad.pm.transact.TransactionMarker;
import com.nomad.server.ConnectionPool;
import com.nomad.server.DataDefinitionService;
import com.nomad.server.PmDataInvoker;
import com.nomad.server.ServerContext;
import com.nomad.utility.ModelUtil;

import com.nomad.utility.PooledObject;

public class TransactThreadInvoker implements Serializable, PmDataInvoker, TransactInvoker {

    protected static Logger LOGGER = LoggerFactory.getLogger(TransactThreadInvoker.class);
    private final MessageSenderReceiver msr;
    private final PmDataInvoker dataInvoker;

    public TransactThreadInvoker(final PmDataInvoker in) {
        dataInvoker = in;
        msr = new MessageSenderReceiverImpl(in.getDataDefinitionService());
    }

    /**
     * defined DataDef class
     *
     * @param dataDef
     */

    @Override
    public DataDefinitionService getDataDefinitionService() {
        return dataInvoker.getDataDefinitionService();
    }

    protected TransactionMarker getTransactionMarker() {
        TransactionMarker result = SessionAssociation.getTransactInvoker();
        if (result == null) {
            result = new TransactionMarker(dataInvoker, msr);
            SessionAssociation.setTransactInvoker(result);
        } else {
            result.setDataInvoker(dataInvoker);
        }
        return result;
    }

    @Override
    public <T extends Model> StatisticResult<T> getList(final Criteria<T> criteria) throws SystemException {
        return dataInvoker.getList(criteria);
    }

    private Model addModel(final Model m) throws SystemException {
        LOGGER.info("add transaction for {}", m);
        final TransactionMarker marker = getTransactionMarker();
        final Collection<Model> result = dataInvoker.addModel(Arrays.asList(m));

        if (marker != null) {
            for (Model model : result) {
                marker.addOperation(model, TransactElement.Operation.INSERT_MODEL);
            }
        }
        return result.iterator().next();

    }

    @Override
    public BigInteger getNextKey(final String tableName, int count) {
        return dataInvoker.getNextKey(tableName, count);
    }

    @Override
    public Model getModel(final Identifier identifier) throws ModelNotExistException, SystemException {
        return dataInvoker.getModel(identifier);
    }



    @Override
    public int eraseModel(final Collection<Identifier> identifiers) throws ModelNotExistException, SystemException {
        LOGGER.debug("delete transaction for {}", identifiers);
        final TransactionMarker marker = getTransactionMarker();
        int result = 0;
        if (marker != null) {
            final Collection<Model> oldModels = dataInvoker.getModel(identifiers);
            Map<Identifier, Model> oldData= ModelUtil.convertToMap(oldModels);
            for (final Identifier id : identifiers) {
                Model oldModel=oldData.get(id);
                if(oldModel!=null){
                    marker.addOperation(oldModel, TransactElement.Operation.DELETE_MODEL);
                }
            }
        }
        return result;
    }

    @Override
    public int eraseModel(final Criteria<? extends Model> criteria) throws SystemException {
        final TransactionMarker marker = getTransactionMarker();
        if (marker != null) {
            final Collection<? extends Model> l = dataInvoker.getList(criteria).getResultList();
            for (Model model : l) {
                marker.addOperation(model, TransactElement.Operation.DELETE_MODEL);
            }
        }
        return dataInvoker.eraseModel(criteria);
    }

    @Override
    public Connection getConnection() throws SystemException {
        return dataInvoker.getConnection();
    }

    @Override
    public void rollBack(final String transactionId) throws SystemException {
        final TransactionMarker marker = getTransactionMarker();
        if (marker.getElements().size() == 0) {
            return;
        }
        transactRollBack(marker.getElements());
        marker.transactCommit();
    }

    @Override
    public void commit(final String transactionId) throws SystemException {
        final TransactionMarker marker = getTransactionMarker();
        marker.transactCommit();
    }

    private void transactRollBack(final List<TransactElement> elements) throws SystemException {
        LOGGER.debug("RollBack! {}", elements);
        for (int i = (elements.size() - 1); i >= 0; i--) {
            final TransactElement element = elements.get(i);
            LOGGER.debug("RollBack! {}", element);
            if (element.getOperation() == TransactElement.Operation.INSERT_MODEL) {
                try {
                    dataInvoker.eraseModel(Arrays.asList(element.getNewModel().getIdentifier()));
                    element.getNewModel().setIdentifier(null);
                } catch (final ModelNotExistException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            if (element.getOperation() == TransactElement.Operation.UPDATE_MODEL) {
                dataInvoker.updateModel(Arrays.asList(element.getOldModel()));
            }
            if (element.getOperation() == TransactElement.Operation.DELETE_MODEL) {
                dataInvoker.addModel(Arrays.asList(element.getOldModel()));
            }
        }

    }

    @Override
    public String startTransaction() throws SystemException {
        return null;
    }

    @Override
    public Identifier createIdentifierFromModel(final Model m) throws SystemException {
        return dataInvoker.createIdentifierFromModel(m);
    }

    @Override
    public void init(final Properties properties, final ServerContext context, String name) throws SystemException {
        dataInvoker.init(properties, context, name);

    }

    @Override
    public void close() {
        dataInvoker.close();
    }

    @Override
    public void setConnectPool(final ConnectionPool<? extends PooledObject> connectPool) {
        dataInvoker.setConnectPool(connectPool);
    }

    @Override
    public Collection<Model> addModel(final Collection<Model> m) throws SystemException {
        final Collection<Model> result = new ArrayList<>(m.size());
        for (final Model t : m) {
            result.add(addModel(t));
        }
        return result;
    }

    @Override
    public Collection<Model> updateModel(final Collection<Model> models) throws SystemException {
        final TransactionMarker marker = getTransactionMarker();
        if (marker != null) {
            for (final Model model : models) {
                final Identifier identifier = model.getIdentifier();
                if (identifier != null) {
                    marker.addOperation(model, TransactElement.Operation.UPDATE_MODEL);
                }
            }
        }
        return dataInvoker.updateModel(models);
    }

    @Override
    public Collection<Model> getModel(final Collection<Identifier> identifier) throws ModelNotExistException, SystemException {
        return dataInvoker.getModel(identifier);
    }

    @Override
    public <T extends Model> StatisticResult<T> getIds(final Criteria<T> criteria) throws SystemException {
        return dataInvoker.getIds(criteria);
    }

    @Override
    public void setDataDefinitionService(final DataDefinitionService dataDefinition) {
        dataInvoker.setDataDefinitionService(dataDefinition);

    }

}
