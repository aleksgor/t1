package com.nomad.communication.binders;

import com.nomad.client.ClientInterface;
import com.nomad.client.ClientPooledInterface;
import com.nomad.client.RawClientInterface;
import com.nomad.communication.MessageExecutorFactory;
import com.nomad.communication.NetworkServer;
import com.nomad.communication.tcp.client.SSLClient;
import com.nomad.communication.tcp.client.TcpClient;
import com.nomad.communication.tcp.server.SSLServer;
import com.nomad.communication.tcp.server.TcpServer;
import com.nomad.communication.udp.client.UdpClient;
import com.nomad.communication.udp.server.UdpServer;
import com.nomad.exception.SystemException;
import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;
import com.nomad.model.CommonClientModel;
import com.nomad.model.CommonServerModel;
import com.nomad.model.server.ProtocolType;
import com.nomad.server.ServerContext;
import com.nomad.utility.SimpleServerContext;

public class ServerFactory {

    public static <T extends CommonMessage, K extends CommonAnswer> NetworkServer getServer(final CommonServerModel serverModel, final ServerContext context,
            final String serverType, final MessageExecutorFactory<T, K> workerFactory) throws SystemException {
        if (ProtocolType.TCP.equals(serverModel.getProtocolType())) {
            return new TcpServer<T, K>(serverModel, context, serverType, workerFactory);
        }
        if (ProtocolType.UDP.equals(serverModel.getProtocolType())) {
            return new UdpServer<T, K>(serverModel, context, serverType, workerFactory);
        }
        if (ProtocolType.SSL.equals(serverModel.getProtocolType())) {
            return new SSLServer<T, K>(serverModel, context, serverType, workerFactory);
        }
        return null;
    }

    public static <K extends CommonMessage, T extends CommonAnswer> ClientPooledInterface<K, T> getPooledClient(final CommonClientModel clientModel, final ServerContext context) throws SystemException  {
        if (ProtocolType.TCP.equals(clientModel.getProtocolType())) {
            return new PooledClient<K, T>(clientModel, context, new TcpClient<K, T>(clientModel, context));
        }
        if (ProtocolType.UDP.equals(clientModel.getProtocolType())) {
            return new PooledClient<K, T>(clientModel, context, new UdpClient<K, T>(clientModel, context));
        }
        if (ProtocolType.SSL.equals(clientModel.getProtocolType())) {
            return new PooledClient<K, T>(clientModel, context, new SSLClient<K, T>(clientModel, context));
        }
        return null;
    }

    public static <K extends CommonMessage, T extends CommonAnswer>   ClientInterface<K, T> getSingleThreadClient(final CommonClientModel clientModel, ServerContext context) throws SystemException {
        if(context==null){
            context= new SimpleServerContext();
        }
        if (ProtocolType.TCP.equals(clientModel.getProtocolType())) {
            return new TcpClient<K, T>(clientModel, context);
        }
        if (ProtocolType.UDP.equals(clientModel.getProtocolType())) {
            return new  UdpClient<K, T>(clientModel, context);
        }
        if (ProtocolType.SSL.equals(clientModel.getProtocolType())) {
            return new SSLClient<K, T>(clientModel, context);
        }
        return null;
    }

    public static   RawClientInterface getRawSingleThreadClient(final CommonClientModel clientModel, ServerContext context)throws SystemException{
        if(context==null){
            context= new SimpleServerContext();
        }
        if (ProtocolType.TCP.equals(clientModel.getProtocolType())) {
            return new TcpClient<CommonMessage, CommonAnswer>(clientModel, context);
        }
        if (ProtocolType.UDP.equals(clientModel.getProtocolType())) {
            return new UdpClient<CommonMessage, CommonAnswer>(clientModel, context);
        }
        if (ProtocolType.SSL.equals(clientModel.getProtocolType())) {
            return new SSLClient<CommonMessage, CommonAnswer>(clientModel, context);
        }
        return null;
    }

}
