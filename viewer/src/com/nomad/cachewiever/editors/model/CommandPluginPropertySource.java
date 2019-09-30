package com.nomad.cachewiever.editors.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;


import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.nomad.model.CommandPluginModel;



public class CommandPluginPropertySource implements IPropertySource {

  private CommandPluginNode node;

  public CommandPluginPropertySource(CommandPluginNode node) {
    this.node = node;
  }

  @Override
  public Object getEditableValue() {
    return null;
  }

  @Override
  public IPropertyDescriptor[] getPropertyDescriptors() {
    Properties prop=node.getCommandPluginModel().getProperties();
    ArrayList<IPropertyDescriptor> properties = new ArrayList<IPropertyDescriptor>();
    properties.add(new TextPropertyDescriptor(CommandPluginNode.PROPERTY_CLASS, "class"));
    properties.add(new TextPropertyDescriptor(CommandPluginNode.PROPERTY_CHECKDELAY, "check delay"));
    properties.add(new TextPropertyDescriptor(CommandPluginNode.PROPERTY_POOLSIZE, "poolsize"));
    properties.add(new TextPropertyDescriptor(CommandPluginNode.PROPERTY_TIMEOUT, "timeout"));
    
    for (Entry<Object, Object> pr : prop.entrySet()) {
      properties.add(new TextPropertyDescriptor(CommandPluginNode.PROPERTY_PROP+pr.getKey(), pr.getKey().toString()));

    }
    
    return properties.toArray(new IPropertyDescriptor[0]);
  }

  @Override
  public Object getPropertyValue(Object id) {
    
    CommandPluginModel dsm= getCommandPluginModel();
    if (id.equals(CommandPluginNode.PROPERTY_CLASS)) {
      return dsm.getClazz();
    } else if (id.equals(CommandPluginNode.PROPERTY_CHECKDELAY)) {
      return ""+dsm.getChechDelay();
    } else if (id.equals(CommandPluginNode.PROPERTY_POOLSIZE)) {
      return "" + dsm.getPoolSize();
    } else if (id.equals(CommandPluginNode.PROPERTY_TIMEOUT)) {
      return "" + dsm.getTimeOut();
    } else{
      if(id.toString().startsWith(CommandPluginNode.PROPERTY_PROP)){
        String propName=id.toString().substring(CommandPluginNode.PROPERTY_PROP.length());
        if(propName!=null){
          return dsm.getProperties().getProperty(propName);
        }
      }
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

  private CommandPluginModel getCommandPluginModel(){
    CommandPluginModel t=node.getCommandPluginModel();
    List<CommandPluginModel> list= ((ServerNode)node.getParent()).getServer().getServerModel().getCommandPlugins();
    for (CommandPluginModel commandPluginModel : list) {
      if(t.getClazz().equals(commandPluginModel.getClazz())){
        return commandPluginModel;
      }
    }
    return null;
  }
  @Override
  public void setPropertyValue(Object id, Object value) {
    CommandPluginModel forUpdate=getCommandPluginModel();
    if(forUpdate==null){
      forUpdate= new CommandPluginModel();
      ((ServerNode)node.getParent()).getServer().getServerModel().getCommandPlugins().add(forUpdate);
    }
    Object oldProperty = null;
    if (id.equals(CommandPluginNode.PROPERTY_CLASS)) {
      oldProperty=forUpdate.getClazz();
      forUpdate.setClazz((String) value);
    } else if (id.equals(CommandPluginNode.PROPERTY_CHECKDELAY)) {
      oldProperty=forUpdate.getChechDelay();
      forUpdate.setChechDelay(Long.parseLong((String) value));
    } else if (id.equals(CommandPluginNode.PROPERTY_TIMEOUT)) {
      oldProperty=forUpdate.getTimeOut();
      forUpdate.setTimeOut(Long.parseLong((String) value));
    } else if (id.equals(CommandPluginNode.PROPERTY_POOLSIZE)) {
      oldProperty=forUpdate.getPoolSize();
      forUpdate.setPoolSize(Integer.parseInt((String) value));
    
    }else{
      if(id.toString().startsWith(CommandPluginNode.PROPERTY_PROP)){
        String propName=id.toString().substring(CommandPluginNode.PROPERTY_PROP.length());
        if(propName!=null){
           forUpdate.getProperties().setProperty(propName,  (String)value);
        }
      }

    }
    node.setCommandPluginModel(forUpdate);
    node.propertyChange(oldProperty, value);
  }
}
