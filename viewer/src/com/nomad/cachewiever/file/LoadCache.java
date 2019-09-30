package com.nomad.cachewiever.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.nomad.model.ServerModel;
import com.nomad.saver.Load;

public class LoadCache extends Load {

  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if (qName.equals("cache")) {
      stack.push(new ArrayList<ServerModel>());
    } else {
      super.startElement(uri, localName, qName, attributes);
    }
  }

  @SuppressWarnings("unchecked")
  public List<ServerModel> parseServerList(File f) throws ParserConfigurationException, SAXException, IOException {

    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = factory.newSAXParser();
    saxParser.parse(f.getAbsolutePath(), this);
    return (List<ServerModel>) stack.pop();
  }

  @SuppressWarnings("unchecked")
  public void endElement(String uri, String localName, String qName) throws SAXException {
    Object container = stack.lastElement();

    if (container instanceof ServerModel) {
      if (qName.equals("serverModel")) {
        ServerModel sm = (ServerModel) container;
        stack.pop();
        List<ServerModel> l = (List<ServerModel>) stack.lastElement();
        l.add(sm);
      } else {
        super.endElement(uri, localName, qName);
      }
    } else {
      super.endElement(uri, localName, qName);

    }

  }

  protected void endCommonElement(String uri, String localName, String qName, ServerModel model) throws SAXException {
    if ("x".equals(qName)) {
      model.getProperties().put("x", text);
    } else if ("y".equals(qName)) {
      model.getProperties().put("y", text);
    } else if ("h".equals(qName)) {
      model.getProperties().put("h", text);
    } else if ("w".equals(qName)) {
      model.getProperties().put("w", text);
    } else {
      super.endCommonElement(uri, localName, qName, model);
    }
  }

}
