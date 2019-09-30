package com.nomad.model;

public interface Serializer {
    String getClazz();

    void setClazz(String clazz);

    String getSerializerClazz();

    void setSerializerClazz(String serializerClazz);
}
