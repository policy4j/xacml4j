package org.xacml4j.v30.spi.function;

/*
 * #%L
 * Artagon XACML 3.0 Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Artagon
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

import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.spi.function.FunctionSpecBuilder.FunctionSpecImpl;


public interface FunctionProvider
{
	/**
	 * Gets function instance for a given function
	 * identifier.
	 *
	 * @param functionId a function identifier
	 * @return {@link FunctionSpecImpl} instance for a given
	 * identifier or {@code null} if function
	 * can not be found for a given identifier
	 */
	FunctionSpec getFunction(String functionId);

	/**
	 * Tests if given function is supported by
	 * this factory
	 *
	 * @param functionId a function identifier
	 * @return {@code true} if function
	 * is supported by this factory
	 */
	boolean isFunctionProvided(String functionId);

	/**
	 * Removes a function with a given identifier
	 *
	 * @param functionId a function identifier
	 * @return {@link FunctionSpec} a reference
	 * to removed function or {@code null}
	 */
	FunctionSpec remove(String functionId);

	/**
	 * Gets all supported function by this factory
	 *
	 * @return {@link Iterable} over all supported
	 * function identifiers
	 */
	Iterable<String> getProvidedFunctions();
}
