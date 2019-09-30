/**
 * Created using the GEF Tutorial by
 * Jean-Charles Mammana,
 * Romain Meson,
 * Jonathan Gramain
 *  
 * Modified by Christopher Kebschull
 *  
 */

package com.nomad.cachewiever.editors.model; 

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.ui.views.properties.IPropertySource;

public class Node  implements IAdaptable { 
	private String name; 
	protected Rectangle layout; 
	private List<Node> children; 
	private List<Connection> sourceConnections;
	private List<Connection> targetConnections;
	private Node parent;
	
	private PropertyChangeSupport listeners;
	public static final String PROPERTY_LAYOUT = "NodeLayout";
	public static final String PROPERTY_ADD = "NodeAddChild";
	public static final String PROPERTY_REMOVE = "NodeRemoveChild";
	public static final String PROPERTY_RENAME = "NodeRename";
	public static final String SOURCE_CONNECTION = "SourceConnectionAdded";
	public static final String TARGET_CONNECTION = "TargetConnectionAdded";
  public static final String PROPERTY_CHANGE = "PropertyChange";
	
	protected IPropertySource propertySource = null;
	
	public Node(){ 
		this.name = "Unknown"; 
		this.layout = new Rectangle(10, 10, 100, 100); 
		this.children = new ArrayList<Node>(); 
		this.parent = null; 
		this.sourceConnections = new ArrayList<Connection>();
		this.targetConnections = new ArrayList<Connection>();
		this.listeners = new PropertyChangeSupport(this);
	} 
	
	public void setName(String name) { 
		String oldName = this.name;
		this.name = name;
		getListeners().firePropertyChange(PROPERTY_RENAME, oldName, this.name);
	} 
	
	public String getName() { 
		return this.name; 
	} 
	
	public void setLayout(Rectangle newLayout) { 
		Rectangle oldLayout = this.layout;
		this.layout = newLayout;
		getListeners().firePropertyChange(PROPERTY_LAYOUT, oldLayout, newLayout);  
		
	} 
  public void propertyChange(Object oldProperty, Object newProperty) { 
    getListeners().firePropertyChange(PROPERTY_CHANGE, oldProperty, newProperty);  
    
  } 
	
	public Rectangle getLayout() { 
		return this.layout; 
		
	} 
	
	public boolean addChild(Node child) { 
		boolean b = this.children.add(child); 
		if (b) {
			child.setParent(this);
			getListeners().firePropertyChange(PROPERTY_ADD,null,child);
		}
		return b;
		
	} 
	
	public boolean removeChild(Node child) {
		boolean b = this.children.remove(child);
		if (b) {
			getListeners().firePropertyChange(PROPERTY_REMOVE, child, null);
		}
		return b; 
	} 
	
	public boolean addConnections (Connection conn) {
		if (conn.getSourceNode() == this) { 
			if (!sourceConnections.contains(conn)) {
				if (sourceConnections.add(conn)) {
					getListeners().firePropertyChange(SOURCE_CONNECTION, null, conn);
					return true;	
				}
				return false;
			}
		}
		else if (conn.getTargetNode() == this) { 
			if (!targetConnections.contains(conn)) {
				if (targetConnections.add(conn)) {
					getListeners().firePropertyChange(TARGET_CONNECTION, null, conn);
					return true;
				}
				return false;
			}
		}
		return false;
	}
	
	public boolean removeConnection(Connection conn) {
		if (conn.getSourceNode() == this) { 
			if (sourceConnections.contains(conn)) {
				if (sourceConnections.remove(conn)) {
					getListeners().firePropertyChange(SOURCE_CONNECTION, null, conn);
					return true;
				}
				return false;
			}
		}
		else if (conn.getTargetNode() == this) {
			if (targetConnections.contains(conn)) {
				if (targetConnections.remove(conn)) {
					getListeners().firePropertyChange(TARGET_CONNECTION, null, conn);
					return true;
				}
				return false;
			}
		}	
		return false;
	}
	
	public List<Node> getChildrenArray() { 
		return this.children; 
		
	}
	public void setParent(Node parent) { 
		this.parent = parent; 
		
	} 
	
	public Node getParent() { 
		return this.parent; 
		
	} 
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}
	
	public PropertyChangeSupport getListeners() {
		return listeners;
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.removePropertyChangeListener(listener);
	}

	@SuppressWarnings("rawtypes")
  @Override
	public Object getAdapter(Class adapter) {
	  /*
		if (adapter == IPropertySource.class) {
			if (propertySource == null)
				propertySource = new NodePropertySource(this);
			return propertySource;
		}
		*/
		return null;
	}
	
	public boolean contains (Node child) {
		return children.contains(child);
	}
	
	public List<Connection> getSourceConnectionsArray() {
		return this.sourceConnections;
	}
	
	public List<Connection> getTargetConnectionsArray() {
		return this.targetConnections;
	}
}

