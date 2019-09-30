package com.nomad.cachewiever.editors.commands;


import org.eclipse.gef.commands.Command;

public class CreateConnectionCommand extends Command {
	private Object source, target;


	/*
	 * (Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#canExecute()
	 */
	public boolean canExecute() {
		if (source == null || target == null)
			return false;
		if (source.equals(target))
			return false;
		return true;
	}

	/*
	 * (Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
	}


	public void setSource(Object model) {
		source = model;
	}

	public void setTarget(Object model) {
		target = model;
	}

	public Object getSource() {
		return source;
	}

	public Object getTarget() {
		return target;
	}

	/*
	 * (Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
	}


}
