package com.nomad.cache.servermanager.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.servermanager.configuration.ManagerServerXMLParser;
import com.nomad.cache.servermanager.models.ProxyServerModel;
import com.nomad.cache.servermanager.models.SessionStorage;
import com.nomad.cache.servermanager.server.plugin.ProxyPluginClassPool;
import com.nomad.model.StatisticData;
import com.nomad.server.ProxyPlugin;
import com.nomad.server.ProxyServerContext;

public class ServerManagerLauncher {
  protected static Logger logger = LoggerFactory.getLogger(ServerManagerLauncher.class);
  private ManagerServer server;
  private ProxyServer proxyServer;
  private Timer sessionTimer;

  public void start() {

    try {
      ManagerServerXMLParser parser = new ManagerServerXMLParser();
      parser.parse();
      ProxyServerModel serverModel = parser.getManagerServerModel();
      start(serverModel);

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public void start(ProxyServerModel serverModel) {
    try {
      SessionStorage sessionStorage = new SessionStorage(serverModel.getSessionTimeout());
      StatisticData statistcData = new StatisticData();
      server = new ManagerServer(serverModel, sessionStorage, statistcData);
      logger.info("Start manager server");
      new Thread(server).start();
      logger.info("Start proxy server");
      
   
      ProxyServerContext context = new ProxyServerContextImpl();
      context.put(ProxyServerContext.ServiceName.ProxyServerModel.toString(), serverModel);
      context.put(ProxyServerContext.ServiceName.SessionStorage.toString(), sessionStorage);
      context.put(ProxyServerContext.ServiceName.StatisticData.toString(), statistcData);
      
      
      Map<String, ProxyPluginClassPool> plugins= loadPlugins(serverModel,context);
      context.put(ProxyServerContext.ServiceName.Plugins.toString(), plugins);

      
      proxyServer = new ProxyServer(context);
      new Thread(proxyServer).start();
      if (serverModel.getSessionTimeout() > 0) {
        sessionTimer = new Timer();
        sessionTimer.schedule(new SessionTimer(context), 0, 1000);
      }

    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public void stop() {
    logger.info("Stopping manager Server");
    if (server != null) {
      server.stop();
    }
    if (proxyServer != null) {
      proxyServer.stop();
    }
    if (sessionTimer != null) {
      sessionTimer.purge();
    }
  }

  private Map<String, ProxyPluginClassPool> loadPlugins(ProxyServerModel serverModel,ProxyServerContext context) {
    Map<String, ProxyPluginClassPool> result = new HashMap<String, ProxyPluginClassPool>();
    for (ProxyPlugin pluginModel : serverModel.getPlugins()) {
      ProxyPluginClassPool ppc = new ProxyPluginClassPool(pluginModel,context);
      List<String> commnds = ppc.getObject().getCommands();
      for (String command : commnds) {
        ProxyPluginClassPool exist = result.get(command);

        if (exist != null) {
          logger.error("plugin conflict! betwin {} and {} command:" + command, exist, ppc);
        } else {
          result.put(command, ppc);
        }

      }
    }

    return result;
  }

}
