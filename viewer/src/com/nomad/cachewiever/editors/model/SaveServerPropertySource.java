package com.nomad.cachewiever.editors.model;

import java.util.ArrayList;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.nomad.model.SaveServerModel;
import com.nomad.model.SaveServerModelImpl;


public class SaveServerPropertySource implements IPropertySource {

  private SaveServerNode node;

  public SaveServerPropertySource(SaveServerNode node) {
    this.node = node;
  }

  @Override
  public Object getEditableValue() {
    return null;
  }

  @Override
  public IPropertyDescriptor[] getPropertyDescriptors() {
    ArrayList<IPropertyDescriptor> properties = new ArrayList<IPropertyDescriptor>();
    properties.add(new TextPropertyDescriptor(SaveServerNode.PROPERTY_HOST, "host"));
    properties.add(new TextPropertyDescriptor(SaveServerNode.PROPERTY_PORT, "port"));
    properties.add(new TextPropertyDescriptor(SaveServerNode.PROPERTY_THREAD, "threads"));
    properties.add(new TextPropertyDescriptor(SaveServerNode.PROPERTY_TIME_OUT, "time out 2"));

    return properties.toArray(new IPropertyDescriptor[0]);
  }

  @Override
  public Object getPropertyValue(Object id) {
     if (id.equals(SaveServerNode.PROPERTY_HOST)) {
      return node.getSaveServerModel().getHost();
    } else if (id.equals(SaveServerNode.PROPERTY_PORT)) {
      return "" + node.getSaveServerModel().getPort();
    } else if (id.equals(SaveServerNode.PROPERTY_THREAD)) {
      return "" + node.getSaveServerModel().getThreads();
    } else if (id.equals(SaveServerNode.PROPERTY_TIME_OUT)) {
      return "" + node.getSaveServerModel().getSessionTimeout();
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
    if (id.equals(SaveServerNode.PROPERTY_HOST)) {
      oldProperty=node.getSaveServerModel().getHost();
      node.getSaveServerModel().setHost((String) value);
      forUpdate.setHost((String) value);
    } else if (id.equals(SaveServerNode.PROPERTY_PORT)) {
      oldProperty=node.getSaveServerModel().getPort();
      node.getSaveServerModel().setPort(Integer.parseInt(value.toString()));
      forUpdate.setPort(Integer.parseInt(value.toString()));
    } else if (id.equals(SaveServerNode.PROPERTY_THREAD)) {
      oldProperty=node.getSaveServerModel().getThreads();
      node.getSaveServerModel().setThreads(Integer.parseInt(value.toString()));
      forUpdate.setThreads(Integer.parseInt(value.toString()));
    } else if (id.equals(SaveServerNode.PROPERTY_TIME_OUT)) {
      oldProperty=node.getSaveServerModel().getSessionTimeout();
      node.getSaveServerModel().setSessionTimeout(Integer.parseInt(value.toString()));
      forUpdate.setSessionTimeout(Integer.parseInt(value.toString()));
    }
    node.propertyChange(oldProperty, value);
  }
}
