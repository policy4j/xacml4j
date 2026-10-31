package org.xacml4j.v30.spi.observe;

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

import java.util.List;
import java.util.Optional;

import org.xacml4j.v30.Advice;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.policy.ConditionResult;
import org.xacml4j.v30.policy.FunctionSpec;

/**
 * Observes the decisions of a decision point (change optimize-pdp-evaluation, design D34). The engine
 * calls it synchronously, on the deciding thread, in evaluation order, and creates no event objects.
 * <p>
 * A start callback returns the token of its span. The engine gives that token to the matching finish or
 * failure, and gives the innermost open token to every callback inside the span, so an observer keeps no
 * stack of its own. Spans nest as request, decision, evaluation (and its extended Indeterminate pass),
 * attribute, and resolver; the attributes that a resolver reads nest inside the resolver again. Every start
 * that returns gets exactly one finish or failure; a start that throws gets neither. A default start
 * returns its parent's token, for an observer that wants no span of that kind.
 * <p>
 * One observer receives the concurrent decisions of the decision point on their threads, so it must be safe
 * across decisions; the callbacks of one decision never overlap. Tokens are not null. The arguments are
 * engine objects that an observer reads during the call and does not change.
 * <p>
 * An exception of a callback fails the decision: the engine keeps the first one, makes no further start or
 * point callback for the decision, and throws it when the root policy has been evaluated, before the result
 * is built, audited, or cached. Spans that are open still get their finish or failure. An exception of a
 * failure callback is added to the propagating failure as suppressed. An exception of
 * {@link #decisionFinished} or {@link #requestFinished} is thrown by {@code decide} after audit and cache
 * publication.
 *
 * @param <S> the type of the tokens
 */
public interface DecisionObserver<S>
{
	/**
	 * The observer of a decision point that has none; the engine never calls it
	 *
	 * @return the observer that observes nothing
	 */
	static DecisionObserver<?> none(){
		return DecisionObservation.NoObserver.INSTANCE;
	}

	/**
	 * A decide call starts
	 *
	 * @param pdp the decision point
	 * @param request the request of the call
	 * @return the token of the request
	 */
	S requestStarted(PolicyDecisionPoint pdp, RequestContext request);

	/**
	 * The decide call returns its response, with every individual result, also the results that request
	 * handlers make without a decision
	 */
	default void requestFinished(S request, ResponseContext response){}

	/** The decide call throws */
	default void requestFailed(S request, Throwable failure){}

	/**
	 * An individual request reaches the decision point, before the lookup of the decision cache. Only leaf
	 * individual requests are reported.
	 *
	 * @param request the token of the request
	 * @param individualRequest the individual request
	 * @param correlation the correlation sequence of the decide call, the same for each of its decisions;
	 * {@link PolicyDecisionPoint#getCorrelationId(long)} formats it
	 * @param number the number of the decision in the decide call, from 1
	 * @return the token of the decision
	 */
	default S decisionStarted(S request, RequestContext individualRequest, long correlation, int number){
		return request;
	}

	/** The decision cache answers; no evaluation callbacks follow */
	default void decisionCacheHit(S decision, Result result){}

	/**
	 * The result of the decision, after the result is built, audited, and published to the cache. A decision
	 * gets exactly one of {@code decisionFinished} and {@link #decisionFailed}.
	 */
	default void decisionFinished(S decision, Result result){}

	/** The decision fails */
	default void decisionFailed(S decision, Throwable failure){}

	/**
	 * One evaluation of a rule, policy, policy set, or policy (set) reference starts. A reference is reported
	 * as the rule, and the policy that it references nests inside, whether its position is pinned or not.
	 */
	default S evaluationStarted(S parent, EvaluationContext context, DecisionRule rule){
		return parent;
	}

	/** The evaluation gives its decision */
	default void evaluationFinished(S evaluation, EvaluationContext context, DecisionRule rule, Decision decision){}

	/** An exception leaves the evaluation */
	default void evaluationFailed(S evaluation, EvaluationContext context, DecisionRule rule, Throwable failure){}

