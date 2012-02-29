package com.artagon.xacml.v30.pdp;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.artagon.xacml.v30.Version;
import com.artagon.xacml.v30.XPathVersion;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class Policy extends BaseCompositeDecisionRule 
	implements PolicyElement
{
	
	private PolicyDefaults policyDefaults;
	private List<Rule> rules;
	private Map<String, VariableDefinition> variableDefinitions;
	private DecisionCombiningAlgorithm<Rule> combine;
	
//	private CombinerParameters combinerParameters;
//	private Map<String, RuleCombinerParameters> ruleCombinerParameters;
	
	private BigInteger maxDelegationDepth;
	private PolicyIssuer issuer;
	
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
			PolicyIssuer issuer,
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
		Preconditions.checkNotNull(combine);
		this.issuer = issuer;
		this.rules = (rules != null)?ImmutableList.copyOf(rules):ImmutableList.<Rule>of();
		this.combine = combine;
		this.reference = new PolicyIDReference(policyId, version);
		this.policyDefaults = policyDefaults;
		if(variables != null){
			this.variableDefinitions = Maps.uniqueIndex
					(variables, new Function<VariableDefinition, String>(){
				@Override
				public String apply(VariableDefinition from) {
					return from.getVariableId();
				}
			});
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
		this(policyId, version, description, policyDefaults, 
				null, target, variables, 
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
	
	
	public static Builder builder(){
		return new Builder();
	}
	
	public BigInteger getMaxDelegationDepth(){
		return maxDelegationDepth;
	}
	
	public PolicyIssuer getIssuer(){
		return issuer;
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
		return variableDefinitions.values();
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
		return rules;
	}
	
	/**
	 * Implementation creates {@link PolicyDelegatingEvaluationContext}
	 */
	@Override
	public EvaluationContext createContext(EvaluationContext context) 
	{
		Preconditions.checkNotNull(context);
		Policy p = context.getCurrentPolicy();
		Preconditions.checkArgument(p == this 
				|| p == null);
		if(p == this){
			return context;
		}
		return new PolicyDelegatingEvaluationContext(context);
	}
	
	@Override
	public Decision evaluate(EvaluationContext context)
	{
		Preconditions.checkNotNull(context);
		Preconditions.checkArgument(isEvaluationContextValid(context));
		Decision decision = combine.combine(context, rules);
		if(log.isDebugEnabled()) {
			log.debug("Policy id=\"{}\" combining algorithm=\"{}\" " +
					"decision result=\"{}\"", 
					new Object[] { getId(), combine.getId(), decision });
		}
		decision = evaluateAdvicesAndObligations(context, decision);
		context.addEvaluatedApplicablePolicy(this, decision);
		return decision;
	}
	
	protected boolean isEvaluationContextValid(EvaluationContext context){
		return context.getCurrentPolicy() == this;
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
		for(VariableDefinition var : variableDefinitions.values()){
			var.accept(v);
		}
		for(ObligationExpression obligation : getObligationExpressions()){
			obligation.accept(v);
		}
		for(AdviceExpression advice : getAdviceExpressions()){
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
		private Map<String, ValueExpression> varDefEvalResults;
		
		/**
		 * Creates policy evaluation context with a given parent context
		 * 
		 * @param context a parent evaluation context
		 */
		PolicyDelegatingEvaluationContext(EvaluationContext context){
			super(context);
			this.varDefEvalResults = new HashMap<String, ValueExpression>();
		}
		
		@Override
		public ValueExpression getVariableEvaluationResult(String variableId) {
			Preconditions.checkNotNull(variableId);
			return varDefEvalResults.get(variableId);
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
	
	public final static class Builder extends BaseBuilder<Builder>
	{
		private Version version;
		private DecisionCombiningAlgorithm<Rule> combiningAlgorithm;
		private Collection<VariableDefinition> variables = new LinkedList<VariableDefinition>();
		private PolicyDefaults policyDefaults;
		private PolicyIssuer policyIssuer;
		private Collection<Rule> rules = new LinkedList<Rule>();
		private Collection<CombinerParameters> combinerParameters = new LinkedList<CombinerParameters>();
		private Collection<RuleCombinerParameters> ruleCombinerParameters = new LinkedList<RuleCombinerParameters>();
		
		private Builder(){
		}
		
		public Builder withIssuer(PolicyIssuer issuer){
			Preconditions.checkNotNull(issuer);
			this.policyIssuer = issuer;
			return this;
		}
		
		public Builder withoutIssuer(){
			this.policyIssuer = null;
			return this;
		}
		
		public Builder withVersion(Version v){
			Preconditions.checkNotNull(v);
			this.version = v;
			return this;
		}
		
		public Builder withVersion(String v){
			return withVersion(Version.parse(v));
		}
				
		public Builder withCombinerParameters(CombinerParameters p){
			Preconditions.checkNotNull(p);
			this.combinerParameters.add(p);
			return this;
		}
		
		public Builder withoutCombinerParameters(){
			this.combinerParameters.clear();
			return this;
		}
		
		public Builder withRuleCombinerParameters(RuleCombinerParameters p){
			Preconditions.checkNotNull(p);
			this.ruleCombinerParameters.add(p);
			return this;
		}
		
		public Builder withoutRuleCombinerParameters(){
			this.ruleCombinerParameters.clear();
			return this;
		}
		
		public Builder withDefaults(PolicyDefaults defaults){
			Preconditions.checkNotNull(defaults);
			this.policyDefaults = defaults;
			return this;
		}
		
		public Builder withoutDefaults(){
			this.policyDefaults = null;
			return this;
		}
		
		public Builder withVariable(VariableDefinition var){
			Preconditions.checkNotNull(var);
			this.variables.add(var);
			return this;
		}
		
		public Builder withoutVariables(){
			this.variables.clear();
			return this;
		}
		
		public Builder withRule(Rule rule){
			Preconditions.checkNotNull(rule);
			this.rules.add(rule);
			return this;
		}
		
		public Builder withRule(Rule.Builder b){
			Preconditions.checkNotNull(b);
			this.rules.add(b.build());
			return this;
		}
		
		public Builder withoutRules(){
			this.rules.clear();
			return this;
		}
		
		public Builder withCombiningAlgorithm(DecisionCombiningAlgorithm<Rule> algo)
		{
			Preconditions.checkNotNull(algo);
			this.combiningAlgorithm = algo;
			return this;
		}

		@Override
		protected Builder getThis() {
			return this;
		}
		
		public Policy create(){
			return new Policy(
					id, 
					version, 
					description, 
					policyDefaults, 
					policyIssuer,
					target, 
					variables, 
					combinerParameters,
					ruleCombinerParameters,
					combiningAlgorithm,
					rules, 
					adviceExpressions, 
					obligationExpressions);
		}
	}
}
