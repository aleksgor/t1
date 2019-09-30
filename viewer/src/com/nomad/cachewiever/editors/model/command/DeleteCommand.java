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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.commands.Command;

import com.nomad.cachewiever.editors.model.Connection;
import com.nomad.cachewiever.editors.model.Node;


public class DeleteCommand extends Command {

	private Node model;
	private Node parentModel;
	private List<Connection> sourceConnections;
	private List<Connection> targetConnections;
	private HashMap<Node,ArrayList<Connection>> sourceChildConnections = new HashMap<Node,ArrayList<Connection>>() ;
	private HashMap<Node,ArrayList<Connection>> targetChildConnections = new HashMap<Node,ArrayList<Connection>>() ;
	
	public void execute() {
		if (model != null && parentModel != null) {
			removeConnections(sourceConnections);
			removeConnections(targetConnections);
			removeChildConnections(model);
			parentModel.removeChild(model);
		}
	}
	
	public void setModel(Object model) {
		if (model instanceof Node) {
			this.model = (Node)model;
			sourceConnections = getSourceConnections(this.model);
			targetConnections = getTargetConnections(this.model);
		}
	}
	
	public void setParentModel(Object model) {
		if (model instanceof Node) { 
			parentModel = (Node)model;
		}
	}
	
	public void undo() {
		parentModel.addChild(model);
		addConnections(sourceConnections);
		addConnections(targetConnections);
		addChildConnections(model);
	}
	
	private void addConnections(List<Connection> connections) {
		Connection conn;
		for (Iterator<Connection> iter = connections.iterator(); iter.hasNext();) {
			conn = iter.next();
			conn.connect();
		}
	}
	
	private void removeConnections(List<Connection> connections) {
		Connection conn;
		for (Iterator<Connection> iter = connections.iterator(); iter.hasNext();) {
			conn = iter.next();
			conn.disconnect();
		}
	}
	
	private void addChildConnections(Node node) {
		Node child;
		ArrayList<Connection> sourceConnections;
		ArrayList<Connection> targetConnections;
		for (Iterator<Node> iter = node.getChildrenArray().iterator(); iter.hasNext();) {
			child = iter.next();
			sourceConnections = sourceChildConnections.get(child);
			targetConnections = targetChildConnections.get(child);
			addConnections(sourceConnections);
			addConnections(targetConnections);
		}
	}
	
	private ArrayList<Connection> getSourceConnections (Node node) {
		return new ArrayList<Connection>(node.getSourceConnectionsArray());
	}
	
	private ArrayList<Connection> getTargetConnections (Node node) {
		return new ArrayList<Connection>(node.getTargetConnectionsArray());
	}
	
	private void removeChildConnections(Node node) {
		Node child;
		ArrayList<Connection> sourceConnections;
		ArrayList<Connection> targetConnections;
		for (Iterator<Node> iter = node.getChildrenArray().iterator(); iter.hasNext();) {
			child = iter.next();
			sourceChildConnections.put(child, getSourceConnections(child));
			targetChildConnections.put(child, getTargetConnections(child));
			sourceConnections = getSourceConnections(child);
			targetConnections = getTargetConnections(child);
			removeConnections(sourceConnections);
			removeConnections(targetConnections);
			removeChildConnections(child);			
		}
	}
}
