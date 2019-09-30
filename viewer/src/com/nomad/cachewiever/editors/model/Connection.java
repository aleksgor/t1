package com.nomad.cachewiever.editors.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;

import com.nomad.model.ConnectModel;

public class Connection implements IAdaptable {
	
	public static final int CONNECTION_DESIGN = 1;
	public static final int CONNECTION_RESOURCES = 2;
	public static final int CONNECTION_WORKPACKAGES = 3;
	
	 public static final String PROPERTY_LISTENER_SERVER_PORT="serverPort";
   public static final String PROPERTY_LISTENER_SERVER_HOST="serverHost";
   public static final String PROPERTY_LISTENER_SERVER_MANAGER_PORT="serverManagerPort";
   public static final String PROPERTY_LISTENER_CLIENT_PORT="clientPort";
   public static final String PROPERTY_LISTENER_CLIENT_HOST="clientHost";
   public static final String PROPERTY_LISTENER_THREADS="threads";
   
	 
	 
  private IPropertySource propertySource = null;
	private int connectionType;
	
	protected ListenerNode sourceNode;
	protected ServerNode targetNode;
	private  ConnectModel connectModel; 
	
	public Connection(ListenerNode sourceNode, ServerNode targetNode, int connectionType, ConnectModel connectModel ) {
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
		this.connectionType = connectionType;
		this.connectModel=connectModel;
	}

	public ConnectModel getConnectModel() {
    return connectModel;
  }

  public ListenerNode getSourceNode() {
		return sourceNode;
	}
	
	public ServerNode getTargetNode() {
		return targetNode;
	}

	public void connect() {
		sourceNode.addConnections(this);
		targetNode.addConnections(this);
	}
	
	public void disconnect() {
		sourceNode.removeConnection(this);
		targetNode.removeConnection(this);
	}
	
	public void reconnect(ListenerNode sourceNode, ServerNode targetNode) {
		if (sourceNode == null || targetNode == null ) {
			throw new IllegalArgumentException();
		}
		disconnect();
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
		connect();
	}

	public void setConnectionType(int connectionType) {
		this.connectionType = connectionType;
	}

	public int getConnectionType() {
		return connectionType;
	}

  @SuppressWarnings("rawtypes")
  @Override
  public Object getAdapter(Class adapter) {
    if (adapter == IPropertySource.class) {
      if (propertySource == null)
        propertySource = new NodePropertySource(this);
      return propertySource;
    }
    return null;  
  }

}

