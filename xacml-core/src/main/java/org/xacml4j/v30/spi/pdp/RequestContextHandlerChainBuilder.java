package org.xacml4j.v30.spi.pdp;

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
