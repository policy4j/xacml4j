package com.artagon.xacml.v3;

import java.util.HashMap;
import java.util.Map;

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
	
	public Attributes getAttributes(AttributeCategoryId categoryId){
		return attributes.get(categoryId);
	}
}
