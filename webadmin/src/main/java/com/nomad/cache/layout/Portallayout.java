package com.nomad.cache.layout;

import java.io.IOException;
import java.util.List;

import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.sys.ContentRenderer;
import org.zkoss.zul.Panel;
import org.zkoss.zul.impl.XulElement;

public class Portallayout extends XulElement {
    private String _maximizedMode = "column";

    public Portallayout() {
    }

    public Panel getPanel(int col, int row) {
        if ((col < 0) || (row < 0) || (getChildren().size() <= col))
            return null;
        List<Component> children = getChildren().get(col).getChildren();
        return ((children.size() <= row) ? null : (Panel) children.get(row));
    }

    public boolean setPanel(Panel panel, int col, int row) {
        if ((col < 0) || (row < 0) || (panel == null) || (getChildren().size() <= col))
            return false;
        Portalchildren children = (Portalchildren) getChildren().get(col);
        if (children.getChildren().size() <= row) {
            return children.appendChild(panel);
        }
        return children.insertBefore(panel, children.getChildren().get(row));
    }

    public int[] getPosition(Panel panel) {
        int[] pos = { -1, -1 };
        if ((panel == null) || (panel.getParent() == null))
            return pos;
        pos[0] = getChildren().indexOf(panel.getParent());
        if (pos[0] < 0)
            pos[1] = pos[0];
        else
            pos[1] = panel.getParent().getChildren().indexOf(panel);
        return pos;
    }

    public void setMaximizedMode(String mode) {
        if ((!("whole".equals(mode))) && (!("column".equals(mode))))
            throw new WrongValueException("Uknown mode: " + mode);
        if (!(_maximizedMode.equals(mode))) {
            _maximizedMode = mode;
            smartUpdate("maximizedMode", mode);
        }
    }

    public String getMaximizedMode() {
        return _maximizedMode;
    }

    @Override
    public String getZclass() {
        return ((_zclass == null) ? "z-portallayout" : _zclass);
    }

    @Override
    protected void renderProperties(ContentRenderer renderer) throws IOException {
        super.renderProperties(renderer);
        if (!("column".equals(_maximizedMode)))
            render(renderer, "maximizedMode", _maximizedMode);
    }

    @Override
    public void beforeChildAdded(Component child, Component refChild) {
        if (!(child instanceof Portalchildren)) {
            throw new UiException("Unsupported child for Portallayout: " + child);
        }
        super.beforeChildAdded(child, refChild);
    }

    @Override
    public void service(AuRequest request, boolean everError) {
        String cmd = request.getCommand();
        if (cmd.equals("onPortalMove")) {
            PortalMoveEvent evt = PortalMoveEvent.getPortalMoveEvent(request);
            disableClientUpdate(true);
            try {
                Portalchildren to = evt.getTo();
                int droppedIndex = evt.getDroppedIndex();
                Panel dragged = evt.getDragged();

                if ((dragged.getParent() == to) && (droppedIndex >= to.getChildren().indexOf(dragged))) {
                    ++droppedIndex;
                }
                to.insertBefore(dragged, (droppedIndex < to.getChildren().size()) ? (Component) to.getChildren().get(droppedIndex) : null);
            } finally {
                disableClientUpdate(false);
            }
            Events.postEvent(evt);
        } else {
            super.service(request, everError);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

    static {
        addClientEvent(Portallayout.class, "onPortalMove", 1);
    }
}