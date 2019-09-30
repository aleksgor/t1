package com.nomad.cache.servermanager.server.plugin;

import com.nomad.server.ProxyCommandPlugin;
import com.nomad.utile.pool.PooledObjectImpl;

public abstract class CommonProxyCommandPlugin extends PooledObjectImpl implements ProxyCommandPlugin{
	
	
  @Override
  public void closeObject() {
    
  }

}
