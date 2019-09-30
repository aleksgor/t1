package com.nomad.cachewiever.editors.model;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Properties;


import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.nomad.model.DataSourceModel;


public class DataSourcePropertySource implements IPropertySource {

  private DataSourceNode node;

  public DataSourcePropertySource(DataSourceNode node) {
    this.node = node;
  }

  @Override
  public Object getEditableValue() {
    return null;
  }

  @Override
  public IPropertyDescriptor[] getPropertyDescriptors() {
    Properties prop=node.getDataSource().getProperties();
    ArrayList<IPropertyDescriptor> properties = new ArrayList<IPropertyDescriptor>();
    properties.add(new TextPropertyDescriptor(DataSourceNode.PROPERTY_CLASS, "class"));
    properties.add(new TextPropertyDescriptor(DataSourceNode.PROPERTY_NAME, "name"));
    properties.add(new TextPropertyDescriptor(DataSourceNode.PROPERTY_THREAD, "threads"));
    
    for (Entry<Object, Object> pr : prop.entrySet()) {
      properties.add(new TextPropertyDescriptor(DataSourceNode.PROPERTY_PROP+pr.getKey(), pr.getKey().toString()));

    }
    
    return properties.toArray(new IPropertyDescriptor[0]);
  }

  @Override
  public Object getPropertyValue(Object id) {
    
    DataSourceModel dsm= getDataSourceModel();
    if (id.equals(DataSourceNode.PROPERTY_CLASS)) {
      return dsm.getClazz();
    } else if (id.equals(DataSourceNode.PROPERTY_NAME)) {
      return dsm.getName();
    } else if (id.equals(DataSourceNode.PROPERTY_THREAD)) {
      return "" + dsm.getThreads();
    } else{
      if(id.toString().startsWith(DataSourceNode.PROPERTY_PROP)){
        String propName=id.toString().substring(DataSourceNode.PROPERTY_PROP.length());
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

  private DataSourceModel getDataSourceModel(){
    return ((ServerNode)node.getParent()).getServer().getServerModel().getDataSources().get(node.getDataSource().getName());
  }
  @Override
  public void setPropertyValue(Object id, Object value) {
    DataSourceModel forUpdate=getDataSourceModel();
    if(forUpdate==null){
      forUpdate= new DataSourceModel();
      ((ServerNode)node.getParent()).getServer().getServerModel().getDataSources().put(forUpdate.getName(),forUpdate);
    }
    Object oldProperty = null;
    if (id.equals(DataSourceNode.PROPERTY_CLASS)) {
      oldProperty=forUpdate.getClazz();
      forUpdate.setClazz((String) value);
    } else if (id.equals(DataSourceNode.PROPERTY_NAME)) {
      ((ServerNode)node.getParent()).getServer().getServerModel().getDataSources().remove(forUpdate.getName());
      forUpdate.setName((String) value);
      ((ServerNode)node.getParent()).getServer().getServerModel().getDataSources().put(forUpdate.getName(), forUpdate);
    } else if (id.equals(DataSourceNode.PROPERTY_THREAD)) {
      oldProperty=forUpdate.getThreads();
      forUpdate.setThreads(Integer.parseInt( (String)value));
    
    }else{
      if(id.toString().startsWith(DataSourceNode.PROPERTY_PROP)){
        String propName=id.toString().substring(DataSourceNode.PROPERTY_PROP.length());
        if(propName!=null){
           forUpdate.getProperties().setProperty(propName,  (String)value);
        }
      }

    }
    node.setDataSource(forUpdate);
    node.propertyChange(oldProperty, value);
  }
}
