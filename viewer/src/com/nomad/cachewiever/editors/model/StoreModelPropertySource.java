package com.nomad.cachewiever.editors.model;

import java.util.ArrayList;
import java.util.List;


import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.nomad.model.StoreModel;

public class StoreModelPropertySource implements IPropertySource {

  private StoreModelNode node;

  public StoreModelPropertySource(StoreModelNode node) {
    this.node = node;
  }

  @Override
  public Object getEditableValue() {
    return null;
  }

  @Override
  public IPropertyDescriptor[] getPropertyDescriptors() {
    String[] answers={"true","false"};
    ArrayList<IPropertyDescriptor> properties = new ArrayList<IPropertyDescriptor>();
    properties.add(new TextPropertyDescriptor(StoreModelNode.PROPERTY_DATA_SOURCE, "data source name"));
    properties.add(new TextPropertyDescriptor(StoreModelNode.PROPERTY_IDENTIFIER, "Class Identifier"));
    properties.add(new TextPropertyDescriptor(StoreModelNode.PROPERTY_MODEL, "modelName"));
    properties.add(new TextPropertyDescriptor(StoreModelNode.PROPERTY_PKG, "package"));
    properties.add(new ComboBoxPropertyDescriptor(StoreModelNode.PROPERTY_READ_THROUGH, "read through",answers));
    properties.add(new ComboBoxPropertyDescriptor(StoreModelNode.PROPERTY_WRITE_THROUGH, "write through",answers));

    return properties.toArray(new IPropertyDescriptor[0]);
  }

  @Override
  public Object getPropertyValue(Object id) {
    if (id.equals(StoreModelNode.PROPERTY_DATA_SOURCE)) {
      return node.getStoreModel().getDataSource();
    } else if (id.equals(StoreModelNode.PROPERTY_IDENTIFIER)) {
      return ""+node.getStoreModel().getIdentifier();
    } else if (id.equals(StoreModelNode.PROPERTY_MODEL)) {
      return node.getStoreModel().getModel();
    } else if (id.equals(StoreModelNode.PROPERTY_PKG)) {
      return "" + node.getStoreModel().getPkg();
    } else if (id.equals(StoreModelNode.PROPERTY_READ_THROUGH)) {
      return node.getStoreModel().isReadThrough()?0:1;
    } else if (id.equals(StoreModelNode.PROPERTY_WRITE_THROUGH)) {
      return  node.getStoreModel().isWriteThrough()?0:1;
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

  private StoreModel getStoreModel(){
    List<StoreModel>ls=((ServerNode)node.getParent()).getServer().getServerModel().getStoreModels();
    StoreModel itis= node.getStoreModel();
    for (StoreModel data : ls) {
      if(data.getModel().equals(itis.getModel())){
        return data;
      }
    }
    return null;
  }
  @Override
  public void setPropertyValue(Object id, Object value) {
    StoreModel forUpdate=getStoreModel();
    if(forUpdate==null){
      forUpdate= new StoreModel();
      ((ServerNode)node.getParent()).getServer().getServerModel().getStoreModels().add(forUpdate);
    }
    Object oldProperty = null;
    if (id.equals(StoreModelNode.PROPERTY_DATA_SOURCE)) {
      oldProperty=forUpdate.getDataSource();
      forUpdate.setDataSource((String) value);
    } else if (id.equals(StoreModelNode.PROPERTY_IDENTIFIER)) {
      oldProperty=forUpdate.getIdentifier();
      forUpdate.setIdentifier((String) value);
    } else if (id.equals(StoreModelNode.PROPERTY_MODEL)) {
      oldProperty=forUpdate.getModel();
      forUpdate.setModel((String) value);
    } else if (id.equals(StoreModelNode.PROPERTY_PKG)) {
      oldProperty=forUpdate.getPkg();
      forUpdate.setPkg((String) value);
    } else if (id.equals(StoreModelNode.PROPERTY_READ_THROUGH)) {
      oldProperty=forUpdate.isReadThrough()?0:1;
      Integer i=(Integer)value;
      forUpdate.setReadThrough(i==0);
    } else if (id.equals(StoreModelNode.PROPERTY_WRITE_THROUGH)) {
      oldProperty=forUpdate.isWriteThrough()?0:1;
      Integer i=(Integer)value;
      forUpdate.setWriteThrough(i==0);
    }
    node.propertyChange(oldProperty, value);
  }
}
