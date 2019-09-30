package com.nomad.cachewiever.editors.model;

import java.util.ArrayList;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.nomad.model.SaveClientModel;
import com.nomad.model.SaveClientModelImpl;


public class SaveClientPropertySource implements IPropertySource {

  private SaveClientNode node;

  public SaveClientPropertySource(SaveClientNode node) {
    this.node = node;
  }

  @Override
  public Object getEditableValue() {
    return null;
  }

  @Override
  public IPropertyDescriptor[] getPropertyDescriptors() {
    ArrayList<IPropertyDescriptor> properties = new ArrayList<IPropertyDescriptor>();
    properties.add(new TextPropertyDescriptor(SaveClientNode.PROPERTY_HOST, "host"));
    properties.add(new TextPropertyDescriptor(SaveClientNode.PROPERTY_PORT, "port"));
    properties.add(new TextPropertyDescriptor(SaveClientNode.PROPERTY_THREAD, "threads"));
    properties.add(new TextPropertyDescriptor(SaveClientNode.PROPERTY_TIME_OUT, "time out 1"));

    return properties.toArray(new IPropertyDescriptor[0]);
  }

  @Override
  public Object getPropertyValue(Object id) {
     if (id.equals(SaveClientNode.PROPERTY_HOST)) {
      return node.getSaveClientModel().getHost();
    } else if (id.equals(SaveClientNode.PROPERTY_PORT)) {
      return "" + node.getSaveClientModel().getPort();
    } else if (id.equals(SaveClientNode.PROPERTY_THREAD)) {
      return "" + node.getSaveClientModel().getThreads();
    } else if (id.equals(SaveClientNode.PROPERTY_TIME_OUT)) {
      return "" + node.getSaveClientModel().getTimeOut();
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

  private SaveClientModel getSaveClientModel(){
     return ((ServerNode)node.getParent()).getServer().getServerModel().getSaveClientModel();
  }
  @Override
  public void setPropertyValue(Object id, Object value) {
    SaveClientModel forUpdate=getSaveClientModel();
    if(forUpdate==null){
      forUpdate= new SaveClientModelImpl();
      ((ServerNode)node.getParent()).getServer().getServerModel().setSaveClientModel(forUpdate);
    }
    Object oldProperty = null;
    if (id.equals(SaveClientNode.PROPERTY_HOST)) {
      oldProperty=node.getSaveClientModel().getHost();
      node.getSaveClientModel().setHost((String) value);
      forUpdate.setHost((String) value);
    } else if (id.equals(SaveClientNode.PROPERTY_PORT)) {
      oldProperty=node.getSaveClientModel().getPort();
      node.getSaveClientModel().setPort(Integer.parseInt(value.toString()));
      forUpdate.setPort(Integer.parseInt(value.toString()));
    } else if (id.equals(SaveClientNode.PROPERTY_THREAD)) {
      oldProperty=node.getSaveClientModel().getThreads();
      node.getSaveClientModel().setThreads(Integer.parseInt(value.toString()));
      forUpdate.setThreads(Integer.parseInt(value.toString()));
    } else if (id.equals(SaveClientNode.PROPERTY_TIME_OUT)) {
      oldProperty=node.getSaveClientModel().getTimeOut();
      node.getSaveClientModel().setTimeOut(Integer.parseInt(value.toString()));
      forUpdate.setTimeOut(Integer.parseInt(value.toString()));
    }
    node.propertyChange(oldProperty, value);
  }
}
