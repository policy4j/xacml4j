package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import com.artagon.xacml.util.Preconditions;

public class Result 
{
	private Status status;
	private Decision decision;
	private Collection<DefaultObligation> obligations;
	private Collection<DefaultAdvice> associatedAdvice;
	
	
	public Result(Status status){
		Preconditions.checkArgument(status.isFailure());
		this.status = status;
	}
	
	public Result(Decision decision, 
			Collection<DefaultAdvice> associatedAdvice, Collection<DefaultObligation> obligations){
		Preconditions.checkArgument(!decision.isIndeterminate());
		this.status = new Status(StatusId.OK);
		this.associatedAdvice = new LinkedList<DefaultAdvice>(associatedAdvice);
		this.obligations = new LinkedList<DefaultObligation>(obligations);
	}
	
	public Status getStatus(){
		return status;
	}
	
	public Decision getDecision(){
		return decision;
	}
	
	public Collection<DefaultObligation> getObligations(){
		return Collections.unmodifiableCollection(obligations);
	}
	
	public Collection<DefaultAdvice> getAssociatedAdvice(){
		return Collections.unmodifiableCollection(associatedAdvice);
	}
}
