package com.nomad.cachewiever.editors.editpolicy;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import com.nomad.cachewiever.editors.model.command.ConnectionDeleteCommand;



public class AppConnectionDeleteEditPolicy extends ConnectionEditPolicy {

	@Override
	protected Command getDeleteCommand(GroupRequest arg0) {
		ConnectionDeleteCommand command = new ConnectionDeleteCommand();
		command.setLink(getHost().getModel());
		return command;
	}
}
