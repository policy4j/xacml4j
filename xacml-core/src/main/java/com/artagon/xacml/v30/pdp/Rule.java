package com.artagon.xacml.v30.pdp;

import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.Effect;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.MatchResult;
import com.google.common.base.Preconditions;

public class Rule extends BaseDecisionRule implements PolicyElement
{
	private Effect effect;
	
	private Rule(Rule.Builder b){
		super(b);
		this.effect = b.effect;
	}

	public static Builder builder(String ruleId, Effect effect){
		return new Builder(ruleId, effect);
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
	 * Implementation returns the same context which was
	 * passed as parent context. Rule evaluation shares
	 * a context with a parent policy
	 */
	@Override
	public EvaluationContext createContext(EvaluationContext context) {
		return context;
	}
	
	/**
	 * Implementation checks if this rule is 
	 * enclosed by the parent policy in the evaluation context
	 */
	@Override
	protected boolean isEvaluationContextValid(EvaluationContext context){
		return context.getCurrentPolicy() != null;
	}
	

	@Override
	public final Decision evaluate(EvaluationContext context)
	{
		Preconditions.checkArgument(
				isEvaluationContextValid(context));
		ConditionResult result = (condition == null)?ConditionResult.TRUE:condition.evaluate(context); 
		if(log.isDebugEnabled()){
			log.debug("Rule id=\"{}\" condition " +
					"evaluation result=\"{}\"", getId(), result);
		}
		if(result == ConditionResult.TRUE){
			Decision d = effect.toDecision();
			d = evaluateAdvicesAndObligations(context, d);		
			if(log.isDebugEnabled()){
				log.debug("Rule id=\"{}\" " +
						"decision result=\"{}\"", getId(), d);
			}
			return d;
		}
		if(result == ConditionResult.INDETERMINATE){
			return getExtendedIndeterminate();
		}
		return Decision.NOT_APPLICABLE;
	}
	
	/**
	 * Combines {@link #isMatch(EvaluationContext)} and 
	 * {@link #evaluate(EvaluationContext)} calls to one single
	 * method invocation
	 */
	@Override
	public  Decision evaluateIfMatch(EvaluationContext context)
	{
		MatchResult r = isMatch(context);
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
					"match result is=\"{}\", " +
					"not evaluating rule", getId(), r);
		}
		return (r == MatchResult.INDETERMINATE)?
				getExtendedIndeterminate():Decision.NOT_APPLICABLE;
	}
		
	private Decision getExtendedIndeterminate(){
		return (effect == Effect.DENY)?
				Decision.INDETERMINATE_D:Decision.INDETERMINATE_P;
	}

	@Override
	public void accept(PolicyVisitor v) 
	{
		v.visitEnter(this);
		if(getTarget() != null){
			getTarget().accept(v);
		}
		if(getCondition() != null){
			getCondition().accept(v);
		}
		v.visitLeave(this);
	}
	
	public static class Builder extends BaseDecisionRuleBuilder<Builder>
	{
		private Effect effect;
		
		private Builder(String ruleId, Effect effect){
			super(ruleId);
			Preconditions.checkNotNull(effect, "Rule effect can't be null");
			this.effect = effect;
		}

		public Builder withEffect(Effect effect){
			Preconditions.checkNotNull(effect, "Rule effect can't be null");
			this.effect = effect;
			return this;
		}
		
		@Override
		protected Builder getThis() {
			return this;
		}
		public Rule build(){
			return new Rule(this);
		}
	}
}
