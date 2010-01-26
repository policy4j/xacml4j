package com.artagon.xacml.v3;

import java.util.Map;

import org.w3c.dom.Node;


public class Attributes 
{
	private String xmlId;
	private CategoryId categoryId;
	private Node content;
	private Map<String, Attribute> attributes;
	
	
	public CategoryId getCategoryId(){
		return categoryId;
	}
	
	public Attribute getAttribute(String attributeId){
		return attributes.get(attributeId);
	}
	
	public String getXmlId(){
		return xmlId;
	}
	
	public Node getContent(){
		return content;
	}
}
