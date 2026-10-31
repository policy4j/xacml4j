package org.xacml4j.v30.policy;

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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.PolicyElement;
import org.xacml4j.v30.XPathVersion;
import org.xacml4j.v30.policy.combine.impl.DenyOverridesPolicyCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.impl.DenyOverridesPolicyOrderedCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.impl.DenyUnlessPermitPolicyCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.impl.FirstApplicablePolicyCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.impl.LegacyDenyOverridesPolicyCombineAlgorithm;
import org.xacml4j.v30.policy.combine.impl.LegacyOrderedDenyOverridesPolicyCombineAlgorithm;
import org.xacml4j.v30.policy.combine.impl.LegacyOrderedPermitOverridesPolicyCombineAlgorithm;
import org.xacml4j.v30.policy.combine.impl.LegacyPermitOverridesPolicyCombineAlgorithm;
import org.xacml4j.v30.policy.combine.impl.OnPermitApplySecondPolicyCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.impl.OnlyOneApplicablePolicyCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.impl.PermitOverridesPolicyCombineAlgorithm;
import org.xacml4j.v30.policy.combine.impl.PermitOverridesPolicyOrderedCombineAlgorithm;
import org.xacml4j.v30.policy.combine.impl.PermitUnlessDenyPolicyCombiningAlgorithm;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class PolicySet extends
	BaseCompositeDecisionRule implements PolicyElement
{
	private final PolicySetDefaults policySetDefaults;
	private final DecisionCombiningAlgorithm<CompositeDecisionRule> combiningAlgorithm;
	private final List<CompositeDecisionRule> decisionRules;
	private final boolean canRespond;
	/** Whether the combining algorithm is one of the engine's own classes, which stage in the root (review R-AN) */
	private final boolean builtInAlgorithm;

	private final Map<String, Multimap<String, CombinerParameter>> policyCombinerParameters;
	private	final Map<String, Multimap<String, CombinerParameter>> policySetCombinerParameters;

	private final PolicySetIDReference reference;
	/**
	 * The hash code once computed; every field that it reads is fixed when the policy set is built (cycle AR).
	 * Hash inputs must remain immutable, including values supplied by trusted extensions.
	 */
	private int hashCode;

	private PolicySet(Builder b)
	{
		super(b);
		this.reference = PolicySetIDReference.builder(id).version(b.version).build();
		this.policySetDefaults = b.policyDefaults;
		Preconditions.checkNotNull(b.combiningAlgorithm,
				"Policy decision combining algorithm must be specified");
		this.combiningAlgorithm = b.combiningAlgorithm;
		this.decisionRules = b.policies.build();
		this.builtInAlgorithm = Prepared.ALGORITHMS.contains(combiningAlgorithm.getClass());
		this.canRespond = hasResponseExpressions() || !builtInAlgorithm
				|| decisionRules.stream().anyMatch(PolicySet::canRespondToParent);
		ImmutableMap.Builder<String, Multimap<String, CombinerParameter>> forPolicySets = ImmutableMap.builder();
		ImmutableMap.Builder<String, Multimap<String, CombinerParameter>> forPolicies = ImmutableMap.builder();
		for(Entry<String, Multimap<String, CombinerParameter>> e : b.policySetCombinerParams.entrySet()){
			forPolicySets.put(e.getKey(), ImmutableListMultimap.copyOf(e.getValue()));
		}
		for(Entry<String, Multimap<String, CombinerParameter>> e : b.policyCombinerParams.entrySet()){
			forPolicies.put(e.getKey(), ImmutableListMultimap.copyOf(e.getValue()));
		}
		this.policySetCombinerParameters = forPolicySets.build();
		this.policyCombinerParameters = forPolicies.build();

	}

	public static Builder builder(String id){
		return new Builder(id);
	}

	@Override
	public CompositeDecisionRuleIDReference getReference() {
		return reference;
	}

	/**
	 * Gets all combiner parameters for a given policy identifier
	 *
	 * @param policyId a policy identifier
	 * @return a collection of combiner parameters
	 */
	public Collection<CombinerParameter> getPolicyCombinerParams(String policyId){
		Multimap<String, CombinerParameter> p = policyCombinerParameters.get(policyId);
		return  (p == null)?ImmutableList.<CombinerParameter>of():p.values();
	}

	/**
	 * Gets policy combiner parameter with a given name
	 *
	 * @param policyId a policy identifier
	 * @param name a parameter name
	 * @return a collection of combiner parameters
	 */
	public Collection<CombinerParameter> getPolicyCombinerParam(String policyId, String name){
		Multimap<String, CombinerParameter> p = policyCombinerParameters.get(policyId);
		return  (p == null)?ImmutableList.<CombinerParameter>of():p.get(name);
	}

	/**
	 * Gets all combiner parameters for a given policy set identifier
	 *
	 * @param policySetId a policy set identifier
	 * @return a collection of combiner parameters
	 */
	public Collection<CombinerParameter> getPolicySetCombinerParams(String policySetId){
		Multimap<String, CombinerParameter> p = policySetCombinerParameters.get(policySetId);
		return  (p == null)?ImmutableList.<CombinerParameter>of():p.values();
	}

	/**
	 * Gets policy set combiner parameter with a given name
	 *
	 * @param policySetId a policy set identifier
	 * @param name a parameter name
	 * @return a collection of combiner parameters
	 */
	public Collection<CombinerParameter> getPolicySetCombinerParam(String policySetId, String name){
		Multimap<String, CombinerParameter> p = policySetCombinerParameters.get(policySetId);
		return  (p == null)?ImmutableList.<CombinerParameter>of():p.get(name);
	}

	/**
	 * Gets policy set decision combining algorithm
	 *
	 * @return a decision combining algorithm
	 */
	public DecisionCombiningAlgorithm<CompositeDecisionRule> getPolicyDecisionCombiningAlgorithm(){
		return combiningAlgorithm;
	}

	/**
	 * Gets policy set defaults
	 *
	 * @return {@link PolicySetDefaults}
	 */
	public PolicySetDefaults getDefaults() {
		return policySetDefaults;
	}

	/**
	 * Creates {@link EvaluationContext} to evaluate this policy
	 * set to be used in {@link PolicySet#isMatch(EvaluationContext)}
	 * or {@link PolicySet#evaluate(EvaluationContext)}
	 *
	 * @param context a parent evaluation context
	 * @return {@code XacmlEvaluationContextParam} defaultProvider to evaluate
	 * this policy set
	 */
	@Override
	public EvaluationContext createContext(EvaluationContext context) {
		Preconditions.checkNotNull(context);
		if(context.getCurrentPolicySet() == this){
			return context;
		}
		// A foreign context can change its answer between the factory and constructor checks.
		// Every own set context answers getCurrentPolicySet() from its final outer reference.
		return new PolicySetDescendantEvaluationContext(context, context instanceof PolicySetDescendantEvaluationContext);
	}

	@Override
	protected final boolean isEvaluationContextValid(EvaluationContext context){
		return this.equals(context.getCurrentPolicySet());
	}

	protected Decision combineDecisions(EvaluationContext context){
		List<CompositeDecisionRule> children = Prepared.children(context, this);
		if(children != decisionRules) RootEvaluationContext.observationOf(context).childrenSelected(context, this, children);
		return combiningAlgorithm.combine(context, children);
	}

	@Override
	boolean canRespond(){
		return canRespond;
	}

	@Override
	boolean hasBuiltInCombiningAlgorithm(){
		return builtInAlgorithm;
	}

	/** A child of another implementation, or a reference, which resolves later, counts as able to respond */
	private static boolean canRespondToParent(CompositeDecisionRule child){
		if(child instanceof Policy policy) return policy.canRespondToParent();
		if(child instanceof PolicySet policySet) return policySet.canRespondToParent();
		return true;
	}

	private static final class Prepared
	{
		private static final Set<Class<?>> ALGORITHMS = Set.of(
				DenyOverridesPolicyCombiningAlgorithm.class, DenyOverridesPolicyOrderedCombiningAlgorithm.class,
				PermitOverridesPolicyCombineAlgorithm.class, PermitOverridesPolicyOrderedCombineAlgorithm.class,
				DenyUnlessPermitPolicyCombiningAlgorithm.class, PermitUnlessDenyPolicyCombiningAlgorithm.class,
				FirstApplicablePolicyCombiningAlgorithm.class, OnlyOneApplicablePolicyCombiningAlgorithm.class,
				OnPermitApplySecondPolicyCombiningAlgorithm.class, LegacyDenyOverridesPolicyCombineAlgorithm.class,
				LegacyOrderedDenyOverridesPolicyCombineAlgorithm.class, LegacyPermitOverridesPolicyCombineAlgorithm.class,
				LegacyOrderedPermitOverridesPolicyCombineAlgorithm.class);

		private static List<CompositeDecisionRule> children(EvaluationContext context, PolicySet set){
			if(!set.builtInAlgorithm) return set.decisionRules;
			EvaluationContext top = DescendantEvaluationContext.topOf(context);
			if(!(top instanceof RootEvaluationContext decision) || decision.generation().isEmpty()) return set.decisionRules;
			Generation generation = decision.generation().orElseThrow();
			PreparedList existing = generation.sets.get(set);
			boolean indexable = generation.indexes.isPresent() && set.decisionRules.size() > 1
					&& decision.hasRequestAttributeResolution()
					&& set.combiningAlgorithm.getClass() != OnPermitApplySecondPolicyCombiningAlgorithm.class;
			if(existing == null){
				ArrayList<CompositeDecisionRule> children = new ArrayList<>(set.decisionRules.size());
				TargetIndex.Builder<CompositeDecisionRule> index = indexable && !context.isValidateFuncParamsAtRuntime()
						? new TargetIndex.Builder<>(context) : null;
				for(CompositeDecisionRule child : set.decisionRules){
					CompositeDecisionRule preparedChild = child;
					if(child instanceof PolicyIDReference || child instanceof PolicySetIDReference){
						CompositeDecisionRuleIDReference ref = (CompositeDecisionRuleIDReference)child;
						preparedChild = decision.outcome(ref).<CompositeDecisionRule>map(target -> new PinnedReference(ref, target))
								.orElse(child);
					}
					children.add(preparedChild);
					if(index != null) index.add(preparedChild);
				}
				PreparedList prepared = new PreparedList(children);
				PreparedList winner = generation.sets.putIfAbsent(set, prepared);
				existing = winner == null ? prepared : winner;
				if(winner == null && index != null){
					generation.indexes.orElseThrow().sets.putIfAbsent(set, index.build(prepared.children()));
				}
			}
			if(!indexable || decision.hasMisses() || context.isValidateFuncParamsAtRuntime()) return existing.children();
			ConcurrentMap<PolicySet, TargetIndex<CompositeDecisionRule>> indexes = generation.indexes.orElseThrow().sets;
			TargetIndex<CompositeDecisionRule> index = indexes.get(set);
			if(index == null){
				TargetIndex<CompositeDecisionRule> built = TargetIndex.create(context, existing.children());
				TargetIndex<CompositeDecisionRule> winner = indexes.putIfAbsent(set, built);
				index = winner == null ? built : winner;
			}
			return index.candidates(context);
		}
	}

	public List<? extends CompositeDecisionRule> getDecisions() {
		return decisionRules;
	}

	public boolean contains(CompositeDecisionRule r){
		return decisionRules.contains(r);
	}

	public void accept(PolicyTreeVisitor v) {
		v.visitEnter(this);
		if(getTarget() != null){
			getTarget().accept(v);
		}
		if(policySetDefaults != null){
			policySetDefaults.accept(v);
		}
		for(DecisionRule decision : decisionRules){
			decision.accept(v);
		}
		for(ObligationExpression obligation : obligationExpressions){
			obligation.accept(v);
		}
		for(AdviceExpression advice : adviceExpressions){
			advice.accept(v);
		}
		v.visitLeave(this);
	}

	protected boolean equalsTo(PolicySet o) {
		return super.equalsTo(o)
			&& Objects.equal(policySetDefaults, o.policySetDefaults)
			&& Objects.equal(combiningAlgorithm, o.combiningAlgorithm)
			&& Objects.equal(decisionRules, o.decisionRules)
			&& Objects.equal(policyCombinerParameters, o.policyCombinerParameters)
			&& Objects.equal(policySetCombinerParameters, o.policySetCombinerParameters)
			&& Objects.equal(reference, o.reference);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		return (o instanceof PolicySet)
			&& ((PolicySet)o).equalsTo(this);
	}

	@Override
	public int hashCode() {
		int h = hashCode;
		if(h == 0){
			// Arrays.hashCode of the fields in this order, without the array (design D22)
			int fields = 1;
			fields = 31 * fields + java.util.Objects.hashCode(policySetDefaults);
			fields = 31 * fields + java.util.Objects.hashCode(combiningAlgorithm);
			fields = 31 * fields + java.util.Objects.hashCode(decisionRules);
			fields = 31 * fields + java.util.Objects.hashCode(policyCombinerParameters);
			fields = 31 * fields + java.util.Objects.hashCode(policySetCombinerParameters);
			fields = 31 * fields + java.util.Objects.hashCode(reference);
			h = super.hashCode() * 31 + fields;
			hashCode = h;
		}
		return h;
	}

	class PolicySetDescendantEvaluationContext
			extends CompositeRuleEvaluationContext {

		/**
		 * Constructs delegating evaluation context
		 * which delegates all invocations to the enclosing
		 * policy set or root context to evaluate given
		 * policy set
		 *
		 * @param parentContext a parent context
		 */
		PolicySetDescendantEvaluationContext(
				EvaluationContext parentContext){
			this(parentContext, false);
		}

		private PolicySetDescendantEvaluationContext(
				EvaluationContext parentContext, boolean policySetChecked){
			super(parentContext);
			if(!policySetChecked){
				Preconditions.checkArgument(
						parentContext.getCurrentPolicySet() != PolicySet.this);
			}
			// Tests run with assertions: wherever the proof skips the walk, the walk agrees
			assert !answersNoPolicy(parentContext) || parentContext.getCurrentPolicy() == null;
			Preconditions.checkArgument(
					answersNoPolicy(parentContext) || parentContext.getCurrentPolicy() == null);
		}

		/**
		 * Tells whether a parent is known to answer no current policy, so that it is not asked (cycle AZ):
		 * the root of the engine, a final class that answers none, or a set context of the engine whose top
		 * is that root. Such a set context was made after this check, or this proof, said that its own
		 * parent answers none, and its chain never changes: the contexts of this package keep their parent
		 * and top in final fields, and answer the current policy only by the walk of
		 * {@link DescendantEvaluationContext#getCurrentPolicy()}, which finds no policy context on that
		 * chain and asks the root last. Any other parent is asked: a context of another implementation or
		 * with one at its top, a mock, and every other context of this package.
		 */
		private static boolean answersNoPolicy(EvaluationContext parent){
			return parent instanceof RootEvaluationContext
					|| parent instanceof PolicySetDescendantEvaluationContext
							&& DescendantEvaluationContext.topOf(parent) instanceof RootEvaluationContext;
		}

		@Override
		public Collection<CompositeDecisionRuleIDReference> getReferencedCompositeDecisionRules() {
			return super.getReferencedCompositeDecisionRules();
		}

		@Override
		public EvaluationContext createExtIndeterminateEvalContext() {
			return new PolicySetPassEvaluationContext(this);
		}

		/**
		 * The policy set of this context, which answers {@link #getCurrentPolicySet()} (design D17)
		 */
		final PolicySet policySet() {
			return PolicySet.this;
		}

		@Override
		public XPathVersion getXPathVersion() {
			PolicySetDefaults defaults = PolicySet.this.getDefaults();
			if(defaults != null &&
					defaults.getXPathVersion() != null){
				return defaults.getXPathVersion();
			}
			return super.getXPathVersion();
		}
	}

	/** The context of the extended Indeterminate pass of a policy set, which stages on its own */
	private static final class PolicySetPassEvaluationContext extends CompositeRuleEvaluationContext
	{
		PolicySetPassEvaluationContext(PolicySetDescendantEvaluationContext parentContext){
			super(parentContext);
		}

		@Override
		public EvaluationContext createExtIndeterminateEvalContext() {
			return this;
		}

		@Override
		public boolean isExtendedIndeterminateEval() {
			return true;
		}
	}

	public final static class Builder extends BaseCompositeDecisionRule.Builder<Builder>
	{
		private DecisionCombiningAlgorithm<CompositeDecisionRule> combiningAlgorithm;
		private PolicySetDefaults policyDefaults;
		private ImmutableList.Builder<CompositeDecisionRule> policies = ImmutableList.builder();

		private Map<String, Multimap<String, CombinerParameter>> policyCombinerParams = Maps.newLinkedHashMap();
		private Map<String, Multimap<String, CombinerParameter>> policySetCombinerParams = Maps.newLinkedHashMap();

		private Builder(String policySetId){
			super(policySetId);
		}


		public Builder withPolicyCombinerParameter(String policyId, CombinerParameter p){
			Preconditions.checkNotNull(policyId);
			Preconditions.checkNotNull(p);
			Multimap<String, CombinerParameter> params = policyCombinerParams.get(policyId);
			if(params == null){
				params = LinkedHashMultimap.create();
				policyCombinerParams.put(policyId, params);
			}
			params.put(p.getName(), p);
			return this;
		}

		public Builder withPolicySetCombinerParameter(String policySetId, CombinerParameter p){
			Preconditions.checkNotNull(policySetId);
			Preconditions.checkNotNull(p);
			Multimap<String, CombinerParameter> params = policySetCombinerParams.get(policySetId);
			if(params == null){
				params = LinkedHashMultimap.create();
				policySetCombinerParams.put(policySetId, params);
			}
			params.put(p.getName(), p);
			return this;
		}

		public Builder withoutPolicyCombinerParameters(){
			this.policyCombinerParams.clear();
			return this;
		}

		public Builder withoutPolicySetCombinerParameters(){
			this.policySetCombinerParams.clear();
			return this;
		}

		public Builder defaults(PolicySetDefaults defaults){
			this.policyDefaults = defaults;
			return this;
		}

		public Builder withoutDefaults(){
			this.policyDefaults = null;
			return this;
		}

		public Builder policy(Policy p){
			Preconditions.checkNotNull(p);
			this.policies.add(p);
			return this;
		}

		public Builder policySet(PolicySet p){
			Preconditions.checkNotNull(p);
			this.policies.add(p);
			return this;
		}

		public Builder compositeDecisionRules(Iterable<CompositeDecisionRule> rules)
		{
			this.policies.addAll(rules);
			return getThis();
		}

		public Builder compositeDecisionRules(CompositeDecisionRule ... rules)
		{
			this.policies.add(rules);
			return getThis();
		}

		public Builder withPolicy(Policy.Builder b){
			Preconditions.checkNotNull(b);
			this.policies.add(b.build());
			return this;
		}

		public Builder withPolicy(Builder b){
			Preconditions.checkNotNull(b);
			this.policies.add(b.build());
			return this;
		}

		public Builder withoutRules(){
			this.policies = ImmutableList.builder();
			return this;
		}

		public Builder withCombiningAlgorithm(DecisionCombiningAlgorithm<CompositeDecisionRule> alg)
		{
			Preconditions.checkNotNull(alg);
			this.combiningAlgorithm = alg;
			return this;
		}

		@Override
		protected Builder getThis() {
			return this;
		}

		public PolicySet build(){
			return new PolicySet(this);
		}
	}
}
