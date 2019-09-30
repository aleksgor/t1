package com.nomad.cachewiever.editors.model;


import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.ui.views.properties.IPropertySource;

import com.nomad.cachewiever.editors.model.Node;
import com.nomad.cachewiever.views.model.Server;

public class ServerNode extends Node {

  public static final String PROPERTY_NAME = "ServerName";
  public static final String PROPERTY_GROUP = "ServerGroup";
  public static final String PROPERTY_HOST = "SerberHost";
  public static final String PROPERTY_MANAGEMENT_PORT = "ServerManagementPort";
  public static final String PROPERTY_THREADS = "ServerThreads";
  public static final String PROPERTY_COMMAND_PORT = "ServerCommandPort";
  public static final String PROPERTY_SESSION_TIMEOUT = "ServerSessionTimeout";
  public static final String PROPERTY_CLEANER_TIMER = "CleanerTimer";

  private Server server;

  public ServerNode(Server server){
    this();
    this.server=server;
    
  }
  public ServerNode() {
  }


  public Server getServer() {
    return server;
  }

  public void setServer(Server server) {
    Server oldserver = this.server;
    this.server = server; 
    getListeners().firePropertyChange(PROPERTY_HOST, oldserver, server);


  }
  
  public void setLayout(Rectangle newLayout) { 
    Rectangle oldLayout = this.layout;
    this.layout = newLayout;
    getListeners().firePropertyChange(PROPERTY_LAYOUT, oldLayout, newLayout);  
    
    server.setX(newLayout.x);
    server.setY(newLayout.y);
    server.setH(newLayout.height);
    server.setW(newLayout.width);
    
  } 
  @Override
  public Object clone() throws CloneNotSupportedException {
    ServerNode srv = new ServerNode();

    /*
    ServerNode srv = new ServerNode();
    srv.setColor(this.color);
    srv.setServer(this.server);
    srv.setName(this.getName());
    srv.setParent(this.getParent());
    srv.setLayout(new Rectangle(getLayout().x + 10, getLayout().y + 10, getLayout().width, getLayout().height));
    Iterator<Node> it = this.getChildrenArray().iterator();
    while (it.hasNext()) {
      Node node = it.next();
      if (node instanceof ListenerNode) {
        ListenerNode child = (ListenerNode) node;
      //  Node clone = (Node) child.clone();
      //  srv.addChild(clone);
      //  clone.setLayout(child.getLayout());
      }
    }
    */
    return srv;
  }
  
  @SuppressWarnings("rawtypes")
  public Object getAdapter(Class adapter) {
    if (adapter == IPropertySource.class) {
      if (propertySource == null)
        propertySource = new ServerPropertySource(this);
      return propertySource;
    }
    return null;
  }

}