package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import com.artagon.xacml.util.Preconditions;

public class Result extends XacmlObject
{
	private Status status;
	private Decision decision;
	private Collection<Obligation> obligations;
	private Collection<Advice> associatedAdvice;
	private Collection<Attributes> attributes;
	private Collection<PolicyIdentifier> policyIdentifiers;
	
	/**
	 * Constructs result with a given
	 * failure status
	 * 
	 * @param status an failure status
	 */
	public Result(Status status){
		Preconditions.checkArgument(status.isFailure());
		this.status = status;
		this.policyIdentifiers = new LinkedList<PolicyIdentifier>();
	}
	
	/**
	 * Constructs result with a given decision
	 * 
	 * @param decision a decision
	 * @param associatedAdvice an associated advice
	 * @param obligations an associated obligations
	 * @param attributes an attributes to be
	 * included in response
	 */
	public Result(
			Decision decision, 
			Collection<Advice> associatedAdvice, 
			Collection<Obligation> obligations,
			Collection<Attributes> attributes){
		Preconditions.checkArgument(!decision.isIndeterminate());
		this.status = Status.createSuccessStatus();
		this.associatedAdvice = new LinkedList<Advice>(associatedAdvice);
		this.obligations = new LinkedList<Obligation>(obligations);
		this.attributes = new LinkedList<Attributes>(attributes);
		this.policyIdentifiers = new LinkedList<PolicyIdentifier>();
	}
	
	/**
	 * Gets result status
	 * @return
	 */
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
	
	public Collection<PolicyIdentifier> getPolicyIdentifiers(){
		return Collections.unmodifiableCollection(policyIdentifiers);
	}
}
