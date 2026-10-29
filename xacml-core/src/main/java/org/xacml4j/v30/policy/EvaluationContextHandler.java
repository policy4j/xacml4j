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


import java.util.Map;
import java.util.Optional;

import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Content;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.types.Entity;

public interface EvaluationContextHandler
{
	/**
	 * Resolves given {@link AttributeReferenceKey}
	 *
	 * @param context an evaluation context
	 * @param key an attribute key
	 * @return {@link Optional<BagOfValues>}
	 * @throws EvaluationException if an error occurs while resolving attribute
	 */
	Optional<BagOfValues> resolve(
			EvaluationContext context,
			AttributeReferenceKey key);

	/**
	 * Resolves entity for the given {@link CategoryId}
	 *
	 * @param id a category id
	 * @return optional with resolved entity
	 */
	Optional<Entity> getEntity(CategoryId id);

	/**
	 * Whether a designator whose request entity has a bag resolves to that bag with no other effect
	 * on the decision, so that a child can be selected or its target matched from the entity and
	 * the resolution left out.
	 *
	 * @return {@code true} when this contract holds, {@code false} by default
	 */
	default boolean usesRequestEntityForDesignators(){
		return false;
	}

	Map<AttributeDesignatorKey, BagOfValues> getResolvedDesignators();
	Map<AttributeSelectorKey, BagOfValues> getResolvedSelectors();

	/** @return whether the information point supplied a designator bag, without creating the view (design D32) */
	default boolean hasResolvedDesignators(){
		return !getResolvedDesignators().isEmpty();
	}
}
