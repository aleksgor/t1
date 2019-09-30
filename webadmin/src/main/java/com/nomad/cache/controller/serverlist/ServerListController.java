package com.nomad.cache.controller.serverlist;



import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.TreeNode;

public class ServerListController extends SelectorComposer<Component> {


  public TreeModel<TreeNode<DetailViewModel>> getHostList() {
    return new DefaultTreeModel<>(getstubHosts());
  }

  private ServerTreeNode getstubHosts() {
    final ServerTreeNode root = new ServerTreeNode(null);

    final DetailViewModel h1 = new DetailViewModel("","");
    h1.setHost("localhos");
    final ServerTreeNode sh1 = new ServerTreeNode(h1);
    root.add(sh1);

    final DetailViewModel h2 = new DetailViewModel("","");
    h2.setHost("localhos");
    final ServerTreeNode sh2 = new ServerTreeNode(h2);

    final DetailViewModel h21 = new DetailViewModel("","");
    h21.setType("S1");
    h21.setPort(8090);
    sh2.add(new ServerTreeNode(h21));

    final DetailViewModel h22 = new DetailViewModel("","");
    h22.setType("S1333");
    h22.setPort(8090);
    sh2.add(new ServerTreeNode(h22));

    root.add(sh2);

    return root;
  }
}
