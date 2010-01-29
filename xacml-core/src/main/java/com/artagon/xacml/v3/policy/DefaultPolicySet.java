package com.artagon.xacml.v3.policy;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.Decision;

public class DefaultPolicySet extends BaseCompositeDecisionRule implements PolicySet
{
	private final static Logger log = LoggerFactory.getLogger(DefaultPolicySet.class);
	
	private DecisionCombiningAlgorithm<CompositeDecisionRule> combine;
	private List<CompositeDecisionRule> decisionRules;
	
	/**
	 * Constructs a policy set with a given identifier
	 * target, decision combining algorithm, policies,
	 * advice and obligation expressions
	 * 
	 * @param id a policy set identifier
	 * @param target a policy set target
	 * @param combine a policy combining algorithm
	 * @param policies a collection of policies or policy sets
	 * @param adviceExpressions a collection of advice expressions
	 * @param obligationExpressions a collection of obligation expressions
	 */
	public DefaultPolicySet(String id, Target target, 
			DecisionCombiningAlgorithm<CompositeDecisionRule> combine, 
			Collection<? extends CompositeDecisionRule> policies, 
			Collection<AdviceExpression> adviceExpressions,
			Collection<ObligationExpression> obligationExpressions) 
	{
		super(id, target, adviceExpressions, obligationExpressions);
		Preconditions.checkNotNull(combine);
		this.combine = combine;
		this.decisionRules = new LinkedList<CompositeDecisionRule>(policies);
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
	public List<? extends DecisionRule> getDecisions() {
		return Collections.unmodifiableList(decisionRules);
	}

	@Override
	public void accept(PolicyVisitor v) 
	{
		v.visitEnter(this);
		if(getTarget() != null){
			getTarget().accept(v);
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
