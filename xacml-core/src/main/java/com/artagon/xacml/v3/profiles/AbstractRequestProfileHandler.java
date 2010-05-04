package com.artagon.xacml.v3.profiles;

import java.util.Collection;
import java.util.Collections;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.PolicyDecisionCallback;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestProfileHandler;
import com.artagon.xacml.v3.Result;

public abstract class AbstractRequestProfileHandler implements RequestProfileHandler
{
	private String id;
	private RequestProfileHandler next;
	
	public AbstractRequestProfileHandler(String id){
		Preconditions.checkNotNull(id);
		this.id = id;
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
}
