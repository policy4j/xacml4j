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

import org.xacml4j.v30.*;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class PolicySet extends
	BaseCompositeDecisionRule
{
	private final PolicySetDefaults policySetDefaults;
	private final DecisionCombiningAlgorithm combiningAlgorithm;
	private final List<? extends DecisionRule> decisionRules;

	private final Map<String, Multimap<String, CombinerParameter>> policyCombinerParameters;
	private	final Map<String, Multimap<String, CombinerParameter>> policySetCombinerParameters;

	private final PolicySetReference reference;

	private PolicySet(Builder b)
	{
		super(b);
		this.reference = PolicySetReference.builder(id).version(b.version).build();
		this.policySetDefaults = b.policyDefaults;
		Preconditions.checkNotNull(b.combiningAlgorithm,
				"Policy decision combining algorithm must be specified");
		this.combiningAlgorithm = b.combiningAlgorithm;
		this.decisionRules = b.policies.build();
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
		return (p == null) ? ImmutableList.<CombinerParameter>of() : p.values();
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
		return (p == null) ? ImmutableList.<CombinerParameter>of() : p.get(name);
	}

	/**
	 * Gets all combiner parameters for a given policy set identifier
	 *
	 * @param policySetId a policy set identifier
	 * @return a collection of combiner parameters
	 */
	public Collection<CombinerParameter> getPolicySetCombinerParams(String policySetId){
		Multimap<String, CombinerParameter> p = policySetCombinerParameters.get(policySetId);
		return (p == null) ? ImmutableList.<CombinerParameter>of() : p.values();
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
		return (p == null) ? ImmutableList.<CombinerParameter>of() : p.get(name);
	}

	/**
	 * Gets policy set decision combining algorithm
	 *
	 * @return a decision combining algorithm
	 */
	public DecisionCombiningAlgorithm getPolicyDecisionCombiningAlgorithm(){
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
	 * Creates {@link DecisionRuleEvaluationContext} to evaluate this policy
	 * set to be used in {@link PolicySet#isMatch(org.xacml4j.v30.EvaluationContext)}
	 * or {@link PolicySet#evaluate(org.xacml4j.v30.EvaluationContext)}
	 *
	 * @param context a parent evaluation context
	 * @return {@code DecisionRuleEvaluationContext} instance to evaluate
	 * this policy set
	 */
	@Override
	public DecisionRuleEvaluationContext createContext(EvaluationContext ctx) {
		Preconditions.checkNotNull(ctx);
        DecisionRuleEvaluationContext context = (DecisionRuleEvaluationContext)ctx;
		if(context.getCurrentPolicySet() == this){
			return context;
		}
		return new PolicySetDelegatingEvaluationContext(context);
	}

	@Override
	protected final boolean isEvaluationContextValid(DecisionRuleEvaluationContext context){
		return this.equals(context.getCurrentPolicySet());
	}

	protected Decision combineDecisions(DecisionRuleEvaluationContext context){
		return combiningAlgorithm.combine(context, decisionRules);
	}

	public List<? extends DecisionRule> getChildRules() {
		return decisionRules;
	}

	public boolean contains(CompositeDecisionRule r){
		return decisionRules.contains(r);
	}

	@Override
	public void accept(PolicyVisitor pv)
	{
        Visitor v = (Visitor)pv;
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

    public interface Visitor extends PolicyVisitor{
        void visitEnter(PolicySet ps);
        void visitLeave(PolicySet ps);
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
		return super.hashCode() * 31 +
				Objects.hashCode(policySetDefaults, combiningAlgorithm,
						decisionRules, policyCombinerParameters,
						policySetCombinerParameters, reference);
	}

	class PolicySetDelegatingEvaluationContext
			extends DelegatingEvaluationContext {


		/**
		 * Constructs delegating evaluation context
		 * which delegates all invocations to the enclosing
		 * policy set or root context to evaluate given
		 * policy set
		 *
		 * @param parentContext a parent context
		 */
		PolicySetDelegatingEvaluationContext(
				DecisionRuleEvaluationContext parentContext){
			super(parentContext);
			Preconditions.checkArgument(
					parentContext.getCurrentPolicySet() != PolicySet.this);
			Preconditions.checkArgument(
					parentContext.getCurrentPolicy() == null);
		}

		@Override
		public PolicySet getCurrentPolicySet() {
			return PolicySet.this;
		}

		@Override
		public Status getEvaluationStatus() {
			return getDelegate().getEvaluationStatus();
		}

		@Override
		public void setEvaluationStatus(Status code) {
			getDelegate().setEvaluationStatus(code);
		}

		@Override
		public DecisionRuleEvaluationContext getParentContext() {
			return getDelegate();
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

	public final static class Builder extends BaseCompositeDecisionRule.Builder<Builder>
	{
		private DecisionCombiningAlgorithm combiningAlgorithm;
		private PolicySetDefaults policyDefaults;
		private ImmutableList.Builder<DecisionRule> policies = ImmutableList.builder();

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

		public Builder compositeDecisionRules(Iterable<? extends DecisionRule> rules)
		{
			this.policies.addAll(rules);
			return getThis();
		}

		public Builder compositeDecisionRules(DecisionRule ... rules)
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

		public Builder withCombiningAlgorithm(DecisionCombiningAlgorithm alg)
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
