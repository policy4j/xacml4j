package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.Collections;

public abstract class AbstractPolicyDecisionPoint implements PolicyDecisionPoint, PolicyDecisionCallback
{
	private RequestProfileHandler handler;
	
	protected AbstractPolicyDecisionPoint(
			Collection<RequestProfileHandler> chainOfHandlers)
	{
		RequestProfileHandler previous = null;
		for(RequestProfileHandler h : chainOfHandlers)
		{
			if(handler  == null){
				handler = h;
			}
			if(previous != null){
				previous.setNext(h);
			}
			previous = h;
		}
	}
	
	@Override
	public final Response decide(Request request) {
		 Collection<Result> results = (handler == null)?
				 Collections.singleton(doDecide(request)):handler.handle(request, this);
		 return new Response(results);
	}
	
	@Override
	public final Result requestDecision(Request request) {
		return doDecide(request);
	}

	protected abstract Result doDecide(Request request);
	
	
}
