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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.PolicyElement;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.XPathVersion;
import org.xacml4j.v30.policy.combine.impl.DenyOverridesRuleCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.impl.DenyOverridesRuleOrderedCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.impl.DenyUnlessPermitRuleCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.impl.FirstApplicableRuleCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.impl.LegacyDenyOverridesRuleCombineAlgorithm;
import org.xacml4j.v30.policy.combine.impl.LegacyOrderedDenyOverridesRuleCombineAlgorithm;
import org.xacml4j.v30.policy.combine.impl.LegacyOrderedPermitOverridesRuleCombineAlgorithm;
import org.xacml4j.v30.policy.combine.impl.LegacyPermitOverridesRuleCombineAlgorithm;
import org.xacml4j.v30.policy.combine.impl.PermitOverridesRuleCombineAlgorithm;
import org.xacml4j.v30.policy.combine.impl.PermitOverridesRuleOrderedCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.impl.PermitUnlessDenyRuleCombiningAlgorithm;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class Policy extends BaseCompositeDecisionRule
		implements PolicyElement {
	private static final Set<Class<?>> BUILT_IN_ALGORITHMS = Set.of(
			DenyOverridesRuleCombiningAlgorithm.class, DenyOverridesRuleOrderedCombiningAlgorithm.class,
			PermitOverridesRuleCombineAlgorithm.class, PermitOverridesRuleOrderedCombiningAlgorithm.class,
			DenyUnlessPermitRuleCombiningAlgorithm.class, PermitUnlessDenyRuleCombiningAlgorithm.class,
			FirstApplicableRuleCombiningAlgorithm.class, LegacyDenyOverridesRuleCombineAlgorithm.class,
			LegacyOrderedDenyOverridesRuleCombineAlgorithm.class, LegacyPermitOverridesRuleCombineAlgorithm.class,
			LegacyOrderedPermitOverridesRuleCombineAlgorithm.class);
	private static final VarHandle PREPARED_CONDITIONS;

	static {
		try{
			PREPARED_CONDITIONS = MethodHandles.lookup().findVarHandle(Policy.class, "preparedConditions", PreparedConditions.class);
		}catch(ReflectiveOperationException e){
			throw new ExceptionInInitializerError(e);
		}
	}

	private final PolicyDefaults policyDefaults;
	private final Map<String, VariableDefinition> variableDefinitions;
	private final List<Rule> rules;
	private final DecisionCombiningAlgorithm<Rule> combiningAlgorithm;
	private final Map<String, Multimap<String, CombinerParameter>> ruleCombiningParameters;
	private final boolean canRespond;
	/** Whether the combining algorithm is one of the engine's own classes, which stage in the root (review R-AN) */
	private final boolean builtInAlgorithm;
	/** Whether the conditions of the rules may share values within a decision, by a cheap scan (design D45) */
	private final boolean mayShareConditions;
	/**
	 * The conditions of the rules prepared to share values, by the first decision that shares them: the
	 * policy keeps them, so that what they hold of the policy goes with it (design D45, review R-D45-sol)
	 */
	private PreparedConditions preparedConditions;
	/**
	 * The hash code once computed; every field that it reads is fixed when the policy is built (cycle AR).
	 * Hash inputs must remain immutable, including values supplied by trusted extensions.
	 */
	private int hashCode;

	/**
	 * A reference to itself
	 */
	private final PolicyIDReference reference;

	private Policy(Builder b){
		super(b);
		Preconditions.checkNotNull(b.combiningAlgorithm,
				"Rule decision combining algorithm must be specified");
		this.combiningAlgorithm = b.combiningAlgorithm;
		this.policyDefaults = b.policyDefaults;
		this.reference = PolicyIDReference.builder(b.id).version(b.version).build();
		this.rules = b.rules.build();
		this.builtInAlgorithm = BUILT_IN_ALGORITHMS.contains(combiningAlgorithm.getClass());
		this.canRespond = hasResponseExpressions() || !builtInAlgorithm
				|| rules.stream().anyMatch(Rule::mayWriteResponses);
		this.mayShareConditions = PreparedConditions.mayShare(rules);
		this.variableDefinitions = Maps.uniqueIndex(b.variables,
				new Function<VariableDefinition, String>(){
					@Override
					public String apply(VariableDefinition from) {
						return from.getVariableId();
					}});
		this.ruleCombiningParameters = Maps.newHashMap();
		for(Entry<String, Multimap<String, CombinerParameter>> p : b.ruleCombinerParameters.entrySet()){
			ruleCombiningParameters.put(p.getKey(), ImmutableListMultimap.copyOf(p.getValue()));
		}
	}

	public static Builder builder(String policyId){
		return new Builder(policyId);
	}

	/**
	 * Gets policy defaults
	 *
	 * @return policy defaults
	 */
	public PolicyDefaults getDefaults() {
		return policyDefaults;
	}

	public Collection<CombinerParameter> getRuleCombinerParams(String ruleId){
		Multimap<String, CombinerParameter> p = ruleCombiningParameters.get(ruleId);
		return  (p == null)?ImmutableList.<CombinerParameter>of():p.values();
	}

	public Collection<CombinerParameter> getRuleCombinerParam(String ruleId, String name){
		Multimap<String, CombinerParameter> p = ruleCombiningParameters.get(ruleId);
		return  (p == null)?ImmutableList.<CombinerParameter>of():p.get(name);
	}

	/**
	 * Gets policy rule combining algorithm
	 *
	 * @return {@link DecisionCombiningAlgorithm} defaultProvider
	 */
	public DecisionCombiningAlgorithm<Rule> getRuleCombiningAlgorithm(){
		return combiningAlgorithm;
	}

	/**
	 * Gets policy variable definitions
	 *
	 * @return a collection of {@link VariableDefinition} instances
	 */
	public Collection<VariableDefinition> getVariableDefinitions(){
		return variableDefinitions.values();
	}

	@Override
	public CompositeDecisionRuleIDReference getReference() {
		return reference;
	}

	/**
	 * Gets {@link VariableDefinition} by identifier
	 *
	 * @param variableId variable identifier
	 * @return {@link VariableDefinition} or {@code null}
	 * if variable definition can not be resolved
	 */
	public VariableDefinition getVariableDefinition(String variableId){
		return variableDefinitions.get(variableId);
	}

	/**
	 * Gets all policy rules
	 *
	 * @return a collection of {@link Rule}
	 * instances
	 */
	public List<Rule> getRules(){
		return rules;
	}

	public boolean contains(Rule r){
		return rules.contains(r);
	}

	/**
	 * Implementation creates policy-bound evaluation context
	 */
	@Override
	public EvaluationContext createContext(EvaluationContext context)
	{
		Preconditions.checkNotNull(context);
		CompositeDecisionRule p = context.getCurrentPolicy();
		Preconditions.checkArgument(p == this
				|| p == null);
		if(p == this){
			return context;
		}
		return new PolicyDescendantEvaluationContext(context);
	}

	protected Decision combineDecisions(EvaluationContext context){
		List<Rule> selected = Prepared.children(context, this);
		if(selected != rules) RootEvaluationContext.observationOf(context).childrenSelected(context, this, selected);
		return combiningAlgorithm.combine(context, selected);
	}

	private static final class Prepared
	{
		private static List<Rule> children(EvaluationContext context, Policy policy){
			if(!BUILT_IN_ALGORITHMS.contains(policy.combiningAlgorithm.getClass())) return policy.rules;
			EvaluationContext top = DescendantEvaluationContext.topOf(context);
			if(!(top instanceof RootEvaluationContext decision) || decision.generation().isEmpty()) return policy.rules;
			Generation generation = decision.generation().orElseThrow();
			if(generation.indexes.isEmpty() || policy.rules.size() < 2 || !decision.hasRequestAttributeResolution()
					|| decision.hasMisses() || context.isValidateFuncParamsAtRuntime()) return policy.rules;
			ConcurrentMap<Policy, TargetIndex<Rule>> indexes = generation.indexes.orElseThrow().policies;
			TargetIndex<Rule> index = indexes.get(policy);
			if(index == null){
				TargetIndex<Rule> built = TargetIndex.createRules(context, policy.rules);
				TargetIndex<Rule> winner = indexes.putIfAbsent(policy, built);
				index = winner == null ? built : winner;
			}
			return index.candidates(context);
		}
	}

	@Override
	boolean canRespond(){
		return canRespond;
	}

	@Override
	boolean hasBuiltInCombiningAlgorithm(){
		return builtInAlgorithm;
	}

	/** Whether a decision may prepare the conditions of the rules to share values: without, they are evaluated as written (design D45) */
	boolean mayShareConditions(){
		return mayShareConditions;
	}

	/**
	 * The conditions of the rules prepared to share values (design D45). Decisions that race each prepare
	 * them, and the first to publish its preparation wins: every decision reads that one. A plain read
	 * suffices, as the fields of a preparation are final.
	 */
	PreparedConditions preparedConditions(){
		PreparedConditions prepared = preparedConditions;
		if(prepared == null){
			PreparedConditions built = PreparedConditions.prepare(this);
			prepared = (PreparedConditions)PREPARED_CONDITIONS.compareAndExchange(this, null, built);
			if(prepared == null){
				prepared = built;
			}
		}
		return prepared;
	}

	@Override
	protected boolean isEvaluationContextValid(EvaluationContext context){
		return this.equals(context.getCurrentPolicy());
	}

	@Override
	public String toString(){
		MoreObjects.ToStringHelper h = MoreObjects.toStringHelper(this);
		return toStringBuilder(h)
		.add("variableDefinitions", variableDefinitions)
		.add("policyDefaults",policyDefaults)
		.add("rules", rules)
		.add("combiningAlgorithm", combiningAlgorithm)
		.add("ruleCombiningParameters", ruleCombiningParameters)
		.toString();
	}

	protected boolean equalsTo(Policy o) {
		return super.equalsTo(o)
			&& Objects.equal(policyDefaults, o.policyDefaults)
			&& Objects.equal(variableDefinitions, o.variableDefinitions)
			&& Objects.equal(rules, o.rules)
			&& Objects.equal(combiningAlgorithm, o.combiningAlgorithm)
			&& Objects.equal(ruleCombiningParameters, o.ruleCombiningParameters);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		return (o instanceof Policy)
			&& ((Policy)o).equalsTo(this);
	}

	@Override
	public int hashCode() {
		int h = hashCode;
		if(h == 0){
			// Arrays.hashCode of the fields in this order, without the array (design D22)
			int fields = 1;
			fields = 31 * fields + java.util.Objects.hashCode(policyDefaults);
			fields = 31 * fields + java.util.Objects.hashCode(variableDefinitions);
			fields = 31 * fields + java.util.Objects.hashCode(rules);
			fields = 31 * fields + java.util.Objects.hashCode(combiningAlgorithm);
			fields = 31 * fields + java.util.Objects.hashCode(ruleCombiningParameters);
			h = super.hashCode() * 31 + fields;
			hashCode = h;
		}
		return h;
	}

	public void accept(PolicyTreeVisitor v) {
		v.visitEnter(this);
		if(getTarget() != null){
			getTarget().accept(v);
		}
		for(VariableDefinition var : variableDefinitions.values()){
			var.accept(v);
		}
		for(ObligationExpression obligation : obligationExpressions){
			obligation.accept(v);
		}
		for(AdviceExpression advice : adviceExpressions){
			advice.accept(v);
		}
		for(DecisionRule rule : rules){
			rule.accept(v);
		}
		v.visitLeave(this);
	}

	/**
	 * An {@link EvaluationContext} implementation for evaluating
	 * {@link Policy} instances
	 */
	class PolicyDescendantEvaluationContext extends CompositeRuleEvaluationContext
	{
		private final Map<String, ValueExp> varDefEvalResults;
		/**
		 * The rule that a built-in combining algorithm of the policy evaluates in this context now, instead
		 * of in a context of its own; none outside that evaluation. Written by {@link #enterRule} and
		 * {@link #exitRule()} only (design D47)
		 */
		Rule rule;
		/** The status of that rule's evaluation, apart from the status of this context (design D47) */
		private Status ruleStatus;

		/**
		 * Creates policy evaluation context with a given parent context
		 *
		 * @param context a parent evaluation context
		 */
		PolicyDescendantEvaluationContext(EvaluationContext context){
			super(context);
			// A policy without variables never writes the map: setVariableEvaluationResult
			// rejects an undefined id first (change optimize-pdp-evaluation, D10)
			this.varDefEvalResults = variableDefinitions.isEmpty() ? Collections.emptyMap()
					: new HashMap<>(variableDefinitions.size() * 2);
		}

		/**
		 * Starts the evaluation of a rule in this context instead of a context of its own, for a built-in
		 * combining algorithm of the policy (design D47): an engine rule whose context no code of another
		 * implementation can receive, on the deciding thread of an unobserved decision of the engine's root,
		 * without validation at runtime, and while no other rule is evaluated here. The rule's status
		 * starts empty, whatever this context and the rules before it recorded.
		 *
		 * @param child a child that the combining algorithm evaluates now
		 * @return {@code true} when the rule is evaluated in this context until {@link #exitRule()};
		 * {@code false}, and nothing changed, when it gets a context of its own
		 */
		boolean enterRule(DecisionRule child){
			if(rule != null || !builtInAlgorithm || child.getClass() != Rule.class || !((Rule)child).evaluatesInPolicyContext()
					|| !(topOf(this) instanceof RootEvaluationContext root) || !root.isDecidingThread()
					|| root.getDecisionObservation().isEnabled() || root.isValidateFuncParamsAtRuntime()){
				return false;
			}
			rule = (Rule)child;
			ruleStatus = null;
			return true;
		}

		/**
		 * Ends the evaluation of the rule in this context, also when it failed; the statuses that it set by
		 * rule, in this context and above stay (design D47)
		 */
		void exitRule(){
			rule = null;
			ruleStatus = null;
		}

		/** While a rule is evaluated here, its own status (design D47) */
		@Override
		public Optional<Status> getEvaluationStatus(){
			return rule == null ? super.getEvaluationStatus() : Optional.ofNullable(ruleStatus);
		}

		@Override
		public void setEvaluationStatusIfAbsent(Supplier<Status> supplier){
			if(rule == null){
				super.setEvaluationStatusIfAbsent(supplier);
			}else if(ruleStatus == null){
				setEvaluationStatus(supplier.get());
			}
		}

		/**
		 * While a rule is evaluated here, as the rule's own context did with this one as its parent: the
		 * rule's status, its status by the rule, and then the status of this context, passed up by the
		 * policy unless a pass keeps the status that started it (design D47, plan review PR-D47-argon P3-1)
		 */
		@Override
		public void setEvaluationStatus(Status status){
			Rule evaluated = rule;
			if(evaluated == null){
				super.setEvaluationStatus(status);
				return;
			}
			ruleStatus = status;
			setEvaluationStatus(evaluated, status);
			setEvaluationStatusOf(Policy.this, status);
		}

		@Override
		public EvaluationContext createExtIndeterminateEvalContext() {
			return new PolicyDescendantEvaluationContext(this){
				@Override
				public EvaluationContext createExtIndeterminateEvalContext() {
					return this;
				}

				@Override
				public boolean isExtendedIndeterminateEval() {
					return true;
				}
			};
		}

		@Override
		public ValueExp getVariableEvaluationResult(String variableId) {
			return varDefEvalResults.get(variableId);
		}

		@Override
		public EvaluationContext getParentContext() {
			return getParent();
		}

		@Override
		public void setVariableEvaluationResult(String variableId,
				ValueExp value) {
			Preconditions.checkNotNull(variableId);
			Preconditions.checkNotNull(value);
			Preconditions.checkArgument(
					variableDefinitions.containsKey(variableId),
					"Given variable id=\"%s\" is not defined the policy id=\"%s\"",
					variableId, id);
			varDefEvalResults.put(variableId, value);
		}

		/**
		 * The policy of this context, which answers {@link #getCurrentPolicy()} (design D17)
		 */
		final Policy policy() {
			return Policy.this;
		}

		@Override
		public XPathVersion getXPathVersion() {
			PolicyDefaults defaults = Policy.this.getDefaults();
			if(defaults != null &&
					defaults.getXPathVersion() != null){
				return defaults.getXPathVersion();
			}
			return super.getXPathVersion();
		}
	}

	public final static class Builder extends BaseCompositeDecisionRule.Builder<Builder>
	{
		private DecisionCombiningAlgorithm<Rule> combiningAlgorithm;
		private Collection<VariableDefinition> variables = Lists.newLinkedList();
		private PolicyDefaults policyDefaults;

		private ImmutableList.Builder<Rule> rules = ImmutableList.builder();
		private Map<String, Multimap<String, CombinerParameter>> ruleCombinerParameters = Maps.newLinkedHashMap();

		public Builder(String policyId){
			super(policyId);
		}

		public Builder ruleCombinerParameter(String ruleId, CombinerParameter p){
			Preconditions.checkNotNull(ruleId);
			Preconditions.checkNotNull(p);
			Multimap<String, CombinerParameter> params = ruleCombinerParameters.get(ruleId);
			if(params == null){
				params = LinkedHashMultimap.create();
				ruleCombinerParameters.put(ruleId, params);
			}
			params.put(p.getName(), p);
			return this;
		}

		public Builder ruleCombineParams(String ruleId, Iterable<CombinerParameter> params){
			Preconditions.checkNotNull(ruleId);
			Preconditions.checkNotNull(params);
			for(CombinerParameter p : params){
				ruleCombinerParameter(ruleId, p);
			}
			return this;
		}

		public Builder noRuleCombinerParams(){
			this.ruleCombinerParameters.clear();
			return this;
		}

		public Builder defaults(PolicyDefaults defaults){
			this.policyDefaults = defaults;
			return this;
		}

		public Builder noDefaults(){
			this.policyDefaults = null;
			return this;
		}

		public Builder vars(Iterable<VariableDefinition> vars){
			for (VariableDefinition var : vars) {
				var(var);
			}
			return this;
		}

		public Builder var(VariableDefinition var){
			Preconditions.checkNotNull(var);
			this.variables.add(var);
			return this;
		}

		public Builder withoutVariables(){
			this.variables.clear();
			return this;
		}

		public Builder rule(Rule ...rules){
			this.rules.add(rules);
			return this;
		}

		public Builder rule(Rule.Builder b){
			Preconditions.checkNotNull(b);
			this.rules.add(b.build());
			return this;
		}

		public Builder rules(Iterable<Rule> rules){
			this.rules.addAll(rules);
			return this;
		}

		public Builder withoutRules(){
			this.rules = ImmutableList.builder();
			return this;
		}

		public Builder combiningAlgorithm(DecisionCombiningAlgorithm<Rule> alg)
		{
			Preconditions.checkNotNull(alg);
			this.combiningAlgorithm = alg;
			return this;
		}

		@Override
		protected Builder getThis() {
			return this;
		}

		public Policy build(){
			return new Policy(this);
		}
	}
}
