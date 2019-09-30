/**
 * Created using the GEF Tutorial by
 * Jean-Charles Mammana,
 * Romain Meson,
 * Jonathan Gramain
 *  
 * Modified by Christopher Kebschull
 *  
 */

package com.nomad.cachewiever.editors.model.command;

import org.eclipse.gef.commands.Command;

import com.nomad.cachewiever.editors.model.Node;



public class RenameCommand extends Command {
	private Node model; 
	private String oldName; 
	private String newName; 
	
	public void execute() { 
		this.oldName = model.getName(); 
		this.model.setName(newName); 
	} 
	
	public void setModel(Object model) { 
		this.model = (Node)model; 
	} 
	
	public void setNewName(String newName) {
		this.newName = newName; 
	} 
	
	public void undo() { 
		this.model.setName(oldName); 
	} 
}
