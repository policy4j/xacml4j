package com.artagon.xacml.v3;

import java.util.Collection;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.impl.DefaultAttribute;

public interface Attributes 
{
	/**
	 * An unique identifier of the attribute in
	 * the request context
	 * 
	 * @return unique identifier of the
	 * attribute in the request context
	 */
	String getId();
	
	String getScope();
	
	/**
	 * Gets an attribute category
	 * 
	 * @return attribute category
	 */
	AttributeCategoryId getCategoryId();

	/**
	 * Tests if this instance contains an
	 * attribute with a given identifier
	 * 
	 * @param attributeId an attribute id
	 * @return <code>true</code> if contains
	 */
	boolean containsAttribute(String attributeId);

	/**
	 * Gets a collection of attributes by identifier
	 * 
	 * @param attributeId an attribute id
	 * @return a collection of {@link DefaultAttribute}
	 * instances or an empty collection if no
	 * matching attributes found
	 */
	Collection<Attribute> getAttributes(String attributeId);

	Collection<Attribute> getAttributes(String attributeId, String issuer);
	
	Collection<Attribute> getAttributes();

	Collection<Attribute> getIncludeInResultAttributes();

	Collection<AttributeValue> getAttributeValues(
			String attributeId, String issuer, AttributeValueType type);

	Node getContent();

}