package com.artagon.xacml.v3.profiles;

import java.util.Collection;
import java.util.Collections;

import com.artagon.xacml.v3.context.ContextFactory;
import com.artagon.xacml.v3.context.Request;
import com.artagon.xacml.v3.context.Result;
import com.artagon.xacml.v3.pdp.PolicyDecisionCallback;
import com.google.common.base.Preconditions;

public abstract class AbstractRequestProfileHandler implements RequestProfileHandler
{
	private String id;
	private RequestProfileHandler next;
	private ContextFactory contextFactory;
	
	public AbstractRequestProfileHandler(String id, ContextFactory contextFactory){
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
	
	protected final ContextFactory getContextFactory(){
		return contextFactory;
	}
}
