package com.nomad.cachewiever.editors.model;


import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.nomad.model.SessionClientModel;



public class SessionClientPropertySource implements IPropertySource {

  private SessionClientNode node;

  public SessionClientPropertySource(SessionClientNode node) {
    this.node = node;
  }

  @Override
  public Object getEditableValue() {
    return null;
  }

  @Override
  public IPropertyDescriptor[] getPropertyDescriptors() {
 
    IPropertyDescriptor[] result={ new TextPropertyDescriptor(SaveClientNode.PROPERTY_HOST, "host"),
        new TextPropertyDescriptor(SaveClientNode.PROPERTY_PORT, "port"),
        new TextPropertyDescriptor(SaveClientNode.PROPERTY_THREAD, "threads"),
        new TextPropertyDescriptor(SaveClientNode.PROPERTY_TIME_OUT, "timeout 4")};
    
    return result;
    
     
  //  return properties.toArray(new IPropertyDescriptor[0]);
  }

  @Override
  public Object getPropertyValue(Object id) {
     if (id.equals(SessionClientNode.PROPERTY_HOST)) {
      return node.getSessionClientModel().getHost();
    } else if (id.equals(SessionClientNode.PROPERTY_PORT)) {
      return "" + node.getSessionClientModel().getPort();
    } else if (id.equals(SessionClientNode.PROPERTY_THREAD)) {
      return "" + node.getSessionClientModel().getThreads();
    } else if (id.equals(SessionClientNode.PROPERTY_TIME_OUT)) {
      return "" + node.getSessionClientModel().getTimeOut();
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

  private SessionClientModel getSessionClientModel(){
     return ((ServerNode)node.getParent()).getServer().getServerModel().getSessionClientModel();
  }
  @Override
  public void setPropertyValue(Object id, Object value) {
    SessionClientModel forUpdate=getSessionClientModel();
    if(forUpdate==null){
      forUpdate= new SessionClientModel();
      ((ServerNode)node.getParent()).getServer().getServerModel().setSessionClientModel(forUpdate);
    }
    Object oldProperty = null;
    if (id.equals(ListenerNode.PROPERTY_HOST)) {
      oldProperty=node.getSessionClientModel().getHost();
      node.getSessionClientModel().setHost((String) value);
      forUpdate.setHost((String) value);
    } else if (id.equals(ListenerNode.PROPERTY_PORT)) {
      oldProperty=node.getSessionClientModel().getPort();
      node.getSessionClientModel().setPort(Integer.parseInt(value.toString()));
      forUpdate.setPort(Integer.parseInt(value.toString()));
    } else if (id.equals(ListenerNode.PROPERTY_THREAD)) {
      oldProperty=node.getSessionClientModel().getThreads();
      node.getSessionClientModel().setThreads(Integer.parseInt(value.toString()));
      forUpdate.setThreads(Integer.parseInt(value.toString()));
    } else if (id.equals(ListenerNode.PROPERTY_TIME_BACKLOG)) {
      oldProperty=node.getSessionClientModel().getTimeOut();
      node.getSessionClientModel().setTimeOut(Integer.parseInt(value.toString()));
      forUpdate.setTimeOut(Integer.parseInt(value.toString()));
    }
    node.propertyChange(oldProperty, value);
  }
}
