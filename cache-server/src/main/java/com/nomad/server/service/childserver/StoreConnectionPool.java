package com.nomad.server.service.childserver;

import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonclientserver.RawMessageImpl;
import com.nomad.client.ClientStatus;
import com.nomad.client.RawClientPooledInterface;
import com.nomad.exception.SystemException;
import com.nomad.message.MessageHeader;
import com.nomad.message.OperationStatus;
import com.nomad.message.RawMessage;
import com.nomad.model.CommonClientModel;
import com.nomad.model.CommonClientModelImpl;
import com.nomad.model.ConnectModel;
import com.nomad.model.ConnectStatus;
import com.nomad.model.ServiceCommand;
import com.nomad.server.CacheServerConstants;
import com.nomad.server.CacheServerFactory;
import com.nomad.server.DataDefinitionService;
import com.nomad.server.ServerContext;
import com.nomad.server.service.ChildrenServerService;
import com.nomad.utility.MessageUtil;
import com.nomad.utility.pool.ObjectPoolImpl;

public class StoreConnectionPool extends ObjectPoolImpl<RawClientPooledInterface> implements ClientStatus {

    private static Logger LOGGER = LoggerFactory.getLogger(StoreConnectionPool.class);
    private final ConnectModel connect;
    private volatile ConnectStatus status;
    private ScheduledFuture<?> checkFuture = null;
    private ScheduledFuture<?>  characteristicFuture = null;
    private final  RawMessage testMessage;
    private static RawMessage characteristicMessage;
    private volatile  ChildrenServerService childService;
    private CommonClientModel clientModel =null;




    public StoreConnectionPool(final int threads, final ConnectModel connect, final int timeout, final ServerContext context, final String statisticName, final ChildrenServerService childService) {
        super(threads, timeout, timeout * 2, context, false, statisticName);
        statisticGroupName = CacheServerConstants.Statistic.STORE_CONNECTION_POOL_STATISTIC_GROUP_NAME;
        this.connect = connect;
        if (connect != null) {
            clientModel = new CommonClientModelImpl();
            clientModel.setHost(connect.getListener().getHost());
            clientModel.setPort(connect.getListener().getPort());
            clientModel.setProtocolType(connect.getListener().getProtocolType());
            clientModel.setThreads( connect.getListener().getMaxThreads());
            clientModel.setTimeout(timeout);
        }

        testMessage = getRawMessage();
        setStatus(ConnectStatus.OK);
        characteristicFuture = context.getScheduledExecutorService().scheduleAtFixedRate(new CharacteristicClientTest(this), 10, TimeUnit.SECONDS);
        this.childService=childService;
    }

    public List<String> getDataSources() {
        if (connect == null || connect.getDataSources() == null) {
            return Collections.emptyList();
        }
        return connect.getDataSources();
    }
    public boolean isLocal(){
        return clientModel==null;
    }
    private static RawMessage getRawMessage() {
        final MessageHeader header = new MessageHeader();
        header.setCommand(ServiceCommand.TEST.name());
        header.setVersion((byte) 0x0);
        return new RawMessageImpl(header, MessageUtil.getEmptyBody((byte) 0x0));

    }

    public DataDefinitionService getDataDefinitionService() throws SystemException {
        return context.getDataDefinitionService(null);
    }

