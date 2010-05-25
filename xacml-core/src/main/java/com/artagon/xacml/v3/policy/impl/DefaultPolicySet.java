package com.artagon.xacml.v3.policy.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.AdviceExpression;
import com.artagon.xacml.v3.CombinerParameters;
import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.DecisionCombinerParameters;
import com.artagon.xacml.v3.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.DecisionRule;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.ObligationExpression;
import com.artagon.xacml.v3.PolicyCombinerParameters;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.PolicySetCombinerParameters;
import com.artagon.xacml.v3.PolicySetDefaults;
import com.artagon.xacml.v3.PolicyVisitor;
import com.artagon.xacml.v3.Target;
import com.artagon.xacml.v3.Version;
import com.google.common.base.Preconditions;

final class DefaultPolicySet extends BaseCompositeDecisionRule implements PolicySet
{
	private final static Logger log = LoggerFactory.getLogger(DefaultPolicySet.class);
	
	private PolicySetDefaults policySetDefaults;
	private DecisionCombiningAlgorithm<CompositeDecisionRule> combine;
	private List<CompositeDecisionRule> decisionRules;
	private CombinerParameters combinerParameters;
	private PolicySetCombinerParameters policySetCombinerParameters;
	private PolicyCombinerParameters policyCombinerParameters;
	
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
	public DefaultPolicySet(
			String id, 
			Version version,
			String description,
			PolicySetDefaults policySetDefaults,
			Target target, 
			CombinerParameters combinerParameters,
			PolicyCombinerParameters policyCombinerParameters,
			PolicySetCombinerParameters policySetCombinerParameters,
			DecisionCombiningAlgorithm<CompositeDecisionRule> combine, 
			Collection<CompositeDecisionRule> policies, 
			Collection<AdviceExpression> adviceExpressions,
			Collection<ObligationExpression> obligationExpressions) 
	{
		super(id, version, description, target, adviceExpressions, obligationExpressions);
		Preconditions.checkNotNull(combine);
		this.combine = combine;
		this.decisionRules = new LinkedList<CompositeDecisionRule>(policies);
		this.policySetDefaults = policySetDefaults;
		this.combinerParameters = combinerParameters;
		this.policyCombinerParameters = policyCombinerParameters;
		this.policySetCombinerParameters = policySetCombinerParameters;
	}
	
	public DefaultPolicySet(
			String id, 
			Version version,
			String description,
			Target target, 
			DecisionCombiningAlgorithm<CompositeDecisionRule> combine, 
			Collection<CompositeDecisionRule> policies, 
			Collection<AdviceExpression> adviceExpressions,
			Collection<ObligationExpression> obligationExpressions) 
	{
		this(id, version, description, null, target, 
				null, null, null, combine, policies, adviceExpressions, obligationExpressions);
	}
	
	@Override
	public PolicySetDefaults getDefaults() {
		return policySetDefaults;
	}
	
	@Override
	public DecisionCombinerParameters getCombinerParameters() {
		return combinerParameters;
	}

	@Override
	public PolicyCombinerParameters getPolicyCombinerParameters() {
		return policyCombinerParameters;
	}

	@Override
	public PolicySetCombinerParameters getPolicySetCombinerParameters() {
		return policySetCombinerParameters;
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
		return new PolicySetDelegatingEvaluationContext(context, this);
	}

	@Override
	protected boolean isEvaluationContextValid(EvaluationContext context){
		return context.getCurrentPolicySet() == this;
	}
	
	@Override
	protected Decision doEvaluate(EvaluationContext context) 
	{
		log.debug("Evaluating PolicySet Id=\"{}\", Combine Algorithm Id=\"{}\"", 
				getId(), combine.getId());
		return combine.combine(decisionRules, context);
	}
	
	@Override
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
			combinerParameters.accept(v);
		}
		if(policyCombinerParameters != null){
			policyCombinerParameters.accept(v);
		}
		if(policySetCombinerParameters != null){
			policySetCombinerParameters.accept(v);
		}
		combine.accept(v);
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
}
