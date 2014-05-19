package org.xacml4j.v30.spi.pip;

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

import java.util.Calendar;
import java.util.List;

import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.EvaluationException;

import com.google.common.base.Ticker;


public interface ResolverContext
{
	/**
	 * Gets current evaluation context date/time
	 *
	 * @return {@link Calendar} instance
	 * representing current date/time
	 */
	Calendar getCurrentDateTime();

	/**
	 * Gets ticker
	 *
	 * @return {@link Ticker}
	 */
	Ticker getTicker();

	/**
	 * Gets resolver descriptor
	 *
	 * @return {@link ResolverDescriptor}
	 */
	ResolverDescriptor getDescriptor();

	/**
	 * Gets request context keys
	 *
	 * @return a list {@link List} of {@link BagOfAttributeExp}
	 * instances
	 * @throws EvaluationException if an error
	 * occurs
	 */
	List<BagOfAttributeExp> getKeys();
}
