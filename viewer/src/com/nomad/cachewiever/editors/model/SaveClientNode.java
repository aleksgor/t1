package com.nomad.cachewiever.editors.model;

import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.views.properties.IPropertySource;

import com.nomad.cachewiever.editors.model.Node;
import com.nomad.cachewiever.utility.CacheColorConstants;
import com.nomad.model.SaveClientModel;

public class SaveClientNode extends Node {
  public static final String PROPERTY_PORT="port";
  public static final String PROPERTY_THREAD="thread";
  public static final String PROPERTY_HOST="host";
  public static final String PROPERTY_TIME_OUT="tumeout";
  
  private SaveClientModel listener;
  private Color color =CacheColorConstants.white;

  public SaveClientNode(SaveClientModel listener) {
    this.listener = listener;
  }

  public SaveClientModel getSaveClientModel() {
    return listener;
  }

  public void setSaveClientModel(SaveClientModel listener) {
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
        propertySource = new SaveClientPropertySource(this);
      return propertySource;
    }
    return null;
  }

}