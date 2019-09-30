package com.nomad.cache.servermanager.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonClientServer.FullMessageImpl;
import com.nomad.cache.commonClientServer.MessageImpl;
import com.nomad.cache.servermanager.models.ProxyServerModel;
import com.nomad.cache.servermanager.models.SessionStorage;
import com.nomad.cache.servermanager.server.connectpool.StoreConnectionPool;
import com.nomad.cache.servermanager.server.plugin.CommonProxyCommandPlugin;
import com.nomad.cache.servermanager.server.plugin.ProxyPluginClassPool;
import com.nomad.model.BaseCommand;
import com.nomad.model.Model;
import com.nomad.message.FullMessage;
import com.nomad.message.ModelMessage;
import com.nomad.message.MessageAssembler;
import com.nomad.message.MessageAssemblerFactory;
import com.nomad.message.MessageHeader;
import com.nomad.message.ModelMessage.Result;
import com.nomad.server.ProxyServerContext;

public class ManagerImpl {
  private volatile SessionStorage storage;
  private static Logger logger = LoggerFactory.getLogger(ManagerImpl.class);
  private ProxyServerModel managerServerModel;
  private ExecutorService executor;
  private volatile Map<String, ProxyPluginClassPool> plugins;

  @SuppressWarnings("unchecked")
  public ManagerImpl(ProxyServerContext context) {
    super();
    
     storage=(SessionStorage)context.get(ProxyServerContext.ServiceName.SessionStorage.toString());
     managerServerModel=(ProxyServerModel)context.get(ProxyServerContext.ServiceName.ProxyServerModel.toString());
     plugins=( Map<String, ProxyPluginClassPool>)context.get(ProxyServerContext.ServiceName.Plugins.toString());
     logger.info("managerServerModel size:"+(managerServerModel.getConnectPool().size()));
     executor = Executors.newFixedThreadPool(managerServerModel.getProxyThreads());

    
  }

  private MessageHeader copyHeader(MessageHeader header) {
    MessageHeader result = new MessageHeader();
    result.setCommand(header.getCommand());
    result.setModelId(header.getModelId());
    result.setModelName(header.getModelName());
    result.setSessionId(header.getSessionId());
    result.setVersion(header.getVersion());
    return result;
  }

  public FullMessage sendMessage(MessageHeader header, InputStream input) throws Exception {
    logger.debug("header:"+header);
    if (header.getSessionId() == null && !BaseCommand.StartNewSession.equals(header.getSessionId())) {
      header.setSessionId(UUID.randomUUID().toString());
      storage.setSession(header.getSessionId());
      MessageHeader startnewSession = copyHeader(header);
      startnewSession.setCommand(BaseCommand.StartNewSession);
      sendBroadcastMessage(startnewSession, new MessageImpl(null));

    }
    if (header.getSessionId() != null) {
      if (!storage.checkseSsion(header.getSessionId())) {
        logger.error("invalid session:" + header.getSessionId());

        return new FullMessageImpl(header, new MessageImpl(null, Result.InvalidSession));
      } else {
        storage.updateSession(header.getSessionId());
      }
    }
    String command = header.getCommand();
    if (plugins != null || plugins.get(command) != null) {
      ProxyPluginClassPool pluginPool = plugins.get(command);
      if (pluginPool != null) {
        CommonProxyCommandPlugin plugin = pluginPool.getObject();
        try {
          return plugin.executeMessage(header, input);
        } finally {
          plugin.freeObject();
        }
      }
    }

    final MessageAssembler assembler = MessageAssemblerFactory.getMessageAssembler(header.getVersion());
    ModelMessage inMessage =(ModelMessage) assembler.getObject(input);
    logger.debug("header:"+header+"message:"+inMessage);
    if (BaseCommand.CloseSession.equals(header.getCommand())) {
      storage.removeSession(header.getSessionId());
      return sendBroadcastMessage(header, inMessage);
    } else if (BaseCommand.Commit.equals(header.getCommand())) {
      return sendBroadcastMessage(header, inMessage);
    } else if (BaseCommand.Rollback.equals(header.getCommand())) {
      return sendBroadcastMessage(header, inMessage);
    } else if (BaseCommand.Delete.equals(header.getCommand())) {
      return sendBroadcastMessage(header, inMessage);
    } else if (BaseCommand.Get.equals(header.getCommand())) {
      return get(header, inMessage);
    } else if (BaseCommand.Put.equals(header.getCommand())) {
      return put(header, inMessage);
    } else if (BaseCommand.Prepare.equals(header.getCommand())) {
      return sendBroadcastMessage(header, inMessage);
    } else if (BaseCommand.StartNewSession.equals(header.getCommand())) {

      header.setSessionId(UUID.randomUUID().toString());
      storage.setSession(header.getSessionId());

      return sendBroadcastMessage(header, new MessageImpl(null));
    } else if (BaseCommand.Contains.equals(header.getCommand())) {
      return getModelFromAnyServer(header, inMessage).getMessage();
    } else if (BaseCommand.CloseSessions.equals(header.getCommand())) {
      return closeSessions(header, input);
    }else{
      return new FullMessageImpl(header, new MessageImpl(null, Result.InvalidOperationName));
    }


  }
 
