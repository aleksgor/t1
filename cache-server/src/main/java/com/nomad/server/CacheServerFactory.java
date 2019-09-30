package com.nomad.server;

import com.nomad.client.ClientPooledInterface;
import com.nomad.client.RawClientPooledInterface;
import com.nomad.communication.binders.PooledClient;
import com.nomad.communication.binders.RawPooledClient;
import com.nomad.communication.tcp.client.TcpClient;
import com.nomad.communication.udp.UDPCacheServer;
import com.nomad.communication.udp.client.UdpClient;
import com.nomad.exception.SystemException;
import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;
import com.nomad.model.CommonClientModel;
import com.nomad.model.ListenerModel;
import com.nomad.model.server.ProtocolType;

public class CacheServerFactory {
    public static ServerListener getServer(final ListenerModel serverModel) {
        switch (serverModel.getProtocolType()) {
        case TCP:
            return new TCPListener();
        case UDP:
            return new UDPCacheServer();
        case SSL:
            return new SSLTcpListener();
        }
        return null;
    }

    public static <K extends CommonMessage, T extends CommonAnswer> ClientPooledInterface<K, T> getCacheClient(final CommonClientModel clientModel,
            final ServerContext context) throws SystemException {
        if (ProtocolType.TCP.equals(clientModel.getProtocolType())) {

            return new PooledClient<K, T>(clientModel, context, new TcpClient<K, T>(clientModel, context));
        }
        if (ProtocolType.UDP.equals(clientModel.getProtocolType())) {
            try {
                return new PooledClient<K, T>(clientModel, context, new UdpClient<K, T>(clientModel, context));
            } catch (final Exception e) {
                return null;
            }
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static RawClientPooledInterface getRawCacheClient(final CommonClientModel clientModel, final ServerContext context) throws SystemException {
        if (ProtocolType.TCP.equals(clientModel.getProtocolType())) {
            return new RawPooledClient(clientModel, context, new TcpClient(clientModel, context));
        }
        if (ProtocolType.UDP.equals(clientModel.getProtocolType())) {
            return new RawPooledClient(clientModel, context, new UdpClient(clientModel, context));
        }
        return null;
    }
}
