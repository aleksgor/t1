package com.nomad.datadefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.nomad.exception.SystemException;
import com.nomad.model.Field;
import com.nomad.model.Join;
import com.nomad.model.ModelDescription;
import com.nomad.model.RequestType;
import com.nomad.utility.ParseXml;

public class DataDefinitionFileLoader {

    protected static Logger LOGGER = LoggerFactory.getLogger(DataDefinitionFileLoader.class);


    public Map<String, ModelDescription> loadFile(final String nameFile) throws SystemException{
        try {
            final File file = new File(nameFile);
            List<URL> urls = new ArrayList<>();
            if (file.exists()) {
                urls = java.util.Collections.singletonList(new File(nameFile).toURI().toURL());
            } else {
                final Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(nameFile);
                while (resources.hasMoreElements()) {
                    urls.add(resources.nextElement());
                }
            }
            return loadFiles(urls);
        } catch (final MalformedURLException e) {
            throw new SystemException(e);
            
        } catch (final IOException e) {
            throw new SystemException(e);
        }
    }

    public Map<String, ModelDescription> loadFiles(final List<URL> urls) throws SystemException {
        Map<String, ModelDescription> result = new HashMap<>();
        if (urls == null) {
            return result;
        }
        for (final URL url : urls) {
            LOGGER.info("load config from:"+url);
            try {
                final InputStream inputStream = url.openStream();
                final DocumentBuilderFactory documentBuildFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder b = documentBuildFactory.newDocumentBuilder();
                final Node rootNode = b.parse(inputStream);
                final NodeList nodeList = rootNode.getChildNodes();
                final int n = nodeList.getLength();
                for (int i = 0; i < n; i++) {
                    final Node currentNode = nodeList.item(i);
                    final String nodeName = currentNode.getNodeName();
                    if (currentNode.getNodeType() == 1)
                        if (nodeName.equals("models"))
                            loadModels(currentNode, result);
                }
                inputStream.close();

            } catch (SAXException | ParserConfigurationException | IOException e) {
                throw new SystemException(e);
            }
        }
        return result;
    }

    private void loadModels(final Node node, Map<String, ModelDescription> result) {
        final NodeList nodeList = node.getChildNodes();
        final int n = nodeList.getLength();
        for (int i = 0; i < n; i++) {
            final Node currentNode = nodeList.item(i);
            final String nodeName = currentNode.getNodeName();
            if (currentNode.getNodeType() != Node.TEXT_NODE) {
                if (nodeName.equals("model")) {
                    final ModelDescription tableDescription = new ModelDescriptionImpl();
                    final String name = ParseXml.getAttributeText(currentNode, "name");
                    final String clazz = ParseXml.getAttributeText(currentNode, "class");
                    final String idClass = ParseXml.getAttributeText(currentNode, "identifier");
                    final String tableName = ParseXml.getAttributeText(currentNode, "table");
                    final String extend = ParseXml.getAttributeText(currentNode, "extends");
                    final String parentObject = ParseXml.getAttributeText(currentNode, "object-parent");
                    tableDescription.setParentObject(parentObject);
                    if (extend != null) {
                        final ModelDescription modelDescription = result.get(extend);
                        if (modelDescription != null)
                            tableDescription.setData(modelDescription);
                        tableDescription.setExtend(extend);
                    }
                    tableDescription.setClassId(idClass);
                    tableDescription.setClazz(clazz);
                    tableDescription.setDataBaseName(tableName);
                    tableDescription.setModelName(name);
                    loadXml(currentNode, tableDescription);
                    result.put(tableDescription.getModelName(), tableDescription);
                }
            }
        }
    }

