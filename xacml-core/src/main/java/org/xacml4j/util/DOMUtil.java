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
import java.nio.charset.StandardCharsets;
import java.util.AbstractList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xacml4j.v30.SyntaxException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;


public class DOMUtil
{

	private static TransformerFactory transformerFactory;
	private static DocumentBuilderFactory documentBuilderFactory;
	private static final Logger LOG = LoggerFactory.getLogger(DOMUtil.class);

	static{
		try{
			transformerFactory = TransformerFactory.newInstance();
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setValidating(false);
			documentBuilderFactory.setNamespaceAware(true);
			documentBuilderFactory.setIgnoringComments(true);
			documentBuilderFactory.setIgnoringElementContentWhitespace(true);

			//REDHAT
			//https://www.blackhat.com/docs/us-15/materials/us-15-Wang-FileCry-The-New-Age-Of-XXE-java-wp.pdf
			documentBuilderFactory.setAttribute(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

			//OWASP
			//https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html
			documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			// Disable external DTDs as well
			documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			// and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
			documentBuilderFactory.setXIncludeAware(false);
			documentBuilderFactory.setExpandEntityReferences(false);

			documentBuilderFactory.setFeature("http://apache.org/xml/features/allow-java-encodings", true);

		}catch(Exception e){
			LOG.error(e.getMessage(), e);
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
	public static Document copyNode(Element source)
	{
		if (source == null) {
			return null;
		}
		try{
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Element copy =  (Element)doc.importNode(source, true);
			doc.appendChild(copy);
			return doc;
		}catch (ParserConfigurationException e){
			throw SyntaxException.invalidXml(e.getMessage(), e);
		}
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
		InputSource source = new InputSource(new StringReader(src));
		source.setEncoding(StandardCharsets.UTF_8.name());
		return parseXml(source);
	}


	public static Optional<Node> parseXml(InputSource src)
	{
		Preconditions.checkNotNull(src);
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = documentBuilderFactory
					.newDocumentBuilder();
			documentBuilder.setErrorHandler(new ErrorHandler() {
				@Override
				public void warning(SAXParseException exception) throws SAXException {
					LOG.debug("Error at=\"{}:{}\"", exception.getLineNumber(), exception.getColumnNumber());
					LOG.debug(exception.getMessage(), exception);
				}
				@Override
				public void error(SAXParseException exception) throws SAXException {
					LOG.debug("At=\"{}:{}\"", exception.getLineNumber(), exception.getColumnNumber());
					LOG.debug(exception.getMessage(), exception);
				}
				@Override
				public void fatalError(SAXParseException exception) throws SAXException {
					LOG.debug("At=\"{}:{}\"", exception.getLineNumber(), exception.getColumnNumber());
					LOG.debug(exception.getMessage(), exception);
				}
			});
		} catch (ParserConfigurationException e) {
			throw new IllegalStateException(
					e.getMessage(), e);
		}
		try {
			return Optional.of(
					documentBuilder.parse(src));
		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
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
	 * Tests two DOM nodes for equality. Two documents, and two document
	 * fragments, are equal when their children are equal; for a document
	 * that is the document element and the comments and processing
	 * instructions beside it. Two attribute nodes are equal when their
	 * namespace, name and value are equal. Two elements are equal when
	 * their names, namespaces, attributes (namespace declarations apart)
	 * and children are equal, the children in their order. Two text nodes
	 * are equal when their trimmed text is equal. Text nodes of white
	 * space only are not compared. Two comments are equal when their text
	 * is equal, two processing instructions when their target and data
	 * are equal. Namespace prefixes do not matter. The nodes are not
	 * changed.
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
	 * A hash code that agrees with {@link #isEqual(Node, Node)}: nodes
	 * that are equal have the same hash code. A document that has its
	 * document element only has the hash code of that element.
	 *
	 * @param n a DOM node
	 * @return the hash code, 0 for {@code null}
	 */
	public static int hashCode(Node n){
		if(n == null){
			return 0;
		}
		int hash = 1;
		// Nodes in document order; END_OF_ELEMENT closes a node that can have
		// children, so that <a><b/></a><c/> and <a><b/><c/></a> give different
		// sequences. A document adds nothing of its own.
		Deque<Object> pending = new ArrayDeque<>();
		pending.push(n);
		while(!pending.isEmpty()){
			Object next = pending.pop();
			if(next == END_OF_ELEMENT){
				hash = 31 * hash + 1;
			}else if(next instanceof Text){
				hash = 31 * hash + ((Text)next).getData().trim().hashCode();
			}else if(next instanceof Attr){
				hash = 31 * hash + attributeKey((Attr)next).hashCode();
			}else{
				Node node = (Node)next;
				if(node instanceof Element){
					hash = 31 * hash + elementHashCode((Element)node);
				}else if(!(node instanceof Document)){
					hash = 31 * hash + Objects.hash(node.getNodeType(), node.getNodeName(), node.getNodeValue());
				}
				if(!(node instanceof Document)){
					pending.push(END_OF_ELEMENT);
				}
				List<Node> children = children(node, true);
				for(int i = children.size() - 1; i >= 0; i--){
					pending.push(children.get(i));
				}
			}
		}
		return hash;
	}

	private static final Object END_OF_ELEMENT = new Object();

	/** The name, the namespace and the attributes of an element; the order of the attributes does not matter. */
	private static int elementHashCode(Element e){
		return 31 * Objects.hash(elementName(e), e.getNamespaceURI()) + attributes(e).hashCode();
	}

	/**
	 * The attributes of an element, namespace declarations apart: for
	 * each one its namespace, name and value. A multiset, because a DOM
	 * that was not made by a parser can hold two attributes with the same
	 * namespace and local name under different prefixes.
	 */
	private static Multiset<List<String>> attributes(Element e){
		Multiset<List<String>> attributes = HashMultiset.create();
		NamedNodeMap attrs = e.getAttributes();
		for(int i = 0; i < attrs.getLength(); i++){
			Attr attr = (Attr)attrs.item(i);
			if(!isNamespaceDeclaration(attr)){
				attributes.add(attributeKey(attr));
			}
		}
		return attributes;
	}

	/** The namespace, the name and the value of an attribute. The prefix is not part of it. */
	private static List<String> attributeKey(Attr attr){
		return Arrays.asList(attr.getNamespaceURI(), attributeName(attr), attr.getValue());
	}

	private static String elementName(Element e){
		return (e.getLocalName() != null)?e.getLocalName():e.getNodeName();
	}

	/** The local name of an attribute in a namespace, the whole name of one without. */
	private static String attributeName(Attr attr){
		return (attr.getNamespaceURI() == null)?attr.getName():attr.getLocalName();
	}

	/** {@code xmlns} and {@code xmlns:prefix} declare a namespace; another name that starts with xmlns does not. */
	private static boolean isNamespaceDeclaration(Attr attr){
		String name = attr.getName();
		return name.equals("xmlns") || name.startsWith("xmlns:");
	}

	/** The child nodes of a node, without the text nodes of white space only when asked so. */
	private static List<Node> children(Node parent, boolean skipBlankText){
		NodeList nodes = parent.getChildNodes();
		List<Node> children = new ArrayList<>(nodes.getLength());
		for(int i = 0; i < nodes.getLength(); i++){
			Node child = nodes.item(i);
			if(skipBlankText && child instanceof Text && ((Text)child).getData().trim().isEmpty()){
				continue;
			}
			children.add(child);
		}
		return children;
	}

	/**
	 * Creates a {@link String} representation
	 *
	 * @param n a DOM node
	 * @return a string representation
	 */
	public static String toString(Node n){
		if(n == null){
			return null;
		}
		Element e = null;
		if(n  instanceof Document){
			e = ((Document)n).getDocumentElement();
		}
		if(n  instanceof Element){
			e = ((Element)n);
		}
		StringBuilder fqname = new StringBuilder();
		if(e != null){
			if(!Strings.isNullOrEmpty(
					n.getNamespaceURI())){
				fqname
						.append('{')
						.append(e.getNamespaceURI())
						.append('}');
			}
			fqname.append(e.getLocalName());
			return fqname.toString();
		}
		return fqname.append(notTypeToString(n)).toString();
	}
	
	private static String notTypeToString(Node node){
			switch(node.getNodeType()) {
				case Node.ELEMENT_NODE:                return "Element";
				case Node.DOCUMENT_NODE:               return "Document";
				case Node.DOCUMENT_TYPE_NODE:          return "Document type";
				case Node.DOCUMENT_FRAGMENT_NODE:      return "Document fragment";
				case Node.ENTITY_NODE:                 return "Entity";
				case Node.ENTITY_REFERENCE_NODE:       return "Entity reference";
				case Node.NOTATION_NODE:               return "Notation";
				case Node.TEXT_NODE:                   return "Text";
				case Node.COMMENT_NODE:                return "Comment";
				case Node.CDATA_SECTION_NODE:          return "CDATA Section";
				case Node.ATTRIBUTE_NODE:              return "Attribute";
				case Node.PROCESSING_INSTRUCTION_NODE: return "Processing Instruction";
			}
			return "Unidentified";
	}

	/**
	 * Compares two DOM nodes as {@link #isEqual(Node, Node)} does.
	 *
	 * @param trimEmptyTextNodes if {@code true}, text nodes of white space
	 * only are not compared; the nodes are not changed
	 */
	public static boolean compareNodes(Node expected, Node actual, boolean trimEmptyTextNodes)
	{
		// Pairs of nodes that still have to be compared. Not recursive: the
		// depth of a document from a request is not bounded here.
		Deque<Node[]> pending = new ArrayDeque<>();
		pending.push(new Node[]{expected, actual});
		while(!pending.isEmpty()){
			Node[] pair = pending.pop();
			Node a = pair[0];
			Node b = pair[1];
			if(a.getNodeType() != b.getNodeType()){
				return false;
			}
			if(a instanceof Text){
				if(!((Text)a).getData().trim().equals(((Text)b).getData().trim())){
					return false;
				}
				continue;
			}
			if(a instanceof Attr){
				if(!attributeKey((Attr)a).equals(attributeKey((Attr)b))){
					return false;
				}
				continue;
			}
			if(a instanceof Element){
				Element ae = (Element)a;
				Element be = (Element)b;
				if(!Objects.equals(elementName(ae), elementName(be)) ||
						!Objects.equals(ae.getNamespaceURI(), be.getNamespaceURI()) ||
						!attributes(ae).equals(attributes(be))){
					return false;
				}
			}else if(!Objects.equals(a.getNodeName(), b.getNodeName()) ||
					!Objects.equals(a.getNodeValue(), b.getNodeValue())){
				// a comment (its text), a processing instruction (its target and
				// data); the same for every document and every document fragment
				return false;
			}
			// The children of every other kind of node: of an element, of a
			// document (its document element and the comments and processing
			// instructions beside it), of a document fragment.
			List<Node> ac = children(a, trimEmptyTextNodes);
			List<Node> bc = children(b, trimEmptyTextNodes);
			if(ac.size() != bc.size()){
				return false;
			}
			for(int i = 0; i < ac.size(); i++){
				pending.push(new Node[]{ac.get(i), bc.get(i)});
			}
		}
		return true;
	}

	/** Compares two DOM nodes; text nodes of white space only are compared too. */
	public static boolean compareNodes(Node expected, Node actual){
		return compareNodes(expected, actual, false);
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
