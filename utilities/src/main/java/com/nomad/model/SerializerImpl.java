package com.nomad.model;


public class SerializerImpl implements Serializer {

  private String clazz;
  private String serializerClazz;

  public SerializerImpl() {
  }
  public SerializerImpl(final String clazz, final String serializerClazz) {
    super();
    this.clazz = clazz;
    this.serializerClazz = serializerClazz;
  }
  @Override
  public String getClazz() {
    return clazz;
  }
  @Override
  public void setClazz(final String clazz) {
    this.clazz = clazz;
  }
  @Override
  public String getSerializerClazz() {
    return serializerClazz;
  }
  @Override
  public void setSerializerClazz(final String serializerClazz) {
    this.serializerClazz = serializerClazz;
  }
  @Override
  public String toString() {
    return "Serializer [clazz=" + clazz + ", serializerClazz=" + serializerClazz + "]";
  }

}
