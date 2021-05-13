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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.pdp.PolicyDecisionPointContext;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * A chain of {@link RequestContextHandler}
 * implementations implemented as {@link RequestContextHandler}
 *
 * @author Giedrius Trumpickas
 */
public class RequestContextHandlerChain
	implements RequestContextHandler
{
	private List<RequestContextHandler> handlers;

	public RequestContextHandlerChain(
			Iterable<RequestContextHandler> handlers)
	{
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
	public Collection<String> getFeatures() {
		Set<String> features = Sets.newLinkedHashSet();
		for(RequestContextHandler h : handlers){
			features.addAll(h.getFeatures());
		}
		return features;
	}

	@Override
	public final Collection<Result> handle(RequestContext req,
			PolicyDecisionPointContext context)
	{
		if(handlers.isEmpty()){
			return ImmutableList.of(context.requestDecision(req));
		}
		return postProcessResults(req,
				handlers.get(0).handle(req, context));
	}

	/**
	 * @exception IllegalStateException if this handler
	 * has no handlers in the internal list or handler
	 * was already set for last handler in the list
	 */
	@Override
	public final void setNext(RequestContextHandler handler)
	{
		Preconditions.checkState(!handlers.isEmpty());
		handlers.get(handlers.size() - 1).setNext(handler);
	}

	/**
	 * A hook to perform post-processing of
	 * decision results
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
