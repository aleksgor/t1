package com.nomad.cache.servermanager.server;

import java.util.HashMap;

import com.nomad.server.ProxyServerContext;

@SuppressWarnings("serial")
public class ProxyServerContextImpl extends HashMap<String,Object> implements ProxyServerContext {

  @Override
  public Object get(String contextName) {
   
    return super.get(contextName);
  }

  @Override
  public Object remove(String contextName) {
    
    return super.remove(contextName);
  }

  
}
