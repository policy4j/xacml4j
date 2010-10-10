package com.artagon.xacml.v3.pdp;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.Result;
import com.google.common.collect.Iterables;

/**
 * A chain of {@link RequestContextHandler}
 * implementations implemented as {@link RequestContextHandler}
 * 
 * @author Giedrius Trumpickas
 */
public class RequestContextHandlerChain implements RequestContextHandler
{
	private List<RequestContextHandler> handlers;
	
	public RequestContextHandlerChain(
			Iterable<RequestContextHandler> handlers)
	{
		Concu
		this.handlers = new LinkedList<RequestContextHandler>();
		Iterables.addAll(this.handlers, handlers);
		RequestContextHandler prev = null;
		for(RequestContextHandler h : handlers){
			if(prev == null){
				prev = h;
				continue;
			}
			prev.setNext(h);
			prev = h;
		}
	}
	
	public RequestContextHandlerChain(
			RequestContextHandler ...handlers){
		this(Arrays.asList(handlers));
	}
	
	@Override
	public final Collection<Result> handle(RequestContext request, PolicyDecisionCallback pdp) 
	{
		if(handlers.isEmpty()){
			return Collections.singleton(pdp.requestDecision(request));
		}
		return postProcessResults(request, handlers.get(0).handle(request, pdp));
	}

	@Override
	public final void setNext(RequestContextHandler handler) 
	{
		if(handlers.isEmpty()){
			throw new IllegalArgumentException("Can't set next handler, " +
					"this handler does not have any handlers");
		}
		handlers.get(handlers.size() - 1).setNext(handler);
	}
	
	/**
	 * A hooh to post-process decision results
	 * 
	 * @param req a decision request
	 * @param results a collection of decision results
	 * @return a collection of decision results
	 */
	protected Collection<Result> postProcessResults(RequestContext req, 
			Collection<Result> results){
		return results;
	}
}
