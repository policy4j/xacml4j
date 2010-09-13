package com.artagon.xacml.v3.pdp.profiles;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.pdp.PolicyDecisionCallback;
import com.artagon.xacml.v3.pdp.RequestProfileHandler;
import com.google.common.base.Preconditions;

public abstract class AbstractRequestContextHandler implements RequestProfileHandler
{
	private AtomicReference<RequestProfileHandler> next;
		
	protected AbstractRequestContextHandler(){
		this.next = new AtomicReference<RequestProfileHandler>();
	}
	protected final Collection<Result> handleNext(RequestContext request, PolicyDecisionCallback pdp)
	{
		RequestProfileHandler h = next.get();
		return (h == null)?
				Collections.singleton(pdp.requestDecision(request)):
					h.handle(request, pdp);
	}

	public final void setNext(RequestProfileHandler handler) {
		Preconditions.checkNotNull(handler);
		this.next.set(handler);
	}
}
