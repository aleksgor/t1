package com.nomad.utility;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ParseXml {

    /** Creates a new instance of ParseXML */
    public ParseXml() {
    }

    public static String getNodeText(Node node) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            return node.getNodeValue();
        }
        NodeList nodeList = node.getChildNodes();
        int n = nodeList.getLength();
        for (int i = 0; i < n; i++) {
            Node node2 = nodeList.item(i);
            String result = getNodeText(node2);
            if (result != null)
                return result;
        }
        return null;
    }

    public static String getAttributeText(Node node, String name) {
        NamedNodeMap attributes = node.getAttributes();
        int n = attributes.getLength();
        for (int i = 0; i < n; i++) {
            Node currentNode = attributes.item(i);
            if (currentNode.getNodeType() == Node.ATTRIBUTE_NODE) {
                String nodeName = currentNode.getNodeName();
                if (nodeName.equals(name)) {
                    return currentNode.getNodeValue();
                }
            }
        }
        return null;
    }

    public static int getAttributeInt(Node node, String name) {
        String s=getAttributeText(node,name);
        int result=0;

        try{
            result=Integer.parseInt(s);
        }catch(Throwable t){}
        return result;
    }

    public static String getAttributeTextCS(Node node, String name) {
        NamedNodeMap attributes = node.getAttributes();
        int n = attributes.getLength();
        for (int i = 0; i < n; i++) {
            Node currentNode = attributes.item(i);
            if (currentNode.getNodeType() == Node.ATTRIBUTE_NODE) {
                String nodeName = currentNode.getNodeName();
                if (nodeName.toUpperCase().equals(name.toUpperCase())) {
                    return currentNode.getNodeValue();
                }
            }
        }
        return null;
    }

    public static int getNodeInteger(Node node) {
        String s = getNodeText(node);
        if (s != null) {
            try {
                return Integer.parseInt(s);
            } catch (Throwable x) {
            }
        }
        return 0;
    }

    public static long getNodeLong(Node node) {
        String s = getNodeText(node);
        if (s != null) {
            try {
                return Long.parseLong(s);
            } catch (Throwable x) {
            }
        }
        return 0;
    }

    public static String fUp(String s) {
        if (s == null)
            return null;
        if (s.length() == 0)
            return "";
        if (s.length() == 1)
            return s.toUpperCase();
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static String fDown(String s) {
        if (s == null)
            return null;
        if (s.length() == 0)
            return "";
        if (s.length() == 1)
            return s.toLowerCase();
        return s.substring(0, 1).toLowerCase() + s.substring(1);
    }

    public static boolean getNodeBoolean(Node node, boolean defaultValue) {
        String s = getNodeText(node);
        boolean result = defaultValue;
        if (s.equals("1"))
            result = true;
        if (s.equals("0"))
            result = false;
        if (s.equals("true"))
            result = true;
        if (s.equals("false"))
            result = false;
        return result;
    }

}
