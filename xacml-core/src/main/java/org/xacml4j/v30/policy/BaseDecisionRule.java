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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.spi.observe.DecisionObservation;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

abstract class  BaseDecisionRule implements DecisionRule
{
	protected static final Logger log = LoggerFactory.getLogger(BaseDecisionRule.class);

	protected final String id;
	private final String description;
	private final Target target;
	final TargetBeforeContext.Plan contextFreeTarget;
	protected final Condition condition;
	protected final ImmutableList<AdviceExpression> adviceExpressions;
	protected final ImmutableList<ObligationExpression> obligationExpressions;
	/** Whether evaluating the target or the condition can write responses (design D36) */
	private final boolean targetOrConditionCanWrite;
	/**
	 * Whether every advice and obligation expression gives the same response at each evaluation
	 * (cycle AN), so that the responses of a decision are kept from its first evaluation (cycle AU)
	 */
	private final boolean constantResponses;
	/** The responses of a Permit and of a Deny decision when constant, kept as first evaluated; immutable, so read and written without a lock */
	private DecisionResponses permitResponses;
	private DecisionResponses denyResponses;


	protected BaseDecisionRule(
			Builder<?> b){
		Preconditions.checkNotNull(b.id,
				"Decision rule identifier can not be null");
		this.id = b.id;
		this.description = b.description;
		this.target = b.target;
		this.contextFreeTarget = TargetBeforeContext.prepare(target);
		this.condition = b.condition;
		this.adviceExpressions = b.adviceExpressions.build();
		this.obligationExpressions = b.obligationExpressions.build();
		this.targetOrConditionCanWrite = ResponseWriters.canWrite(target) || ResponseWriters.canWrite(condition);
		this.constantResponses = allConstant(adviceExpressions, AdviceExpression.class)
				&& allConstant(obligationExpressions, ObligationExpression.class);
	}

	/**
	 * Tells whether every expression gives the same response at each evaluation: it is exactly of
	 * the engine's class, which decides what applies by its effect, and every assignment is a
	 * literal value (cycle AN, review R-AN)
	 */
	private static boolean allConstant(List<? extends BaseDecisionRuleResponseExpression> expressions, Class<?> engineClass){
		for(BaseDecisionRuleResponseExpression e : expressions){
			if(e.getClass() != engineClass || e.constantAssignments().isEmpty()){
				return false;
			}
		}
		return true;
	}

	@Override
	public String getId(){
		return id;
	}

	/**
	 * Gets decision rule description
	 *
	 * @return decision rule description
	 */
	public String getDescription(){
		return description;
	}

	/**
	 * Gets decision rule target
	 *
	 * @return {@link Target} or {@code null}
	 * if rule is applicable to any request
	 */
	public Target getTarget(){
		return target;
	}

	/**
	 * Gets decision rule condition
	 *
	 * @return {@link Condition} or {@code null}
	 * implying always {@code true} condition
	 */
	public Condition getCondition(){
		return condition;
	}

	/**
	 * Gets advice expressions in this rule
	 *
	 * @return a collection of {@link AdviceExpression}
	 */
	public Collection<AdviceExpression> getAdviceExpressions(){
		return adviceExpressions;
	}

	/**
	 * Gets obligation expressions in this rule
	 *
	 * @return a collection of {@link ObligationExpression}
	 */
	public Collection<ObligationExpression> getObligationExpressions(){
		return obligationExpressions;
	}

	/**
	 * Tells whether this rule has advice or obligation expressions (design D36)
	 */
	final boolean hasResponseExpressions(){
		return !adviceExpressions.isEmpty() || !obligationExpressions.isEmpty();
	}

	/**
	 * Tells whether evaluating this rule's target or condition can write responses (design D36).
	 * They are evaluated with the rule's own context, outside the staging of a composite rule, so
	 * they count for the rule that encloses it.
	 */
	final boolean targetOrConditionCanWrite(){
		return targetOrConditionCanWrite;
	}

	/**
	 * Tells whether evaluating this rule, its target and condition included, can write responses
	 * (design D36)
	 */
	final boolean mayWriteResponses(){
		return hasResponseExpressions() || targetOrConditionCanWrite;
	}

	/**
	 * Evaluates this rule, as one evaluation of the observation of its decision (design D34)
	 *
	 * @param context an evaluation context
	 * @return {@link Decision} the decision of this rule
	 */
	@Override
	public final Decision evaluate(EvaluationContext context)
	{
		DecisionObservation o = RootEvaluationContext.observationOf(context);
		if(!o.isEnabled()){
			return evaluateRule(context);
		}
		Object parent = o.evaluationStarted(context, this);
		Decision d;
		try{
			d = evaluateRule(context);
		}catch(Throwable t){
			o.evaluationFailed(context, this, parent, t);
			throw t;
		}
		o.evaluationFinished(context, this, parent, d);
		return d;
	}

	abstract Decision evaluateRule(EvaluationContext context);

