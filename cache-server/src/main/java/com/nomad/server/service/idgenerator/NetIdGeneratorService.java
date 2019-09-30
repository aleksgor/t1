package com.nomad.server.service.idgenerator;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonclientserver.idgenerator.IdGeneratorMessageImpl;
import com.nomad.exception.ErrorCodes;
import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.model.ConnectStatus;
import com.nomad.model.Identifier;
import com.nomad.model.idgenerator.IdGeneratorClientModel;
import com.nomad.model.idgenerator.IdGeneratorCommand;
import com.nomad.model.idgenerator.IdGeneratorMessage;
import com.nomad.model.idgenerator.IdGeneratorService;
import com.nomad.server.ServerContext;
import com.nomad.server.service.session.server.NetworkSessionService;

public class NetIdGeneratorService extends AbstractIdGeneratorService implements IdGeneratorService {

    private static Logger LOGGER = LoggerFactory.getLogger(NetworkSessionService.class);
    private Map<String, List<IdGeneratorConnectionPool>> modelMap = new HashMap<>();

    private List<IdGeneratorClientModel> models = new ArrayList<>();

    public NetIdGeneratorService(final List<IdGeneratorClientModel> models, final ServerContext context) {
        super(context);
        this.models = models;
    }

    private IdGeneratorMessage execute(final IdGeneratorMessage message) {

        List<IdGeneratorConnectionPool> pools = modelMap.get(message.getModelName());
        if (pools == null) {

        }
        List<IdGeneratorConnectionPool> goodPools = new ArrayList<>(pools.size());
        for (IdGeneratorConnectionPool idGeneratorConnectionPool : pools) {
            if (ConnectStatus.OK.equals(idGeneratorConnectionPool.getStatus())) {
                goodPools.add(idGeneratorConnectionPool);
            }
        }
        if (goodPools.size() == 0) {
            LOGGER.error("No good IdGeneratorConnection server");
            message.setResultCode(-1);
            return message;
        }
        Random random = new Random();
        Set<Integer> triedPools = new HashSet<>(goodPools.size());
        while (triedPools.size() < goodPools.size()) {
            Integer index = random.nextInt(pools.size());
            while (triedPools.contains(index)) {
                index = random.nextInt(pools.size());
            }
            triedPools.add(index);
            IdGeneratorConnectionPool pool = goodPools.get(index);
            IdGeneratorClient client = null;
            try {
                client = (IdGeneratorClient) pool.getObject();
                IdGeneratorMessage result = client.sendMessage(message);
                if (result.getResultCode() == 0) {
                    return result;
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                pool.setStatus(ConnectStatus.ERROR);
            } finally {
                if (client != null) {
                    client.freeObject();
                }
            }
        }
        message.setResultCode(-1);
        return message;

    }

    @Override
    public void start() throws SystemException {
        super.start();
        int maxThread = 0;
        Map<String, List<IdGeneratorConnectionPool>> modelMap = new HashMap<>();
        for (final IdGeneratorClientModel sessionClientModel : models) {
            IdGeneratorConnectionPool connectionPool = new IdGeneratorConnectionPool(sessionClientModel, context);
            sessionClientModel.getModelNames();
            for (String modelName : sessionClientModel.getModelNames()) {
                List<IdGeneratorConnectionPool> pools = modelMap.get(modelName);
                if (pools == null) {
                    pools = new ArrayList<>();
                    modelMap.put(modelName, pools);
                }
                pools.add(connectionPool);
            }
            maxThread = Math.max(maxThread, sessionClientModel.getThreads());
        }
        this.modelMap = modelMap;
    }

    @Override
    public List<BigInteger> nextId(String modelName, int count) {
        IdGeneratorMessage message = new IdGeneratorMessageImpl();
        message.setModelName(modelName);
        message.setCount(count);
        message = execute(message);
        return message.getValue();
    }

    @Override
    public List<Identifier> nextIdentifier(String modelName, int count) throws LogicalException,  SystemException {
        IdGeneratorMessage message = new IdGeneratorMessageImpl();
        message.setModelName(modelName);
        message.setCommand(IdGeneratorCommand.GET_NEXT_ID);
        message.setCount(count);
        message = execute(message);
        IdInformation information = getIdInformation(modelName);
        List<Identifier> result = new ArrayList<>(message.getValue().size());
        for (BigInteger bigInteger : message.getValue()) {
            try {
                Identifier identifier = information.getClazz().newInstance();
                information.getMethod().invoke(identifier, bigInteger);
                result.add(identifier);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                LOGGER.error(e.getMessage(),e);
                throw new LogicalException(ErrorCodes.IdGenerator.ERROR_IDGENERATOR_NO_METHOD, information.getClazz().getName());
            }
        }
        return result;
    }


    @Override
    public void stop() {
        if (modelMap != null) {
            Map<String, IdGeneratorConnectionPool> uniquePools = new HashMap<>();
            for (List<IdGeneratorConnectionPool> pools : modelMap.values()) {
                for (IdGeneratorConnectionPool pool : pools) {
                    uniquePools.put(pool.getPoolId(), pool);
                }
            }
            for (IdGeneratorConnectionPool pool : uniquePools.values()) {
                pool.close();
            }
        }
    }

}