	/**
	 * A policy or policy set evaluates its children once more for its extended Indeterminate, after an
	 * Indeterminate target or condition, or after its advice or obligations failed. The children nest inside;
	 * their advice and obligations are evaluated, and their responses are never returned. Observers rely on
	 * these callbacks, not on {@link EvaluationContext#isExtendedIndeterminateEval()}.
	 */
	default S extendedIndeterminateStarted(S evaluation, EvaluationContext context, CompositeDecisionRule rule){
		return evaluation;
	}

	/** The extended value: Indeterminate{D}, {P}, {DP}, or NotApplicable */
	default void extendedIndeterminateFinished(S pass, EvaluationContext context, CompositeDecisionRule rule, Decision decision){}

	/** An error leaves the pass; the pass maps every exception to Indeterminate */
	default void extendedIndeterminateFailed(S pass, EvaluationContext context, CompositeDecisionRule rule, Throwable failure){}

	/**
	 * The context handler starts one resolution of a designator or selector. Every resolution of the context
	 * reaches it, memo hits included.
	 */
	default S attributeRequested(S parent, EvaluationContext context, AttributeReferenceKey key){
		return parent;
	}

	/**
	 * The outcome of the resolution
	 *
	 * @param source where the outcome comes from
	 * @param value the bag, or empty for absence; MustBePresent is applied later and is not part of the outcome
	 */
	default void attributeResolved(S attribute, EvaluationContext context, AttributeReferenceKey key,
			AttributeSource source, Optional<BagOfValues> value){}

	/** The resolution throws: a mapped processing error, or a selector path syntax error */
	default void attributeFailed(S attribute, EvaluationContext context, AttributeReferenceKey key, Throwable failure){}

	/**
	 * The information point asks one matching resolver for the key: one candidate of its fallback loop. The
	 * functions of a parallel resolver run on other threads and report nothing.
	 */
	default S resolverStarted(S parent, EvaluationContext context, String resolverId, AttributeReferenceKey key){
		return parent;
	}

	/** The answer of the resolver comes from the cache of the information point, and the resolver is not called */
	default void resolverCacheHit(S resolver, EvaluationContext context, String resolverId, AttributeReferenceKey key){}

	/**
	 * The answer of the resolver for the key: the bag of the requested attribute, or the bag that the selector
	 * selects from the content; empty means no value, and the next resolver is asked
	 */
	default void resolverFinished(S resolver, EvaluationContext context, String resolverId, AttributeReferenceKey key,
			Optional<BagOfValues> value){}

	/**
	 * The resolver fails, with the mapped resolution failure; a selector path error on the content of the
	 * resolver counts too
	 */
	default void resolverFailed(S resolver, EvaluationContext context, String resolverId, AttributeReferenceKey key,
			Throwable failure){}

	/**
	 * The result of one target match, after the engine has mapped a failure and set the status. An element
	 * without a target matches, and is reported too.
	 */
	default void targetMatched(S current, EvaluationContext context, DecisionRule rule, MatchResult result){}

	/**
	 * The result of a condition, after a failure is mapped to Indeterminate. A missing condition is True without
	 * evaluation and is not reported.
	 */
	default void conditionEvaluated(S current, EvaluationContext context, DecisionRule rule, ConditionResult result){}

	/**
	 * A policy set or policy gives its combining algorithm a prepared or indexed candidate list instead of
	 * its original children; not reported when the algorithm gets the original children
	 */
	default void childrenSelected(S current, EvaluationContext context, CompositeDecisionRule rule,
			List<? extends DecisionRule> selected){}

	/**
	 * A function call fails in its implementation, or is rejected: a type error, a dynamic type that a type
	 * assertion rejects, or a failed validation at runtime. The failure is the exception that the call
	 * propagates. The failure of an argument is reported where it happens.
	 */
	default void functionFailed(S current, EvaluationContext context, FunctionSpec function, Throwable failure){}

	/**
	 * The advice and obligations of an element for its decision were evaluated and staged; reported only when
	 * there is at least one. Publication to an enclosing policy set is not reported again.
	 */
	default void responsesStaged(S current, EvaluationContext context, DecisionRule rule, Decision decision,
			List<Advice> advice, List<Obligation> obligations){}
}
