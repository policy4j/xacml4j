package org.xacml4j.v30.pdp;

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

import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.types.Entity;

@FunctionalInterface
public interface RequestContextCallback
{
	/***
	 * Gets request context entity of the given category
	 * 
	 * @param category a request entity category
	 * @return {@link Entity}
	 */
	Optional<Entity> getEntity(CategoryId category);

	default Optional<Entity> getEntity(Optional<CategoryId> category){
		return category.isPresent()?getEntity(category.get()):Optional.empty();
	}

	default Optional<BagOfValues> resolve(AttributeSelectorKey selectorKey){
		return getEntity(selectorKey.getCategory())
				.flatMap(e->e.resolve(selectorKey));
	}

	default Optional<BagOfValues> resolve(AttributeDesignatorKey designatorKey){
		return getEntity(designatorKey.getCategory())
				.flatMap(e->e.resolve(designatorKey));
	}
}
