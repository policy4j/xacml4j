package com.artagon.xacml.v30;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;

public class PolicySet extends 
	BaseCompositeDecisionRule implements PolicyElement
{
	private BigInteger maxDelegationDepth;
	private PolicyIssuer issuer;
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
			PolicyIssuer issuer,
			Target target, 
			Collection<CombinerParameters> combinerParameters,
			Collection<PolicyCombinerParameters> policyCombinerParameters,
			Collection<PolicySetCombinerParameters> policySetCombinerParameters,
			DecisionCombiningAlgorithm<CompositeDecisionRule> combine, 
			Collection<CompositeDecisionRule> policies, 
			Collection<AdviceExpression> adviceExpressions,
			Collection<ObligationExpression> obligationExpressions) 
	{
		super(id, version, description, target, adviceExpressions, obligationExpressions);
		checkNotNull(combine, "Policy set combining algorithm must be specified");
		Preconditions.checkNotNull(combinerParameters);
		Preconditions.checkNotNull(policyCombinerParameters);
		Preconditions.checkNotNull(policySetCombinerParameters);
		this.reference = new PolicySetIDReference(id, version);
		this.combine = combine;
		this.decisionRules = new LinkedList<CompositeDecisionRule>(policies);
		this.policySetDefaults = policySetDefaults;
		this.issuer = issuer;
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
				null, null,
				target, 
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
		this(id, version, null, null, null, null, 
				Collections.<CombinerParameters>emptyList(), 
				Collections.<PolicyCombinerParameters>emptyList(), 
				Collections.<PolicySetCombinerParameters>emptyList(), 
				combine, 
				Collections.<CompositeDecisionRule>emptyList(), 
				Collections.<AdviceExpression>emptyList(), 
				Collections.<ObligationExpression>emptyList());
	}
	
	public BigInteger getMaxDelegationDepth(){
		return maxDelegationDepth;
	}
	
	public PolicyIssuer getIssuer(){
		return issuer;
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
		if(context.getCurrentPolicySet() == this){
			return context;
		}
		return new PolicySetDelegatingEvaluationContext(context);
	}

	@Override
	protected boolean isEvaluationContextValid(EvaluationContext context){
		return context.getCurrentPolicySet() == this;
	}
	
	@Override
	protected Decision doEvaluate(EvaluationContext context) 
	{
		Decision decision = combine.combine(decisionRules, context);
		context.addEvaluatedApplicablePolicySet(this, decision);
		return decision;
	}
	
	public List<? extends CompositeDecisionRule> getDecisions() {
		return Collections.unmodifiableList(decisionRules);
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
		for(ObligationExpression obligation : getObligationExpressions()){
			obligation.accept(v);
		}
		for(AdviceExpression advice : getAdviceExpressions()){
			advice.accept(v);
		}
		v.visitLeave(this);
	}		

	class PolicySetDelegatingEvaluationContext extends DelegatingEvaluationContext 
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

}
