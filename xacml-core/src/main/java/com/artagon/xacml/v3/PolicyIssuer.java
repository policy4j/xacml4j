package com.artagon.xacml.v3;

import java.util.Arrays;
import java.util.Collection;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.artagon.xacml.util.DOMUtil;
import com.google.common.base.Objects;
import com.google.common.collect.Multimap;

public class PolicyIssuer extends BaseAttributeHolder
{
	private Document content;
	private Multimap<String, Attribute> attributes;
	
	public PolicyIssuer(Node content, 
			Collection<Attribute> attributes){
		super(attributes);
		this.content = DOMUtil.copyNode(content);
	}
	
	public PolicyIssuer(Collection<Attribute> attributes){
		this(null ,attributes);
	}
	
	public PolicyIssuer(Node content, 
			Attribute ...attributes){
		this(content, Arrays.asList(attributes));
	}
	
	public PolicyIssuer(Attribute ...attributes){
		this(null, Arrays.asList(attributes));
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
	
	public Collection<Attribute> getAttributes(String attributeId){
		return attributes.get(attributeId);
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
		.add("content", DOMUtil.toString(content.getDocumentElement()))
		.toString();
	}
}
