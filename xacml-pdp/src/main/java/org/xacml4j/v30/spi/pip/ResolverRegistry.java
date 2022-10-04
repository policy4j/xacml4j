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
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.EvaluationContext;


public interface ResolverRegistry
{
    /**
     * Gets a matching {@link ResolverDescriptor} for a given
     * evaluation context and given {@link AttributeDesignatorKey}
     *
     * @param context an evaluation context
     * @param key an attribute designator key
     * @return iterable over found matching {@link ResolverDescriptor>} instances
     */
	Iterable<AttributeResolverDescriptor> getMatchingAttributeResolver(
			EvaluationContext context, AttributeDesignatorKey key);

	/**
	 * Gets a matching {@link ResolverDescriptor} for a given
	 * evaluation context and given {@link AttributeSelectorKey}
	 *
	 * @param context an evaluation context
	 * @param key an attribute designator key
	 * @return iterable over found matching {@link ResolverDescriptor} instances
	 */
	Iterable<ContentResolverDescriptor> getMatchingContentResolver(
			EvaluationContext context, AttributeSelectorKey selectorKey);

	void addResolver(AttributeResolverDescriptor r);
	void addResolver(ContentResolverDescriptor r);
	void addResolver(String policyOrSetId, ContentResolverDescriptor r);
	void addResolver(String policyOrSetId, AttributeResolverDescriptor r);

}
