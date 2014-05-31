package org.xacml4j.v30.pdp;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */


import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.MatchResult;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class Rule extends BaseDecisionRule implements PolicyElement
{
	private final Effect effect;

	private Rule(Builder b){
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
		if(isEvaluationContextValid(context)){
			return context;
		}
		return new RuleEvaluationContext(context);
	}

	@Override
	protected boolean isEvaluationContextValid(EvaluationContext context){
		return this == context.getCurrentRule() &&
			   context.getCurrentPolicy() != null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		return (o instanceof Rule)
			&& ((Rule)o).equalsTo(this);
	}

	protected boolean equalsTo(Rule r) {
		return super.equalsTo(r)
			&& effect.equals(r.effect);
	}

	@Override
	public String toString(){
		return toStringBuilder(Objects.toStringHelper(this))
				.add("effect", effect)
				.toString();
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 31 + Objects.hashCode(effect);
	}

	@Override
	public final Decision evaluate(EvaluationContext context)
	{
		if(!isEvaluationContextValid(context)){
			return getExtendedIndeterminate();
		}
		MatchResult m  = isMatch(context);
		if(log.isDebugEnabled()){
			log.debug("Decision rule id=\"{}\" " +
					"target evaluation is=\"{}\"", id, m);
		}
		if(m == MatchResult.INDETERMINATE){
			return getExtendedIndeterminate();
		}
		if(m == MatchResult.NOMATCH){
			return Decision.NOT_APPLICABLE;
		}
		ConditionResult result = (condition == null)?ConditionResult.TRUE:condition.evaluate(context);
		if(log.isDebugEnabled()){
			log.debug("Decision rule id=\"{}\" " +
					"condition evaluation is=\"{}\"", id, result);
		}
		if(result == ConditionResult.TRUE){
			Decision d = effect.toDecision();
			if(!context.isExtendedIndeterminateEval()){
				try{
					evaluateAdvicesAndObligations(context, d);
				}catch(EvaluationException e){
					return getExtendedIndeterminate();
				}
			}
			return d;
		}
		if(result == ConditionResult.INDETERMINATE){
			return getExtendedIndeterminate();
		}
		return Decision.NOT_APPLICABLE;
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

	public class RuleEvaluationContext extends DelegatingEvaluationContext
	{
		public RuleEvaluationContext(EvaluationContext context){
			super(context);
		}

		@Override
		public DecisionRule getCurrentRule() {
			return Rule.this;
		}
	}

	public static class Builder extends BaseDecisionRule.Builder<Builder>
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