	/**
	 * Testing if this decision rule is applicable to
	 * the current evaluation context
	 *
	 * @param context an evaluation context
	 * @return {@link MatchResult} a match result
	 */
	@Override
	public MatchResult isMatch(EvaluationContext context){
		// A no-match from the precheck of the enclosing algorithm is answered from the parent context;
		// a match is answered in this rule's own context, like a target evaluation (design D41)
		Optional<MatchResult> prechecked = TargetBeforeContext.takePrechecked(context, this);
		boolean noMatch = prechecked.isPresent() && prechecked.get() == MatchResult.NOMATCH;
		if(!noMatch && !isEvaluationContextValid(context)){
			return MatchResult.INDETERMINATE;
		}
		MatchResult r = prechecked.isPresent() ? prechecked.get() : match(context);
		RootEvaluationContext.observationOf(context).targetMatched(context, this, r);
		return r;
	}

	private MatchResult match(EvaluationContext context){
		try{
			return (target == null)?MatchResult.MATCH:target.match(context);
		}catch(EvaluationException e){
			context.setEvaluationStatus(e.getStatus());
			return MatchResult.INDETERMINATE;
		}
		catch(Exception e){
			context.setEvaluationStatus(Status.processingError()
			                                  .error(e).build());
			return MatchResult.INDETERMINATE;
		}
	}

	/**
	 * Evaluates the condition of this rule, which must have one, and reports its result (design D34)
	 *
	 * @param context an evaluation context
	 * @return {@link ConditionResult} the result of the condition
	 */
	final ConditionResult evaluateCondition(EvaluationContext context){
		ConditionResult r = condition.evaluate(context);
		RootEvaluationContext.observationOf(context).conditionEvaluated(context, this, r);
		return r;
	}

	/**
	 * Evaluates all matching to the given
	 * decision rule advice and obligations
	 *
	 * @param context an evaluation context
	 * @param result a rule evaluation result
	 */
	protected final void evaluateAdvicesAndObligations(
			EvaluationContext context, Decision result) throws EvaluationException
	{
		if(result.isIndeterminate() ||
				result == Decision.NOT_APPLICABLE ||
				!hasResponseExpressions()){
			return;
		}
		ImmutableList<Advice> advices;
		ImmutableList<Obligation> obligations;
		try
		{
			// Immutable lists, which the contexts above keep as they are (cycle AU)
			DecisionResponses kept = (result == Decision.PERMIT)?permitResponses:denyResponses;
			if(kept != null){
				advices = kept.advices();
				obligations = kept.obligations();
			}else{
				advices = evaluateAdvices(context, result);
				obligations = evaluateObligations(context, result);
				if(constantResponses){
					keep(result, new DecisionResponses(advices, obligations));
				}
			}
			if(!advices.isEmpty()){
				context.addAdvices(result, advices);
			}
			if(!obligations.isEmpty()){
				context.addObligations(result, obligations);
			}
		}catch(EvaluationException e){
			context.setEvaluationStatus(e.getStatus());
			throw e;
		}
		catch(Exception e){
			Status status = Status.processingError()
			                      .message(e.getMessage())
			                      .error(e)
			                      .build();
			context.setEvaluationStatus(status);
			throw new EvaluationException(status, e);
		}
		if(!advices.isEmpty() || !obligations.isEmpty()){
			RootEvaluationContext.observationOf(context).responsesStaged(context, this, result, advices, obligations);
		}
	}

	private void keep(Decision result, DecisionResponses responses){
		if(result == Decision.PERMIT){
			permitResponses = responses;
		}else{
			denyResponses = responses;
		}
	}

	/** The advice and obligations of one decision of a rule (cycle AU) */
	private record DecisionResponses(ImmutableList<Advice> advices, ImmutableList<Obligation> obligations) {}

	/**
	 * Evaluates advice expressions matching given decision
	 * {@link Decision} result
	 *
	 * @param context an evaluation context
	 * @param result a decision evaluation result
	 * @return the applicable {@link Advice} instances, an empty immutable list if there is none
	 * @throws EvaluationException if an evaluation error occurs
	 */
	private ImmutableList<Advice> evaluateAdvices(EvaluationContext context,
			Decision result)
		throws EvaluationException
	{
		ImmutableList.Builder<Advice> advices = null;
		try{
			for(int i = 0; i < adviceExpressions.size(); i++){
				AdviceExpression adviceExp = adviceExpressions.get(i);
				if(adviceExp.isApplicable(result)){
					Advice a = adviceExp.evaluate(context);
					Preconditions.checkState(a != null);
					if(advices == null){
						// At most the expressions left apply
						advices = ImmutableList.builderWithExpectedSize(adviceExpressions.size() - i);
					}
					advices.add(a);
				}
			}
			return (advices == null)?ImmutableList.of():advices.build();
		}catch(EvaluationException e){
			if(log.isDebugEnabled()){
				log.debug("Failed to evaluate decision rule advices", e);
			}
			context.setEvaluationStatus(e.getStatus());
			throw e;
		}catch(Exception e){
			Status status = Status.processingError()
			                      .message(e.getMessage())
			                      .error(e)
			                      .build();
			context.setEvaluationStatus(status);
			throw new EvaluationException(status, e);
		}
	}

