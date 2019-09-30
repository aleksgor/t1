package com.nomad.cache.layout;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.ext.DragControl;
import org.zkoss.zul.Panel;
import org.zkoss.zul.impl.XulElement;

public class Portalchildren extends XulElement implements DragControl {
    public Portalchildren() {
    }

    @Override
    public String getZclass() {
        return ((_zclass == null) ? "z-portalchildren" : _zclass);
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

    @Override
    public void beforeParentChanged(Component parent) {
        if ((parent != null) && (!(parent instanceof Portallayout)))
            throw new UiException("Wrong parent: " + parent);
        super.beforeParentChanged(parent);
    }

    @Override
    public void beforeChildAdded(Component child, Component refChild) {
        if (!(child instanceof Panel)) {
            throw new UiException("Unsupported child for Portalchildren: " + child);
        }
        super.beforeChildAdded(child, refChild);
    }
}