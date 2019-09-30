package com.nomad.cache.layout;

import java.util.Map;

import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.AuRequests;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Panel;

public class PortalMoveEvent extends Event {
    private final Portalchildren _from;
    private final Portalchildren _to;
    private final Panel _dragged;
    private final int _droppedIndex;

    public static final PortalMoveEvent getPortalMoveEvent(AuRequest request) {
        Map<String, Object> data = request.getData();
        Desktop desktop = request.getDesktop();
        return new PortalMoveEvent(request.getCommand(), request.getComponent(), (Portalchildren) desktop.getComponentByUuid((String) data.get("from")),
                (Portalchildren) desktop.getComponentByUuid((String) data.get("to")), (Panel) desktop.getComponentByUuid((String) data.get("dragged")),
                AuRequests.getInt(data, "index", 0));
    }

    public PortalMoveEvent(String evtnm, Component target, Portalchildren from, Portalchildren to, Panel dragged, int droppedIndex) {
        super(evtnm, target);
        _from = from;
        _to = to;
        _dragged = dragged;
        _droppedIndex = droppedIndex;
    }

    public Portalchildren getFrom() {
        return _from;
    }

    public Portalchildren getTo() {
        return _to;
    }

    public final Panel getDragged() {
        return _dragged;
    }

    public int getDroppedIndex() {
        return _droppedIndex;
    }
}