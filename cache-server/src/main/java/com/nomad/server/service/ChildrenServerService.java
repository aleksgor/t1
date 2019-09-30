package com.nomad.server.service;

import java.util.Collection;

import com.nomad.exception.SystemException;
import com.nomad.model.ConnectModel;
import com.nomad.model.Identifier;
import com.nomad.server.ServiceInterface;
import com.nomad.server.service.childserver.StoreConnectionPool;

public interface ChildrenServerService extends ServiceInterface{

    Collection<StoreConnectionPool> getCacheConnectionsPools(String modelName);

    Collection<StoreConnectionPool> getUniqueStoreCacheConnectionPools(String modelName);

    Collection<String> getConnectionsPoolsIds(Identifier id) ;

    void registerClient(ConnectModel colleague) throws SystemException;

    StoreConnectionPool getLocalServer();

    int getStoriesCount(Identifier id) ;

    Collection<String> fillPoolsIds(Identifier id, Collection<String> input);

    StoreConnectionPool getStoreConnectionPool(String serverName);

    void updateRating(final double rating, final StoreConnectionPool pool);

    Collection<String> getFullConnectionsPoolsIds(Identifier id);


}
