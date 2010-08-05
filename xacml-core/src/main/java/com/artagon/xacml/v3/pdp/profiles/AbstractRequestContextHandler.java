package com.artagon.xacml.v3.pdp.profiles;

import java.util.Collection;
import java.util.Collections;

import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.pdp.PolicyDecisionCallback;
import com.artagon.xacml.v3.pdp.RequestProfileHandler;
import com.google.common.base.Preconditions;

public abstract class AbstractRequestContextHandler implements RequestProfileHandler
{
	private RequestProfileHandler next;
		
	protected final Collection<Result> handleNext(RequestContext request, PolicyDecisionCallback pdp)
	{
		return (next == null)?
				Collections.singleton(pdp.requestDecision(request)):
					next.handle(request, pdp);
	}

	public final void setNext(RequestProfileHandler handler) {
		Preconditions.checkNotNull(handler);
		this.next = handler;
	}
}