    private void loadXml(final Node node, final ModelDescription model) {
        final NodeList nodeList = node.getChildNodes();
        final int n = nodeList.getLength();
        for (int i = 0; i < n; i++) {
            final Node currentNode = nodeList.item(i);
            final String nodeName = currentNode.getNodeName();
            if (currentNode.getNodeType() != Node.TEXT_NODE) {
                if (nodeName.equals("field")) {
                    final String name = ParseXml.getAttributeText(currentNode, "name");
                    final String primaryKey = ParseXml.getAttributeText(currentNode, "primary-key");
                    final String sType = ParseXml.getAttributeText(currentNode, "type");
                    final FieldImpl field = loadField(currentNode, name);
                    if (sType != null) {
                        final Field.Type type = Field.Type.valueOf("type_" + sType);
                        field.setType(type);
                    }
                    if (field == null){
                        LOGGER.error("Error field :{}  not valid !!", name);
                    }

                    if (primaryKey != null) {
                        field.setIdentifier(primaryKey.equals("true"));
                    }
                    model.addField(field);
                }
                if (nodeName.equals("relation")) {
                    final RelationImpl relation = new RelationImpl();
                    relation.setName(ParseXml.getAttributeText(currentNode, "name"));
                    relation.setChildrenModel(ParseXml.getAttributeText(currentNode, "relationTo"));
                    relation.setFieldName(ParseXml.getAttributeText(currentNode, "field"));
                    final String join = ParseXml.getAttributeText(currentNode, "join");
                    if (join != null) {
                        relation.setJoin(Join.valueOf(join));
                    }

                    relation.setParentModel(model.getModelName());
                    loadRelationXml(currentNode, relation);
                    model.getRelations().put(relation.getName(), relation);
                }
            }
        }
    }

    private void loadRelationXml(final Node node, final RelationImpl relation) {
        final NodeList nodeList = node.getChildNodes();
        final int n = nodeList.getLength();
        for (int i = 0; i < n; i++) {
            final Node currentNode = nodeList.item(i);
            final String nodeName = currentNode.getNodeName();
            if (currentNode.getNodeType() != Node.TEXT_NODE) {
                if (nodeName.equals("column")) {
                    final ConditionImpl item = new ConditionImpl();
                    item.setParentFieldName(ParseXml.getAttributeText(currentNode, "name"));
                    item.setChildFieldName(ParseXml.getAttributeText(currentNode, "relationTo"));
                    relation.getConditions().add(item);
                }
            }
        }
    }

    private FieldImpl loadField(final Node node, final String name) {
        final NodeList nodeList = node.getChildNodes();
        final int n = nodeList.getLength();
        FieldImpl result = null;
        for (int i = 0; i < n; i++) {
            final Node currentNode = nodeList.item(i);
            final String nodeName = currentNode.getNodeName();
            if (currentNode.getNodeType() != Node.TEXT_NODE) {
                if (nodeName.equals("column")) {
                    final String tableName = ParseXml.getAttributeText(currentNode, "name");
                    final String type = ParseXml.getAttributeText(currentNode, "sql-type");
                    final String sLength = ParseXml.getAttributeText(currentNode, "length");
                    final String scale = ParseXml.getAttributeText(currentNode, "scale");
                    int length = 0;
                    int iScale = 0;
                    try {
                        if (sLength != null) {
                            length = new Integer(sLength).intValue();
                        }
                    } catch (final Throwable e) {
                    }
                    try {
                        if (scale != null) {
                            iScale = new Integer(scale).intValue();
                        }
                    } catch (final Throwable e) {
                    }
                    final RequestType requestType = getRequestType(type, length, iScale);
                    result = new FieldImpl(name, tableName, requestType);
                    result.setSqlType(getSQLType(type, length, iScale));
                    result.setLength(length);
                }
            }
        }
        return result;
    }

