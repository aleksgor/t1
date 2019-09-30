package com.nomad.cachewiever.editors.model;

import java.util.ArrayList;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.nomad.model.SaveServerModel;
import com.nomad.model.SaveServerModelImpl;


public class SessionServerPropertySource implements IPropertySource {

  private SessionServerNode node;

  public SessionServerPropertySource(SessionServerNode node) {
    this.node = node;
  }

  @Override
  public Object getEditableValue() {
    return null;
  }

  @Override
  public IPropertyDescriptor[] getPropertyDescriptors() {
    ArrayList<IPropertyDescriptor> properties = new ArrayList<IPropertyDescriptor>();
    properties.add(new TextPropertyDescriptor(SessionServerNode.PROPERTY_HOST, "host"));
    properties.add(new TextPropertyDescriptor(SessionServerNode.PROPERTY_PORT, "port"));
    properties.add(new TextPropertyDescriptor(SessionServerNode.PROPERTY_THREAD, "threads"));
    properties.add(new TextPropertyDescriptor(SessionServerNode.PROPERTY_TIME_OUT, "time out 3"));

    return properties.toArray(new IPropertyDescriptor[0]);
  }

  @Override
  public Object getPropertyValue(Object id) {
     if (id.equals(SessionServerNode.PROPERTY_HOST)) {
      return node.getSessionServerModel().getHost();
    } else if (id.equals(SessionServerNode.PROPERTY_PORT)) {
      return "" + node.getSessionServerModel().getPort();
    } else if (id.equals(SessionServerNode.PROPERTY_THREAD)) {
      return "" + node.getSessionServerModel().getThreads();
    } else if (id.equals(SessionServerNode.PROPERTY_TIME_OUT)) {
      return "" + node.getSessionServerModel().getSessionTimeLive();
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

  private SaveServerModel getSaveServerModel(){
     return ((ServerNode)node.getParent()).getServer().getServerModel().getSaveServerModel();
  }
  @Override
  public void setPropertyValue(Object id, Object value) {
    SaveServerModel forUpdate=getSaveServerModel();
    if(forUpdate==null){
      forUpdate= new SaveServerModelImpl();
      ((ServerNode)node.getParent()).getServer().getServerModel().setSaveServerModel(forUpdate);
    }
    Object oldProperty = null;
    if (id.equals(ListenerNode.PROPERTY_HOST)) {
      oldProperty=node.getSessionServerModel().getHost();
      node.getSessionServerModel().setHost((String) value);
      forUpdate.setHost((String) value);
    } else if (id.equals(ListenerNode.PROPERTY_PORT)) {
      oldProperty=node.getSessionServerModel().getPort();
      node.getSessionServerModel().setPort(Integer.parseInt(value.toString()));
      forUpdate.setPort(Integer.parseInt(value.toString()));
    } else if (id.equals(ListenerNode.PROPERTY_THREAD)) {
      oldProperty=node.getSessionServerModel().getThreads();
      node.getSessionServerModel().setThreads(Integer.parseInt(value.toString()));
      forUpdate.setThreads(Integer.parseInt(value.toString()));
    } else if (id.equals(ListenerNode.PROPERTY_TIME_BACKLOG)) {
      oldProperty=node.getSessionServerModel().getSessionTimeLive();
      node.getSessionServerModel().setSessionTimeLive(Integer.parseInt(value.toString()));
      forUpdate.setSessionTimeout(Integer.parseInt(value.toString()));
    }
    node.propertyChange(oldProperty, value);
  }
}
