package com.artagon.xacml.v30.pdp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.Status;
import com.artagon.xacml.v30.StatusCode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public class Result extends XacmlObject
{
	private Status status;
	private Decision decision;
	private Map<String, Obligation> obligations;
	private Map<String, Advice> associatedAdvice;
	private Map<AttributeCategory, Attributes> includeInResultAttributes;
	private Collection<CompositeDecisionRuleIDReference> policyReferences;
	private Map<AttributeCategory, Attributes> resolvedAttributes;
	
	/**
	 * Constructs result with a given
	 * failure status and {@link Decision#INDETERMINATE}
	 * 
	 * @param status an failure status
	 */
	public Result(Decision decision, 
			Status status, 
			Collection<Attributes> attributes, 
			Collection<Attributes> resolvedAttributes,
			Collection<CompositeDecisionRuleIDReference> evaluatedPolicies){
		this(decision, status, 
				Collections.<Advice>emptyList(),
				Collections.<Obligation>emptyList(),
				attributes, 
				resolvedAttributes, 
				evaluatedPolicies);
	}
	
	/**
	 * Constructs result with a given
	 * failure status and {@link Decision#isIndeterminate()}
	 * <code>true</code> or {@link Decision#NOT_APPLICABLE}
	 * 
	 * @param status an failure status
	 */
	public Result(Decision decision, 
			Status status, 
			Collection<Attributes> attributes, 
			Collection<Attributes> resolvedAttributes){
		this(decision, status, 
				Collections.<Advice>emptyList(),
				Collections.<Obligation>emptyList(),
				attributes, 
				resolvedAttributes,
				Collections.<CompositeDecisionRuleIDReference>emptyList());
	}
	
	public Result(Decision decision, 
			Status status){
		this(decision, status, 
				Collections.<Advice>emptyList(),
				Collections.<Obligation>emptyList(),
				Collections.<Attributes>emptyList(),
				Collections.<Attributes>emptyList(),
				Collections.<CompositeDecisionRuleIDReference>emptyList());
	}
	
	public Result(
			Decision decision, 
			Status status,
			Iterable<Obligation> obligations,
			Iterable<Attributes> includeInResult){
		this(decision,  status, 
				Collections.<Advice>emptyList(), 
				obligations, 
				includeInResult,
				Collections.<Attributes>emptyList(),
				Collections.<CompositeDecisionRuleIDReference>emptyList());
	}
	
	/**
	 * Constructs result with a given decision
	 * 
	 * @param decision a decision
	 * @param associatedAdvice an associated advice
	 * @param obligations an associated obligations
	 * @param includeInResultAttributes an attributes to be
	 * included in response
	 */
	public Result(
			Decision decision, 
			Status status,
			Iterable<Advice> associatedAdvice, 
			Iterable<Obligation> obligations,
			Iterable<Attributes> includeInResultAttributes,
			Iterable<Attributes> resolvedAttributes,
			Iterable<CompositeDecisionRuleIDReference> policyIdentifiers){
		Preconditions.checkNotNull(decision);
		Preconditions.checkNotNull(status);
		Preconditions.checkNotNull(obligations);
		Preconditions.checkNotNull(associatedAdvice);
		Preconditions.checkNotNull(includeInResultAttributes);
		Preconditions.checkNotNull(resolvedAttributes);
		Preconditions.checkArgument(!(decision.isIndeterminate() ^ 
				status.isFailure()));
		this.decision = decision;
		this.status = status;
		this.associatedAdvice = new LinkedHashMap<String, Advice>();
		this.obligations = new LinkedHashMap<String, Obligation>();
		this.includeInResultAttributes = new HashMap<AttributeCategory, Attributes>();
		this.policyReferences = new LinkedList<CompositeDecisionRuleIDReference>();
		this.resolvedAttributes = new HashMap<AttributeCategory, Attributes>();
		Iterables.addAll(this.policyReferences, policyIdentifiers);
		for(Attributes attribute : includeInResultAttributes){
			this.includeInResultAttributes.put(attribute.getCategory(), attribute);
		}
		for(Attributes attribute : resolvedAttributes){
			this.resolvedAttributes.put(attribute.getCategory(), attribute);
		}
		for(Obligation o : obligations){
			this.obligations.put(o.getId(), o);
		}
		for(Advice a : associatedAdvice){
			this.associatedAdvice.put(a.getId(), a);
		}
	}
	
	public Result(Builder b){
		this.decision = b.decision;
		this.status = b.status;
		this.associatedAdvice = Collections.unmodifiableMap(b.associatedAdvice);
		this.obligations = Collections.unmodifiableMap(b.obligations);
		this.includeInResultAttributes = Collections.unmodifiableMap(b.includeInResultAttributes);
		this.policyReferences = Collections.unmodifiableCollection(b.policyReferences);
		this.resolvedAttributes = Collections.unmodifiableMap(b.resolvedAttributes);
	}
	
	public static Result.Builder createOk(Decision d){
		Preconditions.checkArgument(!d.isIndeterminate());
		return new Result.Builder(d, Status.createSuccess());
	}
	
	public static Result.Builder createIndeterminate(Status status){
		Preconditions.checkArgument(status.isFailure());
		return new Result.Builder(Decision.INDETERMINATE, status);
	}
	
	public static Result.Builder createIndeterminate(StatusCode status){
		Preconditions.checkArgument(status.isFailure());
		return new Result.Builder(Decision.INDETERMINATE, new Status(status));
	}
	
	public static Result.Builder createIndeterminateSyntaxError(
			String format, Object ...params){
		return createIndeterminate(Status.createSyntaxError(format, params));
	}
	
	public static Result.Builder createIndeterminateSyntaxError(){
		return createIndeterminate(Status.createSyntaxError());
	}
	
	public static Result.Builder createIndeterminateProcessingError(
			String format, Object ...params){
		return createIndeterminate(Status.createProcessingError(format, params));
	}
	
	public static Result.Builder createIndeterminateProcessingError(){
		return createIndeterminate(Status.createProcessingError());
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
	public Collection<Attributes> getIncludeInResultAttributes(){
		return Collections.unmodifiableCollection(includeInResultAttributes.values());
	}
	
	/**
	 * Gets all resolved by PIP attributes from external
	 * sources during request context evaluation
	 * 
	 * @return a collection of resolved attributes 
	 */
	public Collection<Attributes> getResolvedAttributes(){
		return Collections.unmodifiableCollection(resolvedAttributes.values());
	}
	
	public Attributes getAttribute(AttributeCategory categoryId){
		return includeInResultAttributes.get(categoryId);
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
	
	public static class Builder
	{
		private Status status;
		private Decision decision;
		private Map<String, Obligation> obligations = new LinkedHashMap<String, Obligation>();
		private Map<String, Advice> associatedAdvice = new LinkedHashMap<String, Advice>();
		private Map<AttributeCategory, Attributes> includeInResultAttributes = new LinkedHashMap<AttributeCategory, Attributes>();
		private Collection<CompositeDecisionRuleIDReference> policyReferences = new LinkedList<CompositeDecisionRuleIDReference>();
		private Map<AttributeCategory, Attributes> resolvedAttributes = new LinkedHashMap<AttributeCategory, Attributes>();
		
		public Builder(Decision d, Status s){
			Preconditions.checkNotNull(d);
			Preconditions.checkNotNull(s);
			Preconditions.checkArgument(!(d.isIndeterminate() ^ 
					s.isFailure()));
			this.decision = d;
			this.status = s;
		}
		
		public Builder includeInResult(Attributes a){
			Preconditions.checkNotNull(a);
			Preconditions.checkState(includeInResultAttributes.put(a.getCategory(), a) != null);
			return this;
		}
		
		public Builder includeInResultAttr(Iterable<Attributes> attributes){
			for(Attributes a : attributes){
				includeInResultAttributes.put(a.getCategory(), a);
			}
			return this;
		}
		
		public Builder resolvedAttr(Iterable<Attributes> attributes){
			for(Attributes a : attributes){
				resolvedAttributes.put(a.getCategory(), a);
			}
			return this;
		}
		
		public Builder referencedPolicy(CompositeDecisionRuleIDReference ... refs)
		{
			Preconditions.checkNotNull(refs);
			for(CompositeDecisionRuleIDReference ref : refs){
				Preconditions.checkNotNull(ref);
				this.policyReferences.add(ref);
			}
			return this;
		}
		
		public Builder evaluatedPolicies(Iterable<CompositeDecisionRuleIDReference> refs)
		{
			Preconditions.checkNotNull(refs);
			for(CompositeDecisionRuleIDReference ref : refs){
				Preconditions.checkNotNull(ref);
				this.policyReferences.add(ref);
			}
			return this;
		}
				
		public Builder advice(Iterable<Advice> advice){
			for(Advice a : advice){
				advice(a);
			}
			return this;
		}
		
		public Builder advice(Advice advice){
			Preconditions.checkNotNull(advice);
			Advice a = associatedAdvice.get(advice.getId());			
			if(a != null){
				associatedAdvice.put(a.getId(), a.merge(advice));
				return this;
			}
			associatedAdvice.put(advice.getId(), advice);
			return this;
		}
		
		public Builder obligation(Obligation obligation){
			Preconditions.checkNotNull(obligation);
			Obligation a = obligations.get(obligation.getId());
			if(a != null){
				obligations.put(a.getId(), a.merge(obligation));
				return this;
			}
			obligations.put(obligation.getId(), obligation);
			return this;
		}
		
		public Builder obligation(Iterable<Obligation> obligations){
			for(Obligation o : obligations){
				Preconditions.checkNotNull(o);
				obligation(o);
			}
			return this;
		}
		
		public Result create(){
			return new Result(this);
		}
	}
}
