package org.xacml4j.v30;

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

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;

/**
 * Evaluation context for {@link CompositeDecisionRule} evaluations
 *
 * @see {@link CompositeDecisionRule#createContext(EvaluationContext)}
 * @see {@link CompositeDecisionRule#evaluate(EvaluationContext)}
 */
public interface EvaluationContext
{
	/**
	 * Indicates that evaluation is performed
	 * just to determine extended indeterminate
	 * and there is no need to evaluate advice and
	 * obligations in the decision rules
	 *
	 * @return {@code true} if context
	 * was build to evaluate extended indeterminate
	 */
	boolean isExtendedIndeterminateEval();

	/**
	 * Creates an evaluation context to evaluate
	 * policy tree for extended indeterminate
	 *
	 * @see {@link <a href="http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html#_Toc325047187"/a> }
	 * @return {@link EvaluationContext} to evaluate
	 * extended indeterminate
	 */
	EvaluationContext createExtIndeterminateEvalContext();

	/**
	 * Gets context clock
	 *
	 * @return clock ticker
	 */
	default Clock getClock(){
		return Clock.systemUTC();
	}

	/**
	 * Gets an authorization decision cache TTL,
	 * cache TTL is calculated based on
	 * the attributes used in the authorization
	 * decision caching TTLs
	 *
	 * @return a decision cache TTL as {@link Duration}
	 */
	Duration getDecisionCacheTTL();

	/**
	 * Sets a decision cache TTL
	 *
	 * @param ttl a new time to cache
	 * time for a decision
	 */
	default void setDecisionCacheTTL(int ttl){
		setDecisionCacheTTL(Duration.ofSeconds(ttl));
	}

	/**
	 * Sets a decision cache TTL
	 *
	 * @param ttl a new time to cache
	 * time for a decision
	 */
	void setDecisionCacheTTL(Duration ttl);

	/**
	 * Gets time zone used in PDP time
	 * calculations
	 *
	 * @return {@link TimeZone}
	 */
	default TimeZone getTimeZone(){
		return TimeZone.getTimeZone(getClock().getZone());
	}

	/**
	 * Gets evaluation context current date/time
	 * in the evaluation time-zone
	 *
	 * @return {@link ZonedDateTime} defaultProvider
	 * in the evaluation context time-zone
	 */
	default ZonedDateTime getCurrentDateTime(){
		return ZonedDateTime.now(getClock());
	}

	/**
	 * Tests if function parameters
	 * need to be validate at runtime
	 * every time function is invoked
	 *
	 * @return {@code true} if parameters
	 * need to be validated at runtime
	 */
	boolean isValidateFuncParamsAtRuntime();

	/**
	 * Enables/Disables function parameters validation
	 * at runtime
	 *
	 * @param validate a flag to validate
	 */
	void setValidateFuncParamsAtRuntime(boolean validate);

	/**
	 * Gets evaluation status information to be
	 * included in the response as {@link StatusCode}
	 * defaultProvider
	 *
	 * @return {@link Status} or
	 * {@code null} if status
	 * information is unavailable
	 */
	Optional<Status> getEvaluationStatus();

	/**
	 * Sets extended evaluation failure
	 * status information to be included
	 * in the response
	 *
	 * @param code a status code indicating
	 * evaluation failure status
	 */
	void setEvaluationStatus(Status code);


	/**
	 * Gets parent evaluation context
	 *
	 * @return parent evaluation context or {@code null}
	 */
	EvaluationContext getParentContext();

	/**
	 * Returns a list of all policies which were found
	 * to be fully applicable during evaluation.
	 *
	 * @return a collection of {@link CompositeDecisionRuleIDReference}
	 */
	Collection<CompositeDecisionRuleIDReference> getEvaluatedPolicies();

	/**
	 * Adds evaluated policy or policy set
	 * evaluation result to the context
	 *
	 * @param policy an evaluated policy or policy set
	 * @param result a policy or policy set evaluation result
	 */
	void addEvaluationResult(CompositeDecisionRule policy, Decision result);

	/**
	 * Gets currently evaluated policy.
	 * If invocation returns
	 * {@code null}, {@link EvaluationContext#getCurrentPolicySet()}
	 * will return NOT {@code null} reference
	 * to the currently evaluated policy set
	 *
	 *
	 * @return {@link CompositeDecisionRule} or {@code null}
	 */
	CompositeDecisionRule getCurrentPolicy();

	/**
	 * Gets currently evaluated policy set
	 *
	 * @return {@link CompositeDecisionRule} or {@code null}
	 */
	CompositeDecisionRule getCurrentPolicySet();

	/**
	 * Gets current rule
	 *
	 * @return {@link DecisionRule} or {@code null}
	 */
	DecisionRule getCurrentRule();

	/**
	 * Gets current {@link CompositeDecisionRuleIDReference}
	 *
	 * @return current {@link CompositeDecisionRuleIDReference} or
	 * {@code null}
	 */
	CompositeDecisionRuleIDReference getCurrentPolicyIDReference();

	/**
	 * Gets currently evaluated {@link CompositeDecisionRuleIDReference}
	 *
	 * @return {@link CompositeDecisionRuleIDReference} or {@code null}
	 */
	CompositeDecisionRuleIDReference getCurrentPolicySetIDReference();

