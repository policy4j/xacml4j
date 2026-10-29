package org.xacml4j.v30.policy;

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
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.PolicyElement;
import org.xacml4j.v30.Status;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class Rule extends BaseDecisionRule implements PolicyElement
{
	private final Effect effect;
	/**
	 * The hash code once computed; every field that it reads is fixed when the rule is built (cycle AR).
	 * Hash inputs must remain immutable, including values supplied by trusted extensions.
	 */
	private int hashCode;

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
		return toStringBuilder(MoreObjects.toStringHelper(this))
				.add("effect", effect)
				.toString();
	}

	@Override
	public int hashCode() {
		int h = hashCode;
		if(h == 0){
			// (31 + ...) is Arrays.hashCode of the one-element array, without the array (design D22)
			h = super.hashCode() * 31 + (31 + java.util.Objects.hashCode(effect));
			hashCode = h;
		}
		return h;
	}

	@Override
	final Decision evaluateRule(EvaluationContext context)
	{
		if(!isEvaluationContextValid(context)){
			context.setEvaluationStatusIfAbsent(()->Status.processingError().build());
			return getExtendedIndeterminate();
		}
		MatchResult m  = isMatch(context);
		if(m == MatchResult.INDETERMINATE){
			context.setEvaluationStatusIfAbsent(()->Status.processingError().build());
			return getExtendedIndeterminate();
		}
		if(m == MatchResult.NOMATCH){
			return Decision.NOT_APPLICABLE;
		}
		ConditionResult result = (condition == null)?ConditionResult.TRUE:evaluateCondition(context);
		if(result == ConditionResult.TRUE){
			Decision d = effect.toDecision();
			// Also inside an extended Indeterminate pass: a failing matching obligation or advice
			// makes the rule Indeterminate (XACML 3.0, section 7.18)
			try{
				evaluateAdvicesAndObligations(context, d);
			}catch(EvaluationException e){
				context.setEvaluationStatusIfAbsent(()->Status.processingError()
				                                              .error(e)
				                                              .build());
				return getExtendedIndeterminate();
			}
			return d;
		}
		if(result == ConditionResult.INDETERMINATE){
			context.setEvaluationStatusIfAbsent(()->Status.processingError()
			                                  .build());
			return getExtendedIndeterminate();
		}
		return Decision.NOT_APPLICABLE;
	}

	private Decision getExtendedIndeterminate(){
		return (effect == Effect.DENY)?
				Decision.INDETERMINATE_D:Decision.INDETERMINATE_P;
	}

	public void accept(PolicyTreeVisitor v) {
		v.visitEnter(this);
		if(getTarget() != null){
			getTarget().accept(v);
		}
		if(getCondition() != null){
			getCondition().accept(v);
		}
		v.visitLeave(this);
	}

	public class RuleEvaluationContext extends DescendantEvaluationContext
	{
		public RuleEvaluationContext(EvaluationContext context){
			super(context);
		}

		/**
		 * The rule of this context, which answers {@link #getCurrentRule()} (design D17)
		 */
		final Rule rule() {
			return Rule.this;
		}

		@Override
		public EvaluationContext createExtIndeterminateEvalContext() {
			return new ExtendedIndeterminateRuleContext(this);
		}
	}

	/**
	 * The extended Indeterminate context of a rule context, named so that
	 * {@link #isOwnContext(DescendantEvaluationContext)} can tell it from a subclass of
	 * {@link RuleEvaluationContext} declared elsewhere (design D17)
	 */
	final class ExtendedIndeterminateRuleContext extends RuleEvaluationContext
	{
		ExtendedIndeterminateRuleContext(EvaluationContext context){
			super(context);
		}

		@Override
		public boolean isExtendedIndeterminateEval() {
			return true;
		}
	}

	/**
	 * Tests if a context is a rule context that this class creates; a subclass of the public
	 * {@link RuleEvaluationContext} declared elsewhere can override its queries
	 *
	 * @param context a rule context or another descendant context
	 * @return {@code true} if the context is exactly a {@link RuleEvaluationContext} or an
	 * {@link ExtendedIndeterminateRuleContext}
	 */
	static boolean isOwnContext(DescendantEvaluationContext context){
		Class<?> type = context.getClass();
		return type == RuleEvaluationContext.class || type == ExtendedIndeterminateRuleContext.class;
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
