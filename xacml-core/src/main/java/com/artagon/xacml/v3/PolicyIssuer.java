package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.LinkedList;

import org.w3c.dom.Node;

import com.google.common.base.Preconditions;

public class PolicyIssuer extends XacmlObject
{
	private Node content;
	private Collection<Attribute> attributes;
	
	public PolicyIssuer(Node content, 
			Collection<Attribute> attributes)
	{
		Preconditions.checkArgument(attributes != null);
		this.content = content;
		this.attributes = new LinkedList<Attribute>(attributes);
	}
	
	/**
	 * Gets a free form XML 
	 * describing policy issuer.
	 * 
	 * @return {@link Node} or <code>null</code>
	 */
	public Node getContent(){
		return content;
	}
}
