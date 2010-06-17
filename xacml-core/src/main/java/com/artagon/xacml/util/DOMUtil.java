package com.artagon.xacml.util;

import java.util.Stack;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.google.common.base.Preconditions;


public class DOMUtil 
{	
	public static String getXPath(Node n) 
	{
		Preconditions.checkNotNull(n);
		Stack<Node> hierarchy = new Stack<Node>();
		StringBuffer buffer = new StringBuffer();
		hierarchy.push(n);
		Node parent = getParent(n);
		while (parent != null) 
		{
			hierarchy.push(parent);
			if(parent.getNodeType() == Node.DOCUMENT_NODE){
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
			if (node.getNodeType() == Node.ELEMENT_NODE) 
			{
				if (previous != null && previous.getNodeType() != Node.DOCUMENT_NODE) {
					buffer.append("/");
				}
				buffer.append(node.getNodeName());
				buffer.append("[").append(getNodeIndex(node)).append("]");
			}
			if(node.getNodeType() == Node.TEXT_NODE){
				buffer.append("/text()");
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
		Document sourceDoc = source.getOwnerDocument();
		DOMImplementation domImpl = sourceDoc.getImplementation();
		Document doc = domImpl.createDocument(
				null, null, null); 
		Node copy =  doc.importNode(source, true);
		doc.appendChild(copy);
		return doc;
	}
	
	/**
	 * Gets parent node of the given node
	 * 
	 * @param node a DOM node
	 * @return a parent node {@link Node} instance
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
}
