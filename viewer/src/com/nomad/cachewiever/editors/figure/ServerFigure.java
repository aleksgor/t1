package com.nomad.cachewiever.editors.figure;


import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Rectangle;

import com.nomad.cachewiever.editors.model.ServerNode;
import com.nomad.cachewiever.utility.CacheColorConstants;

public class ServerFigure extends Figure {

  private ServerNode server = null;
  private Label name = new Label();

  private Label host;
  private Label mport;
  private Label cport;
  private Label threads;
  private Label cltimeout;

  public ServerFigure(ServerNode serverNode) {
    this.server = serverNode;
    // setLayoutManager(new GridLayout(1, false));
    ToolbarLayout layout = new ToolbarLayout();
    layout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
    setLayoutManager(layout);
    name.setText("Server:" + server.getServer().getServerModel().getServerName());
    name.setForegroundColor(ColorConstants.darkGray);
    add(name);

    Panel labels = new Panel();
    labels.setLayoutManager(new GridLayout(1, false));
    add(labels);

    host = addLabel("host:" + serverNode.getServer().getServerModel().getHost(), labels);
    mport = addLabel("management port:" + serverNode.getServer().getServerModel().getManagementPort(), labels);
    cport = addLabel("command port:" + serverNode.getServer().getServerModel().getCommandPort(), labels);
    threads = addLabel("Proxy threads:" + serverNode.getServer().getServerModel().getThreads(), labels);
    cltimeout = addLabel("cleaner timer (ms):" + serverNode.getServer().getServerModel().getCleanerTimer(), labels);

    setForegroundColor(CacheColorConstants.darkGray);
    setBackgroundColor(ColorConstants.white);
    setBorder(new LineBorder(1));
    setOpaque(true);

  }

  private Label addLabel(String data, IFigure parent) {
    Label label = new Label(data);
    label.setForegroundColor(ColorConstants.darkGray);
    parent.add(label);
    return label;
  }

  public ServerNode getServerNode() {
    return server;
  }

  public void setLayout(Rectangle rect) {
    name.setText("Server:" + server.getServer().getServerModel().getServerName());

    host.setText("host:" + server.getServer().getServerModel().getHost());
    mport.setText("management port:" + server.getServer().getServerModel().getManagementPort());
    cport.setText("command port:" + server.getServer().getServerModel().getCommandPort());
    threads.setText("Proxy threads:" + server.getServer().getServerModel().getThreads());
    cltimeout.setText("cleaner timer (ms):" + server.getServer().getServerModel().getCleanerTimer());
    getParent().setConstraint(this, rect);
  }
}