package com.nomad.io.serializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.serializer.Serializer;
import com.nomad.server.ServerContext;
import com.nomad.utility.pool.ObjectPoolImpl;

public class SerializerPool extends ObjectPoolImpl<SerializerPooledObject<Object>>{

    private final static Logger LOGGER = LoggerFactory.getLogger(SerializerPool.class);
    private final String clazz;

    public SerializerPool(final int poolSize, final long timeout, final String clazz, final ServerContext context) {
        super(poolSize, timeout, 0, context, true);
        this.clazz = clazz;
        try {
            init();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public String getPoolId() {
        return this.getClass().getName()+":"+clazz;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public SerializerPooledObject<Object> getNewPooledObject() {
        try {
            final Serializer<Object>serializer=(Serializer<Object>)Class.forName(clazz).newInstance();
            final SerializerPooledObject<Object> pool=new SerializerPooledObject<>(serializer);
            return pool;
        } catch (final ClassNotFoundException e) {
            LOGGER.error(e.getMessage(),e);
        } catch (final InstantiationException e) {
            LOGGER.error(e.getMessage(),e);
        } catch (final IllegalAccessException e) {
            LOGGER.error(e.getMessage(),e);
        }
        return null;
    }



}
