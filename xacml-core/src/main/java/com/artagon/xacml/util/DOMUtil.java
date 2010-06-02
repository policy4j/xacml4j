package com.artagon.xacml.util;

import java.util.Stack;

import org.w3c.dom.Attr;
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
		while (parent != null && 
				parent.getNodeType() != Node.DOCUMENT_NODE) 
		{
			hierarchy.push(parent);
			parent = getParent(parent);
		}
		Node node = null;
		while (!hierarchy.isEmpty() && 
				(node = hierarchy.pop()) != null) 
		{
			if (node.getNodeType() == Node.ELEMENT_NODE) 
			{
				if (buffer.length() > 0) {
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
		}
		return buffer.toString();
	}
	
	public static Node getParent(Node node){
		return (node.getNodeType() == Node.ATTRIBUTE_NODE)?
				((Attr)node).getOwnerElement():node.getParentNode();
	}
	
	public static int getNodeIndex(Node node)
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
