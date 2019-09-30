package com.nomad.plugin;

import java.beans.PropertyDescriptor;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonclientserver.BodyImpl;
import com.nomad.cache.commonclientserver.FullMessageImpl;
import com.nomad.cache.commonclientserver.ResultImpl;
import com.nomad.exception.SystemException;
import com.nomad.message.FullMessage;
import com.nomad.message.OperationStatus;
import com.nomad.model.Field;
import com.nomad.model.Identifier;
import com.nomad.model.ModelDescription;
import com.nomad.server.CommandPlugin;
import com.nomad.server.CommonCommandPlugin;
import com.nomad.server.DataDefinitionService;
import com.nomad.server.ServerContext;
import com.nomad.utility.DataInvokerPool;
import com.nomad.utility.MessageUtil;
import com.nomad.utility.PooledDataInvoker;

public class IdGenerator extends CommonCommandPlugin implements CommandPlugin {
    private final static List<String> commands = new ArrayList<>();
    public static final String dataSourceName = "DataSourceName";
    private volatile static DataInvokerPool dataInvokerPool;
    private volatile ServerContext context;
    private Properties property;
    private DataDefinitionService dataDefinition;
    static {
        commands.add("getId");
    }

    private static Logger LOGGER = LoggerFactory.getLogger(IdGenerator.class);

    @Override
    public void init(ServerContext context, Properties property) throws SystemException {
        this.context = context;
        this.property = property;
        dataDefinition = context.getDataDefinitionService(null);
    }

    @Override
    public List<String> getCommands() {
        return commands;
    }

    private PooledDataInvoker getPooledDataInvoker() throws SystemException {
        if (dataInvokerPool == null) {
            dataInvokerPool = context.getDataInvoker(property.getProperty(dataSourceName));
            if (dataInvokerPool == null) {
                throw new SystemException("Dsata source " + property.get(dataSourceName) + " not present in " + context);
            }

        }
        return dataInvokerPool.getObject();
    }

    @Override
    public FullMessage executeMessage(FullMessage message) throws SystemException {
        LOGGER.debug("extcute message:{}", message);
        PooledDataInvoker dataInvoker = getPooledDataInvoker();
        try {
            ModelDescription description = dataDefinition.getModelDescription(message.getHeader().getModelName());
            if(description==null){
                throw new SystemException("model: "+message.getHeader().getModelName()+" does not supported!");
            }
            BigInteger nextKey = dataInvoker.getNextKey(message.getHeader().getModelName(),1);
            @SuppressWarnings("unchecked")
            Class<Identifier> identifierClass = (Class<Identifier>) Class.forName(description.getClassId());
            Identifier result = identifierClass.newInstance();
            for (Field field : description.getPrimaryKeyFields()) {
                try{
                    new PropertyDescriptor(field.getName(), result.getClass()).getWriteMethod().invoke(result, nextKey.longValue());
                }catch (Exception e){
                    LOGGER.error(e.getMessage());
                }
            }

            return new FullMessageImpl(message.getHeader(), new BodyImpl(MessageUtil.getStatisticResult(Arrays.asList(result))), new ResultImpl(OperationStatus.OK));
        } catch (Exception e) {

            return new FullMessageImpl(message.getHeader(), null, new ResultImpl(OperationStatus.ERROR,e.getMessage()));

        } finally {
            if (dataInvoker != null) {
                dataInvoker.freeObject();
            }
        }
    }

    @Override
    public void close() {

    }

    @Override
    protected long getSize() {
        return 0;
    }

}
