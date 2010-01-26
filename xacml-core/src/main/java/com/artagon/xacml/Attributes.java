package com.artagon.xacml;

import java.util.Collection;
import java.util.Collections;

import org.w3c.dom.Node;


public class Attributes 
{
	private String xmlId;
	private CategoryId categoryId;
	private Node content;
	private Collection<Attribute> attributes;
	
	
	public CategoryId getCategoryId(){
		return categoryId;
	}
	
	public Collection<Attribute> getAttributes(){
		return Collections.unmodifiableCollection(attributes);
	}
	
	public String getXmlId(){
		return xmlId;
	}
	
	public Node getContent(){
		return content;
	}
}
