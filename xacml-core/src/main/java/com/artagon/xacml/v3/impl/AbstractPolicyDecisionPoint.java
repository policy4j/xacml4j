package com.artagon.xacml.v3.impl;

import java.util.Collection;
import java.util.Collections;

import com.artagon.xacml.v3.RequestContextFactory;
import com.artagon.xacml.v3.PolicyDecisionCallback;
import com.artagon.xacml.v3.PolicyDecisionPoint;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestProfileHandler;
import com.artagon.xacml.v3.Response;
import com.artagon.xacml.v3.Result;

public abstract class AbstractPolicyDecisionPoint implements PolicyDecisionPoint, PolicyDecisionCallback
{
	private RequestProfileHandler handler;
	private RequestContextFactory contextFactory;
	
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
		 return contextFactory.createResponse(results);
	}
	
	@Override
	public final Result requestDecision(Request request) {
		return doDecide(request);
	}

	protected abstract Result doDecide(Request request);
	
	
}
