package org.xacml4j.v30;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.xacml4j.v30.types.Entity;

public final class Result
{
	private final static Logger LOG  = LoggerFactory.getLogger(Result.class);

	/** The status of the results of {@link #ok(Decision)}, immutable and shared (design D22) */
	private final static Status OK = Status.ok().build();

	private final Status status;
	private final Decision decision;
	/**
	 * One per id, in the order of their first id; equal and hashed as the maps by id that held them
	 * (cycle AU)
	 */
	private final ImmutableList<Obligation> obligations;
	private final ImmutableList<Advice> associatedAdvice;
	/** The request's categories to include, in their order, one per category id (XACML 3.0 §5.48, cycle AM) */
	private final ImmutableList<Category> includeInResultAttributes;
	private final ImmutableList<CompositeDecisionRuleIDReference> policyReferences;
	private final ImmutableMap<CategoryId, Category> resolvedAttributes;
	/** Computed on the first use, as {@link Status#hashCode()} is (cycle AM) */
	private int hashCode;

	private Result(Builder b){
		this(b.decision, b.status, ImmutableList.copyOf(b.obligations.values()),
				ImmutableList.copyOf(b.associatedAdvice.values()),
				// shared and empty when nothing was added (design D32)
				includedCategories(b.includeInResultAttributes),
				b.policyReferences == null ? ImmutableList.of() : b.policyReferences.build(),
				b.resolvedAttributes == null ? ImmutableMap.of() : b.resolvedAttributes.build());
	}

	private Result(Decision decision, Status status,
			ImmutableList<Obligation> obligations, ImmutableList<Advice> associatedAdvice,
			ImmutableList<Category> includeInResultAttributes,
			ImmutableList<CompositeDecisionRuleIDReference> policyReferences,
			ImmutableMap<CategoryId, Category> resolvedAttributes){
		Preconditions.checkArgument(decision != null, "Decision must be specified");
		Preconditions.checkArgument(status != null, "Status must be specified");
		this.decision = decision;
		this.status = status;
		this.obligations = obligations;
		this.associatedAdvice = associatedAdvice;
		this.includeInResultAttributes = includeInResultAttributes;
		this.policyReferences = policyReferences;
		this.resolvedAttributes = resolvedAttributes;
	}

	/**
	 * Creates the result of a Permit, Deny or NotApplicable decision with the OK status, without a
	 * builder, as the decision point does (cycle AU). The obligations and the advice keep their
	 * order, and each has one response per id.
	 *
	 * @param decision a decision that is not Indeterminate
	 * @param obligations the obligations, one per id
	 * @param advice the advice, one per id
	 * @param includeInResult the request's categories to include, one per category id
	 * @param resolvedAttributes the categories of the attributes resolved for the decision, one per category id
	 * @param policyIdentifiers the policy identifier list
	 * @return the result
	 * @exception IllegalArgumentException if the decision is Indeterminate, or an id repeats
	 */
	public static Result of(Decision decision,
			ImmutableList<Obligation> obligations, ImmutableList<Advice> advice,
			Collection<Category> includeInResult, Collection<Category> resolvedAttributes,
			ImmutableList<CompositeDecisionRuleIDReference> policyIdentifiers){
		Preconditions.checkArgument(!decision.isIndeterminate());
		return new Result(decision, OK, distinctIds(obligations), distinctIds(advice),
				includedCategories(includeInResult), Preconditions.checkNotNull(policyIdentifiers),
				byCategory(resolvedAttributes));
	}

