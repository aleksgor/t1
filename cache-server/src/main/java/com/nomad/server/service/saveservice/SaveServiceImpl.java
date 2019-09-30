package com.nomad.server.service.saveservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.ErrorCodes;
import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.io.serializer.SerializerFactory;
import com.nomad.message.SaveCommand;
import com.nomad.message.SaveRequest;
import com.nomad.message.SaveResult;
import com.nomad.model.ConnectStatus;
import com.nomad.model.Identifier;
import com.nomad.model.SaveClientModelImpl;
import com.nomad.model.SaveClientModelSerializer;
import com.nomad.model.SaveServerModelImpl;
import com.nomad.model.SaveServerModelSerializer;
import com.nomad.model.saveserver.SaveClientModel;
import com.nomad.server.SaveService;
import com.nomad.server.ServerContext;
import com.nomad.server.service.saveservice.model.SaveRequestImpl;
import com.nomad.server.service.saveservice.model.SaveRequestSerializer;
import com.nomad.server.service.saveservice.model.SaveResultImpl;
import com.nomad.server.service.saveservice.model.SaveResultSerializer;
import com.nomad.util.executorpool.ExecutorsPool;
import com.nomad.util.executorpool.PooledExecutor;

public class SaveServiceImpl implements SaveService {

    private static Logger LOGGER = LoggerFactory.getLogger(SaveService.class);

    private final List<SaveClientModel> saveClientModels = new ArrayList<>();
    private final ServerContext context;

    private final List<SaveClientPool> connectPools = new ArrayList<>();
    private ExecutorsPool execPool;
    private final long clientId;
    private int maxThread = 1;
    private volatile boolean available = false;

    public SaveServiceImpl(final List<SaveClientModel> clientModels, final ServerContext context) {
        LOGGER.info("init save service serverModel:{} started", clientModels);
        SerializerFactory.registerSerializer(SaveRequestImpl.class, SaveRequestSerializer.class);
        SerializerFactory.registerSerializer(SaveResultImpl.class, SaveResultSerializer.class);
        SerializerFactory.registerSerializer(SaveClientModelImpl.class, SaveClientModelSerializer.class);
        SerializerFactory.registerSerializer(SaveServerModelImpl.class, SaveServerModelSerializer.class);
        saveClientModels.addAll(clientModels);
        clientId = context.getServerModel().getServerId();
        this.context = context;

    }

    @Override
    public Collection<Identifier> isReadyToSave(final Collection<Identifier> ids, final String sessionId) throws SystemException, LogicalException {
        LOGGER.debug("isReadyToSave id:{}, session:{}", ids, sessionId);
        final Collection<Identifier> result = new ArrayList<>(ids.size());
        final SaveRequest request = new SaveRequestImpl(Collections.singletonList(sessionId), ids, SaveCommand.TRY_BLOCK, clientId);
        final Collection<SaveResult> results = execAllTasks(request, false);
        for (final SaveResult saveResult : results) {
            result.addAll(saveResult.getAllowedIds());
        }
        LOGGER.debug("isReadyToSave result:{}, session:{}", result, sessionId);
        return result;
    }

    @Override
    public void cleanSession(final Collection<String> sessionIds) throws SystemException, LogicalException  {
        LOGGER.debug("Clean session  :{}", sessionIds);
        final SaveRequest request = new SaveRequestImpl(sessionIds, Collections.<Identifier> emptyList(), SaveCommand.RELEASE, clientId);
        execAllTasks(request, false);
    }

    @Override
    public Collection<Identifier> internalCheck(final Collection<Identifier> ids, final String sessionId, final long clientId) throws SystemException, LogicalException  {
        LOGGER.debug("internalCheck  sessionId:{}, clientId:{} ids:{},", new Object[] { sessionId, clientId, ids });
        final SaveRequest request = new SaveRequestImpl(Collections.singletonList(sessionId), ids, SaveCommand.PRECHECK, clientId);
        final Collection<SaveResult> results = execAllTasks(request, false);
        final Collection<Identifier> result = new ArrayList<Identifier>();
        final int count=results.size();
        final Map<Identifier, Integer> map = new HashMap<>();
        for (final SaveResult saveResult : results) {
            for (final Identifier identifier : saveResult.getAllowedIds()) {
                Integer counter = map.get(identifier);
                if (counter == null) {
                    counter = 0;
                }
                map.put(identifier, ++counter);
            }
        }
        for (final Entry<Identifier, Integer> entry : map.entrySet()) {
            if(entry.getValue()==count){
                result.add(entry.getKey());
            }
        }
        return result;
    }

    @Override
    public void stop() {
        for (final SaveClientPool pool : connectPools) {
            pool.stop();
        }
        execPool.close();
    }

    @Override
    public void start() throws SystemException {
        LOGGER.info("Init save service server model:{} ", saveClientModels);

        for (final SaveClientModel clientModel : saveClientModels) {
            final SaveClientPool pool = new SaveClientPool(clientModel, context);
            connectPools.add(pool);
            // test connection
            maxThread = Math.max(maxThread, clientModel.getThreads());
        }
        available = saveClientModels.size() > 0;
        execPool = new ExecutorsPool(maxThread, context, "Save session client pool:"+context.getServerModel().getServerName(), 10000000);
        LOGGER.info("Init save service server model:{} successful ", saveClientModels);

    }

    private Collection<SaveResult> execAllTasks(final SaveRequest message, final boolean allServers) throws SystemException, LogicalException {
        final Collection<SaveResult> result = new ArrayList<>(connectPools.size());
        PooledExecutor executor = null;
        try {
            final Collection<Callable<SaveResult>> requests = getTasks(message, allServers);
            if (requests.isEmpty()) {
                throw new LogicalException(ErrorCodes.Cache.ERROR_CACHE_NO_ACTIVE_SERVERSES);
            }
            executor = execPool.getObject();
            final List<Future<SaveResult>> results = executor.executeAll(requests);
            for (final Future<SaveResult> future : results) {
                result.add(future.get());
            }
        } catch (InterruptedException | ExecutionException x) {
            LOGGER.error("Error" + x.getMessage() + " in:" + context.getServerModel().getServerName());
            throw new SystemException(x);
        } finally {
            if (executor != null) {
                executor.freeObject();
            }
        }
        return result;
    }

    private Collection<Callable<SaveResult>> getTasks(final SaveRequest message, final boolean allServers) {
        final Collection<Callable<SaveResult>> tasks = new ArrayList<>(connectPools.size());
        for (final SaveClientPool connectPool : connectPools) {
            if (ConnectStatus.OK.equals(connectPool.getStatus()) || allServers) {
                final SaveServiceCallable callable = new SaveServiceCallable(connectPool, message);
                tasks.add(callable);
            }
        }
        return tasks;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String toString() {
        return "SaveServiceImpl [saveClientModels=" + saveClientModels +  ", connectPools=" + connectPools + ", execPool=" + execPool + ", clientId="
                + clientId + ", maxThread="
                + maxThread + ", available=" + available + "]";
    }

}
