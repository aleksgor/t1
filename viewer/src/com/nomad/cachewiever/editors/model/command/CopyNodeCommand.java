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

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;

import com.nomad.cachewiever.editors.model.Node;
import com.nomad.cachewiever.editors.model.ServerNode;


public class CopyNodeCommand extends Command {
	private ArrayList<Node> list = new ArrayList<Node>();
	
	public boolean addElement(Node node) { 
		if (!list.contains(node)) {
			return list.add(node); 
		}
		return false;
	}
	
	@Override 
	public boolean canExecute() {
		if (list == null || list.isEmpty()) return false;
		Iterator<Node> it = list.iterator(); 
		while (it.hasNext()) {
			if (!isCopyableNode(it.next())) return false;
		}
		return true;
	}
	
	@Override 
	public void execute() {
		if (canExecute()) Clipboard.getDefault().setContents(list);
	}
	
	@Override 
	public boolean canUndo() {
		return false;
	}
	
	public boolean isCopyableNode(Node node) { 
		if (node instanceof ServerNode )
			return true; 
		return false;
	}
}