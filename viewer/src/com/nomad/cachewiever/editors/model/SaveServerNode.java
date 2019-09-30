package com.nomad.cachewiever.editors.model;

import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.views.properties.IPropertySource;

import com.nomad.cachewiever.editors.model.Node;
import com.nomad.cachewiever.utility.CacheColorConstants;
import com.nomad.model.SaveServerModel;

public class SaveServerNode extends Node {
  public static final String PROPERTY_PORT="port";
  public static final String PROPERTY_TIME_OUT="timeOut";
  public static final String PROPERTY_THREAD="thread";
  public static final String PROPERTY_HOST="host";
  
  private SaveServerModel listener;
  private Color color =CacheColorConstants.white;

  public SaveServerNode(SaveServerModel listener) {
    this.listener = listener;
  }

  public SaveServerModel getSaveServerModel() {
    return listener;
  }

  public void setSaveServerModel(SaveServerModel listener) {
    this.listener = listener;
  }

  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Object getAdapter(Class adapter) {
    if (adapter == IPropertySource.class) {
      if (propertySource == null)
        propertySource = new SaveServerPropertySource(this);
      return propertySource;
    }
    return null;
  }

}