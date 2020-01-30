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

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.*;

import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.BagOfAttributeValues;
import org.xacml4j.v30.Content;

import com.google.common.base.Ticker;
import org.xacml4j.v30.EvaluationContext;


public interface ResolverContext
{
	/**
	 * Gets optionally requested content type
	 *
	 * @return {@link Optional<Content.Type>/>}
	 */
	Optional<Content.Type> getContentType();

	/**
	 * Gets current evaluation context date/time
	 *
	 * @return {@link Calendar} defaultProvider
	 * representing current date/time
	 */
	ZonedDateTime getCurrentDateTime();

	/**
	 * Gets ticker
	 *
	 * @return {@link Ticker}
	 */
	Clock getClock();

	/**
	 * Gets resolver descriptor
	 *
	 * @return {@link ResolverDescriptor}
	 */
	ResolverDescriptor getDescriptor();

	<T extends Resolver> T getResolver();

	/**
	 * Resolves context key
	 */
	Optional<BagOfAttributeValues> resolve(AttributeReferenceKey key);


	boolean isCacheable();

	/**
	 * Gets resolved keys, key resolution implies
	 * that keys were used to look up attributes
	 * from the back end sources
	 */
	Map<AttributeReferenceKey, BagOfAttributeValues> getResolvedKeys();

	EvaluationContext getEvaluationContext();
}
