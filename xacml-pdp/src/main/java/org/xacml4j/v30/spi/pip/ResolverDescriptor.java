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

import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.CategoryId;

import java.util.List;


public interface ResolverDescriptor
{
	/**
	 * An unique identifier for
	 * this category resolver
	 *
	 * @return an unique identifier
	 */
	String getId();

	/**
	 * Gets resolver name
	 *
	 * @return resolver name
	 */
	String getName();

	/**
	 * Gets resolver category
	 *
	 * @return {@link CategoryId}
	 */
	CategoryId getCategory();

	/**
	 * Gets key references for a resolver
	 *
	 * @return list of {@link AttributeReferenceKey}
	 */
	List<AttributeReferenceKey> getKeyRefs();

	/**
	 * Test if category resolved by resolver can be cached by PIP
	 *
	 * @return {@code true} if category can be cached
	 */
	boolean isCacheable();

	/**
	 * Gets preferred cache TTL for an category resolved via this resolver
	 *
	 * @return a TTL in seconds or {@code 0}
	 */
	int getPreferredCacheTTL();
}
