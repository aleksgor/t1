package com.nomad.server.service.saveservice;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.SystemException;
import com.nomad.message.SaveCommand;
import com.nomad.message.SaveRequest;
import com.nomad.message.SaveResult;
import com.nomad.model.ConnectStatus;
import com.nomad.model.saveserver.SaveClientModel;
import com.nomad.server.CacheServerConstants;
import com.nomad.server.ServerContext;
import com.nomad.server.service.common.NetworkConnectionPoolImpl;
import com.nomad.server.service.saveservice.model.SaveRequestImpl;

public class SaveClientPool extends NetworkConnectionPoolImpl<SaveResult, SaveRequest> {

    private static Logger LOGGER = LoggerFactory.getLogger(SaveClientPool.class);

    public SaveClientPool( final SaveClientModel client, final ServerContext context) throws SystemException {
        super(client, context,false, CacheServerConstants.Statistic.SAVE_SERVICE_GROUP_NAME);
        statisticGroupName = CacheServerConstants.Statistic.SAVE_SERVICE_GROUP_NAME;
        status=ConnectStatus.OK;
    }

    @Override
    public String getPoolId() {
        return "Save Service:" + client.getHost() + ":" + client.getPort();
    }


    @Override
    public SaveServiceClient getNewPooledObject() {
        SaveServiceClient connection = null;
        try {
            connection = new SaveServiceClient(client,context);
            LOGGER.info("Init result:" + connection);
            status=ConnectStatus.OK;
            connection.setPool(this);
            return connection;
        } catch (final Exception e) {
            LOGGER.error("Error create connection:", e);
            status=ConnectStatus.INACCESSIBLE;
            throw new RuntimeException();
        }

    }

    @Override
    protected String getInternalStatisticName(){
        String intName="local";
        if(client.getHost()!=null){
            intName=client.getHost() + "_" + client.getPort();
        }
        return intName;
    }


    @Override
    public String toString() {
        return "SaveClientPool [host=" + client.getHost() + ", port=" + client.getPort() + ", status=" + status + "]";
    }

    @Override
    public boolean connectTest() {
        SaveServiceClient client=null;
        try{
            final SaveRequest message= new SaveRequestImpl(SaveCommand.TEST,0);
            client = (SaveServiceClient) getObject();
            final SaveResult answer=client.sendMessage(message);
            if(answer.getResultCode()==0){
                return true;
            }
        }catch(final Exception e){

        }finally{
            if(client != null){
                client.freeObject();
            }
        }
        return false;
    }

}
