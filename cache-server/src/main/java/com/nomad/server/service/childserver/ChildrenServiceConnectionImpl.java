package com.nomad.server.service.childserver;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonclientserver.ManagementMessageImpl;
import com.nomad.client.ClientPooledInterface;
import com.nomad.exception.SystemException;
import com.nomad.message.ManagementMessage;
import com.nomad.model.ConnectModel;
import com.nomad.model.ConnectStatus;
import com.nomad.model.Identifier;
import com.nomad.model.ListenerModel;
import com.nomad.model.ManagerCommand;
import com.nomad.model.ServerModel;
import com.nomad.model.ServerModelImpl;
import com.nomad.model.StoreModel;
import com.nomad.server.CacheServerConstants;
import com.nomad.server.ServerContext;
import com.nomad.server.ServerContext.ServiceName;
import com.nomad.server.service.ChildrenServerService;
import com.nomad.server.service.ManagementClientConnectionPool;
import com.nomad.server.service.ManagementService;

public class ChildrenServiceConnectionImpl implements ChildrenServerService {

    private final ServerContext context;
    private final Map<String, StoreConnectionPool> servers = new HashMap<>();
    private volatile Map<String, BestServerCalculator> bestServerCalculators = new ConcurrentHashMap<>();
    private static Logger LOGGER = LoggerFactory.getLogger(ChildrenServiceConnectionImpl.class);
    private final ServerModel serverModel;
    private final StoreConnectionPool localCacheServer;

    public ChildrenServiceConnectionImpl(final ServerModel serverModel, final ServerContext context) {
        this.context = context;
        int maxThreads = 0;
        for (final ListenerModel listener : serverModel.getListeners()) {
            maxThreads += listener.getMaxThreads();
        }
        localCacheServer = new StoreConnectionPool(maxThreads, null, 1000, context, CacheServerConstants.Statistic.LISTENER_STATISTIC_GROUP_NAME, this);
        servers.put(localCacheServer.getPoolId(), localCacheServer);

        final List<StoreModel> storeModels = serverModel.getStoreModels();
        for (final StoreModel storeModel : storeModels) {
            if (storeModel.isCache()) {
                final BestServerCalculator calculator = new BestServerCalculator(storeModel);
                calculator.addConnectPool(localCacheServer, storeModel);
                bestServerCalculators.put(storeModel.getModel(), calculator);
            }
        }

        this.serverModel = serverModel;
    }

    /**
     * get servers for model or all servers in case modelName is null
     */
    @Override
    public Collection<StoreConnectionPool> getCacheConnectionsPools(final String modelName) {
        Collection<StoreConnectionPool> pools = null;
        if (modelName == null || modelName.length() == 0) {
            pools = new ArrayList<>(servers.size());
            for (final StoreConnectionPool storeConnectionPool : servers.values()) {
                if (ConnectStatus.OK.getCode() == storeConnectionPool.getStatus().getCode()) {
                    pools.add(storeConnectionPool);
                }
            }
        } else {
            final BestServerCalculator calculator = bestServerCalculators.get(modelName);
            if (calculator != null) {
                pools = calculator.getReadyCachePools();
            }
        }
        return pools;
    }

    @Override
    public StoreConnectionPool getLocalServer() {
        return localCacheServer;
    }

    private void addConnectClient(final ConnectModel colleague) throws SystemException {
        // try to get information about server
        final ManagementService managementService = (ManagementService) context.get(ServiceName.MANAGEMENT_SERVICE);
        final ManagementClientConnectionPool pool = managementService.getClientPool(colleague.getManagementServer());
        final ManagementMessage message= new ManagementMessageImpl();
        message.setCommand(ManagerCommand.GET_SERVER_INFO.toString());
        ClientPooledInterface<ManagementMessage, ManagementMessage> client=null;
        ManagementMessage managementMessage = null;
        try{
            client=pool.getClient();
            managementMessage = client.sendMessage(message);
            if (managementMessage == null) {
                return;
            }
        }finally{
            if(client !=null){
                client.freeObject();
            }
        }

        final ServerModelImpl server = (ServerModelImpl) managementMessage.getData();
        LOGGER.info("add into ConnectPool server: {}", server);
        final StoreConnectionPool newPool = new StoreConnectionPool(colleague.getListener().getMinThreads(), colleague, 10000, context,
                CacheServerConstants.Statistic.LISTENER_STATISTIC_GROUP_NAME, this);
        servers.put(newPool.getPoolId(), newPool);

        final List<StoreModel> models = server.getStoreModels();
        for (final StoreModel model : models) {
            BestServerCalculator calculator = bestServerCalculators.get(model.getModel());
            if (calculator == null) {
                calculator = new BestServerCalculator(model);
                bestServerCalculators.put(model.getModel(), calculator);
            }
            calculator.addConnectPool(newPool, model);
        }
       
    }

