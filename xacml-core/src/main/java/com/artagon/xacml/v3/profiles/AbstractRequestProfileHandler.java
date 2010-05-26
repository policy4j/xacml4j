package com.artagon.xacml.v3.profiles;

import java.util.Collection;
import java.util.Collections;

import com.artagon.xacml.v3.PolicyDecisionCallback;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestFactory;
import com.artagon.xacml.v3.Result;
import com.google.common.base.Preconditions;

public abstract class AbstractRequestProfileHandler implements RequestProfileHandler
{
	private String id;
	private RequestProfileHandler next;
	private RequestFactory contextFactory;
	
	public AbstractRequestProfileHandler(String id, RequestFactory contextFactory){
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(contextFactory);
		this.id = id;
		this.contextFactory = contextFactory;
	}

	@Override
	public final String getId() {
		return id;
	}
	
	protected final Collection<Result> handleNext(Request request, PolicyDecisionCallback pdp){
		return (next == null)?
				Collections.singleton(pdp.requestDecision(request)):
					next.handle(request, pdp);
	}

	public final void setNext(RequestProfileHandler handler) {
		Preconditions.checkNotNull(handler);
		this.next = handler;
	}
	
	protected final RequestFactory getContextFactory(){
		return contextFactory;
	}
}
