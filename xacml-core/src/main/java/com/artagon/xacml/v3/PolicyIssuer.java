package com.artagon.xacml.v3;

import java.util.Collection;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.artagon.xacml.util.DOMUtil;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class PolicyIssuer 
{
	private Element content;
	private Multimap<String, Attribute> attributes;
	
	public PolicyIssuer(Node content, 
			Collection<Attribute> attributes)
	{
		Preconditions.checkArgument(attributes != null);
		this.content = DOMUtil.copyNode(content);
		this.attributes = LinkedListMultimap.create();
		for(Attribute attr : attributes){
			this.attributes.put(attr.getAttributeId(), attr);
		}
		this.attributes = Multimaps.unmodifiableMultimap(this.attributes);
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
		return attributes.values();
	}
	
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof PolicyIssuer)){
			return false;
		}
		PolicyIssuer pi = (PolicyIssuer)o;
		return DOMUtil.isEqual(content, pi.content) && 
		attributes.equals(pi.attributes);
	}
	
	public String toString(){
		return Objects.toStringHelper(this)
		.add("attributes", attributes)
		.add("content", DOMUtil.toString(content))
		.toString();
	}
}