    public void updateClient(final ConnectModel colleague) {
        poolSize = colleague.getThreads();
        if (colleague != null) {
            if (!clientModel.getHost().equals(colleague.getListener().getHost()) || clientModel.getPort() != colleague.getListener().getPort()) {
                clientModel.setHost( colleague.getListener().getHost());
                clientModel.setPort(colleague.getListener().getPort());
                clientModel.setProtocolType(colleague.getListener().getProtocolType());
                clientModel.setThreads( connect.getListener().getMaxThreads());
                final List<RawClientPooledInterface> oldPool = pool;
                pool = new Vector<>(poolSize);
                try {
                    init();
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage());
                }
                for (final RawClientPooledInterface pooledConnection : oldPool) {
                    pooledConnection.setShouldClose(true);
                }
            }
        }
    }

    @Override
    public String getPoolId() {
        if (connect == null) {
            return "localPool";
        }
        return clientModel.getHost() + ":" + clientModel.getPort();
    }

    public ConnectModel getConnectModel() {
        return connect;
    }

    @Override
    public RawClientPooledInterface getNewPooledObject() {
        RawClientPooledInterface connection = null;

        try {
            if (connect == null) { // mean local engine
                connection =  new LocalPooledEngine( context);
            } else {

                connection =  CacheServerFactory.getRawCacheClient(clientModel, context);
                LOGGER.info(" init result: {}", connection);
            }
            connection.setPool(this);
            return connection;
        } catch (final Exception e) {
            LOGGER.error("Error create connection:{} ", e.getMessage());
            setStatus(ConnectStatus.INACCESSIBLE);
            throw new RuntimeException();
        }

    }



    @Override
    protected String getInternalStatisticName() {
        if (clientModel == null) {
            return "local";
        }
        return clientModel.getHost() + ":" + clientModel.getPort();
    }

    @Override
    public ConnectStatus getStatus() {
        return status;
    }

    private boolean connectTest() {
        RawClientPooledInterface client = null;
        try {
            client = getObject();
            final RawMessage answer = client.sendRawMessage(testMessage);
            if (OperationStatus.OK.equals(answer.getResult().getOperationStatus())) {
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

    @Override
    public void setStatus(final ConnectStatus status) {
        LOGGER.info("Set status:" + status);
        if (ConnectStatus.OK.getCode() != status.getCode() && checkFuture == null) {
            checkFuture = context.getScheduledExecutorService().scheduleAtFixedRate(new CheckStatus(), 3, TimeUnit.SECONDS);
        } else if (ConnectStatus.OK.equals(status) && checkFuture != null) {
            context.getScheduledExecutorService().stop(checkFuture);
            checkFuture = null;
        }
        this.status = status;
    }

    private class CheckStatus implements Runnable {

        @Override
        public void run() {
            LOGGER.info("Schedule check status!:"+this.getClass().getName());

            if (connectTest()) {
                setStatus(ConnectStatus.OK);
            } else {
                setStatus(ConnectStatus.INACCESSIBLE);

            }
        }
    }

    @Override
    public void close() {
        super.close();
        if(checkFuture!=null){
            checkFuture.cancel(true);
        }
        if(characteristicFuture != null){
            characteristicFuture.cancel(true);
        };

    }
    private static RawMessage getCharacteristicMessage(){
        if(characteristicMessage==null){

            final MessageHeader header= new MessageHeader();
            header.setCommand(ServiceCommand.CHARACTERISTIC_TEST.toString());
            header.setVersion((byte)1);
            final byte[] body=MessageUtil.getEmptyBody((byte)1);
            characteristicMessage= new RawMessageImpl(header,body);
        }

        return characteristicMessage;
    }
    private class CharacteristicClientTest implements Runnable{
        private final StoreConnectionPool pool;
        CharacteristicClientTest (final StoreConnectionPool pool){
            this.pool=pool;
        }
        @Override
        public void run() {

            final RawClientPooledInterface client = getObject();
            try{
                final RawMessage answer = client.sendRawMessage(getCharacteristicMessage());
                if (OperationStatus.OK.equals(answer.getResult().getOperationStatus())) {
                    try{
                        final double busy= Double.parseDouble(answer.getHeader().getMainSession());
                        childService.updateRating(busy , pool);

                    }catch(final Exception e){
                        LOGGER.error(e.getMessage(),e);
                    }
                }else{
                    LOGGER.error("cannot conect to:"+clientModel.getHost()+":"+clientModel.getPort());
                    setStatus(ConnectStatus.ERROR);
                }
            }catch(final Exception e){
                LOGGER.error("cannot conect to:"+clientModel.getHost()+":"+clientModel.getPort());
                setStatus(ConnectStatus.ERROR);
            }finally{
                client.freeObject();
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((characteristicFuture == null) ? 0 : characteristicFuture
                        .hashCode());
        result = prime * result
                + ((checkFuture == null) ? 0 : checkFuture.hashCode());
        result = prime * result
                + ((childService == null) ? 0 : childService.hashCode());
        result = prime * result
                + ((clientModel == null) ? 0 : clientModel.hashCode());
        result = prime * result + ((connect == null) ? 0 : connect.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result
                + ((testMessage == null) ? 0 : testMessage.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StoreConnectionPool other = (StoreConnectionPool) obj;
        if (characteristicFuture == null) {
            if (other.characteristicFuture != null)
                return false;
        } else if (!characteristicFuture.equals(other.characteristicFuture))
            return false;
        if (checkFuture == null) {
            if (other.checkFuture != null)
                return false;
        } else if (!checkFuture.equals(other.checkFuture))
            return false;
        if (childService == null) {
            if (other.childService != null)
                return false;
        } else if (!childService.equals(other.childService))
            return false;
        if (clientModel == null) {
            if (other.clientModel != null)
                return false;
        } else if (!clientModel.equals(other.clientModel))
            return false;
        if (connect == null) {
            if (other.connect != null)
                return false;
        } else if (!connect.equals(other.connect))
            return false;
        if (status != other.status)
            return false;
        if (testMessage == null) {
            if (other.testMessage != null)
                return false;
        } else if (!testMessage.equals(other.testMessage))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "StoreConnectionPool [connect=" + connect + ", status=" + status
                + ", checkFuture=" + checkFuture + ", characteristicFuture="
                + characteristicFuture + ", testMessage=" + testMessage
                + ", childService=" + childService + ", clientModel="
                + clientModel + "]";
    }

}
