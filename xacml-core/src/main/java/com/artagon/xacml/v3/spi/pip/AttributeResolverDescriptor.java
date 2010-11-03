package com.artagon.xacml.v3.spi.pip;

import java.util.Map;
import java.util.Set;

import com.artagon.xacml.v3.AttributeDesignatorKey;

public interface AttributeResolverDescriptor extends ResolverDescriptor
{
	
	/**
	 * Tests if resolver is capable of resolving
	 * given attribute
	 * 
	 * @param category an attribute category
	 * @param attributeId an attribute identifier
	 * @param dataType an attribute data type
	 * @param issuer an optional attribute issuer
	 * @return <code>true</code> if resolver
	 * is capable of resolving given attribute
	 */
	boolean canResolve(AttributeDesignatorKey key);
	
	/**
	 * Gets resolver identifier
	 * 
	 * @return a resolver identifier
	 */
	String getIssuer();
	
	/**
	 * Gets attribute of the given category
	 * with a given identifier descriptor
	 * 
	 * @param attributeId an attribute identifier
	 * @return {@link AttributeDescriptor}
	 */
	AttributeDescriptor getAttribute(String attributeId);
	
	/**
	 * Gets a provided attribute identifiers
	 * 
	 * @return an immutable {@link Set} of attribute identifiers
	 */
	Set<String> getProvidedAttributeIds();
	
	/**
	 * Gets supported attributes
	 *  
	 * @return a map by the attribute id
	 */
	Map<String, AttributeDescriptor> getAttributes();
	
	/**
	 * Tests if an attribute resolver can resolve
	 * an attribute with a given identifier
	 * 
	 * @param attributeId
	 * @return <code>true</code>
	 */
	boolean isAttributeProvided(String attributeId);
	

}
