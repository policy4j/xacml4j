package org.xacml4j.v30;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class Result
{
	private Status status;
	private Decision decision;
	private Map<String, Obligation> obligations;
	private Map<String, Advice> associatedAdvice;
	private Map<CategoryId, Category> includeInResultAttributes;
	private Collection<CompositeDecisionRuleIDReference> policyReferences;
	private Map<CategoryId, Category> resolvedAttributes;
	private int hashCode;

	private Result(Builder b){
		Preconditions.checkArgument(b.decision !=null, "Decision must be specified");
		Preconditions.checkArgument(b.status !=null, "Status must be specified");
		Preconditions.checkArgument(!(b.decision.isIndeterminate() ^
				b.status.isFailure()));
		this.decision = b.decision;
		this.status = b.status;
		this.obligations = ImmutableMap.copyOf(b.obligations);
		this.associatedAdvice = ImmutableMap.copyOf(b.associatedAdvice);
		this.includeInResultAttributes = b.includeInResultAttributes.build();
		this.policyReferences = b.policyReferences.build();
		this.resolvedAttributes = b.resolvedAttributes.build();
		this.hashCode = Objects.hashCode(decision, status,
				associatedAdvice,
				obligations,
				includeInResultAttributes,
				policyReferences,
				resolvedAttributes);
	}

	public static Result.Builder builder(){
		return new Result.Builder();
	}

	public static Result.Builder builder(Decision d, Status status){
		return new Result.Builder().decision(d).status(status);
	}

	/**
	 * Creates {@link Result.Builder} with given decision
	 * @param d decision
	 * @return decision builder
	 */
	public static Result.Builder createOk(Decision d){
		Preconditions.checkArgument(!d.isIndeterminate());
		return new Result.Builder().decision(d).status(Status.createSuccess());
	}

	public static Result.Builder createIndeterminate(Status status){
		Preconditions.checkArgument(status.isFailure());
		return new Result.Builder().decision(Decision.INDETERMINATE).status(status);
	}

	public static Result.Builder createIndeterminate(Decision d, StatusCode status){
		return new Result.Builder().decision(d).status(new Status(status));
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

	public boolean isIndeterminate(){
		return decision.isIndeterminate();
	}

	public boolean isPermit(){
		return decision == Decision.PERMIT;
	}

	public boolean isDeny(){
		return decision == Decision.DENY;
	}

	public boolean isNotApplicable(){
		return decision == Decision.NOT_APPLICABLE;
	}

	/**
	 * Gets a list of attributes that were part of the request.
	 * The choice of which attributes are included here is made
	 * with the request attributes {@link org.xacml4j.v30.Category#getIncludeInResultAttributes()}}
	 *
	 * @return a collection of {@link Category} instances
	 */
	public Collection<Category> getIncludeInResultAttributes(){
		return includeInResultAttributes.values();
	}

	/**
	 * Gets all resolved by PIP attributes from external
	 * sources during request context evaluation
	 *
	 * @return a collection of resolved attributes
	 */
	public Collection<Category> getResolvedAttributes(){
		return resolvedAttributes.values();
	}

	public Category getAttribute(CategoryId categoryId){
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
		return obligations.values();
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
		return associatedAdvice.values();
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
		return policyReferences;
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

	@Override
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
		private ImmutableMap.Builder<CategoryId, Category> includeInResultAttributes = ImmutableMap.builder();
		private ImmutableCollection.Builder<CompositeDecisionRuleIDReference> policyReferences = ImmutableList.builder();
		private ImmutableMap.Builder<CategoryId, Category> resolvedAttributes = ImmutableMap.builder();


		public Builder includeInResult(Category a){
			Preconditions.checkNotNull(a);
			Preconditions.checkState(includeInResultAttributes.put(a.getCategoryId(), a) != null);
			return this;
		}

		public Builder decision(Decision d){
			Preconditions.checkNotNull(d);
			this.decision = d;
			return this;
		}

		public Builder status(Status status){
			this.status = status;
			return this;
		}

		public Builder includeInResultAttr(Iterable<Category> attributes){
			for(Category a : attributes){
				includeInResultAttributes.put(a.getCategoryId(), a);
			}
			return this;
		}

		public Builder resolvedAttr(Iterable<Category> attributes){
			for(Category a : attributes){
				resolvedAttributes.put(a.getCategoryId(), a);
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

		public Builder evaluatedPolicies(Iterable<? extends CompositeDecisionRuleIDReference> refs)
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
				addAdvice(a);
			}
			return this;
		}

		public Builder advice(Advice ... advices){
			for(Advice a : advices){
				addAdvice(a);
			}
			return this;
		}

		public Builder obligation(Obligation ... obligations){
			for(Obligation o : obligations){
				addObligation(o);
			}
			return this;
		}

		public Builder obligation(Iterable<Obligation> obligations){
			for(Obligation o : obligations){
				addObligation(o);
			}
			return this;
		}

		private Builder addAdvice(Advice advice){
			Preconditions.checkNotNull(advice);
			Advice a = associatedAdvice.get(advice.getId());
			if(a != null){
				a = a.merge(advice);
				associatedAdvice.put(a.getId(), a);
				return this;
			}
			associatedAdvice.put(advice.getId(), advice);
			return this;
		}

		private  Builder addObligation(Obligation obligation){
			Preconditions.checkNotNull(obligation);
			Obligation o = obligations.get(obligation.getId());
			if(o != null){
				o = o.merge(obligation);
				obligations.put(o.getId(), o);
				return this;
			}
			obligations.put(obligation.getId(), obligation);
			return this;
		}

		public Result build(){
			return new Result(this);
		}
	}
}
