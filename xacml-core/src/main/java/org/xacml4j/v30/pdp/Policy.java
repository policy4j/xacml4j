package org.xacml4j.v30.pdp;

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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.ValueExpression;
import org.xacml4j.v30.XPathVersion;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class Policy extends BaseCompositeDecisionRule
		implements PolicyElement {
	private final PolicyDefaults policyDefaults;
	private final Map<String, VariableDefinition> variableDefinitions;
	private final List<Rule> rules;
	private final DecisionCombiningAlgorithm<Rule> combiningAlgorithm;
	private final Map<String, Multimap<String, CombinerParameter>> ruleCombiningParameters;

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
	 * @return {@link DecisionCombiningAlgorithm} instance
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
		return new PolicyDelegatingEvaluationContext(context);
	}

	protected Decision combineDecisions(EvaluationContext context){
		return combiningAlgorithm.combine(context, rules);
	}

	@Override
	protected boolean isEvaluationContextValid(EvaluationContext context){
		return this.equals(context.getCurrentPolicy());
	}

	@Override
	public String toString(){
		ToStringHelper h = Objects.toStringHelper(this);
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
		return super.hashCode() * 31 +
				Objects.hashCode(policyDefaults, variableDefinitions,
						rules, combiningAlgorithm, ruleCombiningParameters);
	}

	@Override
	public void accept(PolicyVisitor v) {
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
	class PolicyDelegatingEvaluationContext extends DelegatingEvaluationContext
	{
		private final Map<String, ValueExpression> varDefEvalResults;

		/**
		 * Creates policy evaluation context with a given parent context
		 *
		 * @param context a parent evaluation context
		 */
		PolicyDelegatingEvaluationContext(EvaluationContext context){
			super(context);
			this.varDefEvalResults = Maps.newHashMap();
		}

		@Override
		public ValueExpression getVariableEvaluationResult(String variableId) {
			return varDefEvalResults.get(variableId);
		}

		@Override
		public EvaluationContext getParentContext() {
			return getDelegate();
		}

		@Override
		public void setVariableEvaluationResult(String variableId,
				ValueExpression value) {
			Preconditions.checkNotNull(variableId);
			Preconditions.checkNotNull(value);
			Preconditions.checkArgument(
					variableDefinitions.containsKey(variableId),
					"Given variable id=\"%s\" is not defined the policy id=\"%s\"",
					variableId, id);
			varDefEvalResults.put(variableId, value);
		}

		@Override
		public Policy getCurrentPolicy() {
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
