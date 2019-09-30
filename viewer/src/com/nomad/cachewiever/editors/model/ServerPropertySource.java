package com.nomad.cachewiever.editors.model;

import java.util.ArrayList;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

public class ServerPropertySource implements IPropertySource {

  private ServerNode node;

  public ServerPropertySource(ServerNode node) {
    this.node = node;
  }

  @Override
  public Object getEditableValue() {

    return null;
  }

  @Override
  public IPropertyDescriptor[] getPropertyDescriptors() {
    ArrayList<IPropertyDescriptor> properties = new ArrayList<IPropertyDescriptor>();
    properties.add(new TextPropertyDescriptor(ServerNode.PROPERTY_CLEANER_TIMER, "cleaner timer"));
    properties.add(new TextPropertyDescriptor(ServerNode.PROPERTY_COMMAND_PORT, "command port"));
    properties.add(new TextPropertyDescriptor(ServerNode.PROPERTY_HOST, "host"));
    properties.add(new TextPropertyDescriptor(ServerNode.PROPERTY_MANAGEMENT_PORT, "management port"));
    properties.add(new TextPropertyDescriptor(ServerNode.PROPERTY_NAME, "name"));
    properties.add(new TextPropertyDescriptor(ServerNode.PROPERTY_GROUP, "Servers group"));
    properties.add(new TextPropertyDescriptor(ServerNode.PROPERTY_SESSION_TIMEOUT, "session timeout (ms)"));
    properties.add(new TextPropertyDescriptor(ServerNode.PROPERTY_THREADS, "threads"));
    return properties.toArray(new IPropertyDescriptor[0]);
  }

  @Override
  public Object getPropertyValue(Object id) {

    if (id.equals(ServerNode.PROPERTY_CLEANER_TIMER))
      return "" + node.getServer().getServerModel().getCleanerTimer();
    else if (id.equals(ServerNode.PROPERTY_COMMAND_PORT))
      return "" + node.getServer().getServerModel().getCommandPort();
    else if (id.equals(ServerNode.PROPERTY_HOST))
      return "" + node.getServer().getServerModel().getHost();
    else if (id.equals(ServerNode.PROPERTY_MANAGEMENT_PORT))
      return "" + node.getServer().getServerModel().getManagementPort();
    else if (id.equals(ServerNode.PROPERTY_NAME))
      return "" + node.getServer().getServerModel().getServerName();
    else if (id.equals(ServerNode.PROPERTY_GROUP))
      return "" + node.getServer().getServerModel().getServerGroup()==null?"":node.getServer().getServerModel().getServerGroup();
    else if (id.equals(ServerNode.PROPERTY_THREADS))
      return "" + node.getServer().getServerModel().getThreads();

    return null;
  }

  @Override
  public boolean isPropertySet(Object id) {

    return false;
  }

  @Override
  public void resetPropertyValue(Object id) {
    
  }

  @Override
  public void setPropertyValue(Object id, Object value) {
    Object oldProperty = null;

    if (id.equals(ServerNode.PROPERTY_CLEANER_TIMER)) {
      oldProperty = node.getServer().getServerModel().getCleanerTimer();
      node.getServer().getServerModel().setCleanerTimer(Integer.parseInt(value.toString()));
    } else if (id.equals(ServerNode.PROPERTY_COMMAND_PORT)) {
      oldProperty = node.getServer().getServerModel().getCommandPort();
      node.getServer().getServerModel().setCommandPort(Integer.parseInt(value.toString()));
    } else if (id.equals(ServerNode.PROPERTY_HOST)) {
      oldProperty = node.getServer().getServerModel().getHost();
      node.getServer().getServerModel().setHost(value.toString());
    } else if (id.equals(ServerNode.PROPERTY_MANAGEMENT_PORT)) {
      oldProperty = node.getServer().getServerModel().getManagementPort();
      node.getServer().getServerModel().setManagementPort(Integer.parseInt(value.toString()));
    } else if (id.equals(ServerNode.PROPERTY_NAME)) {
      oldProperty = node.getServer().getServerModel().getServerName();
      node.getServer().getServerModel().setServerName(value.toString());
    } else if (id.equals(ServerNode.PROPERTY_GROUP)) {
      oldProperty = node.getServer().getServerModel().getServerGroup();
      node.getServer().getServerModel().setServerGroup(value.toString());
    } else if (id.equals(ServerNode.PROPERTY_THREADS)) {
      oldProperty = node.getServer().getServerModel().getThreads();
      node.getServer().getServerModel().setThreads(Integer.parseInt(value.toString()));
      node.propertyChange(oldProperty, value);
    }
    node.propertyChange(oldProperty, value);

  }
}