    private RequestType getRequestType(String s, final int length, final int scale) {
        // VARCHAR, LONGVARCHAR, NUMERIC, DECIMAL, BIT, TINYINT, SMALLINT, INTEGER,
        // BIGINT, REAL, FLOAT, DOUBLE,
        // BINARY, VARBINARY, LONGVARBINARY,
        // DATE, TIME, TIMESTAMP
        /*
         * STRING=1; DOUBLE=2; DATE=3; INT=4; LONG=5; FLOAT=6;
         */
        s = s.toUpperCase();
        RequestType result = RequestType.UNDEFINED;
        if (s.equals("REAL") || s.equals("FLOAT") || s.equals("DOUBLE") || s.equals("FLOAT8"))
            result = RequestType.DOUBLE;
        if (s.equals("DATE") || s.equals("TIME") || s.equals("TIMESTAMP"))
            result = RequestType.DATE;

        if (s.equals("VARCHAR") || s.equals("VARCHAR2") || s.equals("NVARCHAR2") || s.equals("CHAR") || s.equals("TEXT") || s.equals("STRING"))
            result = RequestType.STRING;

        if (s.equals("LONGVARCHAR") || s.equals("CLOB"))
            result = RequestType.CLOB;
        if (s.equals("BLOB") || s.equals("BYTEA"))
            result = RequestType.BLOB;
        if (s.equals("NUMERIC") || s.equals("DECIMAL") || s.equals("NUMBER")) {
            if (scale == 0) {
                if (length < 10)
                    result = RequestType.INT;
                else
                    result = RequestType.LONG;
            } else
                result = RequestType.DOUBLE;

        }
        if (s.equals("BIT") || s.equals("TINYINT") || s.equals("SMALLINT") || s.equals("INTEGER") || s.equals("INT4") || s.equals("INT"))
            result = RequestType.INT;
        if (s.equals("BIGINT") || s.equals("INT8") || s.equals("LONG"))
            result = RequestType.LONG;

        if (RequestType.UNDEFINED.equals(result))
            LOGGER.error("Error type {} type not found", s);
        return result;
    }

    private int getSQLType(String s, final int length, final int scale) {
        // VARCHAR, LONGVARCHAR, NUMERIC, DECIMAL, BIT, TINYINT, SMALLINT, INTEGER,
        // BIGINT, REAL, FLOAT, DOUBLE,
        // BINARY, VARBINARY, LONGVARBINARY,
        // DATE, TIME, TIMESTAMP
        /*
         * STRING=1; DOUBLE=2; DATE=3; INT=4; LONG=5; FLOAT=6;
         */
        s = s.toUpperCase();
        int result = -99;
        if (s.equals("VARCHAR"))
            result = java.sql.Types.VARCHAR;
        if (s.equals("VARCHAR2"))
            result = java.sql.Types.VARCHAR;
        if (s.equals("CHAR"))
            result = java.sql.Types.VARCHAR;
        if (s.equals("NUMBER")) {
            if (scale == 0) {
                if (length < 10)
                    result = java.sql.Types.INTEGER;
                else
                    result = java.sql.Types.BIGINT;
            } else
                result = java.sql.Types.NUMERIC;

        }
        result = java.sql.Types.VARCHAR;
        if (s.equals("LONGVARCHAR"))
            result = java.sql.Types.LONGVARCHAR;
        if (s.equals("CLOB"))
            result = java.sql.Types.LONGVARCHAR;
        if (s.equals("BLOB"))
            result = java.sql.Types.BLOB;
        if (s.equals("NUMERIC"))
            result = java.sql.Types.NUMERIC;
        if (s.equals("DECIMAL"))
            result = java.sql.Types.DECIMAL;
        if (s.equals("BIT"))
            result = java.sql.Types.BIT;
        if (s.equals("TINYINT"))
            result = java.sql.Types.TINYINT;
        if (s.equals("SMALLINT"))
            result = java.sql.Types.SMALLINT;
        if (s.equals("INTEGER"))
            result = java.sql.Types.INTEGER;
        if (s.equals("BIGINT"))
            result = java.sql.Types.BIGINT;
        if (s.equals("REAL"))
            result = java.sql.Types.REAL;
        if (s.equals("FLOAT"))
            result = java.sql.Types.FLOAT;
        if (s.equals("DOUBLE"))
            result = java.sql.Types.DOUBLE;
        if (s.equals("BINARY"))
            result = java.sql.Types.BINARY;
        if (s.equals("VARBINARY"))
            result = java.sql.Types.VARBINARY;
        if (s.equals("LONGVARBINARY"))
            result = java.sql.Types.LONGVARBINARY;
        if (s.equals("DATE"))
            result = java.sql.Types.DATE;
        if (s.equals("TIME"))
            result = java.sql.Types.TIME;
        if (s.equals("TIMESTAMP"))
            result = java.sql.Types.TIMESTAMP;

        if (result == -99) {
            LOGGER.error("Error type1 {} type not found", s);
        }
        return result;
    }


}
