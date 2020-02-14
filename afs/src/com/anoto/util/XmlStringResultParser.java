package com.anoto.util;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XmlStringResultParser {

	public static String parser(String xmlData, String tagResponseName, String tagResultName) {
		String resultado = "";
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xmlData));
			Document doc = dBuilder.parse(is );
			doc.getDocumentElement().normalize();

			NodeList nodes = doc.getElementsByTagName(tagResponseName);
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					resultado = getValue(tagResultName, element);					
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return resultado;
	}

	private static String getValue(String tag, Element element) {
		NodeList nodes = element.getElementsByTagName(tag).item(0)
				.getChildNodes();
		Node node = (Node) nodes.item(0);
		return node.getNodeValue();
	}
}
