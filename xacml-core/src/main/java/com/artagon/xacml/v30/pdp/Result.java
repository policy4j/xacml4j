package com.artagon.xacml.v30.pdp;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.Status;
import com.artagon.xacml.v30.StatusCode;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class Result
{
	private Status status;
	private Decision decision;
	private Map<String, Obligation> obligations;
	private Map<String, Advice> associatedAdvice;
	private Map<AttributeCategory, Attributes> includeInResultAttributes;
	private Collection<CompositeDecisionRuleIDReference> policyReferences;
	private Map<AttributeCategory, Attributes> resolvedAttributes;
	private int hashCode;

	public Result(Builder b){
		this.decision = b.decision;
		this.status = b.status;
		this.associatedAdvice = ImmutableMap.copyOf(b.associatedAdvice);
		this.obligations = ImmutableMap.copyOf(b.obligations);
		this.includeInResultAttributes = ImmutableMap.copyOf(b.includeInResultAttributes);
		this.policyReferences = ImmutableList.copyOf(b.policyReferences);
		this.resolvedAttributes = ImmutableMap.copyOf(b.resolvedAttributes);
		this.hashCode = Objects.hashCode(decision, status,
				associatedAdvice,
				obligations,
				includeInResultAttributes,
				policyReferences,
				resolvedAttributes);
	}

	public static Result.Builder builder(Decision d, Status status){
		return new Result.Builder(d, status);
	}

	public static Result.Builder createOk(Decision d){
		Preconditions.checkArgument(!d.isIndeterminate());
		return new Result.Builder(d, Status.createSuccess());
	}

	public static Result.Builder createIndeterminate(Decision d, Status status){
		Preconditions.checkArgument(status.isFailure());
		Preconditions.checkArgument(d.isIndeterminate());
		return new Result.Builder(d, status);
	}

	public static Result.Builder createIndeterminate(Decision d, StatusCode status){
		return new Result.Builder(d, new Status(status));
	}

	public static Result.Builder createIndeterminateSyntaxError(
			String format, Object ...params){
		return createIndeterminate(Decision.INDETERMINATE,
				Status.createSyntaxError(format, params));
	}

	public static Result.Builder createIndeterminateSyntaxError(){
		return createIndeterminate(Decision.INDETERMINATE,
				Status.createSyntaxError());
	}

	public static Result.Builder createIndeterminateProcessingError(
			String format, Object ...params){
		return createIndeterminate(Decision.INDETERMINATE,
				Status.createProcessingError(format, params));
	}

	public static Result.Builder createIndeterminateProcessingError(){
		return createIndeterminate(Decision.INDETERMINATE,
				Status.createProcessingError());
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

	@Override
	public String toString(){
		return Objects
				.toStringHelper(this)
				.add("status", status)
				.add("decision", decision)
				.add("obligations", obligations.values())
				.add("associatedAdvice", associatedAdvice.values())
				.add("policyIdReferences", policyReferences)
				.add("includeInResultAttrs", includeInResultAttributes.values())
				.add("resolvedAttrs", resolvedAttributes.values())
				.toString();
	}

	@Override
	public int hashCode(){
		return hashCode;
	}

	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof Result)){
			return false;
		}
		Result r = (Result)o;
		return decision.equals(r.decision) &&
			status.equals(r.status) &&
			obligations.equals(r.obligations) &&
			associatedAdvice.equals(r.associatedAdvice) &&
			policyReferences.equals(r.policyReferences) &&
			includeInResultAttributes.equals(r.includeInResultAttributes) &&
			resolvedAttributes.equals(r.resolvedAttributes);
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

		public Result build(){
			return new Result(this);
		}
	}
}