  private FullMessage closeSessions(MessageHeader header, InputStream input) throws Exception {

    final MessageAssembler assembler = MessageAssemblerFactory.getMessageAssembler(header.getVersion());
    ModelMessage message =(ModelMessage) assembler.getObject(input);
    @SuppressWarnings("unchecked")
    List<String> sessions = (List<String>) message.getData();
    storage.removeSession(sessions);
    return sendBroadcastMessage(header, message);
  }

  private FullMessage get(MessageHeader header, ModelMessage inMessage) throws InterruptedException, IOException {

    MessageAnswer serverWithModel = getModelFromAnyServer(header, inMessage);
    StoreConnectionPool srv = null;
    if (serverWithModel == null) {
      srv = managerServerModel.getBestServer(header.getModelName());
      if (srv != null) {
        FullMessage result = new MessageRequest(header, inMessage, srv).executeMessage();
        return result;
      } else {
        return new FullMessageImpl(header, new MessageImpl(null, Result.InvalidModelName));
      }
    } else {
      return serverWithModel.getMessage();
    }

  }

  private FullMessage put(MessageHeader header, ModelMessage inMessage) throws InterruptedException, IOException {

    
    Model model=(Model)inMessage.getData();
    MessageAnswer serverWithModel = getModelFromAnyServer(header, new MessageImpl(model.getIdentifier()));

    
    StoreConnectionPool srv = null;
    logger.debug("serverWithModel:" + serverWithModel);
    if (serverWithModel == null) {
      srv = managerServerModel.getBestServer(header.getModelName());
      if (srv == null) {
        return new FullMessageImpl(header, new MessageImpl(null, Result.InvalidModelName));
      }
    } else {
      srv = serverWithModel.getPool();
    }
    logger.debug("srv:" + srv);
    FullMessage result = new MessageRequest(header, inMessage, srv).executeMessage();

    return result;
  }

  /**
   * input contains objectId
   * 
   * @param header
   * @param input
   * @return
   * @throws InterruptedException
   * @throws IOException
   */
  private MessageAnswer getModelFromAnyServer(MessageHeader header, ModelMessage inMessaget) throws InterruptedException, IOException {
    List<StoreConnectionPool> servers = getChildrenServer(header);
    if (servers == null) {
      logger.error("Model: {} does not supported", header);
      return null;
    }

    Collection<MessageRequest> requests = new ArrayList<MessageRequest>(servers.size());

    MessageHeader newHeader = new MessageHeader();
    newHeader.setCommand(BaseCommand.Contains);
    newHeader.setModelId(header.getModelId());
    newHeader.setModelName(header.getModelName());
    newHeader.setSessionId(header.getSessionId());
    newHeader.setVersion(header.getVersion());
    
    
    for (StoreConnectionPool srv : servers) {
      requests.add(new MessageRequest(newHeader, inMessaget, srv));
    }
    List<Future<MessageAnswer>> resultList = executor.invokeAll(requests, managerServerModel.getRequestTimeout(), TimeUnit.SECONDS);

    for (Future<MessageAnswer> fmsg : resultList) {
      try {
        MessageAnswer ms = fmsg.get();
        if (Result.OK.equals(ms.getMessage().getMessage().getResult()) && ms.getMessage().getMessage().getData() != null) {
          return ms;
        }
      } catch (ExecutionException e) {
        logger.error("error exec:" + servers + ":" + executor + ":" + e);
      } catch (Exception e) {
        logger.error("error exec:" + servers + ":" + executor + ":" + e);
      }
    }

    return null;
  }

  public FullMessage sendBroadcastMessage(MessageHeader header, ModelMessage message) throws Exception {

    // read message

    List<StoreConnectionPool> pools = getChildrenServer(header);

    if (pools == null) {
      logger.error("Model: {} does not supported", header);
      return new FullMessageImpl(header, new MessageImpl(null, Result.Error));
    }

    Collection<MessageRequest> requests = new ArrayList<MessageRequest>(pools.size());

    for (StoreConnectionPool pool : pools) {
      requests.add(new MessageRequest(header, message, pool));

    }
    List<Future<MessageAnswer>> resultList = executor.invokeAll(requests, managerServerModel.getRequestTimeout(), TimeUnit.SECONDS);
    Result res = Result.OK;
    for (Future<MessageAnswer> fmsg : resultList) {
      try {
        FullMessage ms = fmsg.get().getMessage();
        if (!Result.OK.equals(ms.getMessage().getResult()) && Result.OK.equals(res)) {
          res = ms.getMessage().getResult();
        }
      } catch (ExecutionException e) {
        res = Result.Error;
        logger.error("error exec:" + e);
      }
    }

    FullMessage result = new FullMessageImpl(header, new MessageImpl(null, res));
    logger.debug("sendBroadcastMessage:"+result);
    return result;
  }

  private List<StoreConnectionPool> getChildrenServer(MessageHeader header) {
    String id = header.getModelId();
    List<StoreConnectionPool> pools = null;
    if (id == null || id.length() == 0) {
      pools = managerServerModel.getConnectPool();
    } else {
      pools = managerServerModel.getPoolForModel(id);
    }
    return pools;
  }

}
