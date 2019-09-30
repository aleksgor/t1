/**
 * Created using the GEF Tutorial by
 * Jean-Charles Mammana,
 * Romain Meson,
 * Jonathan Gramain
 *  
 * Modified by Christopher Kebschull
 *  
 */

package com.nomad.cachewiever.editors.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;

public class RootFigure extends Figure {
	private XYLayout layout;
	
	public RootFigure() {
		layout = new XYLayout();
		setLayoutManager(layout);
		
		
		setForegroundColor(ColorConstants.black);
		setBorder(new LineBorder(5));
		
	}
	

	public void setLayout(Rectangle rect) {
		setBounds(rect);
	}

}
