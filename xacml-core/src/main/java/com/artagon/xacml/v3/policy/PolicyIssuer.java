package com.artagon.xacml.v3.policy;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.XacmlObject;
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
		this.attributes = Collections.unmodifiableList(new LinkedList<Attribute>(attributes));
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
	
	/**
	 * Gets additional issuer attributes
	 * 
	 * @return additional issuer attributes
	 */
	public Collection<Attribute> getAttributes(){
		return attributes;
	}
}
