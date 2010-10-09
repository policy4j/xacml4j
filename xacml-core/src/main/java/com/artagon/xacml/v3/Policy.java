package com.artagon.xacml.v3;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Preconditions;

public class Policy extends BaseCompositeDecisionRule 
	implements PolicyElement
{	
	private PolicyDefaults policyDefaults;
	private List<Rule> rules;
	private Map<String, VariableDefinition> variableDefinitions;
	private DecisionCombiningAlgorithm<Rule> combine;
	
//	private CombinerParameters combinerParameters;
//	private Map<String, RuleCombinerParameters> ruleCombinerParameters;
//	private PolicyIssuer policyIssuer;
	
	/**
	 * A reference to itself
	 */
	private PolicyIDReference reference;
	
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
	public Policy(
			String policyId,
			Version version,
			String description,
			PolicyDefaults policyDefaults,
			Target target, 
			Collection<VariableDefinition> variables,
			Collection<CombinerParameters> combinerParameters,
			Collection<RuleCombinerParameters> ruleCombinerParameters,
			DecisionCombiningAlgorithm<Rule> combine,
			Collection<Rule> rules, 
			Collection<AdviceExpression> adviceExpressions,
			Collection<ObligationExpression> obligationExpressions) 
	{
		super(policyId, version, description, 
				target, adviceExpressions, obligationExpressions);
		Preconditions.checkNotNull(variables);
		Preconditions.checkNotNull(rules);
		Preconditions.checkNotNull(combine);
		this.rules = new LinkedList<Rule>(rules);
		this.combine = combine;
		this.reference = new PolicyIDReference(policyId, version);
		this.policyDefaults = policyDefaults;
		this.variableDefinitions = new ConcurrentHashMap<String, VariableDefinition>();
		for(VariableDefinition varDef : variables){
			this.variableDefinitions.put(varDef.getVariableId(), varDef);
		}
	}
	
	public Policy(
			String policyId,
			Version version,
			String description,
			PolicyDefaults policyDefaults,
			Target target, 
			Collection<VariableDefinition> variables,
			DecisionCombiningAlgorithm<Rule> combine,
			Collection<Rule> rules, 
			Collection<AdviceExpression> adviceExpressions,
			Collection<ObligationExpression> obligationExpressions) 
	{
		this(policyId, version, description, policyDefaults, target, variables, 
				Collections.<CombinerParameters>emptyList(), 
				Collections.<RuleCombinerParameters>emptyList(),
				combine, rules, adviceExpressions, obligationExpressions);
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
	public Policy(
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
	public Policy(
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
	
	/**
	 * Gets policy defaults
	 * 
	 * @return policy defaults
	 */
	public PolicyDefaults getDefaults() {
		return policyDefaults;
	}

	/**
	 * Gets policy rule combining algorithm
	 * 
	 * @return {@link DecisionCombiningAlgorithm} instance
	 */
	public DecisionCombiningAlgorithm<Rule> getRuleCombiningAlgorithm(){
		return combine;
	}
	
	/**
	 * A rule combining algorithm parameters
	 * 
	 * @return {@link CombinerParameters} or <code>null</code>
	 * if no parameters are specified
	 */
	public CombinerParameters getCombinerParameter(String name) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * A rule combining algorithm parameters
	 * 
	 * @return {@link CombinerParameters} or <code>null</code>
	 * if no parameters are specified
	 */
	public RuleCombinerParameters getRuleCombinerParameter(String ruleId, String name) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets policy variable definitions
	 * 
	 * @return a collection of {@link VariableDefinition} instances
	 */
	public Collection<VariableDefinition> getVariableDefinitions(){
		return Collections.unmodifiableCollection(variableDefinitions.values());
	}
	
	public CompositeDecisionRuleIDReference getReference() {
		return reference;
	}
	
	/**
	 * Gets {@link VariableDefinition} by identifier
	 * 
	 * @param variableId variable identifier
	 * @return {@link VariableDefinition} or <code>null</code>
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
		return new PolicyDelegatingEvaluationContext(context);
	}

	@Override
	protected boolean isEvaluationContextValid(EvaluationContext context){
		return context.getCurrentPolicy() == this;
	}
	
	@Override
	protected Decision doEvaluate(EvaluationContext context)
	{
		Decision decision = combine.combine(rules, context);
		context.addEvaluatedPolicy(this, decision);
		return decision;
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		if(getTarget() != null){
			getTarget().accept(v);
		}
//		if(combinerParameters != null){
//			combinerParameters.accept(v);
//		}
//		if(ruleCombinerParameters != null){
//			ruleCombinerParameters.accept(v);
//		}
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
	
	class PolicyDelegatingEvaluationContext extends DelegatingEvaluationContext
	{			
		private Map<String, ValueExpression> varEvalCache;
		
		/**
		 * Creates policy evaluation context with a given parent context
		 * 
		 * @param context a parent evaluation context
		 */
		PolicyDelegatingEvaluationContext(EvaluationContext context){
			super(context);
			this.varEvalCache = new HashMap<String, ValueExpression>();
		}
		
		

		@Override
		public ValueExpression getVariableEvaluationResult(String variableId) {
			return varEvalCache.get(variableId);
		}

		@Override
		public void setVariableEvaluationResult(String variableId,
				ValueExpression value) {
			varEvalCache.put(variableId, value);
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
}
