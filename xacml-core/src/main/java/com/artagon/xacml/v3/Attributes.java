package com.artagon.xacml.v3;

import java.util.Map;

import org.w3c.dom.Node;


public class Attributes extends XacmlObject
{
	private String id;
	private AttributeCategoryId categoryId;
	private Node content;
	private Map<String, Attribute> attributes;
	
	
	public AttributeCategoryId getCategoryId(){
		return categoryId;
	}
	
	public Attribute getAttribute(String attributeId){
		return attributes.get(attributeId);
	}
	
	public String getId(){
		return id;
	}
	
	public Node getContent(){
		return content;
	}
}
