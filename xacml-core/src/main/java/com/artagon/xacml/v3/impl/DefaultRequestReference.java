package com.artagon.xacml.v3.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import com.artagon.xacml.v3.AttributesReference;
import com.artagon.xacml.v3.RequestReference;
import com.artagon.xacml.v3.XacmlObject;
import com.google.common.base.Preconditions;

final class DefaultRequestReference extends XacmlObject implements RequestReference
{
	private Collection<AttributesReference> references;
	
	/**
	 * Constructs request reference with given referenced attributes
	 * 
	 * @param references an attribute references
	 */
	public DefaultRequestReference(Collection<AttributesReference> references){
		Preconditions.checkNotNull(references);
		this.references = new LinkedList<AttributesReference>(references);
	}
	
	/**
	 * Constructs request reference with given referenced attributes
	 * 
	 * @param references an attribute references
	 */
	public DefaultRequestReference(AttributesReference ...references){
		this(Arrays.asList(references));
	}
	
	/* (non-Javadoc)
	 * @see com.artagon.xacml.v3.RequestReference#getReferencedAttributes()
	 */
	public Collection<AttributesReference> getReferencedAttributes(){
		return Collections.unmodifiableCollection(references);
	}
}
