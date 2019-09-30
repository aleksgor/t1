package com.nomad.cachewiever.editors.model;

import org.eclipse.ui.views.properties.IPropertySource;

import com.nomad.cachewiever.editors.model.Node;
import com.nomad.model.StoreModel;

public class StoreModelNode extends Node {
  public static final String PROPERTY_MODEL="model";
  public static final String PROPERTY_IDENTIFIER="identifier";
  public static final String PROPERTY_PKG="pkg";
  public static final String PROPERTY_READ_THROUGH="readThrough";
  public static final String PROPERTY_WRITE_THROUGH="writeThrough";
  public static final String PROPERTY_DATA_SOURCE="dataSource";
  
  private StoreModel storeModel;

  public StoreModelNode(StoreModel storeModel) {
    this.storeModel = storeModel;
  }

  public StoreModel getStoreModel() {
    return storeModel;
  }

  public void setStoreModel(StoreModel storeModel) {
    this.storeModel = storeModel;
  }


  @SuppressWarnings("rawtypes")
  @Override
  public Object getAdapter(Class adapter) {
    if (adapter == IPropertySource.class) {
      if (propertySource == null)
        propertySource = new StoreModelPropertySource(this);
      return propertySource;
    }
    return null;
  }

}