package com.nomad.cachewiever.editors.model;

import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.views.properties.IPropertySource;

import com.nomad.cachewiever.editors.model.Node;
import com.nomad.cachewiever.utility.CacheColorConstants;
import com.nomad.model.ListenerModel;

public class ListenerNode extends Node {
  public static final String PROPERTY_CLASS="class";
  public static final String PROPERTY_PORT="port";
  public static final String PROPERTY_TIME_BACKLOG="backlog";
  public static final String PROPERTY_THREAD="thread";
  public static final String PROPERTY_HOST="host";
  public static final String PROPERTY_PROTOCOL_VERSION="protocolVersion";
  public static final String PROPERTY_COLOR="color";
  private ListenerModel listener;
  private Color color =CacheColorConstants.listenerBackGround;

  public ListenerNode(ListenerModel listener) {
    this.listener = listener;
  }

  public ListenerModel getListener() {
    return listener;
  }

  public void setListener(ListenerModel listener) {
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
        propertySource = new ListenerPropertySource(this);
      return propertySource;
    }
    return null;
  }

}