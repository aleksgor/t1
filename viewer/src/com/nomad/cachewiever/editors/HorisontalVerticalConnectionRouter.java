package com.nomad.cachewiever.editors;

import org.eclipse.draw2d.AbstractRouter;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;

public final class HorisontalVerticalConnectionRouter extends AbstractRouter {

	public HorisontalVerticalConnectionRouter() {
	}

	int delta = 20;

	public void route(Connection conn) {
		if (conn.getSourceAnchor() == null || conn.getTargetAnchor() == null)
			return;
		IFigure ifSource = conn.getSourceAnchor().getOwner();
		IFigure ifTarget = conn.getTargetAnchor().getOwner();
		if(ifSource==null || ifTarget==null)
			return;
		
		int cenrerSource = ifSource.getBounds().x + ifSource.getBounds().width / 2;
		int cenrerTarget = ifTarget.getBounds().x + ifTarget.getBounds().width / 2;

		Point begin = new Point();
		Point end = new Point();
		Point t1 = new Point();
		Point t2 = new Point();
		Point t3 = new Point();

		Point endTmp = new Point();

		PointList points = new PointList();

		end.x = ifTarget.getBounds().x + ifTarget.getBounds().width / 2;
		end.y = ifTarget.getBounds().y;
		t3.x = end.x;

		if (cenrerSource < cenrerTarget) {
			begin.x = ifSource.getBounds().x + ifSource.getBounds().width;
			begin.y = ifSource.getBounds().y + ifSource.getBounds().height / 2;
			endTmp.x = ifTarget.getBounds().x;
			endTmp.y = ifTarget.getBounds().y + ifTarget.getBounds().height / 2;
		} else {
			begin.x = ifSource.getBounds().x;
			begin.y = ifSource.getBounds().y + ifSource.getBounds().height / 2;
			endTmp.x = ifTarget.getBounds().x + ifTarget.getBounds().width;
			endTmp.y = ifTarget.getBounds().y + ifTarget.getBounds().height / 2;
		}

		int vpl = Math.min(begin.x, endTmp.x) + (Math.abs(begin.x - endTmp.x)) / 2;
		int hy = end.y - delta;
		// vertical
		begin.y = ifSource.getBounds().y + ifSource.getBounds().height / 2;
		if (vpl >= ifSource.getBounds().x - delta / 2 && vpl <= ifSource.getBounds().x + ifSource.getBounds().width + delta / 2) {// outer
			if (cenrerSource > cenrerTarget) {// line on left
				begin.x = ifSource.getBounds().x;
				if (hy > begin.y)  // correct vpl
					vpl = ifSource.getBounds().x - delta;
				 else 
					vpl = Math.min(ifSource.getBounds().x, ifTarget.getBounds().x) - delta;
				

			} else {// right
				begin.x = ifSource.getBounds().x + ifSource.getBounds().width;
				if (hy > begin.y)  // correct vpl
					vpl = ifSource.getBounds().x + ifSource.getBounds().width + delta;
				 else 
					vpl = Math.max((ifSource.getBounds().x + ifSource.getBounds().width), (ifTarget.getBounds().x + ifTarget.getBounds().width)) + delta;
				
			}
		} else { // inner
			if (cenrerSource > cenrerTarget) {// line on left
				begin.x = ifSource.getBounds().x;
				vpl = ifTarget.getBounds().x + ifTarget.getBounds().width + (begin.x - ifTarget.getBounds().x - ifTarget.getBounds().width) / 2;
			} else {// right
				begin.x = ifSource.getBounds().x + ifSource.getBounds().width;
				vpl = ifSource.getBounds().x + ifSource.getBounds().width + (ifTarget.getBounds().x - ifSource.getBounds().x - ifSource.getBounds().width) / 2;
			}
		}

		t1.x = vpl;
		t1.y = begin.y;
		t2.x = t1.x;
		t2.y = hy;
		t3.x = end.x;
		t3.y = hy;

		points.addPoint(begin);
		points.addPoint(t1);
		points.addPoint(t2);
		points.addPoint(t3);
		points.addPoint(end);
		conn.setPoints(points);

	}

}
