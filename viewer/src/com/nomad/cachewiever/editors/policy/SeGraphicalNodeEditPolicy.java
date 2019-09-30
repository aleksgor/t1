package com.nomad.cachewiever.editors.policy;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;

import com.nomad.cachewiever.editors.commands.CreateConnectionCommand;
import com.nomad.cachewiever.editors.commands.ReconnectConnectionCommand;



public class SeGraphicalNodeEditPolicy extends GraphicalNodeEditPolicy {

	protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
		CreateConnectionCommand command = (CreateConnectionCommand) request.getStartCommand();
		EditPart target = getTargetEditPart(request.getLocation());
		

		if (target != null){
			command.setTarget(target.getModel());
		}else{
		  return null;
		}
		return command;
	}


	private EditPart getTargetEditPart(Point r) {
		return getHost().getViewer().findObjectAt(new Point(r.x, r.y));
	}

	/*
	 * (Javadoc)
	 * 
	 * @seeorg.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#
	 * getConnectionCreateCommand
	 * (org.eclipse.gef.requests.CreateConnectionRequest)
	 */
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		CreateConnectionCommand command = new CreateConnectionCommand();
		EditPart source = getTargetEditPart(request.getLocation());
		command.setSource(source.getModel());

		request.setStartCommand(command);
		return command;

	}

	/*
	 * (Javadoc)
	 * 
	 * @seeorg.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#
	 * getReconnectTargetCommand(org.eclipse.gef.requests.ReconnectRequest)
	 */
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		ReconnectConnectionCommand command = new ReconnectConnectionCommand();
		EditPart target = getTargetEditPart(request.getLocation());

		command.setNewTarget( target.getModel());
		return command;

	}

	/*
	 * (Javadoc)
	 * 
	 * @seeorg.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#
	 * getReconnectSourceCommand(org.eclipse.gef.requests.ReconnectRequest)
	 */
	protected Command getReconnectSourceCommand(ReconnectRequest request) {

		ReconnectConnectionCommand command = new ReconnectConnectionCommand();
		EditPart source = getTargetEditPart(request.getLocation());
		command.setNewSource(source.getModel());
		return command;
	}
}
