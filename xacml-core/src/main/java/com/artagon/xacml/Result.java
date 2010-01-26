package com.artagon.xacml;

import java.util.Collection;
import java.util.Collections;

public class Result 
{
	private Status status;
	private DecisionResult decision;
	private Collection<Obligation> obligations;
	private Collection<Advice> associatedAdvice;
	
	
	public Status getStatus(){
		return status;
	}
	
	public DecisionResult getDecision(){
		return decision;
	}
	
	public Collection<Obligation> getObligations(){
		return Collections.unmodifiableCollection(obligations);
	}
	
	public Collection<Advice> getAssociatedAdvice(){
		return Collections.unmodifiableCollection(associatedAdvice);
	}
}
