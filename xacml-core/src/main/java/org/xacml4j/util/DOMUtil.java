package org.xacml4j.util;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xacml4j.v30.SyntaxException;
import org.xml.sax.InputSource;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class DOMUtil
{

	private static TransformerFactory transformerFactory;
	private static DocumentBuilderFactory documentBuilderFactory;

	static{
		try{
			transformerFactory = TransformerFactory.newInstance();
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setValidating(false);
			documentBuilderFactory.setNamespaceAware(true);
			documentBuilderFactory.setIgnoringComments(true);
			documentBuilderFactory.setIgnoringElementContentWhitespace(true);
		}catch(Exception e){
			e.printStackTrace(System.err);
		}
	}

	/** Private constructor for utility class */
	private DOMUtil() { }

	public static List<Node> nodeListToList(NodeList list)
	{
		Objects.requireNonNull(list);
		return new AbstractList<Node>()
		{
				public int size() {
					return list.getLength();
				}

				public Node get(int index) {
					Node item = list.item(index);
					if (item == null)
						throw new IndexOutOfBoundsException(index);
					return item;
				}
			};
	}
	/**
	 * Creates XPath expression for a given DOM node
	 * @param n a DOM node
	 * @return an XPath expression of the node
	 * representing node location in the document
	 */
	public static String getXPath(Node n)
	{
		Preconditions.checkNotNull(n);
		Stack<Node> hierarchy = new Stack<Node>();
		StringBuilder buffer = new StringBuilder();
		hierarchy.push(n);
		Node parent = getParent(n);
		while (parent != null)
		{
			hierarchy.push(parent);
			if(parent.getNodeType()
					== Node.DOCUMENT_NODE){
				break;
			}
			parent = getParent(parent);
		}
		Node node = null;
		Node previous = null;
		while (!hierarchy.isEmpty() &&
				(node = hierarchy.pop()) != null)
		{
			if(node.getNodeType() == Node.DOCUMENT_NODE){
				buffer.append("//");
			}
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if (previous != null &&
						previous.getNodeType() != Node.DOCUMENT_NODE) {
					buffer.append('/');
				}
				buffer.append(node.getNodeName());
				if(previous != null &&
						previous.getNodeType()
						!= Node.DOCUMENT_NODE){
					buffer
					.append('[')
					.append(getNodeIndex(node))
					.append(']');
				}
			}
			if(node.getNodeType() == Node.TEXT_NODE){
				buffer.append("/text()");
				continue;
			}
			if(node.getNodeType() == Node.ATTRIBUTE_NODE){
				Attr attr = (Attr)node;
				buffer.append("/@").append(attr.getName());
			}
			previous = node;
		}
		return buffer.toString();
	}

	/**
	 * Copies node and all its children
	 * to the new document as root element
	 * in the new document
	 *
	 * @param source a source node
	 * @return {@link Document} a new DOM
	 * document with copy of the node as
	 * root element
	 */
	public static Document copyNode(Node source)
	{
		if (source == null) {
			return null;
		}
		Document sourceDoc = (source.getNodeType() == Node.DOCUMENT_NODE)?(Document)source:source.getOwnerDocument();
		Node rootNode = (source.getNodeType() == Node.DOCUMENT_NODE)?sourceDoc.getDocumentElement():source;
		Preconditions.checkState(sourceDoc != null);
		DOMImplementation domImpl = sourceDoc.getImplementation();
		Document doc = domImpl.createDocument(sourceDoc.getNamespaceURI(),
				null, sourceDoc.getDoctype());
		Node copy =  doc.importNode(rootNode, true);
		doc.appendChild(copy);
		return doc;
	}

	/**
	 * Gets parent node of the given node
	 *
	 * @param node a DOM node
	 * @return a parent node {@link Node} defaultProvider
	 */
	private static Node getParent(Node node){
		return (node.getNodeType() == Node.ATTRIBUTE_NODE)?
				((Attr)node).getOwnerElement():node.getParentNode();
	}

	/**
	 * Gets node index relative to its siblings
	 *
	 * @param node a node
	 * @return a node index
	 */
	private static int getNodeIndex(Node node)
	{
		Preconditions.checkNotNull(node);
		int prev_siblings = 1;
		Node prev_sibling = node.getPreviousSibling();
		while (prev_sibling != null) {
			if (prev_sibling.getNodeType() == node
					.getNodeType()) {
				if (prev_sibling.getNodeName()
						.equalsIgnoreCase(node.getNodeName())) {
					prev_siblings++;
				}
			}
			prev_sibling = prev_sibling.getPreviousSibling();
		}
		return prev_siblings;
	}

	public static void printNodeInfo(Node node, Logger logger){
		if(logger.isDebugEnabled()){
			logger.debug("Node name=\"{}:{}\" type=\"{}\"", node.getLocalName(), node.getNamespaceURI(), node.getNodeType());
		}
	}

	public static void printNodeListInfo(NodeList list, Logger logger){
		if(logger.isDebugEnabled()){
			logger.debug("NodeList size=\"{}\" nodes", list.getLength());
			for(int i = 0; i < list.getLength(); i++){
				printNodeInfo(list.item(i), logger);
			}
		}
	}
	/**
	 * Safe cast of the given node to
	 * {@link Element} defaultProvider
	 * @param n an element or a document defaultProvider
	 * @return {@link Element}
	 * @exception IllegalArgumentException if a given node
	 * neither element or document node
	 */
	public static Element getElementNode(Node n)
	{
		if(n == null){
			return null;
		}
		Preconditions.checkArgument(n.getNodeType() == Node.DOCUMENT_NODE
				|| n.getNodeType() == Node.ELEMENT_NODE,
				"Given node must be either DOM document or element node");
		if(n.getNodeType() == Node.DOCUMENT_NODE){
			return ((Document)n).getDocumentElement();
		}
		return (Element)n;
	}

	public static void serializeToXml(Node node, OutputStream out)
		throws TransformerException
	{
		serializeToXml(node, out, true, false);
	}

	public static Optional<Node> stringToNode(String src) {
		if(src == null){
			return Optional.empty();
		}
		return parseXml(
				new InputSource(
						new StringReader(src)));
	}

	public static Optional<Node> parseXml(InputStream src) {
		if(src == null){
			return Optional.empty();
		}
		return parseXml(
				new InputSource(src));

	}

	public static Optional<Node> parseXml(String src) {
		if(src == null){
			return Optional.empty();
		}
		return parseXml(
				new InputSource(
						new StringReader(src)));
	}


	public static Optional<Node> parseXml(InputSource src)
	{
		Preconditions.checkNotNull(src);
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = documentBuilderFactory
					.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new IllegalStateException(
					e.getMessage(), e);
		}
		try {
			return Optional.of(
					documentBuilder.parse(src));
		} catch (Exception e) {
			throw SyntaxException
					.invalidXml(e.getMessage(), e);
		}
	}

	public static String nodeToString(Node node)
	{
		if (node == null) {
			return null;
		}
		final Transformer transformer;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			throw new IllegalStateException(
					e.getMessageAndLocation(), e);
		}
		try
		{
			DOMSource source = new DOMSource(node);
			StreamResult result = new StreamResult(new StringWriter());
			transformer.transform(source, result);
			return result.getWriter().toString();
		} catch (TransformerException e) {
			throw new IllegalArgumentException(
					e.getMessageAndLocation(), e);
		}
	}

	public static void serializeToXml(
			Node node,
			OutputStream out,
			boolean omitXmlDecl,
			boolean prettyPrint)
		throws TransformerException
	{
		Preconditions.checkNotNull(node);
		Preconditions.checkNotNull(out);
		Preconditions.checkState(transformerFactory != null);
		Transformer t = transformerFactory.newTransformer();
		t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,  omitXmlDecl ?"yes":"no");
		t.setOutputProperty(OutputKeys.METHOD, "xml");
		t.setOutputProperty(OutputKeys.INDENT, prettyPrint?"yes":"no");
		DOMSource source = new DOMSource(node);
		Result result = new StreamResult(out);
		t.transform (source, result);
	}

	public static String serializeToXmlString(Node node, boolean omitXmlDecl)
	{
		try{
			StringWriter w = new StringWriter();
			StreamResult result = new StreamResult(w);
			Transformer t = transformerFactory.newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,  omitXmlDecl ?"yes":"no");
			t.setOutputProperty(OutputKeys.METHOD, "xml");
			DOMSource source = new DOMSource(node);
			t.transform (source, result);
			return w.toString();
		}catch(TransformerException e){
			throw new IllegalStateException(e.getCause());
		}

	}

	/**
	 * Test two DOM nodes for equality, if both
	 * nodes are not equal to {@code null}
	 * then they are compared via {@link Node#isEqualNode(Node)}
	 *
	 * @param a a DOM node
	 * @param b a DOM node
	 */
	public static boolean isEqual(Node a, Node b){
		return (a == null)
				? b == null
				: b != null && compareNodes(a, b, true);
	}


	/**
	 * Creates a {@link String} representation
	 *
	 * @param n a DOM node
	 * @return a string representation
	 */
	public static String toString(Element n){
		if(n == null){
			return null;
		}
		StringBuilder fqname = new StringBuilder();
		if(!Strings.isNullOrEmpty(
				n.getNamespaceURI())){
			fqname
			.append('{')
			.append(n.getNamespaceURI())
			.append('}');
		}
		fqname.append(n.getLocalName());
		return fqname.toString();
	}

	private static void trimEmptyTextNodes(Node node) {
		Element element = null;
		if (node instanceof Document) {
			element = ((Document) node).getDocumentElement();
		} else if (node instanceof Element) {
			element = (Element) node;
		} else {
			return;
		}

		List<Node> nodesToRemove = new ArrayList<>();
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child instanceof Element) {
				trimEmptyTextNodes(child);
			} else if (child instanceof Text) {
				Text t = (Text) child;
				if (t.getData().trim().length() == 0) {
					nodesToRemove.add(child);
				}
			}
		}

		for (Node n : nodesToRemove) {
			element.removeChild(n);
		}
	}

	public static boolean compareNodes(Node expected, Node actual, boolean trimEmptyTextNodes)
	{
		if (trimEmptyTextNodes) {
			trimEmptyTextNodes(expected);
			trimEmptyTextNodes(actual);
		}
		return compareNodes(expected, actual);
	}

	public static boolean compareNodes(Node expected, Node actual){
		if (expected.getNodeType() != actual.getNodeType()) {
			return false;
		}
		if (expected instanceof Document) {
			Document expectedDoc = (Document) expected;
			Document actualDoc = (Document) actual;
			compareNodes(expectedDoc.getDocumentElement(), actualDoc.getDocumentElement());
		} else if (expected instanceof Element) {
			Element expectedElement = (Element) expected;
			Element actualElement = (Element) actual;

			// compare element names
			if ( expectedElement.getLocalName() != null &&
					(!expectedElement.getLocalName().equals(actualElement.getLocalName()))) {
				return false;
			}
			// compare element ns
			String expectedNS = expectedElement.getNamespaceURI();
			String actualNS = actualElement.getNamespaceURI();
			if ((expectedNS == null && actualNS != null)
					|| (expectedNS != null && !expectedNS.equals(actualNS))) {
				return false;
			}

			// compare attributes
			NamedNodeMap expectedAttrs = expectedElement.getAttributes();
			NamedNodeMap actualAttrs = actualElement.getAttributes();
			if (countNonNamespaceAttribures(expectedAttrs) != countNonNamespaceAttribures(actualAttrs)) {
				return false;
			}
			for (int i = 0; i < expectedAttrs.getLength(); i++) {
				Attr expectedAttr = (Attr) expectedAttrs.item(i);
				if (expectedAttr.getName().startsWith("xmlns")) {
					continue;
				}
				Attr actualAttr = null;
				if (expectedAttr.getNamespaceURI() == null) {
					actualAttr = (Attr) actualAttrs.getNamedItem(expectedAttr.getName());
				} else {
					actualAttr = (Attr) actualAttrs.getNamedItemNS(expectedAttr.getNamespaceURI(),
					                                               expectedAttr.getLocalName());
				}
				if (actualAttr == null) {
					return false;
				}
				if (!expectedAttr.getValue().equals(actualAttr.getValue())) {
					return false;
				}
			}
			// compare children
			NodeList expectedChildren = expectedElement.getChildNodes();
			NodeList actualChildren = actualElement.getChildNodes();
			if (expectedChildren.getLength() != actualChildren.getLength()) {
				return false;
			}
			for (int i = 0; i < expectedChildren.getLength(); i++) {
				Node expectedChild = expectedChildren.item(i);
				Node actualChild = actualChildren.item(i);
				compareNodes(expectedChild, actualChild);
			}
		} else if (expected instanceof Text) {
			String expectedData = ((Text) expected).getData().trim();
			String actualData = ((Text) actual).getData().trim();

			if (!expectedData.equals(actualData)) {
				return false;
			}
		}
		return true;
	}

	private static int countNonNamespaceAttribures(NamedNodeMap attrs) {
		int n = 0;
		for (int i = 0; i < attrs.getLength(); i++) {
			Attr attr = (Attr) attrs.item(i);
			if (!attr.getName().startsWith("xmlns")) {
				n++;
			}
		}
		return n;
	}

	public static NamespaceContext createNamespaceContext(Node n){
		return new NodeNamespaceContext(n);
	}

	/**
	 * {@link NamespaceContext} implementation
	 * for {@link Node} representing a root
	 * node of some document
	 */
	private static class NodeNamespaceContext
		implements NamespaceContext
	{
		private final static Logger log = LoggerFactory.getLogger(NodeNamespaceContext.class);

		private Node node;

		public NodeNamespaceContext(Node node){
			Preconditions.checkNotNull(node);
			this.node = node;
		}

		@Override
		public String getNamespaceURI(String prefix){
			 if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
		            return XMLConstants.XML_NS_URI;
		     }
			 if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
		            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
		    }
			String namespaceURI = node.lookupNamespaceURI(prefix);
			if(log.isDebugEnabled()){
				log.debug("NamespaceURI=\"{}\" " +
						"for prefix=\"{}\"",
						namespaceURI, prefix);
			}
			return namespaceURI == null?XMLConstants.NULL_NS_URI:namespaceURI;
		}

		@Override
		public String getPrefix(String namespaceURI) {
			String prefix = node.lookupPrefix(namespaceURI);
			if(log.isDebugEnabled()){
				log.debug("Namespace prefix=\"{}\" " +
						"for namespaceURI=\"{}\"",
						prefix, namespaceURI);
			}
			return prefix;
		}

		@Override
		public Iterator<String> getPrefixes(String namespaceURI) {
			String prefix = getPrefix(namespaceURI);
			return (prefix == null)?
					java.util.Collections.<String>emptyList().iterator():
						Collections.singleton(prefix).iterator();
		}



	}
}
