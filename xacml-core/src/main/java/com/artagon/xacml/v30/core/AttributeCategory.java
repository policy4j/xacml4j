package com.artagon.xacml.v30.core;

public interface AttributeCategory 
{
	/**
	 * Gets XACML category identifier
	 * 
	 * @return a XACML category identifier
	 */
	String getId();
	
	/**
	 * Tests if this category is delegated
	 * 
	 * @return <code>true</code> if this
	 * category is delegated
	 */
	boolean isDelegated();
	
	AttributeCategory toDelegatedCategory();
}
