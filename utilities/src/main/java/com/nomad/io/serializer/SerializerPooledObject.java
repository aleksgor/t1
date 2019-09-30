package com.nomad.io.serializer;

import com.nomad.serializer.Serializer;
import com.nomad.utility.PooledObject;
import com.nomad.utility.pool.PooledObjectImpl;

public class SerializerPooledObject<T> extends PooledObjectImpl implements PooledObject {
    private Serializer<T> serializer;

    public SerializerPooledObject(Serializer<T> serializer) {
        this.serializer = serializer;
    }

    @Override
    public void closeObject() {

    }

    public Serializer<T> getSerializer() {
        return serializer;
    }

    @Override
    protected long getSize() {
        return 0;
    }

}