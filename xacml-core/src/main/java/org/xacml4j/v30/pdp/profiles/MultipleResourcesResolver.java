package org.xacml4j.v30.pdp.profiles;

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

import org.xacml4j.v30.AttributeValue;
import org.xacml4j.v30.pdp.RequestContextCallback;

import java.util.Collection;
import java.util.Set;


public interface MultipleResourcesResolver
{
	/**
	 * Gets supported resource identifiers
	 *
	 * @return a {@link Set} of {@link AttributeValue}
	 * instances representing supported resource
	 * identifiers
	 */
	Set<AttributeValue> getSupportedResourceIds();

	/**
	 * Resolves immediate children resources of
	 * the given resource
	 *
	 * @param resource a resource identifier
	 * @param callback a request context attribute callback
	 * @return a collection of immediate children of the given resource
	 */
	Collection<AttributeValue> resolveChildrenResources(
			AttributeValue resource,
			RequestContextCallback callback);

	Collection<AttributeValue> resolveDescendantResources(
			AttributeValue resource,
			RequestContextCallback callback);
}
