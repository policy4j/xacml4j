package com.artagon.xacml.v3.profiles;

import java.util.Collection;
import java.util.Collections;

import com.artagon.xacml.v3.RequestContextFactory;
import com.artagon.xacml.v3.PolicyDecisionCallback;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestProfileHandler;
import com.artagon.xacml.v3.Result;
import com.google.common.base.Preconditions;

public abstract class AbstractRequestProfileHandler implements RequestProfileHandler
{
	private String id;
	private RequestProfileHandler next;
	private RequestContextFactory contextFactory;
	
	public AbstractRequestProfileHandler(String id, RequestContextFactory contextFactory){
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
	
	protected final RequestContextFactory getContextFactory(){
		return contextFactory;
	}
}
