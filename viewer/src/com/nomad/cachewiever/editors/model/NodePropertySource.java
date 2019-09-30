package com.nomad.cachewiever.editors.model;

import java.util.ArrayList;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

public class NodePropertySource implements IPropertySource {

  private Object node;

  public NodePropertySource(Object node) {
    this.node = node;
  }

  @Override
  public Object getEditableValue() {
    return null;
  }

  @Override
  public IPropertyDescriptor[] getPropertyDescriptors() {
    ArrayList<IPropertyDescriptor> properties = new ArrayList<IPropertyDescriptor>();
     if (node instanceof Connection) {
      properties.add(new TextPropertyDescriptor(Connection.PROPERTY_LISTENER_SERVER_HOST, "ServerHost"));
      properties.add(new TextPropertyDescriptor(Connection.PROPERTY_LISTENER_SERVER_PORT, "ServerPort"));
      properties.add(new TextPropertyDescriptor(Connection.PROPERTY_LISTENER_SERVER_MANAGER_PORT, "ServerManagerPort"));
      properties.add(new TextPropertyDescriptor(Connection.PROPERTY_LISTENER_CLIENT_HOST, "ClientHost"));
      properties.add(new TextPropertyDescriptor(Connection.PROPERTY_LISTENER_CLIENT_PORT, "ClientPort"));
      properties.add(new TextPropertyDescriptor(Connection.PROPERTY_LISTENER_THREADS, "Threads"));
      
    } else if (node instanceof RootObject) {
      properties.add(new TextPropertyDescriptor(RootObject.PROPERTY_CAPITAL, "Capital"));
    } else if (node instanceof ListenerNode) {
      // properties.add(new
      // TextPropertyDescriptor(ListenerNode.PROPERTY_FIRSTNAME, "Prenom"));
      // properties.add(new
      // ComboBoxPropertyDescriptor(ListenerNode.PROPERTY_DEPARTMENT,
      // "Department", ListenerNode.DEPARTMENTS));
    }
    return properties.toArray(new IPropertyDescriptor[0]);
  }

  @Override
  public Object getPropertyValue(Object id) {
    if (id.equals(Connection.PROPERTY_LISTENER_SERVER_HOST)){
      return ((Connection)node).getConnectModel().getServerHost();
    } else if(id.equals(Connection.PROPERTY_LISTENER_SERVER_PORT)){
      return ((Connection)node).getConnectModel().getServerPort();
    } else if(id.equals(Connection.PROPERTY_LISTENER_SERVER_MANAGER_PORT)){
        return ((Connection)node).getConnectModel().getServerManagementPort()+"";
    } else if(id.equals(Connection.PROPERTY_LISTENER_CLIENT_HOST)){
      return ((Connection)node).getConnectModel().getClientHost();
    } else if(id.equals(Connection.PROPERTY_LISTENER_CLIENT_PORT)){
      return ((Connection)node).getConnectModel().getClientManagementPort()+"";
    } else if(id.equals(Connection.PROPERTY_LISTENER_THREADS)){
      return ((Connection)node).getConnectModel().getConnectThreads()+"";
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

  @Override
  public void setPropertyValue(Object id, Object value) {
    
   
 
  }
}
