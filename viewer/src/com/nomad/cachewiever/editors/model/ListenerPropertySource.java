package com.nomad.cachewiever.editors.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import org.eclipse.ui.views.properties.ColorPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.nomad.model.ListenerModel;

public class ListenerPropertySource implements IPropertySource {

  private ListenerNode node;

  public ListenerPropertySource(ListenerNode node) {
    this.node = node;
  }

  @Override
  public Object getEditableValue() {
    return null;
  }

  @Override
  public IPropertyDescriptor[] getPropertyDescriptors() {
    ArrayList<IPropertyDescriptor> properties = new ArrayList<IPropertyDescriptor>();
    properties.add(new TextPropertyDescriptor(ListenerNode.PROPERTY_CLASS, "class"));
    properties.add(new ColorPropertyDescriptor(ListenerNode.PROPERTY_COLOR, "color"));
    properties.add(new TextPropertyDescriptor(ListenerNode.PROPERTY_HOST, "host"));
    properties.add(new TextPropertyDescriptor(ListenerNode.PROPERTY_PORT, "port"));
    properties.add(new TextPropertyDescriptor(ListenerNode.PROPERTY_PROTOCOL_VERSION, "protocol version"));
    properties.add(new TextPropertyDescriptor(ListenerNode.PROPERTY_THREAD, "threads"));
    properties.add(new TextPropertyDescriptor(ListenerNode.PROPERTY_TIME_BACKLOG, "backlog"));

    return properties.toArray(new IPropertyDescriptor[0]);
  }

  @Override
  public Object getPropertyValue(Object id) {
    if (id.equals(ListenerNode.PROPERTY_CLASS)) {
      return node.getListener().getClass();
    } else if (id.equals(ListenerNode.PROPERTY_COLOR)) {
      return node.getColor().getRGB();
    } else if (id.equals(ListenerNode.PROPERTY_HOST)) {
      return node.getListener().getHost();
    } else if (id.equals(ListenerNode.PROPERTY_PORT)) {
      return "" + node.getListener().getPort();
    } else if (id.equals(ListenerNode.PROPERTY_PROTOCOL_VERSION)) {
      return node.getListener().getProtocolVersion();
    } else if (id.equals(ListenerNode.PROPERTY_THREAD)) {
      return "" + node.getListener().getThreads();
    } else if (id.equals(ListenerNode.PROPERTY_TIME_BACKLOG)) {
      return "" + node.getListener().getBacklog();
    }

    return null;
  }

  @Override
  public boolean isPropertySet(Object id) {
    return false;
  }

  @Override
  public void resetPropertyValue(Object id) {
    // TODO Auto-generated method stub
  }

  private ListenerModel getListenerModel(){
    List<ListenerModel>ls=((ServerNode)node.getParent()).getServer().getServerModel().getListeners();
    ListenerModel itis= node.getListener();
    for (ListenerModel listenerModel : ls) {
      if(listenerModel.getPort()==itis.getPort()){
        return listenerModel;
      }
    }
    return null;
  }
  @Override
  public void setPropertyValue(Object id, Object value) {
    ListenerModel forUpdate=getListenerModel();
    if(forUpdate==null){
      forUpdate= new ListenerModel();
      ((ServerNode)node.getParent()).getServer().getServerModel().getListeners().add(forUpdate);
    }
    Object oldProperty = null;
    if (id.equals(ListenerNode.PROPERTY_CLASS)) {
      oldProperty=node.getListener().getClazz();
      node.getListener().setClazz((String) value);
      forUpdate.setClazz((String) value);
    } else if (id.equals(ListenerNode.PROPERTY_COLOR)) {
      oldProperty = node.getColor();
      Color newColor = new Color(null, (RGB) value);
      node.setColor(newColor);
    } else if (id.equals(ListenerNode.PROPERTY_HOST)) {
      oldProperty=node.getListener().getHost();
      node.getListener().setHost((String) value);
      forUpdate.setHost((String) value);
    } else if (id.equals(ListenerNode.PROPERTY_PORT)) {
      oldProperty=node.getListener().getPort();
      node.getListener().setPort(Integer.parseInt(value.toString()));
      forUpdate.setPort(Integer.parseInt(value.toString()));
    } else if (id.equals(ListenerNode.PROPERTY_PROTOCOL_VERSION)) {
      oldProperty=node.getListener().getProtocolVersion();
      node.getListener().setProtocolVersion((String) value);
      forUpdate.setProtocolVersion((String) value);
    } else if (id.equals(ListenerNode.PROPERTY_THREAD)) {
      oldProperty=node.getListener().getThreads();
      node.getListener().setThreads(Integer.parseInt(value.toString()));
      forUpdate.setThreads(Integer.parseInt(value.toString()));
    } else if (id.equals(ListenerNode.PROPERTY_TIME_BACKLOG)) {
      oldProperty=node.getListener().getBacklog();
      node.getListener().setBacklog(Integer.parseInt(value.toString()));
      forUpdate.setBacklog(Integer.parseInt(value.toString()));
    }
    node.propertyChange(oldProperty, value);
  }
}
