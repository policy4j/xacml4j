package org.xacml4j.v30.spi.pip;

/*
 * #%L
 * Xacml4J PDP
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

import java.util.function.Function;

import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Content;

/**
 * Content resolvers for tests outside this package, which cannot name the package-private
 * builder of {@link ContentResolverDescriptor}.
 */
public final class ContentResolvers
{
	private ContentResolvers(){
	}

	/** A resolver without a cache that gives the content to every selector of the category */
	public static ContentResolverDescriptor of(String id, CategoryId category, Function<ResolverContext, Content> content){
		return ContentResolverDescriptor.builder(id, id, category).noCache().build(content);
	}
}