	/**
	 * Creates a result with the given status and no responses, without a builder, as the decision
	 * point does for an Indeterminate decision (cycle AU)
	 *
	 * @param decision a decision
	 * @param status the status of the result
	 * @param includeInResult the request's categories to include, one per category id
	 * @param resolvedAttributes the categories of the attributes resolved for the decision, one per category id
	 * @return the result
	 */
	public static Result of(Decision decision, Status status,
			Collection<Category> includeInResult, Collection<Category> resolvedAttributes){
		return new Result(decision, status, ImmutableList.of(), ImmutableList.of(),
				includedCategories(includeInResult), ImmutableList.of(), byCategory(resolvedAttributes));
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
	public static Result.Builder ok(Decision d){
		Preconditions.checkArgument(!d.isIndeterminate());
		return new Result.Builder().decision(d).status(OK);
	}

	public static Result.Builder indeterminate(Status status){
		Preconditions.checkArgument(!status.isSuccess());
		return new Result.Builder().decision(Decision.INDETERMINATE).status(status);
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
	 *
	 * @return a collection of {@link Category} instances
	 */
	public Collection<Category> getIncludeInResultAttributes(){
		return includeInResultAttributes;
	}

	/**
	 * Gets all resolved by PIP attributes from external sources during request context evaluation
	 *
	 * @return a collection of resolved attributes
	 */
	public Collection<Category> getResolvedAttributes(){
		return resolvedAttributes.values();
	}

	public Category getIncludeInResultByCategory(CategoryId categoryId){
		for(int i = 0; i < includeInResultAttributes.size(); i++){
			Category c = includeInResultAttributes.get(i);
			if(c.getCategoryId().equals(categoryId)){
				return c;
			}
		}
		return null;
	}

	public Optional<Entity> getEntity(CategoryId categoryId){
		return Optional.ofNullable(getIncludeInResultByCategory(categoryId))
		               .map(v->v.getEntity());
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
		return obligations;
	}

	/**
	 * Gets obligation via obligation identifier
	 *
	 * @param obligationId an obligation identifier
	 * @return {@link Obligation}, or {@code null} if this result has none with the identifier
	 */
	public Obligation getObligation(String obligationId){
		return withId(obligations, obligationId);
	}

	/**
	 * Returns a collection of advice that provide supplemental
	 * information to the PEP. If the PEP does not understand
	 * an advice, the PEP may safely ignore the advice.
	 *
	 * @return a collection of associated advice
	 */
	public Collection<Advice> getAssociatedAdvice(){
		return associatedAdvice;
	}

	/**
	 * Gets associated advice via advice identifier
	 *
	 * @param adviceId an advice identifier
	 * @return {@link Advice}, or {@code null} if this result has none with the identifier
	 */
	public Advice getAssociatedAdvice(String adviceId){
		return withId(associatedAdvice, adviceId);
	}

	/** The response with an id: a scan, as the lists are short (cycle AU) */
	private static <T extends DecisionRuleResponse> T withId(ImmutableList<T> responses, String id){
		for(int i = 0; i < responses.size(); i++){
			T r = responses.get(i);
			if(r.getId().equals(id)){
				return r;
			}
		}
		return null;
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
		return MoreObjects
				.toStringHelper(this)
				.add("status", status)
				.add("decision", decision)
				.add("obligations", obligations)
				.add("associatedAdvice", associatedAdvice)
				.add("policyIdReferences", policyReferences)
				.add("includeInResultAttrs", includeInResultAttributes)
				.add("resolvedAttrs", resolvedAttributes.values())
				.toString();
	}

	@Override
	public int hashCode(){
		int h = hashCode;
		if(h == 0){
			// Arrays.hashCode of the fields in this order, without the array (design D22); the
			// included categories hash as the map by category id that held them (cycle AM), the
			// responses as the maps by id (cycle AU)
			h = 1;
			h = 31 * h + Objects.hashCode(decision);
			h = 31 * h + Objects.hashCode(status);
			h = 31 * h + hashById(associatedAdvice);
			h = 31 * h + hashById(obligations);
			h = 31 * h + hashByCategory(includeInResultAttributes);
			h = 31 * h + Objects.hashCode(policyReferences);
			h = 31 * h + Objects.hashCode(resolvedAttributes);
			hashCode = h;
		}
		return h;
	}

	/** The hash of a map from category id to category: the sum of its entries' hashes */
	private static int hashByCategory(ImmutableList<Category> categories){
		int h = 0;
		for(int i = 0; i < categories.size(); i++){
			Category c = categories.get(i);
			h += c.getCategoryId().hashCode() ^ c.hashCode();
		}
		return h;
	}

	/** The hash of a map from id to response: the sum of its entries' hashes */
	private static int hashById(ImmutableList<? extends DecisionRuleResponse> responses){
		int h = 0;
		for(int i = 0; i < responses.size(); i++){
			DecisionRuleResponse r = responses.get(i);
			h += r.getId().hashCode() ^ r.hashCode();
		}
		return h;
	}

	@Override
	public boolean equals(Object o){
		if (o == this) {
			return true;
		}
		if (!(o instanceof Result)) {
			return false;
		}
		Result r = (Result)o;
		return decision.equals(r.decision) &&
			status.equals(r.status) &&
			sameById(obligations, r.obligations) &&
			sameById(associatedAdvice, r.associatedAdvice) &&
			policyReferences.equals(r.policyReferences) &&
			includesSameCategories(r) &&
			resolvedAttributes.equals(r.resolvedAttributes);
	}

	/** Equal as maps by id: the same responses, in any order (cycle AU) */
	private static <T extends DecisionRuleResponse> boolean sameById(ImmutableList<T> responses, ImmutableList<T> other){
		if(responses.size() != other.size()){
			return false;
		}
		for(int i = 0; i < responses.size(); i++){
			T r = responses.get(i);
			if(!r.equals(withId(other, r.getId()))){
				return false;
			}
		}
		return true;
	}

	/** The categories to include, a category id at most once, as the map that held them required */
	private static ImmutableList<Category> includedCategories(Collection<Category> included){
		ImmutableList<Category> categories = ImmutableList.copyOf(included);
		for(int i = 1; i < categories.size(); i++){
			CategoryId id = categories.get(i).getCategoryId();
			for(int j = 0; j < i; j++){
				Preconditions.checkArgument(!categories.get(j).getCategoryId().equals(id),
						"Multiple entries with same key: %s", id);
			}
		}
		return categories;
	}

	/** The responses, an id at most once, as the map that held them required (cycle AU) */
	private static <T extends DecisionRuleResponse> ImmutableList<T> distinctIds(ImmutableList<T> responses){
		for(int i = 1; i < responses.size(); i++){
			String id = responses.get(i).getId();
			for(int j = 0; j < i; j++){
				Preconditions.checkArgument(!responses.get(j).getId().equals(id),
						"Multiple entries with same key: %s", id);
			}
		}
		return responses;
	}

	/** The categories by category id, the shared empty map when there are none */
	private static ImmutableMap<CategoryId, Category> byCategory(Collection<Category> categories){
		if(categories.isEmpty()){
			return ImmutableMap.of();
		}
		ImmutableMap.Builder<CategoryId, Category> b = ImmutableMap.builderWithExpectedSize(categories.size());
		for(Category c : categories){
			b.put(c.getCategoryId(), c);
		}
		return b.buildOrThrow();
	}

	/** Equal as maps by category id: the same categories, in any order (cycle AM) */
	private boolean includesSameCategories(Result r){
		if(includeInResultAttributes.size() != r.includeInResultAttributes.size()){
			return false;
		}
		for(int i = 0; i < includeInResultAttributes.size(); i++){
			Category c = includeInResultAttributes.get(i);
			if(!c.equals(r.getIncludeInResultByCategory(c.getCategoryId()))){
				return false;
			}
		}
		return true;
	}



	public static class Builder
	{
		/** At most this many responses given together have their ids compared pairwise; more take the merging path */
		private static final int DISTINCT_CHECKED = 8;

		private Status status;
		private Decision decision;
		// shared and empty until the first element, then in the order added (design D22); a map
		// given whole is kept immutable until a later element changes it (cycle AM)
		private Map<String, Obligation> obligations = ImmutableMap.of();
		private Map<String, Advice> associatedAdvice = ImmutableMap.of();
		// shared and empty until the first category; the first list given is kept as it is, and a
		// list to add to is created by any other add (design D32, cycle AM)
		private List<Category> includeInResultAttributes = ImmutableList.of();
		// created by the first add (design D32)
		private ImmutableList.Builder<CompositeDecisionRuleIDReference> policyReferences;
		private ImmutableMap.Builder<CategoryId, Category> resolvedAttributes;

		private List<Category> includeInResultAttributes(){
			if(!(includeInResultAttributes instanceof ArrayList)){
				includeInResultAttributes = new ArrayList<>(includeInResultAttributes);
			}
			return includeInResultAttributes;
		}

		private ImmutableList.Builder<CompositeDecisionRuleIDReference> policyReferences(){
			if(policyReferences == null){
				policyReferences = ImmutableList.builder();
			}
			return policyReferences;
		}

		private ImmutableMap.Builder<CategoryId, Category> resolvedAttributes(){
			if(resolvedAttributes == null){
				resolvedAttributes = ImmutableMap.builder();
			}
			return resolvedAttributes;
		}

		public Builder includeInResult(Category a){
			Preconditions.checkNotNull(a);
			includeInResultAttributes().add(a);
			return this;
		}

		/** Keeps an immutable list given first as it is, as the request's own list is (cycle AM) */
		public Builder includeInResultAttr(Iterable<Category> attributes){
			if(includeInResultAttributes.isEmpty() && attributes instanceof ImmutableList<Category> list){
				includeInResultAttributes = list;
				return this;
			}
			List<Category> included = includeInResultAttributes();
			for(Category a : attributes){
				included.add(Preconditions.checkNotNull(a));
			}
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

		public Builder resolvedAttr(Iterable<Category> attributes){
			for(Category a : attributes){
				resolvedAttributes().put(a.getCategoryId(), a);
			}
			return this;
		}

		public Builder referencedPolicy(CompositeDecisionRuleIDReference ... refs)
		{
			Preconditions.checkNotNull(refs);
			for(CompositeDecisionRuleIDReference ref : refs){
				Preconditions.checkNotNull(ref);
				policyReferences().add(ref);
			}
			return this;
		}

		public Builder evaluatedPolicies(Iterable<? extends CompositeDecisionRuleIDReference> refs)
		{
			Preconditions.checkNotNull(refs);
			for(CompositeDecisionRuleIDReference ref : refs){
				Preconditions.checkNotNull(ref);
				policyReferences().add(ref);
			}
			return this;
		}

		public Builder advice(Iterable<Advice> advice){
			ImmutableMap<String, Advice> distinct = associatedAdvice.isEmpty() ? distinctById(advice) : ImmutableMap.of();
			if(!distinct.isEmpty()){
				associatedAdvice = distinct;
				return this;
			}
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
			ImmutableMap<String, Obligation> distinct = this.obligations.isEmpty() ? distinctById(obligations) : ImmutableMap.of();
			if(!distinct.isEmpty()){
				this.obligations = distinct;
				return this;
			}
			for(Obligation o : obligations){
				addObligation(o);
			}
			return this;
		}

		/**
		 * The responses as one immutable map in their order when no id repeats, as in the matching
		 * responses of a decision, which its root context keeps one per id; otherwise the empty map,
		 * and the responses take the merging path (cycle AM)
		 */
		@SuppressWarnings("unchecked")
		private static <T extends DecisionRuleResponse> ImmutableMap<String, T> distinctById(Iterable<T> responses){
			if(!(responses instanceof Collection<T> c) || c.isEmpty() || c.size() > DISTINCT_CHECKED){
				return ImmutableMap.of();
			}
			if(c.size() == 1){
				T only = c.iterator().next();
				return ImmutableMap.of(only.getId(), only);
			}
			Object[] array = c.toArray();
			for(int i = 1; i < array.length; i++){
				String id = ((T)array[i]).getId();
				for(int j = 0; j < i; j++){
					if(((T)array[j]).getId().equals(id)){
						return ImmutableMap.of();
					}
				}
			}
			ImmutableMap.Builder<String, T> distinct = ImmutableMap.builderWithExpectedSize(array.length);
			for(Object response : array){
				distinct.put(((T)response).getId(), (T)response);
			}
			return distinct.buildOrThrow();
		}

		/** A map to change: a copy of an immutable one, which a built result may share (cycle AM) */
		private static <T> Map<String, T> modifiable(Map<String, T> responses){
			return responses instanceof LinkedHashMap ? responses : new LinkedHashMap<>(responses);
		}

		private Builder addAdvice(Advice advice){
			Preconditions.checkNotNull(advice);
			Advice a = associatedAdvice.get(advice.getId());
			associatedAdvice = modifiable(associatedAdvice);
			if(a != null){
				a = a.merge(advice);
				associatedAdvice.replace(a.getId(), a);
				return this;
			}
			associatedAdvice.put(advice.getId(), advice);
			return this;
		}

		private Builder addObligation(Obligation obligation){
			Obligation o = obligations.get(obligation.getId());
			obligations = modifiable(obligations);
			if(o != null){
				LOG.debug("Merging obligations a={} b={}", o, obligation);
				o = o.merge(obligation);
				obligations.replace(o.getId(), o);
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