    @Override
    public void registerClient(final ConnectModel colleague) throws SystemException {
        LOGGER.info("register in:" + serverModel.getServerName() + " colleague:" + colleague);
        final boolean found = false;
        for (final StoreConnectionPool pool : servers.values()) {
            final ConnectModel client = pool.getConnectModel();
            if (client != null) { // exclude local
                if (colleague.getManagementServer().equals(client.getManagementServer())) {
                    client.getListener().setMinThreads(colleague.getThreads());
                    client.getListener().setMaxThreads(colleague.getThreads());
                    client.getListener().setProtocolVersion(colleague.getListener().getProtocolVersion());
                    pool.updateClient(client);

                    return;
                }
            }
        }
        if (!found) {
            addConnectClient(colleague);
        }

    }

    @Override
    public synchronized int getStoriesCount(final Identifier id) {
        return bestServerCalculators.get(id.getModelName()).getStoreModel().getCopyCount();

    }

    @Override
    public Collection<String> getConnectionsPoolsIds(final Identifier id) {
        if (id == null || id.getModelName() == null) {
            throw new InvalidParameterException("id:" + id);
        }
        final String modelName = id.getModelName();
        final BestServerCalculator calculator = bestServerCalculators.get(modelName);
        if (calculator == null) {
            return null;
        }
        return calculator.getPoolIdsForCacheManager(id);

    }

    @Override
    public Collection<String> getFullConnectionsPoolsIds(final Identifier id) {
        if (id == null || id.getModelName() == null) {
            throw new InvalidParameterException("id:" + id);
        }
        final String modelName = id.getModelName();
        final BestServerCalculator calculator = bestServerCalculators.get(modelName);
        if (calculator == null) {
            return null;
        }
        return calculator.getFullPoolIdsForCacheManager(id);

    }

    @Override
    public synchronized Collection<String> fillPoolsIds(final Identifier id, final Collection<String> input) {
        final String modelName = id.getModelName();
        final BestServerCalculator calculator = bestServerCalculators.get(modelName);
        if (calculator == null) {
            LOGGER.error("The models modelName does not supported");
            return input;
        }
        final int copyCount = calculator.getStoreModel().getCopyCount();

        final Set<String> inputSet = new HashSet<>(input);

        final Collection<String> allClients = getConnectionsPoolsIds(id);
        for (final String client : allClients) {
            if (!inputSet.contains(client)) {
                inputSet.add(client);
                input.add(client);
            }
            if (input.size() >= copyCount) {
                return input;
            }
        }
        return input;

    }

    @Override
    public void stop() {
        for (final StoreConnectionPool pool : servers.values()) {
            pool.close();
        }
        servers.clear();
        bestServerCalculators.clear();

    }

    @Override
    public StoreConnectionPool getStoreConnectionPool(final String serverName) {
        return servers.get(serverName);
    }

    @Override
    public void start() throws SystemException {

    }

    @Override
    public void updateRating(final double rating, final StoreConnectionPool pool) {
        LOGGER.debug("updateRating:" + rating + " pool:" + pool.getPoolId());
        for (final Entry<String, BestServerCalculator> entry : bestServerCalculators.entrySet()) {
            final Collection<StoreConnectionPool> pools = entry.getValue().getCachePools();
            for (final StoreConnectionPool storeConnectionPool : pools) {
                if (storeConnectionPool.getPoolId().equals(pool.getPoolId())) {
                    entry.getValue().updateConnectPool(pool, rating);
                }
            }
        }

    }

    @Override
    public Collection<StoreConnectionPool> getUniqueStoreCacheConnectionPools(String modelName) {
        if (modelName == null || modelName.length() == 0) { // all models
            Map<String, StoreConnectionPool> map = new HashMap<String, StoreConnectionPool>();
            for (final StoreConnectionPool storeConnectionPool : servers.values()) {
                if (ConnectStatus.OK.getCode() == storeConnectionPool.getStatus().getCode()) {
                    storeConnectionPool.getDataSources();
                    for (String ds : storeConnectionPool.getDataSources()) {
                        map.put(ds, storeConnectionPool);
                    }
                }
            }
            return map.values();
        } else {
            Collection<StoreConnectionPool> result = null;
            final BestServerCalculator calculator = bestServerCalculators.get(modelName);
            if (calculator != null) {
                result = calculator.getUniqueDataSourcesCachePools();
            }
            return result;
        }
    }
}
