package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.Set;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.policy.AttributeValue;
import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.BagOfAttributeValues;
import com.artagon.xacml.v3.policy.impl.DefaultAttribute;

public interface Attributes {

	/**
	 * An unique identifier of the attribute in
	 * the request context
	 * 
	 * @return unique identifier of the
	 * attribute in the request context
	 */
	public abstract String getId();

	/**
	 * Gets an attribute category
	 * 
	 * @return attribute category
	 */
	public abstract AttributeCategoryId getCategoryId();

	public abstract Set<String> getProvidedAttributeIds();

	/**
	 * Tests if this instance contains an
	 * attribute with a given identifier
	 * 
	 * @param attributeId an attribute id
	 * @return <code>true</code> if contains
	 */
	public abstract boolean containsAttribute(String attributeId);

	/**
	 * Gets a collection of attributes by identifier
	 * 
	 * @param attributeId an attribute id
	 * @return a collection of {@link DefaultAttribute}
	 * instances or an empty collection if no
	 * matching attributes found
	 */
	public abstract Collection<Attribute> getAttribute(String attributeId);

	public abstract Collection<Attribute> getAttributes(
			final String attributeId, final String issuer);

	public abstract Collection<Attribute> getIncludeInResultAttributes();

	public abstract BagOfAttributeValues<? extends AttributeValue> getAttributeValues(
			String attributeId, String issuer, AttributeValueType type);

	public abstract Node getContent();

}