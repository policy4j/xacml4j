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

import java.util.LinkedList;
import java.util.List;

import org.xacml4j.v30.pdp.profiles.MultipleResourcesHandler;

import com.google.common.base.Preconditions;

public final class RequestContextHandlerChainBuilder
{
	private List<RequestContextHandler> handlers;

	private RequestContextHandlerChainBuilder(){
		this.handlers = new LinkedList<RequestContextHandler>();
	}

	public static RequestContextHandlerChainBuilder builder(){
		return new RequestContextHandlerChainBuilder();
	}

	/**
	 * Adds default XACML 3.0 handlers to the chain
	 *
	 * @return {@link RequestContextHandlerChainBuilder}
	 */
	public RequestContextHandlerChainBuilder withDefaultHandlers()
	{
		withRequestHandler(new MultipleResourcesHandler());
		return this;
	}

	public RequestContextHandlerChainBuilder withRequestHandler(RequestContextHandler handler)
	{
		Preconditions.checkNotNull(handler);
		this.handlers.add(handler);
		return this;
	}

	public RequestContextHandler build(){
		return new RequestContextHandlerChain(handlers);
	}
}
