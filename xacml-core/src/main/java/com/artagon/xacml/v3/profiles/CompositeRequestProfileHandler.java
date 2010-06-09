package com.artagon.xacml.v3.profiles;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.pdp.PolicyDecisionCallback;

public class CompositeRequestProfileHandler implements RequestProfileHandler
{
	private List<RequestProfileHandler> handlers;
	
	public CompositeRequestProfileHandler(
			RequestProfileHandler ...handlers)
	{
		this.handlers = Arrays.asList(handlers);
		RequestProfileHandler prev = null;
		for(RequestProfileHandler h : handlers){
			if(prev == null){
				prev = h;
				continue;
			}
			prev.setNext(h);
			prev = h;
		}
	}
	
	@Override
	public final Collection<Result> handle(Request request, PolicyDecisionCallback pdp) {
		return handlers.get(0).handle(request, pdp);
	}

	@Override
	public final void setNext(RequestProfileHandler handler) {
		handlers.get(handlers.size() - 1).setNext(handler);
	}
}
