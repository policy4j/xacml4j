package com.artagon.xacml.v3.policy.impl;

import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.AdviceExpression;
import com.artagon.xacml.v3.Condition;
import com.artagon.xacml.v3.ConditionResult;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.Effect;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.ObligationExpression;
import com.artagon.xacml.v3.PolicyVisitor;
import com.artagon.xacml.v3.Rule;
import com.artagon.xacml.v3.Target;
import com.google.common.base.Preconditions;

final class DefaultRule extends BaseDesicionRule implements Rule
{
	private final static Logger log = LoggerFactory.getLogger(DefaultRule.class);
	
	private Effect effect;
	private Condition condition;
	
	/**
	 * Constructs rule with a given identifier, 
	 * condition and effect.
	 * 
	 * @param ruleId a rule unique identifier
	 * @param condition a condition
	 * @param effect a rule effect
	 */
	public DefaultRule(
			String ruleId, 
			String description,
			Target target,
			Condition condition, 
			Effect effect, Collection<AdviceExpression> adviceExpressions,
			Collection<ObligationExpression> obligationExpressions){
		super(ruleId, description, target, adviceExpressions, obligationExpressions);
		Preconditions.checkNotNull(ruleId);
		Preconditions.checkNotNull(effect);
		this.condition = condition;
		this.effect = effect;
	}
	
	/**
	 * Constructs rule instance with a given identifier
	 * target, condition and effect
	 * @param ruleId a rule identifier
	 * @param description
	 * @param target a rule target
	 * @param condition a rule condition
	 * @param effect a rule effect
	 */
	public DefaultRule(
			String ruleId, 
			String description,
			Target target,
			Condition condition, 
			Effect effect){
		this(ruleId, description, target, condition, effect, 
				Collections.<AdviceExpression>emptyList(), 
				Collections.<ObligationExpression>emptyList());
	}
	
	/**
	 * Gets rule effect
	 * 
	 * @return {@link Effect}
	 */
	public Effect getEffect(){
		return effect;
	}
	
	/**
	 * Gets rule condition
	 * 
	 * @return {@link Collection} rule 
	 * condition
	 */
	public Condition getCondition(){
		return condition;
	}
	
	/**
	 * Implementation returns the same context which was
	 * passed as parent context. Rule evaluation shares
	 * a context with a parent policy
	 */
	@Override
	public EvaluationContext createContext(EvaluationContext context) {
		return context;
	}
	
	@Override
	protected boolean isEvaluationContextValid(EvaluationContext context){
		return context.getCurrentPolicy() != null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.artagon.xacml.policy.BaseDesicion#doEvaluate(com.artagon.xacml.policy.EvaluationContext)
	 */
	protected Decision doEvaluate(EvaluationContext context)
	{
		ConditionResult result = (condition == null)?ConditionResult.TRUE:condition.evaluate(context); 
		log.debug("RuleId=\"{}\" condition evaluation result=\"{}\"", getId(), result);
		if(result == ConditionResult.INDETERMINATE){
			return getExtendedIndeterminate();
		}
		Decision d = (result == ConditionResult.TRUE)?
				getEffect().getResult():Decision.NOT_APPLICABLE;
		log.debug("RuleId=\"{}\" decision result=\"{}\"", getId(), d);
		return d;
	}
		
	/**
	 * Gets rule extended "Indeterminate" status
	 * 
	 * @return {@link Decision} instance representing
	 * extended "Indeterminate" status
	 */
	private Decision getExtendedIndeterminate(){
		return effect == Effect.DENY?
				Decision.INDETERMINATE_D:Decision.INDETERMINATE_P;
	}

	@Override
	public void accept(PolicyVisitor v) 
	{
		v.visitEnter(this);
		if(getTarget() != null){
			getTarget().accept(v);
		}
		if(condition != null){
			condition.accept(v);
		}
		v.visitLeave(this);
	}
}
