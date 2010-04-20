package com.artagon.xacml.v3;

import java.util.ArrayList;
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
		Preconditions.checkNotNull(status);
		Preconditions.checkArgument(status.isFailure());
		this.status = status;
		this.policyIdentifiers = Collections.emptyList();
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
			Collection<Attributes> attributes,
			Collection<PolicyIdentifier> policyIdentifiers){
		Preconditions.checkNotNull(decision);
		Preconditions.checkNotNull(obligations);
		Preconditions.checkNotNull(associatedAdvice);
		Preconditions.checkNotNull(attributes);
		Preconditions.checkArgument(!decision.isIndeterminate());
		this.decision = decision;
		this.status = Status.createSuccessStatus();
		this.associatedAdvice = new LinkedList<Advice>(associatedAdvice);
		this.obligations = new LinkedList<Obligation>(obligations);
		this.attributes = new LinkedList<Attributes>(attributes);
		this.policyIdentifiers = new ArrayList<PolicyIdentifier>(policyIdentifiers);
	}
	
	/**
	 * Constructs result with given
	 * {@link Decision} instance
	 * 
	 * @param decision a PDP decision
	 */
	public Result(Decision decision){
		this(decision, 
				Collections.<Advice>emptyList(),
				Collections.<Obligation>emptyList(), 
				Collections.<Attributes>emptyList(), 
				Collections.<PolicyIdentifier>emptyList());
	}
		
	/**
	 * Gets a status of this result. Status indicates 
	 * whether errors occurred during evaluation of 
	 * the decision request, and optionally, information 
	 * about those errors.
	 * 
	 * @return {@link Status}
	 */
	public Status getStatus(){
		return status;
	}
	
	/**
	 * Gets the authorization decision
	 * 
	 * @return {@link Decision}
	 */
	public Decision getDecision(){
		return decision;
	}
	
	/**
	 * Gets a list of attributes that were part of the request. 
	 * The choice of which attributes are included here is made 
	 * with the request attributes {@link Attribute#isIncludeInResult()}
	 * 
	 * @return a collection of {@link Attribute} instances
	 */
	public Collection<Attributes> getAttributes(){
		return attributes;
	}
	
	/**
	 * Returns a collection of obligations that MUST be 
	 * fulfilled by the PEP. If the PEP does not understand 
	 * or cannot fulfill an obligation, then the action of the 
	 * PEP is determined by its bias
	 * 
	 * @return a collection of obligations
	 */
	public Collection<Obligation> getObligations(){
		return Collections.unmodifiableCollection(obligations);
	}
	
	/**
	 * Returns a collection of advice that provide supplemental 
	 * information to the PEP. If the PEP does not understand 
	 * an advice, the PEP may safely ignore the advice.
	 * 
	 * @return
	 */
	public Collection<Advice> getAssociatedAdvice(){
		return Collections.unmodifiableCollection(associatedAdvice);
	}
	
	/**
	 * Gets list of policy identifiers
	 * 
	 * @return list of policy identifiers
	 */
	public Collection<PolicyIdentifier> getPolicyIdentifiers(){
		return Collections.unmodifiableCollection(policyIdentifiers);
	}
}
