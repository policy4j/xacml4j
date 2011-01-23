package com.artagon.xacml.v30;

import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class Rule extends BaseDesicionRule implements PolicyElement
{
	private final static Logger log = LoggerFactory.getLogger(Rule.class);
	
	private String ruleId;
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
	public Rule(String ruleId, 
			String description,
			Target target,
			Condition condition, 
			Effect effect, 
			Collection<AdviceExpression> adviceExpressions,
			Collection<ObligationExpression> obligationExpressions){
		super(description, target, adviceExpressions, obligationExpressions);
		Preconditions.checkNotNull(ruleId);
		Preconditions.checkNotNull(effect);
		this.ruleId = ruleId;
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
	public Rule(
			String ruleId, 
			String description,
			Target target,
			Condition condition, 
			Effect effect){
		this(ruleId, description, target, condition, effect, 
				Collections.<AdviceExpression>emptyList(), 
				Collections.<ObligationExpression>emptyList());
	}
	
	@Override
	public String getId(){
		return ruleId;
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
	

	@Override
	protected Decision doEvaluate(EvaluationContext context)
	{
		if(log.isDebugEnabled()){
			log.debug("Evaluating rule id=\"{}\" " +
					"condition=\"{}\"", getId(), condition);
		}
		ConditionResult result = (condition == null)?ConditionResult.TRUE:condition.evaluate(context); 
		if(log.isDebugEnabled()){
			log.debug("Rule id=\"{}\" condition " +
					"evaluation result=\"{}\"", getId(), result);
		}
		if(result == ConditionResult.INDETERMINATE){
			return getExtendedIndeterminate();
		}
		Decision d = (result == ConditionResult.TRUE)?
				getEffect().getResult():Decision.NOT_APPLICABLE;
		if(log.isDebugEnabled()){
			log.debug("Rule id=\"{}\" decision result=\"{}\"", getId(), d);
		}
		return d;
	}
	
	/**
	 * Combines {@link #isApplicable(EvaluationContext)} and 
	 * {@link #evaluate(EvaluationContext)} calls to one single
	 * method invocation
	 */
	@Override
	public  Decision evaluateIfApplicable(EvaluationContext context)
	{
		MatchResult r = isApplicable(context);
		Preconditions.checkState(r != null);
		if(r == MatchResult.MATCH){
			if(log.isDebugEnabled()){
				log.debug("Rule=\"{}\" match " +
						"result is=\"{}\", evaluating rule", getId(), r);
			}
			return evaluate(context);
		}
		if(log.isDebugEnabled()){
			log.debug("Rule=\"{}\" " +
					"match result is=\"{}\", not evaluating rule", getId(), r);
		}
		return (r == MatchResult.INDETERMINATE)?
				getExtendedIndeterminate():Decision.NOT_APPLICABLE;
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