	/**
	 * Gets XPath version
	 *
	 * @return {@link XPathVersion}
	 */
	XPathVersion getXPathVersion();

	/**
	 * Adds evaluated {@link Advice} matching
	 * given access decision
	 *
	 * @param d an access decision
	 */
	void addAdvices(Decision d, Iterable<Advice> advices);

	/**
	 * Adds evaluated {@link Obligation} matching
	 * given access decision
	 *
	 * @param d an access decision
	 */
	void addObligations(Decision d, Iterable<Obligation> obligations);

	/**
	 * Gets obligations matching given decision
	 *
	 * @param decision an access decision
	 * @return matching obligations
	 */
	Collection<Obligation> getMatchingObligations(Decision decision);

	/**
	 * Gets advices matching given decision
	 *
	 * @param decision an access decision
	 * @return matching advices
	 */
	Collection<Advice> getMatchingAdvices(Decision decision);

	/**
	 * Gets variable evaluation result for given
	 * variable identifier.
	 *
	 * @param variableId a variable identifier
	 * @return {@link ValueExpression} defaultProvider or {@code null}
	 */
	 ValueExpression getVariableEvaluationResult(String variableId);

	/**
	 * Caches current policy variable evaluation result.
	 *
	 * @param variableId a variable identifier
	 * @param value a variable value
	 */
	void setVariableEvaluationResult(String variableId, ValueExpression value);

	/**
	 * Gets all resolved designators in this context
	 *
	 * @return a map of all resolved designators
	 */
	Map<AttributeDesignatorKey, BagOfAttributeValues> getResolvedDesignators();

	/**
	 * Gets all resolved selectors in this context
	 *
	 * @return a map of all resolved designators
	 */
	Map<AttributeSelectorKey, BagOfAttributeValues> getResolvedSelectors();

	/**
	 * Resolves a given {@link AttributeSelectorKey} or {@link AttributeDesignatorKey}
	 * to the {@link BagOfAttributeValues}
	 *
	 * @param ref an attribute selector
	 * @return {@link BagOfAttributeValues}
	 * @throws EvaluationException if an error
	 * occurs while resolving given selector
	 */
	Optional<BagOfAttributeValues> resolve(AttributeReferenceKey ref)
		throws EvaluationException;

	/**
	 * Resolves {@link CategoryId} and {@link Content.Type}
	 * to optional content of the given category and type
	 *
	 * @param categoryId an optional category identifier
	 * @param type a content type
	 * @param <C>
	 * @return optional content {@link Content} of the given category and type
	 */
	<C extends Content> Optional<C> resolve(Optional<CategoryId> categoryId, Content.Type type);


	default Collection<CompositeDecisionRuleIDReference> getReferencedCompositeDecisionRules(){
		return getReferencedCompositeDecisionRules(this, new LinkedList<>());
	}

	default Collection<CompositeDecisionRuleIDReference> getReferencedCompositeDecisionRules(EvaluationContext context,
			Collection<CompositeDecisionRuleIDReference> rules){
		if(context == null){
			return rules;
		}
		rules.add(context.getCurrentPolicy().getReference());
		return getReferencedCompositeDecisionRules(context.getParentContext(), rules);
	}

	/**
	 * Resolves given {@link CompositeDecisionRuleIDReference}
	 * reference
	 *
	 * @param ref a policy reference
	 * @return resolved {@link CompositeDecisionRule} defaultProvider
	 * @throws PolicyResolutionException if
	 * policy reference can not be resolved
	 */
	CompositeDecisionRule resolve(CompositeDecisionRuleIDReference ref)
		throws PolicyResolutionException;

	/**
	 * Resolves a given {@link AttributeSelectorKey} or {@link AttributeDesignatorKey}
	 * asynchronously to a an {@kink Optional} with {@link BagOfAttributeValues}
	 *
	 * @param ref an attribute selector
	 * @return {@link CompletableFuture}
	 * @throws EvaluationException if an error
	 * occurs while resolving given selector
	 */
	default CompletableFuture<Optional<BagOfAttributeValues>> resolveAsync(AttributeReferenceKey ref)
			throws EvaluationException
	{
		return CompletableFuture.completedFuture(resolve(ref));
	}

	/**
	 * Resolves {@link CategoryId} and {@link Content.Type} asynchronously
	 * to an optional with ontent of the given category and type
	 *
	 * @param categoryId an optional category identifier
	 * @param type a content type
	 * @param <C>
	 * @return optional content {@link Content} of the given category and type
	 */
	default <C extends Content> CompletableFuture<Optional<C>> resolveAsync(Optional<CategoryId> categoryId, Content.Type type)
			throws EvaluationException
	{
		return CompletableFuture.completedFuture(resolve(categoryId, type));
	}

	/**
	 * Resolves given {@link CompositeDecisionRuleIDReference}
	 * reference
	 *
	 * @param ref a policy reference
	 * @return resolved {@link CompositeDecisionRule} defaultProvider
	 * @throws PolicyResolutionException if
	 * policy reference can not be resolved
	 */
	default CompletableFuture<CompositeDecisionRule> resolveAsync(CompositeDecisionRuleIDReference ref)
			throws PolicyResolutionException
	{
		return CompletableFuture.completedFuture(resolve(ref));
	}

}
