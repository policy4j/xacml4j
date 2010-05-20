package com.artagon.xacml.v3.policy.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.artagon.xacml.v3.AdviceExpression;
import com.artagon.xacml.v3.CombinerParameters;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.DecisionRule;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.ObligationExpression;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicyDefaults;
import com.artagon.xacml.v3.PolicyVisitor;
import com.artagon.xacml.v3.Rule;
import com.artagon.xacml.v3.RuleCombinerParameters;
import com.artagon.xacml.v3.Target;
import com.artagon.xacml.v3.VariableDefinition;
import com.artagon.xacml.v3.Version;
import com.google.common.base.Preconditions;

final class DefaultPolicy extends BaseCompositeDecisionRule implements Policy
{
	private PolicyDefaults policyDefaults;
	private List<Rule> rules;
	private Map<String, VariableDefinition> variableDefinitions;
	private DecisionCombiningAlgorithm<Rule> combine;
	private CombinerParameters combinerParameters;
	private RuleCombinerParameters ruleCombinerParameters;
	
	
	/**
	 * Creates policy with a given identifier, target, variables.
	 * decision combining algorithm, rules and advice and obligation
	 * expressions
	 * 
	 * @param policyId a policy identifier
	 * @param version a policy version
	 * @param policyDefaults a policy defaults
	 * @param target a policy target
	 * @param variables a policy variable definitions
	 * @param combine a policy rules combine algorithm
	 * @param rules a policy rules
	 * @param adviceExpressions an advice expressions
	 * @param obligationExpressions an obligation expressions
	 */
	public DefaultPolicy(
			String policyId,
			Version version,
			PolicyDefaults policyDefaults,
			Target target, 
			Collection<VariableDefinition> variables, 
			DecisionCombiningAlgorithm<Rule> combine,
			Collection<Rule> rules, 
			Collection<AdviceExpression> adviceExpressions,
			Collection<ObligationExpression> obligationExpressions){
		super(policyId, version, target, adviceExpressions, obligationExpressions);
		Preconditions.checkNotNull(variables);
		Preconditions.checkNotNull(rules);
		Preconditions.checkNotNull(combine);
		this.rules = new LinkedList<Rule>(rules);
		this.combine = combine;
		this.policyDefaults = policyDefaults;
		this.variableDefinitions = new ConcurrentHashMap<String, VariableDefinition>(variables.size());
		for(VariableDefinition varDef : variables){
			this.variableDefinitions.put(varDef.getVariableId(), varDef);
		}
	}
	
	/**
	 * Constructs policy with a given identifier,
	 * version, combining algorithm and rules
	 * @param policyId a policy identifier
	 * @param version a policy version
	 * @param combine a rules combining algorithm
	 * @param rules a collection of rules
	 * @param advice a collection of advice expressions
	 * @param obligations a collection of 
	 * obligation expressions
	 */
	public DefaultPolicy(
			String policyId, 
			Version version,
			DecisionCombiningAlgorithm<Rule> combine,
			Collection<Rule> rules, 
			Collection<AdviceExpression> advice,
			Collection<ObligationExpression> obligations)
	{
		this(policyId, 
				version,
				null,
				null, 
				Collections.<VariableDefinition>emptyList(),
				combine,
				rules,
				advice,
				obligations);
	}
	
	/**
	 * Constructs policy with a given identifier,
	 * version, combining algorithm and rules
	 * @param policyId a policy identifier
	 * @param version a policy version
	 * @param combine a rules combining algorithm
	 * @param rules an array of rules
	 */
	public DefaultPolicy(
			String policyId, 
			Version version,
			DecisionCombiningAlgorithm<Rule> combine,
			Rule ...rules)
	{
		this(policyId, version, combine,
				Arrays.asList(rules),
				Collections.<AdviceExpression>emptyList(),
				Collections.<ObligationExpression>emptyList());
	}
	
	@Override
	public PolicyDefaults getDefaults() {
		return policyDefaults;
	}

	
	@Override
	public CombinerParameters getCombinerParameters() {
		return combinerParameters;
	}

	@Override
	public RuleCombinerParameters getRuleCombinerParameters() {
		return ruleCombinerParameters;
	}

	@Override
	public Collection<VariableDefinition> getVariableDefinitions(){
		return Collections.unmodifiableCollection(variableDefinitions.values());
	}
	
	@Override
	public VariableDefinition getVariableDefinition(String variableId){
		return variableDefinitions.get(variableId);
	}
	
	@Override
	public List<Rule> getRules(){
		return Collections.unmodifiableList(rules);
	}
	
	/**
	 * Implementation creates {@link PolicyDelegatingEvaluationContext}
	 */
	@Override
	public EvaluationContext createContext(EvaluationContext context) 
	{
		Preconditions.checkArgument(context.getCurrentPolicy() == this 
				|| context.getCurrentPolicy() == null);
		if(context.getCurrentPolicy() == this){
			return context;
		}
		return new PolicyDelegatingEvaluationContext(context,this);
	}

	@Override
	protected boolean isEvaluationContextValid(EvaluationContext context){
		return context.getCurrentPolicy() == this;
	}
	
	@Override
	protected Decision doEvaluate(EvaluationContext context)
	{
		return combine.combine(rules, context);
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		if(getTarget() != null){
			getTarget().accept(v);
		}
		if(combinerParameters != null){
			combinerParameters.accept(v);
		}
		if(ruleCombinerParameters != null){
			ruleCombinerParameters.accept(v);
		}
		combine.accept(v);
		for(VariableDefinition var : variableDefinitions.values()){
			var.accept(v);
		}
		for(DecisionRule rule : rules){
			rule.accept(v);
		}
		for(ObligationExpression obligation : getObligationExpressions()){
			obligation.accept(v);
		}
		for(AdviceExpression advice : getAdviceExpressions()){
			advice.accept(v);
		}
		v.visitLeave(this);
	}	
}
