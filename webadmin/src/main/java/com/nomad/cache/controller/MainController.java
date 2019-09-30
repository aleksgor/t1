package com.nomad.cache.controller;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.management.JMX;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Button;
import org.zkoss.zul.Center;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.North;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Panelchildren;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Tree;

import com.nomad.cache.controller.serverlist.DetailViewModel;
import com.nomad.cache.controller.serverlist.ServerTreeNode;
import com.nomad.server.statistic.JmxPublisher;

public class MainController extends GenericForwardComposer<Component> {

    @Wire
    private Tabbox tabbox;
    @Wire
    private Textbox host;
    @Wire
    private Intbox port;

    @Wire
    private Tree serverTree;

    @Wire
    private Timer timer;

    private final Map<String, MBeanServerConnection> hostPorts = new HashMap<>();

    private DefaultTreeModel<DetailViewModel> treeModel;

    private final Map<String, Tab> tabs = new HashMap<>();

    private final String ELEMENT_COUNT_ATTRUBUTE = "elementCountAttrubute";
    private final String CONNECT_ATTRUBUTE = "connectAttribute";


    public void onClick$connect(){
        try {
            String hostPort = host.getValue() + ":" + port.getValue();
            MBeanServerConnection mbsc = hostPorts.get(hostPort);
            if (mbsc == null) {

                mbsc = getConnect(hostPort);
                if (mbsc != null) {
                    Tab tab = tabs.get(host);
                    if (tab != null) {
                        tab.setSelected(true);
                        return;
                    }

                    hostPorts.put(hostPort, mbsc);
                    registerConnection(mbsc, host.getValue());
                    Tabpanel tpanel = getNewTabpanel(host.getValue());
                    tpanel.setAttribute(CONNECT_ATTRUBUTE, mbsc);
                } else {
                    Messagebox.show("hostPort does not work", "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void onTimer$timer () {
        refresh();
    }

    public void refresh() {
        Tabpanel panel = tabbox.getSelectedPanel();
        if(panel==null){
            return;
        }
        MBeanServerConnection mbsc= (MBeanServerConnection) panel.getAttribute(CONNECT_ATTRUBUTE);
        Grid panelsGrid = (Grid) panel.getFirstChild().getFirstChild().getFirstChild().getFirstChild().getFirstChild();

        for (Component componentRow : panelsGrid.getRows().getChildren()) {
            for (Component componentPanel : componentRow.getChildren()) {
                Panel gpanel = (Panel) componentPanel;
                DetailViewModel pdata = (DetailViewModel) gpanel.getAttribute("data");
                try {
                    updateGridInfo(((Grid) gpanel.getFirstChild().getFirstChild()), pdata,mbsc);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private Tabpanel  getNewTabpanel(String host) {
        Tab newTab = new Tab(host);
        newTab.setParent(tabbox.getTabs());

        Tabpanel newTabpanel = new Tabpanel();

        newTabpanel.setParent(tabbox.getTabpanels());
        newTabpanel.setHeight("100%");
        newTabpanel.setVflex("1");

        Borderlayout layout = new Borderlayout();
        layout.setParent(newTabpanel);
        layout.setWidth("100%");
        Center center = new Center();
        center.setAutoscroll(true);
        center.setParent(layout);
        North north = new North();
        north.setParent(layout);
        Button btn = new Button();
        btn.setParent(north);
        btn.setLabel("Refresh");
        EventListener<MouseEvent> clickListener = new EventListener<MouseEvent>() {
            @Override
            public void onEvent(MouseEvent event) throws Exception {
                refresh();
            }
        };
        btn.addEventListener(Events.ON_CLICK, clickListener);

        Panel panel = new Panel();
        panel.setParent(center);
        Panelchildren pch = new Panelchildren();
        pch.setParent(panel);

        Grid grid = new Grid();
        grid.setAttribute(ELEMENT_COUNT_ATTRUBUTE, 0);
        grid.setParent(pch);
        Columns columns = new Columns();
        columns.setParent(grid);

        for (int i = 0; i < 5; i++) {
            Column column = new Column();
            column.setParent(columns);
        }

        Rows rows = new Rows();
        rows.setParent(grid);
        return newTabpanel;
    }

    public void onDoubleClick$serverTree() {

        ServerTreeNode treeNode = serverTree.getSelectedItem().getValue();
        selectTab(treeNode.getData());
        if (treeNode.getData() == null) {
            return;
        }
        if (treeNode.getData().getBean() == null) {
            return;
        }
        Grid grid = selectGrid(treeNode.getData());
        Tabpanel panel = tabbox.getSelectedPanel();
        MBeanServerConnection mbsc= (MBeanServerConnection) panel.getAttribute(CONNECT_ATTRUBUTE);
        if (grid != null) {
            Panel newPanel = getNewPanel(treeNode.getData(), mbsc);
            addPanel(grid, newPanel);
        }

    }

    private void addPanel(Grid grid, Panel panel) {
        int contPanels = (int) grid.getAttribute(ELEMENT_COUNT_ATTRUBUTE);
        int column = grid.getColumns().getChildren().size();
        Row row = null;
        if ((contPanels % column) == 0) {
            // add row
            row = new Row();
            row.setValign("top");
            row.setParent(grid.getRows());
        } else {
            // getLastRow
            row = (Row) grid.getRows().getLastChild();
        }
        panel.setParent(row);
        grid.setAttribute(ELEMENT_COUNT_ATTRUBUTE, (++contPanels));

    }

    private void selectTab(DetailViewModel data) {
        for (Component component : tabbox.getTabs().getChildren()) {
            Tab tab = (Tab) component;
            if (data.getId().equals(tab.getLabel())) {
                tab.setSelected(true);
            }
        }
    }

    private Grid selectGrid(DetailViewModel data) {
        Tabpanel tabPanel = (Tabpanel) tabbox.getTabpanels().getChildren().iterator().next();
        Borderlayout layout = (Borderlayout) tabPanel.getChildren().iterator().next();
        Grid grid = (Grid) layout.getCenter().getFirstChild().getFirstChild().getFirstChild();
        for (Component componentRow : grid.getRows().getChildren()) {
            Row row = (Row) componentRow;
            for (Component componentPanel : row.getChildren()) {
                Panel panel = (Panel) componentPanel;
                DetailViewModel pdata = (DetailViewModel) panel.getAttribute("data");
                if (data.equals(pdata)) {

                    return null;
                }

            }
        }
        return grid;
    }

    private Panel getNewPanel(DetailViewModel data,MBeanServerConnection mbsc) {
        Panel panel = new Panel();
        panel.setAttribute("data", data);
        panel.setTitle(data.getFullName());
        Panelchildren ch = new Panelchildren();
        ch.setParent(panel);

        try {
            Grid grid = new Grid();
            grid.setParent(ch);
            Columns columns = new Columns();
            columns.insertBefore(new Column("name"), null);
            columns.insertBefore(new Column("Value"), null);
            columns.setParent(grid);

            Rows rows = new Rows();
            rows.setParent(grid);

            updateGridInfo(grid, data,mbsc);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return panel;
    }

    private void updateGridInfo(Grid grid, final DetailViewModel data,final MBeanServerConnection mbsc) throws Exception {
        grid.getRows().getChildren().clear();
        List<String[]> values = getattributes(data.getBean());
        for (String[] strings : values) {
            Row row = new Row();
            row.insertBefore(new Label(strings[0]), null);
            row.insertBefore(new Label(strings[1]), null);
            grid.getRows().insertBefore(row, null);
        }

        MBeanOperationInfo[] operations=data.getOperations();
        for (final MBeanOperationInfo mBeanOperationInfo : operations) {
            Row row = new Row();
            row.insertBefore(new Label("Operation"), null);
            Button btn= new Button(mBeanOperationInfo.getName());

            btn.addEventListener(Events.ON_CLICK, new EventListener<MouseEvent>() {
                @Override
                public void onEvent(MouseEvent event) throws Exception {
                    mbsc.invoke(data.getObjectName(), mBeanOperationInfo.getName(), null, null);
                }
            });


            row.insertBefore(btn, null);
            row.setAttribute("operation", mBeanOperationInfo);
            grid.getRows().insertBefore(row, null);

        }
    }

    private void registerConnection(MBeanServerConnection mbsc, String host) {
        try {
            Set<ObjectName> names = new TreeSet<>(mbsc.queryNames(null, null));
            DetailViewModel serverDetails = new DetailViewModel(host, host);
            serverDetails.setHost(host);
            String currentAppServerName = null;
            String currentType = null;
            DetailViewModel appServerDetails = null;
            DetailViewModel typeDetails = null;

            for (ObjectName name : names) {
                String fullName = name.getCanonicalName();
                ObjectInstance instance = mbsc.getObjectInstance(name);
                MBeanInfo info=mbsc.getMBeanInfo(name);
                MBeanOperationInfo[] operations =info.getOperations();
                System.out.println( "name:"+name);
                for (MBeanOperationInfo mBeanOperationInfo : operations) {
                    System.out.println( "op:"+mBeanOperationInfo.getName());
                }
                if (fullName.startsWith(JmxPublisher.CACHE_SERVER_PREFIX)) {

                    String className = instance.getClassName();

                    Class<?> proxyClazz = Class.forName(className);
                    Class<?>[] interfaces = proxyClazz.getInterfaces();
                    Class<?> interfaceClass = interfaces[0];

                    Object data = JMX.newMXBeanProxy(mbsc, name, interfaceClass);
                    String panelName = "";
                    String appServerName = getServerName(fullName);
                    panelName += appServerName;
                    if (currentAppServerName == null || !currentAppServerName.equals(appServerName)) {
                        currentAppServerName = appServerName;
                        appServerDetails = new DetailViewModel(host, panelName);
                        appServerDetails.setObjectName(name);
                        appServerDetails.setData(data);
                        appServerDetails.setName(appServerName);
                        serverDetails.getServers().add(appServerDetails);

                        currentType = null;

                    }
                    String type = getType(fullName);
                    panelName += ":" + type;
                    if (currentType == null || !currentType.equals(type)) {
                        currentType = type;
                        typeDetails = new DetailViewModel(host, panelName);
                        typeDetails.setObjectName(name);
                        typeDetails.setType(type);
                        typeDetails.setData(data);
                        appServerDetails.getServers().add(typeDetails);
                    }
                    DetailViewModel details = getDetails(fullName, data, host, panelName,name);
                    if (details == null) {
                        typeDetails.setBean(data);
                    } else {
                        details.setOperations(operations);
                        details.setBean(data);
                        typeDetails.getServers().add(details);
                    }

                }
            }
            ServerTreeNode root = new ServerTreeNode(null);
            root.add(getTree(serverDetails));
            treeModel = new DefaultTreeModel<DetailViewModel>(root);
            serverTree.setModel(treeModel);

        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    private ServerTreeNode getTree(DetailViewModel serverDetails) {
        ServerTreeNode result = new ServerTreeNode(serverDetails);
        for (DetailViewModel tree : serverDetails.getServers()) {
            result.add(getTree(tree));
        }
        return result;
    }

    private String getType(String fullName) {
        String type = "";
        int index = fullName.indexOf("type=");
        if (index >= 0) {
            String rawType = fullName.substring(index + "type=".length());
            index = rawType.indexOf(",");
            if (index >= 0) {
                type = rawType.substring(0, index);
            } else {
                type = rawType;
            }
        }
        return type;
    }

    private DetailViewModel getDetails(String fullName, Object data, String id, String panelName, ObjectName objectName) {
        String name = "";
        int index = fullName.indexOf("name=");
        if (index >= 0) {
            name = fullName.substring(index + "name=".length());
            index = name.indexOf(",");
            if (index >= 0) {
                name = name.substring(0, index);
            }
        } else {
            return null;
        }
        DetailViewModel result = new DetailViewModel(id, panelName + ":" + name);
        result.setObjectName(objectName);
        result.setData(data);
        index = name.lastIndexOf("-");
        if (index > 0) {
            String port = name.substring(index + 1);
            result.setPort(Integer.parseInt(port));
        } else {
            result.setDetail(name);
        }
        result.setBean(data);
        return result;
    }

    private String getServerName(String objectName) {
        String name = objectName.substring(JmxPublisher.CACHE_SERVER_PREFIX.length());
        int index = name.indexOf(":");
        if (index > 0) {
            name = name.substring(0, index);
        }
        return name;
    }

    private MBeanServerConnection getConnect(String hostPort) {
        try {
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + hostPort + "/jmxrmi");

            JMXConnector jmxc = JMXConnectorFactory.connect(url, null);

            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

            return mbsc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Set<String> prhobitedData = new HashSet<>();
    static {
        prhobitedData.add("class");
        prhobitedData.add("servers");
        prhobitedData.add("data");

    }

    private List<String[]> getattributes(Object data) throws Exception {
        List<String[]> result = new ArrayList<>();
        PropertyDescriptor[] descriptors = Introspector.getBeanInfo(data.getClass()).getPropertyDescriptors();
        for (PropertyDescriptor propertyDescriptor : descriptors) {
            String name = propertyDescriptor.getDisplayName();
            Object value = propertyDescriptor.getReadMethod().invoke(data);
            if (!prhobitedData.contains(name)) {
                result.add(new String[] { name, "" + value });
            }
        }
        return result;
    }
}
