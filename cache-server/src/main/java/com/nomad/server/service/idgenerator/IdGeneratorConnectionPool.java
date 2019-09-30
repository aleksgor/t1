package com.nomad.server.service.idgenerator;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonclientserver.idgenerator.IdGeneratorMessageImpl;
import com.nomad.exception.SystemException;
import com.nomad.model.ConnectStatus;
import com.nomad.model.idgenerator.IdGeneratorClientModel;
import com.nomad.model.idgenerator.IdGeneratorCommand;
import com.nomad.model.idgenerator.IdGeneratorMessage;
import com.nomad.server.CacheServerConstants;
import com.nomad.server.ServerContext;
import com.nomad.server.service.common.NetworkConnectionPoolImpl;

public class IdGeneratorConnectionPool extends NetworkConnectionPoolImpl<IdGeneratorMessage, IdGeneratorMessage> {

    private static Logger LOGGER = LoggerFactory.getLogger(IdGeneratorConnectionPool.class);

    public IdGeneratorConnectionPool(final IdGeneratorClientModel model, final ServerContext context) throws SystemException {
        super(model, context, false, CacheServerConstants.Statistic.ID_GENERATOR_NAME);

    }

    @Override
    public String getPoolId() {
        return "Save Service:" + client.getHost() + ":" + client.getPort();
    }


    @Override
    public IdGeneratorClient getNewPooledObject() {
        IdGeneratorClient connection = null;
        try {
            connection = new IdGeneratorClient(client, context);
            LOGGER.info("Init result:" + connection);
            status = ConnectStatus.OK;
            connection.setPool(this);
            return connection;
        } catch (final Exception e) {
            LOGGER.error("Error create connection:", e);
            status = ConnectStatus.INACCESSIBLE;
            throw new RuntimeException();
        }

    }


    @Override
    public String toString() {
        return "IdGeneratorConnectionPool [host=" + client.getHost() + ", port=" + client.getPort() + ", status=" + status + "]";
    }

    @Override
    public boolean connectTest() {
        IdGeneratorClient client = null;
        try {
            final IdGeneratorMessage message = new IdGeneratorMessageImpl();
            message.setCommand(IdGeneratorCommand.GET_STATUS);
            message.setResultCode(-1);
            client = (IdGeneratorClient) getObject();
            final IdGeneratorMessage answer = client.sendMessage(message);
            if (answer.getResultCode() == 0) {
                return true;
            }
        } catch (final Exception e) {

        } finally {
            if (client != null) {
                client.freeObject();
            }
        }
        return false;
    }


}
