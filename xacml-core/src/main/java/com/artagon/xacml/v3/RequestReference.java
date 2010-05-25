package com.artagon.xacml.v3;

import java.util.Collection;

public interface RequestReference 
{
	/**
	 * Gets all referenced attributes
	 * 
	 * @return collection of referenced attributes
	 */
	Collection<AttributesReference> getReferencedAttributes();

}