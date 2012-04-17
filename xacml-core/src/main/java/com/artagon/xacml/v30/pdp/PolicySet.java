package com.artagon.xacml.v30.pdp;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.artagon.xacml.v30.Version;
import com.artagon.xacml.v30.XPathVersion;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class PolicySet extends 
	BaseCompositeDecisionRule implements PolicyElement
{
	private PolicySetDefaults policySetDefaults;
	private DecisionCombiningAlgorithm<CompositeDecisionRule> combine;
	private List<CompositeDecisionRule> decisionRules;
	private Collection<CombinerParameters> combinerParameters;
	private	Map<String, PolicySetCombinerParameters> policySetCombinerParameters;
	private Map<String, PolicyCombinerParameters> policyCombinerParameters;
	private PolicySetIDReference reference;
	
	/**
	 * Constructs a policy set with a given identifier
	 * target, decision combining algorithm, policies,
	 * advice and obligation expressions
	 * 
	 * @param id a policy set identifier
	 * @param target a policy set target
	 * @param combinerParameters a combiner parameters
	 * @param policyCombinerParameters a policy combiner parameters
	 * @param policySetCombinerParameters a policy set combiner parameters
	 * @param combine a policy combining algorithm
	 * @param policies a collection of policies or policy sets
	 * @param adviceExpressions a collection of advice expressions
	 * @param obligationExpressions a collection of obligation expressions
	 */
	public PolicySet(
			String id, 
			Version version,
			String description,
			PolicySetDefaults policySetDefaults,
			Target target, 
			PolicyIssuer issuer,
			BigInteger maxDelegationDepth,
			Collection<CombinerParameters> combinerParameters,
			Collection<PolicyCombinerParameters> policyCombinerParameters,
			Collection<PolicySetCombinerParameters> policySetCombinerParameters,
			DecisionCombiningAlgorithm<CompositeDecisionRule> combine, 
			Collection<CompositeDecisionRule> policies, 
			Collection<AdviceExpression> adviceExpressions,
			Collection<ObligationExpression> obligationExpressions) 
	{
		super(id, version, description, target, 
				issuer, maxDelegationDepth, 
				adviceExpressions, obligationExpressions);
		checkNotNull(combine, "Policy set combining algorithm must be specified");
		Preconditions.checkNotNull(combinerParameters);
		Preconditions.checkNotNull(policyCombinerParameters);
		Preconditions.checkNotNull(policySetCombinerParameters);
		this.reference = new PolicySetIDReference(id, version);
		this.combine = combine;
		this.decisionRules = ImmutableList.copyOf(policies);
		this.policySetDefaults = policySetDefaults;
		this.combinerParameters = new ArrayList<CombinerParameters>(combinerParameters);
		this.policyCombinerParameters = new HashMap<String, PolicyCombinerParameters>(
				 policyCombinerParameters.size());
		for(PolicyCombinerParameters p : policyCombinerParameters){
			Preconditions.checkState(
					this.policyCombinerParameters.put(p.getPolicyId(), p) == null);
		}
		this.policySetCombinerParameters = new HashMap<String, PolicySetCombinerParameters>(policySetCombinerParameters.size());
		for(PolicySetCombinerParameters p : policySetCombinerParameters){
			Preconditions.checkState(
					this.policySetCombinerParameters.put(p.getPolicySetId(), p) == null);
		}
	}

	public PolicySet(
			String id, 
			Version version,
			String description,
			Target target, 
			DecisionCombiningAlgorithm<CompositeDecisionRule> combine, 
			Collection<CompositeDecisionRule> policies, 
			Collection<AdviceExpression> adviceExpressions,
			Collection<ObligationExpression> obligationExpressions) 
	{
		this(id, version, 
				description, 
				null,
				target,
				null,
				null, 
				Collections.<CombinerParameters>emptyList(), 
				Collections.<PolicyCombinerParameters>emptyList(), 
				Collections.<PolicySetCombinerParameters>emptyList(), 
				combine, policies, adviceExpressions, obligationExpressions);
	}
	
	public PolicySet(
			String id, 
			Version version, 
			DecisionCombiningAlgorithm<CompositeDecisionRule> combine) 
	{
		this(id, version, null, null, null, null, null,
				Collections.<CombinerParameters>emptyList(), 
				Collections.<PolicyCombinerParameters>emptyList(), 
				Collections.<PolicySetCombinerParameters>emptyList(), 
				combine, 
				Collections.<CompositeDecisionRule>emptyList(), 
				Collections.<AdviceExpression>emptyList(), 
				Collections.<ObligationExpression>emptyList());
	}
	
	public static Builder builder(){
		return new Builder();
	}
		
	@Override
	public CompositeDecisionRuleIDReference getReference() {
		return reference;
	}

	public DecisionCombiningAlgorithm<CompositeDecisionRule> getPolicyDecisionCombiningAlgorithm(){
		return combine;
	}
	
	/**
	 * Gets policy set defaults
	 * 
	 * @return {@link PolicySetDefaults}
	 */
	public PolicySetDefaults getDefaults() {
		return policySetDefaults;
	}
	
	public Collection<CombinerParameters> getCombinerParameters() {
		return combinerParameters;
	}

	public Collection<PolicyCombinerParameters> getPolicyCombinerParameters() {
		return policyCombinerParameters.values();
	}

	public Collection<PolicySetCombinerParameters> getPolicySetCombinerParameters() {
		return policySetCombinerParameters.values();
	}
	
	/**
	 * Creates {@link EvaluationContext} to evaluate this policy
	 * set to be used in {@link this#isApplicable(EvaluationContext)}
	 * or {@link this#evaluate(EvaluationContext)}
	 * 
	 * @param context a parent evaluation context
	 * @return {@link EvaluationContext} instance to evaluate
	 * this policy set
	 */
	@Override
	public EvaluationContext createContext(EvaluationContext context) {
		Preconditions.checkNotNull(context);
		if(context.getCurrentPolicySet() == this){
			return context;
		}
		return new PolicySetDelegatingEvaluationContext(context);
	}
	
	protected final boolean isEvaluationContextValid(EvaluationContext context){
		return context.getCurrentPolicySet() == this;
	}
	
	public final Decision evaluate(EvaluationContext context) 
	{
		Preconditions.checkNotNull(context);
		Preconditions.checkArgument(isEvaluationContextValid(context));
		Decision decision = combine.combine(context, decisionRules);
		if(log.isDebugEnabled()) {
			log.debug("PolicySet id=\"{}\" combining algorithm=\"{}\" " +
					"decision result=\"{}\"", 
					new Object[] { getId(), combine.getId(), decision });
		}
		decision = evaluateAdvicesAndObligations(context, decision);
		context.addEvaluatedApplicablePolicySet(this, decision);
		return decision;
	}
	
	public List<? extends CompositeDecisionRule> getDecisions() {
		return decisionRules;
	}

	@Override
	public void accept(PolicyVisitor v) 
	{
		v.visitEnter(this);
		if(getTarget() != null){
			getTarget().accept(v);
		}
		if(policySetDefaults != null){
			policySetDefaults.accept(v);
		}
		if(combinerParameters != null){
			for(CombinerParameters p :combinerParameters){
				p.accept(v);
			}
		}
		if(policyCombinerParameters != null){
			for(PolicyCombinerParameters p : policyCombinerParameters.values()){
				p.accept(v);
			}
		}
		if(policySetCombinerParameters != null){
			for(PolicySetCombinerParameters p : policySetCombinerParameters.values()){
				p.accept(v);
			}
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

	class PolicySetDelegatingEvaluationContext 
		extends DelegatingEvaluationContext 
	{	
		/**
		 * Constructs delegating evaluation context
		 * which delegates all invocations to the enclosing
		 * policy set or root context to evaluate given 
		 * policy set
		 * 
		 * @param parentContext a parent context
		 */
		PolicySetDelegatingEvaluationContext(
				EvaluationContext parentContext){
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
		public XPathVersion getXPathVersion() {
			PolicySetDefaults defaults = PolicySet.this.getDefaults();
			if(defaults != null && 
					defaults.getXPathVersion() != null){
				return defaults.getXPathVersion();
			}
			return super.getXPathVersion();
		}
	}
	
	public final static class Builder extends BaseDecisionRuleBuilder<Builder>
	{
		private Version version;
		private DecisionCombiningAlgorithm<CompositeDecisionRule> combiningAlgorithm;
		private PolicySetDefaults policyDefaults;
		private BigInteger maxDelegationDepth;
		private PolicyIssuer policyIssuer;
		private Collection<CompositeDecisionRule> policies = new LinkedList<CompositeDecisionRule>();
		private Collection<CombinerParameters> combinerParameters = new LinkedList<CombinerParameters>();
		private Collection<PolicyCombinerParameters> policyCombinerParameters = new LinkedList<PolicyCombinerParameters>();
		private Collection<PolicySetCombinerParameters> policySetCombinerParameters = new LinkedList<PolicySetCombinerParameters>();
		
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
		
		public Builder withPolicyCombinerParameters(PolicyCombinerParameters p){
			Preconditions.checkNotNull(p);
			this.policyCombinerParameters.add(p);
			return this;
		}
		
		public Builder withoutPolicyCombinerParameters(){
			this.policyCombinerParameters.clear();
			return this;
		}
		
		public Builder withPolicyCombinerParameters(PolicySetCombinerParameters p){
			Preconditions.checkNotNull(p);
			this.policySetCombinerParameters.add(p);
			return this;
		}
		
		public Builder withoutPolicySetCombinerParameters(){
			this.policySetCombinerParameters.clear();
			return this;
		}
		
		public Builder withDefaults(PolicySetDefaults defaults){
			Preconditions.checkNotNull(defaults);
			this.policyDefaults = defaults;
			return this;
		}
		
		public Builder withoutDefaults(){
			this.policyDefaults = null;
			return this;
		}
		
		public Builder withCompositeDecisionRules(Iterable<CompositeDecisionRule> rules){
			Preconditions.checkNotNull(rules);
			Iterables.addAll(policies, rules);
			return this;
			
		}
		public Builder withPolicy(Policy rule){
			Preconditions.checkNotNull(rule);
			this.policies.add(rule);
			return this;
		}
		
		public Builder withPolicySet(PolicySet rule){
			Preconditions.checkNotNull(rule);
			this.policies.add(rule);
			return this;
		}
		
		public Builder withPolicy(Policy.Builder b){
			Preconditions.checkNotNull(b);
			this.policies.add(b.create());
			return this;
		}
		
		public Builder withPolicy(PolicySet.Builder b){
			Preconditions.checkNotNull(b);
			this.policies.add(b.build());
			return this;
		}
		
		public Builder withoutRules(){
			this.policies.clear();
			return this;
		}
		
		public Builder withCombiningAlgorithm(DecisionCombiningAlgorithm<CompositeDecisionRule> algo)
		{
			Preconditions.checkNotNull(algo);
			this.combiningAlgorithm = algo;
			return this;
		}

		@Override
		protected Builder getThis() {
			return this;
		}
		
		public PolicySet build(){
			return new PolicySet(
					id, 
					version, 
					description, 
					policyDefaults, 
					target,  
					policyIssuer,
					maxDelegationDepth,
					combinerParameters,
					policyCombinerParameters,
					policySetCombinerParameters,
					combiningAlgorithm,
					policies, 
					adviceExpressions, 
					obligationExpressions);
		}
	}

}
