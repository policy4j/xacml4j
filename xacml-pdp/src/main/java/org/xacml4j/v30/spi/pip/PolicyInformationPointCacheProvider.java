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

import org.w3c.dom.Node;

/**
 * A provider interface for pip caches
 * 
 * @author Giedrius Trumpickas
 */
public interface PolicyInformationPointCacheProvider
{
	/**
	 * Gets content from the cache for a given resolver context
	 *
	 * @param context resolver context
	 * @return {@link Node} or @{code null}
	 */
	Content getContent(ResolverContext context);

    /**
     * Puts given content to this cache
     * *
     * @param context a resolver context
     * @param content a content to be cached
     */
	void putContent(ResolverContext context, Content content);

    /**
     * Tries to locate an [@link AttributeSet} in the
     * cache for a given resolver context
     * 
     * @param context a resolver content
     * @return {@link org.xacml4j.v30.spi.pip.AttributeSet} or <code>null</code>
     */
	AttributeSet getAttributes(ResolverContext context);

    /**
     * Stores given attribute set in this cache 
     * *
     * @param context a resolver context
     * @param v an attribute set
     */
	void putAttributes(ResolverContext context, AttributeSet v);
}
