package com.nomad.cachewiever.editors.model.command;

import org.eclipse.gef.commands.Command;

import com.nomad.cachewiever.editors.model.Connection;
import com.nomad.cachewiever.editors.model.ListenerNode;
import com.nomad.cachewiever.editors.model.ServerNode;
import com.nomad.model.ConnectModel;
import com.nomad.model.ServerModel;


public class ConnectionCreateCommand extends Command {
	
	private ListenerNode sourceNode; 
	private ServerNode targetNode;
	private Connection conn;
	private int connectionType;
	
	public void setSourceNode(ListenerNode sourceNode) {
		this.sourceNode = sourceNode;
	}
	
	public void setTargetNode(ServerNode targetNode) {
		this.targetNode = targetNode;
	}
	
	@Override 
	public boolean canExecute() { 
		if (sourceNode == null || targetNode == null) 
			return false;
		else if (sourceNode.equals(targetNode)) 
			return false;
		return true; 
	}
	
	@Override 
	public void execute() {
	  
	  ConnectModel connectModel= new ConnectModel();
	  ServerModel sourceServer=((ServerNode)sourceNode.getParent()).getServer().getServerModel();
	  connectModel.setServerHost(sourceServer.getHost());
    connectModel.setServerPort(sourceNode.getListener().getPort());
    connectModel.setServerManagementPort(sourceServer.getManagementPort());
    connectModel.setClientHost(((ServerNode)targetNode).getServer().getServerModel().getHost());
    connectModel.setClientManagementPort(((ServerNode)targetNode).getServer().getServerModel().getManagementPort());
    connectModel.setConnectThreads(sourceNode.getListener().getThreads());
    ((ServerNode)targetNode).getServer().getServerModel().getClients().add(connectModel);
		conn = new Connection(sourceNode, targetNode, 1,connectModel);
		conn.connect();
	} 
	
	@Override 
	public boolean canUndo() {
		if (sourceNode == null || targetNode == null || conn == null) 
			return false; 
		return true;  		
	} 
	
	@Override 
	public void undo() { 
		conn.disconnect();
	}

	public void setConnectionType(int connectionType) {
		this.connectionType = connectionType;
	}

	public int getConnectionType() {
		return connectionType;
	}
}
