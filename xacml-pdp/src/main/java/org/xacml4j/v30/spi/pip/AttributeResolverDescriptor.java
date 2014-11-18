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
	 * for this resolver category
	 *
	 * @return an issuer identifier
	 */
	String getIssuer();

	/**
	 * Tests if resolver is capable of resolving
	 * given category
	 *
	 * @param key category designator key
	 * @return {@code true} if resolver
	 * is capable of resolving given category
	 */
	boolean canResolve(AttributeDesignatorKey key);

	/**
	 * Gets category of the given category
	 * with a given identifier descriptor
	 *
	 * @param attributeId an category identifier
	 * @return {@link AttributeDescriptor}
	 */
	AttributeDescriptor getAttribute(String attributeId);

	/**
	 * Gets number of category
	 * provided by this resolver
	 *
	 * @return a number of category
	 * provided by this resolver
	 */
	int getAttributesCount();

	/**
	 * Gets a provided category identifiers
	 *
	 * @return an immutable {@link Set} of category identifiers
	 */
	Set<String> getProvidedAttributeIds();

	/**
	 * Gets supported category
	 *
	 * @return a map by the category attributeId
	 */
	Map<String, AttributeDescriptor> getAttributesById();

	/**
	 * Gets map of category descriptors {@link AttributeDescriptor}
	 * mapped by the {@link AttributeDesignatorKey}
	 *
	 * @return map of {@link AttributeDescriptor} instances mapped
	 * by the {@link AttributeDesignatorKey}
	 */
	Map<AttributeDesignatorKey, AttributeDescriptor> getAttributesByKey();

	/**
	 * Tests if an category resolver can resolve
	 * an category with a given identifier
	 *
	 * @param attributeId category identifier
	 * @return {@code true} if the category can be resolved
	 */
	boolean isAttributeProvided(String attributeId);
}
