package org.xacml4j.v30.spi.function;

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

import org.xacml4j.v30.policy.FunctionSpec;

import java.util.*;

/**
 * Provider interface for XACML functions
 *
 * @author Giedrius Trumpickas
 */
public interface FunctionProvider
{
	/**
	 * Gets function provider id
	 *
	 * @return this function provider id
	 */
	default String getId(){
		return getClass().getName();
	}

	/**
	 * Gets function provider description
	 *
	 * @return this function provider descriptor
	 */
	default String getDescription() {
		return getClass().getSimpleName();
	}

	/**
	 * Gets function defaultProvider for a given function
	 * identifier.
	 *
	 * @param functionId a function identifier
	 * @return {@link FunctionSpec} defaultProvider for a given
	 * identifier or {@code null} if function
	 * can not be found for a given identifier
	 */
	Optional<FunctionSpec> getFunction(String functionId);


	/**
	 * Gets all supported function by this factory
	 *
	 * @return {@link Iterable} over all supported
	 * function identifiers
	 */
	Collection<FunctionSpec> getProvidedFunctions();



}
