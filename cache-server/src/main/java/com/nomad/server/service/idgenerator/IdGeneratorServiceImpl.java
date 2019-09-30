package com.nomad.server.service.idgenerator;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.SystemException;
import com.nomad.model.Criteria;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.ServerModel;
import com.nomad.model.StoreModel;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.model.idgenerator.IdGeneratorServerModel;
import com.nomad.model.idgenerator.IdGeneratorService;
import com.nomad.server.ServerContext;
import com.nomad.server.idgenerator.AtomicBigInteger;
import com.nomad.util.DataInvokerPoolImpl;
import com.nomad.utility.DataInvokerPool;
import com.nomad.utility.PooledDataInvoker;
import com.nomad.utility.SimpleCriteria;

public class IdGeneratorServiceImpl extends AbstractIdGeneratorService implements IdGeneratorService {
    private static Logger LOGGER = LoggerFactory.getLogger(IdGeneratorServiceImpl.class);

    private Map<String, IdInformation> counters = new HashMap<>();
    private IdGeneratorServerModel idServerModel;
    private static final String CONTEXT_NAME = "systemGeneratorId";
    private DataInvokerPool systemDataPool;

    public IdGeneratorServiceImpl(ServerContext context, IdGeneratorServerModel model) {
        super(context);
        this.idServerModel = model;
        if (idServerModel.getIncrement() == 0) {
            idServerModel.setIncrement(100);
        }

    }

    private DataInvokerPool getSystemDataInvokerPool() throws SystemException {
        return new DataInvokerPoolImpl(idServerModel.getMaxThreads(), 0, idServerModel.getInvokerClass(), idServerModel.getProperties(), context, CONTEXT_NAME);

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void start() throws SystemException {
        super.start();
        systemDataPool = (DataInvokerPool) context.getDataInvoker(CONTEXT_NAME);

        if (systemDataPool == null) {
            systemDataPool = getSystemDataInvokerPool();
            context.put(CONTEXT_NAME, systemDataPool);
        }

        ServerModel serverModel = context.getServerModel();
        for (String modelName : idServerModel.getModelSource().keySet()) {
            StoreModel storeModel = serverModel.getStoreModel(modelName);
            if (storeModel != null) {
                String dataSourceName = storeModel.getDataSource();
                DataInvokerPool invokerPool = context.getDataInvoker(dataSourceName);
                PooledDataInvoker invoker = invokerPool.getObject();

                PooledDataInvoker counterInvoker = null;

                IdInformation information = null;
                try {
                    counterInvoker = systemDataPool.getObject();
                    try {
                        BigInteger nextkey = counterInvoker.getNextKey(storeModel.getModel(), idServerModel.getIncrement());
                        if (nextkey != null) {
                            information = getIdInformation(modelName);
                            if (information != null) {
                                information.setCounter(new AtomicBigInteger(nextkey.subtract(new BigInteger("" + idServerModel.getIncrement()))));
                                information.setLastReservedCounter(new AtomicBigInteger(nextkey));
                                counters.put(modelName, information);

                            }
                        }
                    } finally {
                        if (counterInvoker != null) {
                            counterInvoker.freeObject();
                        }
                    }

                    if (information == null) {
                        information = getIdInformation(modelName);
                        if (information != null) {
                            SimpleCriteria criteria = new SimpleCriteria();
                            criteria.setModelName(modelName);
                            criteria.addOrderDesc(information.getFieldName());
                            criteria.setPageSize(1);
                            StatisticResult<Model> list = (StatisticResult<Model>) invoker.getList((Criteria) criteria);
                            if (list.getResultList() != null && list.getResultList().size() > 0) {
                                Model model = list.getResultList().iterator().next();
                                final Method getter = new PropertyDescriptor(information.getFieldName(), information.getClazz()).getReadMethod();
                                getter.invoke(model.getIdentifier());
                                counters.put(modelName, information);
                            } else {
                                counters.put(modelName, information);
                            }

                        } else {
                            LOGGER.error(" no informatio about model:" + modelName);
                            throw new SystemException(" no informatio about model:" + modelName);
                        }
                    }
                } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException  e) {
                    throw new SystemException(e);
                } finally {
                    if (invoker != null) {
                        invoker.freeObject();
                    }
                }
            }
        }
    }

    @Override
    public void stop() {
        if (systemDataPool != null) {
            systemDataPool.close();
        }

    }

    @Override
    public List<BigInteger> nextId(String modelName, int count) {
        LOGGER.debug("nextId({})", modelName);
        IdInformation idInformation = counters.get(modelName);
        if (idInformation == null) {
            LOGGER.info("init data for " + modelName);
            return null;
        }
        synchronized (idInformation) {
            BigInteger bigIncrement = new BigInteger("" + idServerModel.getIncrement());
            List<BigInteger> results = new ArrayList<>();

            PooledDataInvoker invoker = null;
            try {
                while (results.size() < count) {
                    if (idInformation.getLastReservedCounter().get().compareTo(idInformation.getCounter().get()) > 0) {
                        results.add(idInformation.getCounter().addAndGet(AtomicBigInteger.ONE));
                    } else {
                        if (invoker == null) {
                            invoker = systemDataPool.getObject();
                        }
                        BigInteger newValue = invoker.getNextKey(modelName, idServerModel.getIncrement());

                        idInformation.getCounter().set(newValue.subtract(bigIncrement));
                        idInformation.getLastReservedCounter().set(newValue);
                    }
                }
            } finally {
                if (invoker != null) {
                    invoker.freeObject();
                }
            }
            return results;
        }
    }

    @Override
    public List<Identifier> nextIdentifier(String modelName, int count) throws SystemException {
        LOGGER.debug("nextId({})", modelName);
        IdInformation idInformation = counters.get(modelName);
        if (idInformation == null) {
            LOGGER.info("init data for " + modelName);
            return null;
        }
        List<BigInteger> newNumbers = nextId(modelName, count);
        List<Identifier> result = new ArrayList<>();
        for (BigInteger bigInteger : newNumbers) {
            
            try {
                Identifier  identifier = idInformation.getClazz().newInstance();
                new PropertyDescriptor(idInformation.getFieldName(), idInformation.getClazz()).getWriteMethod().invoke(identifier, bigInteger);
                result.add(identifier);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | IntrospectionException e) {
                throw new SystemException(e);
            }
        }

        return result;

    }

}
