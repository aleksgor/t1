package com.nomad.cachewiever.editors.model;

import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.views.properties.IPropertySource;

import com.nomad.cachewiever.editors.model.Node;
import com.nomad.cachewiever.utility.CacheColorConstants;
import com.nomad.model.SessionServerModel;

public class SessionServerNode extends Node {
  public static final String PROPERTY_HOST="host";
  public static final String PROPERTY_PORT="port";
  public static final String PROPERTY_TIME_OUT="timeOut";
  public static final String PROPERTY_THREAD="thread";
  private SessionServerModel listener;
  private Color color =CacheColorConstants.white;

  public SessionServerNode(SessionServerModel listener) {
    this.listener = listener;
  }

  public SessionServerModel getSessionServerModel() {
    return listener;
  }

  public void setSessionServerModel(SessionServerModel listener) {
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
        propertySource = new SessionServerPropertySource(this);
      return propertySource;
    }
    return null;
  }

}