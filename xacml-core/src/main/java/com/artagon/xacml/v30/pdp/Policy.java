package com.artagon.xacml.v30.pdp;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.artagon.xacml.v30.XPathVersion;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class Policy extends BaseCompositeDecisionRule 
	implements PolicyElement
{
	private PolicyDefaults policyDefaults;
	private Map<String, VariableDefinition> variableDefinitions;
	private List<Rule> rules;
	private DecisionCombiningAlgorithm<Rule> combine;	
	private Map<String, Multimap<String, CombinerParameter>> ruleCombingingParameters;
	
	/**
	 * A reference to itself
	 */
	private PolicyIDReference reference;
	
	private Policy(PolicyBuilder b){
		super(b);
		Preconditions.checkNotNull(b.combiningAlgorithm, 
				"Rule decision combining algorithm must be specified");
		this.combine = b.combiningAlgorithm;
		this.policyDefaults = b.policyDefaults;
		this.reference = new PolicyIDReference(b.id, b.version);
		this.rules = ImmutableList.copyOf(b.rules);
		this.variableDefinitions = Maps.uniqueIndex(b.variables,
				new Function<VariableDefinition, String>(){
					@Override
					public String apply(VariableDefinition from) {
						return from.getVariableId();
					}});
		for(Entry<String, Multimap<String, CombinerParameter>> p : b.ruleCombinerParameters.entrySet()){
			ruleCombingingParameters.put(p.getKey(), ImmutableListMultimap.copyOf(p.getValue()));
		}
	}
	
	public static PolicyBuilder builder(String policyId){
		return new PolicyBuilder(policyId);
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
		Multimap<String, CombinerParameter> p = ruleCombingingParameters.get(ruleId);
		return  (p == null)?ImmutableList.<CombinerParameter>of():p.values();
	}
	
	public Collection<CombinerParameter> getRuleCombinerParam(String ruleId, String name){
		Multimap<String, CombinerParameter> p = ruleCombingingParameters.get(ruleId);
		return  (p == null)?ImmutableList.<CombinerParameter>of():p.get(name);
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
		ConditionResult result = (condition == null)?ConditionResult.TRUE:condition.evaluate(context); 
		if(log.isDebugEnabled()) {
			log.debug("Policy id=\"{}\" condition " +
					"evaluation result=\"{}\"", id, result);
		}
		if(result == ConditionResult.TRUE){
			Decision decision = combine.combine(context, rules);
			if(log.isDebugEnabled()) {
				log.debug("Policy id=\"{}\" combining algorithm=\"{}\" " +
						"evaluation result=\"{}\"", 
						new Object[] { getId(), combine.getId(), decision });
			}  
			if(!decision.isIndeterminate() || 
					decision != Decision.NOT_APPLICABLE){
				decision = evaluateAdvicesAndObligations(context, decision);
				context.addPolicyEvaluationResult(this, decision);
			}
			return decision;
		}
		if(result == ConditionResult.INDETERMINATE){
			Decision decision = combine.combine(context, rules);
			switch(decision){
				case DENY : return Decision.INDETERMINATE_D;
				case PERMIT : return Decision.INDETERMINATE_P;
				case INDETERMINATE_DP: return Decision.INDETERMINATE_DP;
				default: return decision;
			}
		}
		return Decision.NOT_APPLICABLE;
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
	
	@Override
	public String toString(){
		ToStringHelper h = Objects.toStringHelper(this);
		return _addProperties(h)
		.add("variableDefnitions", variableDefinitions)
		.add("policyDefaults",policyDefaults)
		.add("rules", rules)
		.toString();
	}
	
	public final static class PolicyBuilder extends BaseCompositeDecisionRuleBuilder<PolicyBuilder>
	{
		private DecisionCombiningAlgorithm<Rule> combiningAlgorithm;
		private Collection<VariableDefinition> variables = new LinkedList<VariableDefinition>();
		private PolicyDefaults policyDefaults;

		private Collection<Rule> rules = new LinkedList<Rule>();
		private Map<String, Multimap<String, CombinerParameter>> ruleCombinerParameters = new LinkedHashMap<String, Multimap<String,CombinerParameter>>();
		
		public PolicyBuilder(String policyId){
			super(policyId);
		}
		
		public PolicyBuilder withRuleCombinerParameter(String ruleId, CombinerParameter p){
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
		
		public PolicyBuilder withRuleCombinerParameters(String ruleId, Iterable<CombinerParameter> params){
			Preconditions.checkNotNull(ruleId);
			Preconditions.checkNotNull(params);
			for(CombinerParameter p : params){
				withRuleCombinerParameter(ruleId, p);
			}
			return this;
		}
		
		public PolicyBuilder withoutRuleCombinerParameters(){
			this.ruleCombinerParameters.clear();
			return this;
		}
		
		public PolicyBuilder withDefaults(PolicyDefaults defaults){
			this.policyDefaults = defaults;
			return this;
		}
		
		public PolicyBuilder withoutDefaults(){
			this.policyDefaults = null;
			return this;
		}
		
		public PolicyBuilder withVariables(Iterable<VariableDefinition> vars){
			for (VariableDefinition var : vars) {
				withVariable(var);
			}
			return this;
		}
		
		public PolicyBuilder withVariable(VariableDefinition var){
			Preconditions.checkNotNull(var);
			this.variables.add(var);
			return this;
		}
		
		public PolicyBuilder withoutVariables(){
			this.variables.clear();
			return this;
		}
		
		public PolicyBuilder withRule(Rule rule){
			Preconditions.checkNotNull(rule);
			this.rules.add(rule);
			return this;
		}
		
		public PolicyBuilder withRule(Rule.Builder b){
			Preconditions.checkNotNull(b);
			this.rules.add(b.build());
			return this;
		}
		
		public PolicyBuilder withRules(Iterable<Rule> rules){
			for(Rule rule : rules) {
				withRule(rule);
			}
			return this;
		}
		
		public PolicyBuilder withoutRules(){
			this.rules.clear();
			return this;
		}
		
		public PolicyBuilder withCombiningAlgorithm(DecisionCombiningAlgorithm<Rule> algo)
		{
			Preconditions.checkNotNull(algo);
			this.combiningAlgorithm = algo;
			return this;
		}

		@Override
		protected PolicyBuilder getThis() {
			return this;
		}
		
		public Policy create(){
			return new Policy(this);
		}
	}
}
