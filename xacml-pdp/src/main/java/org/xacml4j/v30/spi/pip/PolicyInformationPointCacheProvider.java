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

import java.util.Optional;

import org.w3c.dom.Node;

public interface PolicyInformationPointCacheProvider
{
	/**
	 * Gets content from the cache for a given resolver context
	 *
	 * @param context resolver context
	 * @return {@link Node} or @{code null}
	 */
	Optional<ContentRef> getContent(ResolverContext context);

	/**
	 * Adds content to cache
	 *
	 * @param context a resolver context
	 * @param content a content
	 */
	void putContent(ResolverContext context, ContentRef content);

	/**
	 * Gets {@link AttributeSet} from cache
	 *
	 * @param context a resolver context
	 * @return {@link Optional<AttributeSet>}
	 */
	Optional<AttributeSet> getAttributes(ResolverContext context);


	/**
	 * Adds attributes to cache
	 *
	 * @param context a resolver context
	 * @param v attributes
	 */
	void putAttributes(ResolverContext context, AttributeSet v);



}
