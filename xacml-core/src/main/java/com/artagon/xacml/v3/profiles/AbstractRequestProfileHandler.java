package com.artagon.xacml.v3.profiles;

import java.util.Collection;
import java.util.Collections;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.PolicyDecisionPoint;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestProfileHandler;
import com.artagon.xacml.v3.Result;

public abstract class AbstractRequestProfileHandler implements RequestProfileHandler
{
	private String id;
	private RequestProfileHandler next;
	private PolicyDecisionPoint pdp;
	
	public AbstractRequestProfileHandler(String id, 
			PolicyDecisionPoint pdp){
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(pdp);
		this.id = id;
		this.pdp = pdp;
	}

	@Override
	public final String getId() {
		return id;
	}
	
	protected final Collection<Result> handleNext(Request request){
		return next == null?Collections.singleton(pdp.decide(request)):next.handle(request);
	}

	public final void setNext(RequestProfileHandler handler) {
		Preconditions.checkNotNull(handler);
		this.next = handler;
	}		
}
