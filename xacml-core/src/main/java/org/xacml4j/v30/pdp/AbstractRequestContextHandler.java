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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.spi.pdp.RequestContextHandler;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public abstract class AbstractRequestContextHandler
	implements RequestContextHandler
{
	private AtomicReference<RequestContextHandler> next;
	private List<String> features;

	protected AbstractRequestContextHandler(String featureId)
	{
		Preconditions.checkNotNull(featureId);
		this.features = ImmutableList.of(featureId);
		this.next = new AtomicReference<RequestContextHandler>();
	}

	@Override
	public Collection<String> getFeatures(){
		return features;
	}

	/**
	 * A helper method to be used by implementations
	 * to invoke a next handler in the chain
	 *
	 * @param request a decision request
	 * @param context a policy decision point context
	 * @return collection of  {@link Result} instances
	 */
	protected final Collection<Result> handleNext(
			RequestContext request,
			PolicyDecisionPointContext context)
	{
		RequestContextHandler h = next.get();
		return (h == null)?
				Collections.singleton(context.requestDecision(request)):
					h.handle(request, context);
	}

	/**
	 * @exception IllegalStateException if this handler
	 * already has handler set
	 */
	@Override
	public final void setNext(RequestContextHandler handler) {
		Preconditions.checkNotNull(handler);
		Preconditions.checkState(next.get() == null,
				"Handler is already has next handler");
		this.next.set(handler);
	}
}
