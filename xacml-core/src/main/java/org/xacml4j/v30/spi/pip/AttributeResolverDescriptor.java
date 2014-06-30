package org.xacml4j.v30.spi.pip;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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
