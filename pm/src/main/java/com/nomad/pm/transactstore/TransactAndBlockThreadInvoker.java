package com.nomad.pm.transactstore;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.nomad.exception.ModelNotExistException;
import com.nomad.exception.SystemException;
import com.nomad.model.BlockInvoker;
import com.nomad.model.Criteria;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.TransactInvoker;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.pm.blocker.BlockStoreImpl;
import com.nomad.pm.exception.SysPmException;
import com.nomad.server.PmDataInvoker;


public class TransactAndBlockThreadInvoker extends TransactThreadInvoker implements Serializable, PmDataInvoker, TransactInvoker {

    private final BlockInvoker blocker = new BlockStoreImpl();
    public String session = "sess";

    public TransactAndBlockThreadInvoker(final PmDataInvoker in) {
        super(in);
    }

    @Override
    public <T extends Model> StatisticResult<T> getList(final Criteria<T> criteria) throws SystemException {

        final StatisticResult<T> result = super.getList(criteria);
        if (result.getResultList() == null) {
            return result;
        }
        final List<Identifier> blocked = new ArrayList<>(result.getResultList().size());
        for (final T model : result.getResultList()) {
            if (!blocker.softBlock(session, model.getIdentifier())) {
                LOGGER.warn("{} bloked!", model.getIdentifier());
                for (final Identifier id : blocked) {
                    blocker.softBlock(session, id);
                }
                throw new SysPmException();
            }
            blocked.add(model.getIdentifier());
        }
        blocked.clear();
        return result;
    }


   
    @Override
    public BigInteger getNextKey(final String tableName, int count) {
        return super.getNextKey(tableName,count);
    }

    @Override
    public Model getModel(final Identifier identifier) throws ModelNotExistException, SystemException {

        if (!blocker.softBlock(session, identifier)) {
            LOGGER.warn("cannot block {}", identifier);
            throw new SysPmException("cannot block " + identifier);

        }
        return super.getModel(identifier);
    }



    @Override
    public int eraseModel(final Criteria<? extends Model> criteria) throws SystemException {

        final StatisticResult<? extends Model> result = super.getIds(criteria);
        if (result.getIdentifiers() == null) {
            return 0;
        }
        final List<Identifier> blocked = new ArrayList<>(result.getIdentifiers().size());
        for (final Identifier identifier : result.getIdentifiers()) {
            if (!blocker.hardBlock(session, identifier)) {
                LOGGER.warn("{} bloked!", identifier);
                for (final Identifier id : blocked) {
                    blocker.hardBlock(session, id);
                }
                throw new SysPmException();
            }
            blocked.add(identifier);
        }
        blocked.clear();
        return super.eraseModel(criteria);

    }

    @Override
    public void rollBack(final String transactionId) throws SystemException {
        super.rollBack(transactionId);
        blocker.cleanSession(session);
    }

    @Override
    public void commit(final String transactionId) throws SystemException {
        blocker.cleanSession(session);
        super.commit(transactionId);
    }


}
