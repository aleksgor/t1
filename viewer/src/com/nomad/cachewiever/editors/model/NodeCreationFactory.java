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

import org.eclipse.gef.requests.CreationFactory;

public class NodeCreationFactory implements CreationFactory {
	
	private Class<?> template;
	
	public NodeCreationFactory(Class<?> t) {
		this.template = t;
	}

	@Override
	public Object getNewObject() {
		if (template == null) return null;
		if (template == ServerNode.class) {
		  ServerNode srv = new ServerNode();
			srv.setName("new Service");
			return srv;
		}
	
		return null;
	}

	@Override
	public Object getObjectType() {
		return template;
	}

}
