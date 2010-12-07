package com.artagon.xacml.v3.context;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import com.artagon.xacml.v3.XacmlObject;
import com.artagon.xacml.v3.policy.AttributesReference;
import com.google.common.base.Preconditions;

public class RequestReference extends XacmlObject
{
	private Collection<AttributesReference> references;
	
	/**
	 * Constructs request reference with given referenced attributes
	 * 
	 * @param references an attribute references
	 */
	public RequestReference(Collection<AttributesReference> references){
		Preconditions.checkNotNull(references);
		this.references = new LinkedList<AttributesReference>(references);
	}
	
	/**
	 * Constructs request reference with given referenced attributes
	 * 
	 * @param references an attribute references
	 */
	public RequestReference(AttributesReference ...references){
		this(Arrays.asList(references));
	}
	
	/**
	 * Gets all referenced attributes
	 * 
	 * @return collection of referenced attributes
	 */
	public Collection<AttributesReference> getReferencedAttributes(){
		return Collections.unmodifiableCollection(references);
	}
}
