package com.artagon.xacml.v3.pdp;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import com.artagon.xacml.v3.context.RequestContext;
import com.artagon.xacml.v3.context.Result;
import com.google.common.base.Preconditions;

public abstract class AbstractRequestContextHandler 
	implements RequestContextHandler
{
	private AtomicReference<RequestContextHandler> next;
		
	protected AbstractRequestContextHandler(){
		this.next = new AtomicReference<RequestContextHandler>();
	}
	
	/**
	 * A helper method to be used by implementations
	 * to invoke a next handler in the chain
	 * 
	 * @param request a decision request
	 * @param pdp a policy decision point callback
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
