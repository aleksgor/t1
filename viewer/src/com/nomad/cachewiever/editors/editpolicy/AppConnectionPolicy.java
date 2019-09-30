package com.nomad.cachewiever.editors.editpolicy;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;

import com.nomad.cachewiever.editors.model.Connection;
import com.nomad.cachewiever.editors.model.ListenerNode;
import com.nomad.cachewiever.editors.model.ServerNode;
import com.nomad.cachewiever.editors.model.command.ConnectionCreateCommand;
import com.nomad.cachewiever.editors.model.command.ConnectionReconnectCommand;



public class AppConnectionPolicy extends GraphicalNodeEditPolicy {

	@Override
	protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
		ConnectionCreateCommand cmd = (ConnectionCreateCommand)request.getStartCommand();
		ServerNode targetNode = (ServerNode)getHost().getModel();
		cmd.setTargetNode(targetNode);
		return cmd;
	}

	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		ConnectionCreateCommand cmd = new ConnectionCreateCommand();
		ListenerNode sourceNode = (ListenerNode)getHost().getModel();
		cmd.setConnectionType(Integer.parseInt(request.getNewObjectType().toString()));
		cmd.setSourceNode(sourceNode);
		request.setStartCommand(cmd);
		return cmd;
	}

	@Override
	protected Command getReconnectSourceCommand(ReconnectRequest request) {
		Connection conn = (Connection)request.getConnectionEditPart().getModel();
		ListenerNode sourceNode = (ListenerNode)getHost().getModel();
		ConnectionReconnectCommand cmd = new ConnectionReconnectCommand(conn);
		cmd.setNewSourceNode(sourceNode);		
		return cmd;
	}

	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		Connection conn = (Connection)request.getConnectionEditPart().getModel();
		ServerNode targetNode = (ServerNode)getHost().getModel();
		ConnectionReconnectCommand cmd = new ConnectionReconnectCommand(conn);
		cmd.setNewTargetNode(targetNode);		
		return cmd;
	}
}
