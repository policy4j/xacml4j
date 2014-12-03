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

import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.TimeZone;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xacml4j.v30.types.XPathExp;

import com.google.common.base.Ticker;

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
	 * @return {@link EvaluationContext} to evaluate
	 * extended indeterminate
	 */
	EvaluationContext createExtIndeterminateEvalContext();

	/**
	 * Gets clock ticker
	 *
	 * @return clock ticker
	 */
	Ticker getTicker();

	/**
	 * Gets an authorization decision cache TTL,
	 * cache TTL is calculated based on
	 * the attributes used in the authorization
	 * decision caching TTLs
	 *
	 * @return a decision cache TTL in seconds
	 */
	int getDecisionCacheTTL();

	/**
	 * Sets a decision cache TTL
	 *
	 * @param ttl a new time to cache
	 * time for a decision
	 */
	void setDecisionCacheTTL(int ttl);

	/**
	 * Gets time zone used in PDP time
	 * calculations
	 *
	 * @return {@link TimeZone}
	 */
	TimeZone getTimeZone();

	/**
	 * Gets evaluation context current date/time
	 * in the evaluation time-zone
	 *
	 * @return {@link Calendar} instance
	 * in the evaluation context time-zone
	 */
	Calendar getCurrentDateTime();

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
	 * instance
	 *
	 * @return {@link Status} or
	 * {@code null} if status
	 * information is unavailable
	 */
	Status getEvaluationStatus();

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
	 * @return {@link ValueExpression} instance or {@code null}
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
	 * Resolves a given {@link AttributeDesignatorKey}
	 * to the {@link BagOfAttributeExp}
	 *
	 * @param ref an attribute designator
	 * @return {@link BagOfAttributeExp}
	 * @throws EvaluationException if an error
	 * occurs while resolving given designator
	 */
	BagOfAttributeExp resolve(AttributeDesignatorKey ref)
		throws EvaluationException;

	/**
	 * Resolves a given {@link AttributeSelectorKey}
	 * to the {@link BagOfAttributeExp}
	 *
	 * @param ref an attribute selector
	 * @return {@link BagOfAttributeExp}
	 * @throws EvaluationException if an error
	 * occurs while resolving given selector
	 */
	BagOfAttributeExp resolve(AttributeSelectorKey ref)
		throws EvaluationException;

	/**
	 * Gets all resolved designators in this context
	 *
	 * @return a map of all resolved designators
	 */
	Map<AttributeDesignatorKey, BagOfAttributeExp> getResolvedDesignators();

	/**
	 * Evaluates a given XPath expression to a {@link NodeList}
	 *
	 * @param xpath an XPath expression
	 * @return {@link NodeList} representing an evaluation result
	 * @throws EvaluationException if an error occurs while evaluating
	 * given xpath expression
	 */
	NodeList evaluateToNodeSet(XPathExp xpath)
		throws EvaluationException;

	/**
	 * Evaluates a given XPath expression to a {@link String}
	 *
	 * @param xpath an XPath expression
	 * @return {@link String} representing an evaluation result
	 * @throws EvaluationException if an error occurs while evaluating
	 * given xpath expression
	 */
	String evaluateToString(XPathExp xpath)
		throws EvaluationException;

	/**
	 * Evaluates a given XPath expression
	 * to a {@link Node}
	 *
	 * @param xpath an XPath expression
	 * @return {@link Node} representing an evaluation result
	 * @throws EvaluationException if an error occurs while evaluating
	 * given xpath expression
	 */
	Node evaluateToNode(XPathExp xpath)
		throws EvaluationException;

	/**
	 * Evaluates a given XPath expression to a {@link Number}
	 *
	 * @param xpath an XPath expression
	 * @return {@link Number} representing an evaluation result
	 * @throws EvaluationException if an error occurs while evaluating
	 * given xpath expression
	 */
	Number evaluateToNumber(XPathExp xpath)
		throws EvaluationException;

	/**
	 * Resolves given {@link CompositeDecisionRuleIDReference}
	 * reference
	 *
	 * @param ref a policy reference
	 * @return resolved {@link CompositeDecisionRule} instance
	 * @throws PolicyResolutionException if
	 * policy reference can not be resolved
	 */
	CompositeDecisionRule resolve(CompositeDecisionRuleIDReference ref)
		throws PolicyResolutionException;

}
