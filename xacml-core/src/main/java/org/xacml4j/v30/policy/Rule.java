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


import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.PolicyElement;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.policy.function.FunctionSpecBuilder;
import org.xacml4j.v30.types.Value;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class Rule extends BaseDecisionRule implements PolicyElement
{
	private final Effect effect;
	/**
	 * Whether no code of another implementation can receive the context of this rule, so that a
	 * built-in combining algorithm may evaluate it in the context of its policy (design D47)
	 */
	private final boolean inPolicyContext;
	/** A target with a match needs a successful precheck before its rule may borrow the policy context. */
	private final boolean targetHasMatch;
	/**
	 * The hash code once computed; every field that it reads is fixed when the rule is built (cycle AR).
	 * Hash inputs must remain immutable, including values supplied by trusted extensions.
	 */
	private int hashCode;

	private Rule(Builder b){
		super(b);
		this.effect = b.effect;
		EngineOnly eligibility = new EngineOnly();
		this.inPolicyContext = eligibility.rule(this);
		this.targetHasMatch = eligibility.targetHasMatch;
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
	 * Creates the context of this rule: the given context when it already evaluates this rule, as the
	 * context of a policy does in which a built-in combining algorithm evaluates it (design D47); else
	 * a context of its own below the given one
	 */
	@Override
	public EvaluationContext createContext(EvaluationContext context) {
		if(isEvaluationContextValid(context)){
			return context;
		}
		return new RuleEvaluationContext(context);
	}

	/** Whether a built-in combining algorithm may evaluate this rule in the context of its policy (design D47) */
	final boolean evaluatesInPolicyContext(){
		return inPolicyContext;
	}

	/** Whether an undecided target must be matched in this rule's own context. */
	final boolean targetNeedsPrecheck(){
		return targetHasMatch;
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

	/**
	 * Tells whether no code of another implementation can receive the context of a rule (design D47, plan
	 * review PR-D47-argon): its target, condition and responses, and the variable definitions that they
	 * reference, are of the engine's classes; every match predicate, applied function and referenced
	 * function is a trusted built-in; and every literal is a value of the engine. A match can pass an
	 * unchecked runtime value to its predicate, so a target with a match also needs a successful
	 * precheck. A higher-order call over unchecked values keeps a rule context. Any other expression, a type assertion or a
	 * quantified expression among them, counts as one that can. Each definition is walked once, so
	 * that a diamond of definitions takes linear time and a cycle ends conservatively.
	 */
	private static final class EngineOnly
	{
		private static final int UNSAFE = 1;
		private static final int RUNTIME_VALUE = 2;
		private static final int FUNCTION_REFERENCE = 4;
		/** The provenance of definitions walked so far, by identity; made at the first reference */
		private Map<VariableDefinition, Integer> walked;
		private boolean targetHasMatch;

		boolean rule(Rule rule){
			return target(rule.getTarget()) && condition(rule.getCondition())
					&& responses(rule.adviceExpressions, AdviceExpression.class)
					&& responses(rule.obligationExpressions, ObligationExpression.class);
		}

		private boolean target(Target target){
			if(target == null){
				return true;
			}
			if(target.getClass() != Target.class){
				return false;
			}
			for(MatchAnyOf anyOf : target.getAnyOf()){
				if(anyOf.getClass() != MatchAnyOf.class){
					return false;
				}
				for(MatchAllOf allOf : anyOf.getAllOf()){
					if(allOf.getClass() != MatchAllOf.class){
						return false;
					}
					for(Match match : allOf.getMatch()){
						targetHasMatch = true;
						if(match.getClass() != Match.class || !FunctionSpecBuilder.usesBuiltInInvocation(match.predicate())
								|| !expression(match.getReference()) || !expression(match.getAttributeValue())){
							return false;
						}
					}
				}
			}
			return true;
		}

		private boolean condition(Condition condition){
			return condition == null || condition.getClass() == Condition.class && expression(condition.getExpression());
		}

		private boolean responses(List<? extends BaseDecisionRuleResponseExpression> responses, Class<?> engineClass){
			for(BaseDecisionRuleResponseExpression response : responses){
				if(response.getClass() != engineClass){
					return false;
				}
				for(AttributeAssignmentExpression assignment : response.getAttributeAssignmentExpressions()){
					if(assignment.getClass() != AttributeAssignmentExpression.class || !expression(assignment.getExpression())){
						return false;
					}
				}
			}
			return true;
		}

		private boolean expression(Expression e){
			return (provenance(e) & UNSAFE) == 0;
		}

		/**
		 * Summarizes whether an expression can supply unchecked runtime values or a function reference.
		 * A call that combines the two can evaluate such values with its context. A built-in Apply
		 * returns a ValueExp, so its function-reference arguments do not escape as a reference. Its
		 * result may contain unchecked values even when all its arguments are literals, so every built-in
		 * call supplies runtime provenance, which also passes through variable definitions.
		 */
		private int provenance(Expression e){
			Class<?> type = e.getClass();
			if(type == AttributeDesignator.class || type == AttributeSelector.class){
				return RUNTIME_VALUE;
			}
			if(e instanceof Value<?>){
				return type.getPackage() == Value.class.getPackage() ? 0 : UNSAFE;
			}
			if(type == BagOfValues.class){
				// A higher-order function evaluates each value of a literal bag with the context of the rule
				BagOfValues bag = (BagOfValues)e;
				int flags = 0;
				for(int i = 0; i < bag.size(); i++){
					flags |= provenance(bag.valueAt(i));
				}
				return flags;
			}
			if(type == FunctionReference.class){
				// A higher-order function calls it with the context of the rule
				return FunctionSpecBuilder.usesBuiltInInvocation(((FunctionReference)e).function())
						? FUNCTION_REFERENCE : UNSAFE;
			}
			if(type == Apply.class){
				Apply apply = (Apply)e;
				if(!FunctionSpecBuilder.usesBuiltInInvocation(apply.function())){
					return UNSAFE;
				}
				int flags = 0;
				for(Expression argument : apply.getArguments()){
					flags |= provenance(argument);
				}
				if((flags & UNSAFE) != 0 || (flags & (RUNTIME_VALUE | FUNCTION_REFERENCE))
						== (RUNTIME_VALUE | FUNCTION_REFERENCE)) return UNSAFE;
				return RUNTIME_VALUE;
			}
			if(type == VariableReference.class){
				VariableDefinition definition = ((VariableReference)e).getDefinition();
				if(definition.getClass() != VariableDefinition.class){
					return UNSAFE;
				}
				if(walked == null){
					walked = new IdentityHashMap<>();
				}
				Integer flags = walked.get(definition);
				if(flags != null) return flags;
				// A cycle is not proven safe. A completed definition retains its provenance on reuse.
				walked.put(definition, UNSAFE);
				int result = provenance(definition.getExpression());
				walked.put(definition, result);
				return result;
			}
			return UNSAFE;
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
