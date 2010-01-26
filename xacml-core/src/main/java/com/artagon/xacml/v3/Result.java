package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import com.artagon.xacml.util.Preconditions;

public class Result 
{
	private Status status;
	private DecisionResult decision;
	private Collection<Obligation> obligations;
	private Collection<Advice> associatedAdvice;
	
	
	public Result(Status status){
		Preconditions.checkArgument(status.isFailure());
		this.status = status;
	}
	
	public Result(DecisionResult decision, 
			Collection<Advice> associatedAdvice, Collection<Obligation> obligations){
		Preconditions.checkArgument(!decision.isIndeterminate());
		this.status = new Status(StatusId.OK);
		this.associatedAdvice = new LinkedList<Advice>(associatedAdvice);
		this.obligations = new LinkedList<Obligation>(obligations);
	}
	
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
