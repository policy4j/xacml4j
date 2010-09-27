package com.artagon.xacml.v3.pdp;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.Result;
import com.google.common.base.Preconditions;

public abstract class AbstractRequestContextHandler implements RequestContextHandler
{
	private AtomicReference<RequestContextHandler> next;
		
	protected AbstractRequestContextHandler(){
		this.next = new AtomicReference<RequestContextHandler>();
	}
	
	protected final Collection<Result> handleNext(RequestContext request, 
			PolicyDecisionCallback pdp)
	{
		RequestContextHandler h = next.get();
		return (h == null)?
				Collections.singleton(pdp.requestDecision(request)):
					h.handle(request, pdp);
	}

	@Override
	public final void setNext(RequestContextHandler handler) {
		Preconditions.checkNotNull(handler);
		Preconditions.checkState(next.get() == null, 
				"Handler is already included in the chain");
		this.next.set(handler);
	}
}
