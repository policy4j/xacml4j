package org.xacml4j.v30.spi.pdp;

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

import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.pdp.PolicyDecisionPointContext;

import java.util.Collection;


public interface RequestContextHandler
{
	/**
	 * Handles given request
	 *
	 * @param request a decision request
	 * @param context a policy decision point context
	 * @return collection of {@link Result} instances
	 */
	Collection<Result> handle(
			RequestContext request,
			PolicyDecisionPointContext context);

	/**
	 * Gets list of supported features by this handler
	 *
	 * @return a list of supported feature identifiers
	 */
	Collection<String> getFeatures();

	/**
	 * Sets next handler in a chain
	 *
	 * @param handler a next in chain request handler
	 * @exception IllegalStateException if "next" handler
	 * is already set for this handler
	 */
	default void setNext(RequestContextHandler handler){
		setNext(handler, true);
	}

	/**
	 * Sets next handler in a chain
	 *
	 * @param handler a next in chain request handler
	 * @param makeImmutable a flag indicating if this handler needs to be made immutable
	 * @exception IllegalStateException if "next" handler
	 * is already set for this handler
	 */
	void setNext(RequestContextHandler handler, boolean makeImmutable);
}
