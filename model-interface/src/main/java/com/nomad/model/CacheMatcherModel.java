package com.nomad.model;

import java.util.Map;

public interface CacheMatcherModel {
    
  String getClazz();

  void setClazz(String clazz);

  Map<String, String> getProperties();
}
