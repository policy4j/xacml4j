package com.artagon.xacml.v30;

import com.artagon.xacml.v30.types.AnyURIType;

/**
 * A well known XACML subject attributes
 * 
 * @author Giedrius Trumpickas
 *
 */
public enum ResourceAttributes 
{
	
	/**
	 * This attribute identifies the resource to which access is requested.
	 */
	RESOURCE_ID("urn:oasis:names:tc:xacml:1.0:resource:resource-id"),
	
	/**
	 * This attribute identifies the namespace of the top element(s) of the 
	 * contents of the {@link Attributes#getContent()}.  
	 * In the case where the resource content is supplied in 
	 * the request context and the resource namespaces are defined in the resource, 
	 * the PEP MAY provide this attribute in the request to 
	 * indicate the namespaces of the resource content. In this case 
	 * there SHALL be one value of this attribute for each unique namespace 
	 * of the top level elements in the {@link Attributes#getContent()}.  
	 * The type of the corresponding attribute SHALL be {@link AnyURIType#ANYURI}
	 */
	TARGET_NAMESPACE("urn:oasis:names:tc:xacml:2.0:resource:target-namespace");
	
		
	private String id;
	
	private ResourceAttributes(String id){
		this.id = id;
	}
	
	@Override
	public String toString(){
		return id;
	}
}


