package com.artagon.xacml.v3;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

public class Request extends XacmlObject
{	
	private Map<AttributeCategoryId, Attributes> attributes;
	
	/**
	 * Constructs a request with a given attributes
	 * @param attributes
	 */
	public Request(Iterable<Attributes> attributes)
	{
		this.attributes = new HashMap<AttributeCategoryId, Attributes>();
		for(Attributes attr : attributes){
			this.attributes.put(attr.getCategoryId(), attr);
		}
	}
	
	/**
	 * Gets attributes for a given category
	 * 
	 * @param categoryId an attribute category id
	 * @return {@link Attributes} for a given category or
	 * <code>null</code> if there is no attributes for
	 * a given category in the request
	 */
	public Attributes getAttributes(AttributeCategoryId categoryId){
		return attributes.get(categoryId);
	}
	
	/**
	 * Gets content for a given category
	 * 
	 * @param categoryId an category identifier
	 * @return {@link Node} or <code>null</code>
	 * if there is no content for a given category
	 * or there is not attributes for a given category
	 * in the request
	 */
	public Node getCategoryContent(AttributeCategoryId categoryId){
		Attributes attrs = getAttributes(categoryId);
		return (attrs != null)?attrs.getContent():null;
	}
}
