package org.xacml4j.v30.spi.pip;

import java.util.Map;
import java.util.Set;

import org.xacml4j.v30.AttributeDesignatorKey;


public interface AttributeResolverDescriptor
	extends ResolverDescriptor
{
	/**
	 * Gets an issuer identifier
	 * for this resolver attributes
	 *
	 * @return an issuer identifier
	 */
	String getIssuer();

	/**
	 * Tests if resolver is capable of resolving
	 * given attribute
	 *
	 * @param key attribute designator key
	 * @return {@code true} if resolver
	 * is capable of resolving given attribute
	 */
	boolean canResolve(AttributeDesignatorKey key);

	/**
	 * Gets attribute of the given category
	 * with a given identifier descriptor
	 *
	 * @param attributeId an attribute identifier
	 * @return {@link AttributeDescriptor}
	 */
	AttributeDescriptor getAttribute(String attributeId);

	/**
	 * Gets number of attributes
	 * provided by this resolver
	 *
	 * @return a number of attributes
	 * provided by this resolver
	 */
	int getAttributesCount();

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
	Map<String, AttributeDescriptor> getAttributesById();

	/**
	 * Gets map of attribute descriptors {@link AttributeDescriptor}
	 * mapped by the {@link AttributeDesignatorKey}
	 *
	 * @return map of {@link AttributeDescriptor} instances mapped
	 * by the {@link AttributeDesignatorKey}
	 */
	Map<AttributeDesignatorKey, AttributeDescriptor> getAttributesByKey();

	/**
	 * Tests if an attribute resolver can resolve
	 * an attribute with a given identifier
	 *
	 * @param attributeId attribute identifier
	 * @return {@code true} if the attribute can be resolved
	 */
	boolean isAttributeProvided(String attributeId);
}
