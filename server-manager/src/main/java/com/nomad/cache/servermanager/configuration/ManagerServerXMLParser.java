package com.nomad.cache.servermanager.configuration;

import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.nomad.cache.servermanager.models.ProxyServerModel;


public class ManagerServerXMLParser {

	private ProxyServerModel server;
	private String buffer = "";
	

	public ProxyServerModel getManagerServerModel() {
		return server;
	}

	public void parse() {

		try {

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			DefaultHandler handler = new DefaultHandler() {

				public void startElement(String uri, String localName, String qName, Attributes attributes)
						throws SAXException {

					if (qName.equals("server")) {
						server = new ProxyServerModel();
					}
					buffer = "";

				}

				public void endElement(String uri, String localName, String qName) throws SAXException {

					if (qName.equals("port")) {
						server.setPort(new Integer(buffer));
					} else if (qName.equals("threads")) {
						server.setThreads(new Integer(buffer));
					} else if (qName.equals("proxyServerPort")) {
						server.setProxyPort(new Integer(buffer));
					} else if (qName.equals("proxyServerThreads")) {
						server.setProxyThreads(new Integer(buffer));
					} else if (qName.equals("sessionTimeOut")) {
						server.setSessionTimeout(new Integer(buffer));
					}
					
					buffer = "";
				}

				public void characters(char ch[], int start, int length) throws SAXException {
					buffer += new String(ch, start, length);
				}
			};

			InputStream is = ClassLoader.getSystemResourceAsStream("ManagerServer.xml");

			saxParser.parse(is, handler);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
