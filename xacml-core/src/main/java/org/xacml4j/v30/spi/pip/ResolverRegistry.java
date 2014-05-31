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

import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;

public interface ResolverRegistry
{
	/**
	 * Gets a matching {@link AttributeResolver} for a given
	 * evaluation context and given {@link AttributeDesignatorKey}
	 *
	 * @param context an evaluation context
	 * @param key an attribute designator key
	 * @return instance of {@link AttributeResolver}
	 * or <ii>null</ii> if not matching resolver is found
	 */
	Iterable<AttributeResolver> getMatchingAttributeResolvers(
			EvaluationContext context, AttributeDesignatorKey key);


	/**
	 * Gets an {@link ContentResolver} for a given
	 * evaluation context and given {@link CategoryId}
	 *
	 * @param context evaluation context
	 * @param category attribute category
	 * @return content resolver
	 */
	ContentResolver getMatchingContentResolver(
			EvaluationContext context, CategoryId category);

	/**
	 * Adds top level attribute resolver
	 *
	 * @param r a top level attribute resolver
	 */
	void addAttributeResolver(AttributeResolver r);

	void addAttributeResolvers(Iterable<AttributeResolver> resolvers);
	void addAttributeResolvers(String policyId, Iterable<AttributeResolver> resolvers);

	void addContentResolvers(Iterable<ContentResolver> resolvers);
	void addContentResolvers(String policyId, Iterable<ContentResolver> resolvers);

	/**
	 * Adds top level content resolver
	 *
	 * @param r a top level content resolver
	 */
	void addContentResolver(ContentResolver r);

	/**
	 * Adds an attribute resolver bound to the specific
	 * policy identifier and all child policies
	 *
	 * @param policyId a policy identifier
	 * @param r an attribute resolver
	 */
	void addAttributeResolver(String policyId, AttributeResolver r);

	/**
	 * Adds a content resolver bound to the specific
	 * policy identifier and all child policies
	 *
	 * @param policyId a policy identifier
	 * @param r a content resolver
	 */
	void addContentResolver(String policyId, ContentResolver r);

	/**
	 * Gets attribute resolve by identifier
	 * @param id a resolver identifier
	 * @return {@link AttributeResolver} or {@code null} if not resolver found
	 */
	AttributeResolver getAttributeResolver(String id);

	/**
	 * Gets content resolver by identifier
	 * @param id a resolver identifier
	 * @return {@link ContentResolver} or {@code null} if not resolver found
	 */
	ContentResolver getContentResolver(String id);
}
