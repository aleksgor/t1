package com.nomad.io.serializer;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonclientserver.BodyImpl;
import com.nomad.cache.commonclientserver.BodyImplSerializer;
import com.nomad.cache.commonclientserver.FullMessageImpl;
import com.nomad.cache.commonclientserver.FullMessageImplSerializer;
import com.nomad.cache.commonclientserver.ManagementMessageImpl;
import com.nomad.cache.commonclientserver.ManagementMessageSerializer;
import com.nomad.cache.commonclientserver.MessageHeaderSerializer;
import com.nomad.cache.commonclientserver.RequestImpl;
import com.nomad.cache.commonclientserver.RequestImplSerializer;
import com.nomad.cache.commonclientserver.ResultImpl;
import com.nomad.cache.commonclientserver.ResultSerializer;
import com.nomad.cache.commonclientserver.idgenerator.BigIntegerSerializer;
import com.nomad.cache.commonclientserver.idgenerator.IdGeneratorMessageImpl;
import com.nomad.cache.commonclientserver.idgenerator.IdGeneratorMessageSerializer;
import com.nomad.cache.commonclientserver.session.SessionAnswerImpl;
import com.nomad.cache.commonclientserver.session.SessionDataSerializer;
import com.nomad.cache.commonclientserver.session.SessionMessageAnswerSerializer;
import com.nomad.cache.commonclientserver.session.SessionMessageImpl;
import com.nomad.cache.commonclientserver.session.SessionMessageSerializer;
import com.nomad.cache.commonclientserver.update.UpdateItemSerializer;
import com.nomad.cache.commonclientserver.update.UpdateRequestSerializer;
import com.nomad.message.MessageHeader;
import com.nomad.model.CacheMatcherModelImpl;
import com.nomad.model.CacheMatcherSerializer;
import com.nomad.model.CommandPluginModelImpl;
import com.nomad.model.CommandPluginModelSerializer;
import com.nomad.model.CommonClientModelImpl;
import com.nomad.model.CommonClientModelSerializer;
import com.nomad.model.CommonServerModelImpl;
import com.nomad.model.CommonServerModelSerializer;
import com.nomad.model.ConnectModelImpl;
import com.nomad.model.ConnectModelSerializer;
import com.nomad.model.DataSourceModelImpl;
import com.nomad.model.DataSourceModelSerializer;
import com.nomad.model.ListenerModelImpl;
import com.nomad.model.ListenerModelSerializer;
import com.nomad.model.MemoryInfo;
import com.nomad.model.MemoryInfoSerializer;
import com.nomad.model.ModelSource;
import com.nomad.model.ModelSourceSerializer;
import com.nomad.model.SaveClientModelImpl;
import com.nomad.model.SaveClientModelSerializer;
import com.nomad.model.SaveServerModelImpl;
import com.nomad.model.SaveServerModelSerializer;
import com.nomad.model.SerializerSerializer;
import com.nomad.model.ServerModelImpl;
import com.nomad.model.ServerModelSerializer;
import com.nomad.model.ServiceIdentifier;
import com.nomad.model.ServiceIdentifierSerializer;
import com.nomad.model.SessionCallBackServerModelImp;
import com.nomad.model.SessionCallBackServerModelSerializer;
import com.nomad.model.StoreModelImpl;
import com.nomad.model.StoreModelSerializer;
import com.nomad.model.block.BlockClientModelImpl;
import com.nomad.model.block.BlockClientModelSerializer;
import com.nomad.model.block.BlockServerModelImpl;
import com.nomad.model.block.BlockServerModelSerializer;
import com.nomad.model.criteria.AbstractCriteria;
import com.nomad.model.criteria.AbstractCriteriaSerializer;
import com.nomad.model.criteria.CriteriaGroupItem;
import com.nomad.model.criteria.CriteriaGroupItemSerializer;
import com.nomad.model.criteria.CriteriaItemImpl;
import com.nomad.model.criteria.CriteriaItemImplSerializer;
import com.nomad.model.criteria.SortFieldImpl;
import com.nomad.model.criteria.SortFieldSerializer;
import com.nomad.model.criteria.StatisticElementImpl;
import com.nomad.model.criteria.StatisticElementSerializer;
import com.nomad.model.criteria.StatisticResultImpl;
import com.nomad.model.criteria.StatisticResultImplSerializer;
import com.nomad.model.idgenerator.IdGeneratorClientModelImpl;
import com.nomad.model.idgenerator.IdGeneratorClientModelSerializer;
import com.nomad.model.idgenerator.IdGeneratorServerModelImpl;
import com.nomad.model.idgenerator.IdGeneratorServerModelSerializer;
import com.nomad.model.management.ManagementClientModelImpl;
import com.nomad.model.management.ManagementClientModelSerializer;
import com.nomad.model.management.ManagementServerModelImpl;
import com.nomad.model.management.ManagementServerModelSerializer;
import com.nomad.model.session.SessionClientModelImpl;
import com.nomad.model.session.SessionClientModelSerializer;
import com.nomad.model.session.SessionDataImpl;
import com.nomad.model.session.SessionServerModelImp;
import com.nomad.model.session.SessionServerModelSerializer;
import com.nomad.model.update.UpdateItemImpl;
import com.nomad.model.update.UpdateRequestImpl;
import com.nomad.serializer.Serializer;
import com.nomad.session.SessionStateImpl;
import com.nomad.session.SessionStateImplSerializer;

public class SerializerFactory {
    private static Logger LOGGER = LoggerFactory.getLogger(SerializerFactory.class);

