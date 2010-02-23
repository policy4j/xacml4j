package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import com.artagon.xacml.util.Preconditions;

public class Result 
{
	private Status status;
	private Decision decision;
	private Collection<Obligation> obligations;
	private Collection<Advice> associatedAdvice;
	private Collection<Attributes> attributes;
	
	public Result(Status status){
		Preconditions.checkArgument(status.isFailure());
		this.status = status;
	}
	
	public Result(Decision decision, 
			Collection<Advice> associatedAdvice, 
			Collection<Obligation> obligations,
			Collection<Attributes> attributes){
		Preconditions.checkArgument(!decision.isIndeterminate());
		this.status = new Status(StatusId.OK);
		this.associatedAdvice = new LinkedList<Advice>(associatedAdvice);
		this.obligations = new LinkedList<Obligation>(obligations);
		this.attributes = new LinkedList<Attributes>(attributes);
	}
	
	public Status getStatus(){
		return status;
	}
	
	public Decision getDecision(){
		return decision;
	}
	
	public Collection<Attributes> getAttributes(){
		return attributes;
	}
	
	public Collection<Obligation> getObligations(){
		return Collections.unmodifiableCollection(obligations);
	}
	
	public Collection<Advice> getAssociatedAdvice(){
		return Collections.unmodifiableCollection(associatedAdvice);
	}
}
