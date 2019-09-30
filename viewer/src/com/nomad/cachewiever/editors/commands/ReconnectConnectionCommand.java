package com.nomad.cachewiever.editors.commands;


import org.eclipse.gef.commands.Command;


public class ReconnectConnectionCommand extends Command {
	@SuppressWarnings("unused")
  private Object newSource = null;
	@SuppressWarnings("unused")
  private Object newTarget = null;

	
	/* (Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
	}



	public void setNewSource(Object model) {
		newSource =  model;
	}

	public void setNewTarget(Object model) {
		newTarget =  model;
	}

	/* (Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {

	}
}
