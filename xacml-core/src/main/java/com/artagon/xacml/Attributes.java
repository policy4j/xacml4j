package com.artagon.xacml;

import org.w3c.dom.Node;


public class Attributes 
{
	private String xmlId;
	private CategoryId categoryId;
	private Node content;
	
	public CategoryId getCategoryId(){
		return categoryId;
	}
	
	
	public String getXmlId(){
		return xmlId;
	}
	
	public Node getContent(){
		return content;
	}
}
