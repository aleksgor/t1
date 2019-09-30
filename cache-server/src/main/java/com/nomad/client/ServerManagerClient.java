package com.nomad.client;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonclientserver.ManagementMessageImpl;
import com.nomad.exception.SystemException;
import com.nomad.message.ManagementMessage;
import com.nomad.message.OperationStatus;
import com.nomad.model.ConnectModel;
import com.nomad.model.ConnectModelImpl;
import com.nomad.model.ConnectStatus;
import com.nomad.model.DataSourceModel;
import com.nomad.model.ManagerCommand;
import com.nomad.model.ServerModel;
import com.nomad.server.ServerContext;
import com.nomad.server.ServerContext.ServiceName;
import com.nomad.server.service.ChildrenServerService;
import com.nomad.server.service.ManagementService;

public class ServerManagerClient {

    protected static Logger LOGGER = LoggerFactory.getLogger(ServerManagerClient.class);
    private volatile ServerContext context;
    public ServerManagerClient(final ServerContext context){
        this.context=context;
    }


    private void setDataSources(ConnectModel colleague, final ServerModel server) {
        colleague.setDataSources(new ArrayList<String>(server.getDataSources().size()));
        for (DataSourceModel dsm : server.getDataSources()) {
            colleague.getDataSources().add(dsm.getName());
        }
        colleague.getStoreModels().clear();
        colleague.getStoreModels().addAll(server.getStoreModels());
    }
    public void registerServerAndClient(final ServerModel server) throws SystemException  {
        LOGGER.info("Register registerServerAndClient :{} ", server);

        final ManagementService managementService = (ManagementService) context.get(ServiceName.MANAGEMENT_SERVICE);
        for (ConnectModel colleague : server.getServers()) {
            setDataSources(colleague, server);
            try {
                ClientPooledInterface<ManagementMessage, ManagementMessage> client=null;
                try{
                    final ManagementMessage managementMessage = new ManagementMessageImpl(ManagerCommand.REGISTER_CLIENT.toString(), colleague);
                    client = managementService.getClientPool(colleague.getManagementServer()).getClient();
                    final ManagementMessage answer = client.sendMessage(managementMessage);

                    if (OperationStatus.OK.equals(answer.getResult().getOperationStatus())) {
                        colleague.setStatus(ConnectStatus.OK);
                        final ChildrenServerService childrenServerService = (ChildrenServerService) context.get(ServiceName.CHILDREN_SERVICE);
                        childrenServerService.registerClient(colleague);

                    } else {
                        colleague.setStatus(ConnectStatus.INACCESSIBLE);
                    }
                    LOGGER.info("Register: "+colleague+" result:"+answer.getResult());
                }finally{
                    if(client!=null){
                        client.freeObject();
                    }
                }

            } catch (final SystemException x) {
                LOGGER.warn(server.getServerName()+" Error  access to " + colleague.getManagementServer().getHost()+" port:"+ colleague.getManagementServer().getPort(),x);
                colleague.setStatus(ConnectStatus.INACCESSIBLE);
                throw x;

            }
        }
        //clients
        for (ConnectModel colleague : server.getClients()) {
            setDataSources(colleague, server);
            ClientPooledInterface<ManagementMessage, ManagementMessage> client=null;
            try {
                final ManagementMessage managementMessage = new ManagementMessageImpl(ManagerCommand.REGISTER_SERVER.toString(), colleague);
                if(colleague.getManagementClient()!=null){
                    client = managementService.getClientPool(colleague.getManagementClient()).getClient();
                    final ManagementMessage answer = client.sendMessage(managementMessage);
                    colleague=(ConnectModelImpl)answer.getData();

                    if (OperationStatus.OK.equals(answer.getResult().getOperationStatus()) && colleague!=null) {
                        colleague.setStatus(ConnectStatus.OK);
                    } else {
                        colleague.setStatus(ConnectStatus.INACCESSIBLE);
                    }

                    LOGGER.info("register "+answer.getResult()+":"+colleague);
                }
            } catch (final Throwable x) {
                LOGGER.warn(server.getServerName()+ " Error access to " + colleague.getManagementClient().getHost()+" port:" + colleague.getManagementClient().getPort(),x);
                colleague.setStatus(ConnectStatus.INACCESSIBLE);

                // throw x;
            } finally {
                if(client!=null){
                    client.freeObject();
                }
            }
        }
    }

}
