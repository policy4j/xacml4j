package com.artagon.xacml.v3.policy.spi;

import java.util.Iterator;

import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.policy.AttributeValue;

public interface ContextAttributeCallback 
{
	/**
	 * Gets single attribute value from a 
	 * request context
	 * 
	 * @param <V>
	 * @param category an attribute category
	 * @param attributeId an attribute identifier
	 * @return an attribute value or <code>null</code>
	 * if such attribute does not exist in request
	 * context
	 */
	<V extends AttributeValue> Attribute<V> getAttribute(
			AttributeCategoryId category, String attributeId);
	
	
	
}