    private static Map<String, SerializerPool> serializerMap = new HashMap<>();
    static{
        registerSerializer(ListenerModelImpl.class,  ListenerModelSerializer.class);
        registerSerializer(ServerModelImpl.class, ServerModelSerializer.class);
        registerSerializer(StoreModelImpl.class, StoreModelSerializer.class);
        registerSerializer(FullMessageImpl.class, FullMessageImplSerializer.class);
        registerSerializer(MessageHeader.class, MessageHeaderSerializer.class);
        // session{
        registerSerializer(SessionMessageImpl.class, SessionMessageSerializer.class);
        registerSerializer(SessionAnswerImpl.class, SessionMessageAnswerSerializer.class);
        registerSerializer(SessionDataImpl.class, SessionDataSerializer.class);
        registerSerializer(SessionStateImpl.class, SessionStateImplSerializer.class);
        registerSerializer(SessionAnswerImpl.class, SessionMessageAnswerSerializer.class);

        //}
        registerSerializer(ServiceIdentifier.class, ServiceIdentifierSerializer.class);

        registerSerializer(ConnectModelImpl.class, ConnectModelSerializer.class);
        registerSerializer(DataSourceModelImpl.class, DataSourceModelSerializer.class);
        registerSerializer(MemoryInfo.class, MemoryInfoSerializer.class);
        registerSerializer(ModelSource.class, ModelSourceSerializer.class);
        registerSerializer(ResultImpl.class, ResultSerializer.class);
        registerSerializer(CommandPluginModelImpl.class, CommandPluginModelSerializer.class);



        registerSerializer(BodyImpl.class, BodyImplSerializer.class);
        registerSerializer(StatisticResultImpl.class, StatisticResultImplSerializer.class);
        registerSerializer(RequestImpl.class, RequestImplSerializer.class);
        // save server
        registerSerializer(SaveClientModelImpl.class, SaveClientModelSerializer.class);
        registerSerializer(SaveServerModelImpl.class, SaveServerModelSerializer.class);


        registerSerializer(SessionServerModelImp.class, SessionServerModelSerializer.class);
        registerSerializer(SessionClientModelImpl.class, SessionClientModelSerializer.class);
        registerSerializer(CacheMatcherModelImpl.class, CacheMatcherSerializer.class);
        // block service
        registerSerializer(BlockServerModelImpl.class, BlockServerModelSerializer.class);
        registerSerializer(BlockClientModelImpl.class, BlockClientModelSerializer.class);
        registerSerializer(com.nomad.model.SerializerImpl.class, SerializerSerializer.class);

        registerSerializer(CommonServerModelImpl.class, CommonServerModelSerializer.class);
        registerSerializer(CommonClientModelImpl.class, CommonClientModelSerializer.class);
        // manager

        registerSerializer(ManagementClientModelImpl.class, ManagementClientModelSerializer.class);
        registerSerializer(ManagementServerModelImpl.class, ManagementServerModelSerializer.class);
        registerSerializer(ManagementMessageImpl.class, ManagementMessageSerializer.class);


        registerSerializer(SessionCallBackServerModelImp.class, SessionCallBackServerModelSerializer.class);

        // criteria
        registerSerializer(AbstractCriteria.class, AbstractCriteriaSerializer.class);
        registerSerializer(CriteriaGroupItem.class, CriteriaGroupItemSerializer.class);
        registerSerializer(CriteriaItemImpl.class, CriteriaItemImplSerializer.class);
        registerSerializer(StatisticElementImpl.class, StatisticElementSerializer.class);
        registerSerializer(SortFieldImpl.class, SortFieldSerializer.class);

        // id generator
        registerSerializer(IdGeneratorMessageImpl.class, IdGeneratorMessageSerializer.class);
        registerSerializer(IdGeneratorServerModelImpl.class, IdGeneratorServerModelSerializer.class);
        registerSerializer(IdGeneratorClientModelImpl.class, IdGeneratorClientModelSerializer.class);

        registerSerializer(BigInteger.class, BigIntegerSerializer.class);
        // updater
        registerSerializer(UpdateItemImpl.class, UpdateItemSerializer.class);
        registerSerializer(UpdateRequestImpl.class, UpdateRequestSerializer.class);

    }

    public static SerializerPooledObject<Object> getSerializer(final String clazz) {
        final SerializerPool pool = serializerMap.get(clazz);
        if (pool != null) {
            return pool.getObject();

        }
        return null;
    }
    public static boolean containsSerializer(final String clazz) {
        return serializerMap.containsKey(clazz);
    }
    public  static <T extends Object> void registerSerializer(final Class<T> clazz, final Class<? extends Serializer<T>> classSerialize) {
        if (serializerMap.get(clazz.getName()) != null) {
            return;
        }
        final SerializerPool pool = new SerializerPool(20, 10000,  classSerialize.getName(), null );
        serializerMap.put(clazz.getName(), pool);
    }

    @SuppressWarnings("unchecked")
    public  static <K extends Object> void registerSerializer(final String clazzName, final String classSerializerName) {
        Class<K> clazz;
        Class<? extends Serializer<K>> classSerializer;
        try {
            clazz = (Class<K>) Thread.currentThread().getContextClassLoader().loadClass(clazzName);
            classSerializer=(Class<? extends Serializer<K>>) Thread.currentThread().getContextClassLoader().loadClass(classSerializerName);
            registerSerializer(clazz,classSerializer);
        } catch (final ClassNotFoundException e) {
            LOGGER.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage(),e);
        }
        registerSerializer(clazz,classSerializer);
    }

    public static Map<String, SerializerPool> getSerializer() {
        return serializerMap;
    }
}
