package com.nomad.cachewiever.editors.part;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MidpointLocator;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.swt.SWT;

import com.nomad.cachewiever.editors.editpolicy.AppConnectionDeleteEditPolicy;
import com.nomad.cachewiever.editors.model.Connection;


public class ConnectionPart extends AppAbstractConnectionEditPart {
	
	protected IFigure createFigure() {
		PolylineConnection connection = (PolylineConnection) super.createFigure();

		connection.setLineWidth(2);
		PolygonDecoration decoration = new PolygonDecoration();
	    decoration.setTemplate(PolygonDecoration.TRIANGLE_TIP);
	    connection.setTargetDecoration(decoration);
	    Label label = new Label();
	    switch (((Connection) getModel()).getConnectionType())  {
	    		case 1:
	            label.setText("?");
	            connection.setLineStyle(SWT.LINE_DASH);
	            label.setBackgroundColor(ColorConstants.green);
	            break;
        		case 2:
	            label.setText("?");
	            connection.setLineStyle(SWT.LINE_DOT);
	            break;
        		case 3:
        			label.setText("distribute work packages");
        			connection.setLineStyle(SWT.LINE_SOLID);
        			label.setBackgroundColor(ColorConstants.green);
        			break;

        		default: return null;
	    }

    label.setOpaque( true );
    connection.add(label, new MidpointLocator(connection, 0));

		return connection;
	}
	
	protected void refreshVisuals() {
	  super.refreshVisuals();

  }

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.CONNECTION_ROLE, new AppConnectionDeleteEditPolicy());
		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new ConnectionEndpointEditPolicy());
	}
}