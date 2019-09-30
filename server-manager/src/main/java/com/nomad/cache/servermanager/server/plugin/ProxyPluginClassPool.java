package com.nomad.cache.servermanager.server.plugin;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.SysException;
import com.nomad.server.ProxyPlugin;
import com.nomad.server.ProxyServerContext;
import com.nomad.utile.pool.ObjectPool;

public class ProxyPluginClassPool extends ObjectPool<CommonProxyCommandPlugin> {
  private static Logger logger = LoggerFactory.getLogger(ProxyPluginClassPool.class);
  private ProxyPlugin  plugin;
  
  private ProxyServerContext context;

  public ProxyPluginClassPool(ProxyPlugin  plugin, ProxyServerContext context) {
    
    super(plugin.getPoolSize(), plugin.getTimeOut(), plugin.getChechDelay());
    this.plugin=plugin;
    this.context = context;
  }

  @Override
  protected CommonProxyCommandPlugin getNewPooledObject() {
    try {
      CommonProxyCommandPlugin result = getDataInvokerInstance();
      result.init(plugin.getProperties(),context);
      return result;
    } catch (SysException e) {
      logger.error(e.getMessage(), e);
    }

    return null;
  }

  private CommonProxyCommandPlugin getDataInvokerInstance() throws SysException {
    try {
      logger.info("DataInvoker load:{}", plugin);
      Class<?> cldi = Class.forName(plugin.getClazz());
      CommonProxyCommandPlugin result = (CommonProxyCommandPlugin) cldi.newInstance();
      return result;
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      throw new SysException("Cannot found:" + plugin.getClazz());
    } catch (InstantiationException e) {
      e.printStackTrace();
      throw new SysException("Cannot found:" + plugin.getClazz());
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      throw new SysException("Cannot found:" + plugin.getClazz());
    }

  }

}