	/**
	 * Evaluates obligation matching given decision
	 * {@link Decision} result
	 *
	 * @param context an evaluation context
	 * @param result an decision result
	 * @return the applicable {@link Obligation} instances, an empty immutable list if there is none
	 * @throws EvaluationException if an evaluation error occurs
	 */
	private ImmutableList<Obligation> evaluateObligations(EvaluationContext context, Decision result)
		throws EvaluationException
	{
		ImmutableList.Builder<Obligation> obligations = null;
		try{
			for(int i = 0; i < obligationExpressions.size(); i++){
				ObligationExpression obligationExp = obligationExpressions.get(i);
				if(obligationExp.isApplicable(result)){
					Obligation o = obligationExp.evaluate(context);
					Preconditions.checkState(o != null);
					if(obligations == null){
						// At most the expressions left apply
						obligations = ImmutableList.builderWithExpectedSize(obligationExpressions.size() - i);
					}
					obligations.add(o);
				}
			}
			return (obligations == null)?ImmutableList.of():obligations.build();
		}catch(EvaluationException e){
			if(log.isDebugEnabled()){
				log.debug("Failed to evaluate decision rule obligations", e);
			}
			context.setEvaluationStatus(e.getStatus());
			throw e;
		}catch(Exception e){
			if(log.isDebugEnabled()){
				log.debug("Failed to evaluate decision rule obligations", e);
			}
			Status status = Status.processingError()
			                      .message(e.getMessage())
			                      .error(e)
			                      .build();
			context.setEvaluationStatus(status);
			throw new EvaluationException(status, e);
		}
	}

	protected MoreObjects.ToStringHelper toStringBuilder(MoreObjects.ToStringHelper b){
		return b.add("id", id)
				.add("description", description)
				.add("target", target)
				.add("condition", condition)
				.add("adviceExp", adviceExpressions)
				.add("obligationExp", obligationExpressions);
	}

	protected boolean equalsTo(BaseDecisionRule r) {
		return Objects.equal(id, r.id)
			&& Objects.equal(target, r.target)
			&& Objects.equal(condition, r.condition)
			&& adviceExpressions.equals(r.adviceExpressions)
			&& obligationExpressions.equals(r.obligationExpressions)
			&& Objects.equal(description, r.description);
	}

	@Override
	public int hashCode() {
		// Arrays.hashCode of the fields in this order, without the array (design D22)
		int h = 1;
		h = 31 * h + java.util.Objects.hashCode(id);
		h = 31 * h + java.util.Objects.hashCode(target);
		h = 31 * h + java.util.Objects.hashCode(condition);
		h = 31 * h + java.util.Objects.hashCode(adviceExpressions);
		h = 31 * h + java.util.Objects.hashCode(obligationExpressions);
		h = 31 * h + java.util.Objects.hashCode(description);
		return h;
	}
	protected abstract boolean isEvaluationContextValid(EvaluationContext context);

	protected abstract static class Builder<T extends Builder<?>>
	{
		protected String id;
		protected String description;
		protected Target target;
		protected Condition condition;
		protected ImmutableList.Builder<AdviceExpression> adviceExpressions = ImmutableList.builder();
		protected ImmutableList.Builder<ObligationExpression> obligationExpressions = ImmutableList.builder();

		protected Builder(String ruleId){
			Preconditions.checkNotNull(ruleId,
					"Decision rule identifier can't be null");
			this.id = ruleId;
		}

		public T id(String id){
			Preconditions.checkNotNull(id,
					"Decision rule identifier can't be null");
			this.id = id;
			return getThis();
		}

		public T description(String desc){
			this.description = desc;
			return getThis();
		}

		public T target(Target target){
			this.target = target;
			return getThis();
		}

		public T condition(Condition condition){
			this.condition = condition;
			return getThis();
		}

		public T withoutCondition(){
			this.condition = null;
			return getThis();
		}

		public T target(Target.Builder b){
			Preconditions.checkNotNull(b);
			this.target = b.build();
			return getThis();
		}

		public T withoutTarget(){
			this.target = null;
			return getThis();
		}



		public T withoutAdvices(){
			this.adviceExpressions = ImmutableList.builder();
			return getThis();
		}

		public T withoutObligations(){
			this.obligationExpressions = ImmutableList.builder();
			return getThis();
		}

		public T advice(AdviceExpression.Builder b){
			Preconditions.checkNotNull(b);
			this.adviceExpressions.add(b.build());
			return getThis();
		}

		public T advice(Iterable<AdviceExpression> advices){
			this.adviceExpressions.addAll(advices);
			return getThis();
		}

		public T advice(AdviceExpression ... advices){
			this.adviceExpressions.add(advices);
			return getThis();
		}

		public T obligation(ObligationExpression ... obligations){
			this.obligationExpressions.add(obligations);
			return getThis();
		}

		public T obligation(Iterable<ObligationExpression> obligations){
			Preconditions.checkNotNull(obligations);
			this.obligationExpressions.addAll(obligations);
			return getThis();
		}

		public T obligation(ObligationExpression.Builder b){
			Preconditions.checkNotNull(b);
			this.obligationExpressions.add(b.build());
			return getThis();
		}

		protected abstract T getThis();
	}

}
