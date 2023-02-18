package org.xacml4j.v30.policy;

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


import org.xacml4j.v30.*;
import org.xacml4j.v30.Content;

import java.util.Map;
import java.util.Optional;

public interface EvaluationContextHandler
{
	/**
	 * Resolves given {@link AttributeReferenceKey}
	 *
	 * @param context an evaluation context
	 * @param key an attribute key
	 * @return {@link Optional< BagOfValues >}
	 * @throws EvaluationException if an error occurs while resolving attribute
	 */
	Optional<BagOfValues> resolve(
			EvaluationContext context,
			AttributeReferenceKey key);

	/**
	 * Resolves content for the given {@link CategoryId}
	 *
	 * @param id a category id
	 * @param <C>
	 * @return optional with resolved content
	 */
	<C extends Content>Optional<C> getContent(CategoryId id);

	Map<AttributeDesignatorKey, BagOfValues> getResolvedDesignators();
	Map<AttributeSelectorKey, BagOfValues> getResolvedSelectors();
}
