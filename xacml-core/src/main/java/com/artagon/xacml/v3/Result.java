package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public class Result extends XacmlObject
{
	private Status status;
	private Decision decision;
	private Map<String, Obligation> obligations;
	private Map<String, Advice> associatedAdvice;
	private Map<AttributeCategory, Attributes> attributes;
	private Collection<CompositeDecisionRuleIDReference> policyReferences;
	
	/**
	 * Constructs result with a given
	 * failure status and {@link Decision#INDETERMINATE}
	 * 
	 * @param status an failure status
	 */
	public Result(Decision decision, 
			Status status, 
			Collection<Attributes> attributes, 
			Collection<CompositeDecisionRuleIDReference> evaluatedPolicies){
		this(decision, status, 
				Collections.<Advice>emptyList(),
				Collections.<Obligation>emptyList(),
				attributes, evaluatedPolicies);
	}
	
	/**
	 * Constructs result with a given
	 * failure status and {@link Decision#isIndeterminate()}
	 * <code>true</code> or {@link Decision#NOT_APPLICABLE}
	 * 
	 * @param status an failure status
	 */
	public Result(Decision decision, 
			Status status, Collection<Attributes> attributes){
		this(decision, status, 
				Collections.<Advice>emptyList(),
				Collections.<Obligation>emptyList(),
				attributes, 
				Collections.<CompositeDecisionRuleIDReference>emptyList());
	}
	
	public Result(Decision decision, 
			Status status){
		this(decision, status, 
				Collections.<Advice>emptyList(),
				Collections.<Obligation>emptyList(),
				Collections.<Attributes>emptyList(),
				Collections.<CompositeDecisionRuleIDReference>emptyList());
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
			Status status,
			Iterable<Advice> associatedAdvice, 
			Iterable<Obligation> obligations,
			Iterable<Attributes> attributes,
			Iterable<CompositeDecisionRuleIDReference> policyIdentifiers){
		Preconditions.checkNotNull(decision);
		Preconditions.checkNotNull(status);
		Preconditions.checkNotNull(obligations);
		Preconditions.checkNotNull(associatedAdvice);
		Preconditions.checkNotNull(attributes);
		this.decision = decision;
		this.status = status;
		this.associatedAdvice = new LinkedHashMap<String, Advice>();
		this.obligations = new LinkedHashMap<String, Obligation>();
		this.attributes = new HashMap<AttributeCategory, Attributes>();
		this.policyReferences = new LinkedList<CompositeDecisionRuleIDReference>();
		Iterables.addAll(this.policyReferences, policyIdentifiers);
		for(Attributes attribute : attributes){
			this.attributes.put(attribute.getCategory(), attribute);
		}
		for(Obligation o : obligations){
			this.obligations.put(o.getId(), o);
		}
		for(Advice a : associatedAdvice){
			this.associatedAdvice.put(a.getId(), a);
		}
	}
	
	
	public static Result createIndeterminate(Status status, Collection<Attributes> attributes){
		return new Result(Decision.INDETERMINATE, status, attributes);
	}
	
	public static Result createIndeterminate(Status status){
		return new Result(Decision.INDETERMINATE, status, Collections.<Attributes>emptyList());
	}
	
	public static Result createIndeterminateSyntaxError(Collection<Attributes> attributes, 
			String format, Object ...params){
		return new Result(Decision.INDETERMINATE, 
				Status.createSyntaxError(format, params), attributes);
	}
	
	public static Result createIndeterminateProcessingError(Collection<Attributes> attributes,
			String format, Object ...params){
		return new Result(Decision.INDETERMINATE, 
				Status.createSyntaxError(format, params), attributes);
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
	 * with the request attributes {@link Attributes#isIncludeInResult()}
	 * 
	 * @return a collection of {@link Attributes} instances
	 */
	public Collection<Attributes> getAttributes(){
		return Collections.unmodifiableCollection(attributes.values());
	}
	
	public Attributes getAttribute(AttributeCategory categoryId){
		return attributes.get(categoryId);
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
		return Collections.unmodifiableCollection(obligations.values());
	}
	
	/**
	 * Gets obligation via obligation identifier
	 * 
	 * @param obligationId an obligation identifier
	 * @return {@link Obligation}
	 */
	public Obligation getObligation(String obligationId){
		return obligations.get(obligationId);
	}
	
	/**
	 * Returns a collection of advice that provide supplemental 
	 * information to the PEP. If the PEP does not understand 
	 * an advice, the PEP may safely ignore the advice.
	 * 
	 * @return a collection of associated advice
	 */
	public Collection<Advice> getAssociatedAdvice(){
		return Collections.unmodifiableCollection(associatedAdvice.values());
	}
	
	/**
	 * Gets associated advice via advice identifier
	 * 
	 * @param adviceId an advice identifier
	 * @return {@link Advice}
	 */
	public Advice getAssociatedAdvice(String adviceId){
		return associatedAdvice.get(adviceId);
	}
	
	/**
	 * Gets list of policy identifiers
	 * 
	 * @return list of policy identifiers
	 */
	public Collection<CompositeDecisionRuleIDReference> getPolicyIdentifiers(){
		return Collections.unmodifiableCollection(policyReferences);
	}
}
