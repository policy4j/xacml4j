package com.artagon.xacml.v30;

import java.util.Calendar;
import java.util.Collection;
import java.util.TimeZone;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public interface EvaluationContext 
{	
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
	 * @return <code>true</code> if parameters
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
	 * Gets extended evaluation failure
	 * status information to be included
	 * in the response as {@link StatusCode}
	 * instance
	 * 
	 * @return {@link StatusCode} or 
	 * <code>null</code> if status
	 * information is unavailable
	 */
	StatusCode getEvaluationStatus();
	
	/**
	 * Sets extended evaluation failure
	 * status information to be included
	 * in the response
	 * 
	 * @param code a status code indicating
	 * evaluation failure status
	 */
	void setEvaluationStatus(StatusCode code);
	
	/**
	 * Gets parent evaluation context
	 * 
	 * @return parent evaluation context or
	 * <code>null</code>
	 */
	EvaluationContext getParentContext();
	
	/**
	 * Returns a list of all policies which were found 
	 * to be fully applicable during evaluation.
	 * 
	 * @return a collection of {@link PolicyIdentifier}
	 */
	Collection<CompositeDecisionRuleIDReference> getEvaluatedPolicies();
	
	/**
	 * Adds evaluated policy and policy 
	 * evaluation result to the context
	 * 
	 * @param policy an evaluated policy
	 * @param result a policy evaluaton result
	 */
	void addEvaluatedPolicy(Policy policy, Decision result);
	
	/**
	 * Adds evaluated policy set and policy set
	 * evaluation result to the context
	 * 
	 * @param policy an evaluated policy set
	 * @param result a policy set evaluation result
	 */
	void addEvaluatedPolicySet(PolicySet policySet, Decision result);
	
	/**
	 * Gets currently evaluated policy.
	 * If invocation returns 
	 * <code>null</code> {@link #getCurrentPolicySet()}
	 * will return NOT <code>null</code> reference
	 * to the currently evaluated policy set
	 *
	 * 
	 * @return {@link Policy} or <code>null</code>
	 */
	Policy getCurrentPolicy();
	
	/**
	 * Gets currently evaluated policy set
	 * 
	 * @return {@link PolicySet} or <code>null</code>
	 */
	PolicySet getCurrentPolicySet();
	
	/**
	 * Gets current {@link PolicyIDReference}
	 * 
	 * @return current {@link PolicyIDReference} or
	 * <code>null</code>
	 */
	PolicyIDReference getCurrentPolicyIDReference();
	
	/**
	 * Gets currently evaluated {@link PolicySetIDReference}
	 * 
	 * @return {@link PolicySetIDReference} or <code>null</code>
	 */
	PolicySetIDReference getCurrentPolicySetIDReference();
	
	/**
	 * Gets all advice instances from
	 * decision evaluated in this context
	 * 
	 * @return collection of {@link Advice}
	 * instances
	 */
	Collection<Advice> getAdvices();
	
	/**
	 * Gets all obligations from decision
	 * evaluated in this context
	 * 
	 * @return collection of {@link Obligation}
	 * instances
	 */
	Collection<Obligation> getObligations();
	
	/**
	 * Gets XPath version
	 * 
	 * @return {@link XPathVersion}
	 */
	XPathVersion getXPathVersion();
	
	/**
	 * Adds evaluated obligation from
	 * decision evaluated in this context 
	 * 
	 * @param obligations a collection of {@link Obligation}
	 */
	void addObligations(Collection<Obligation> obligations);
	
	/**
	 * Adds evaluated {@link Advice} instances to this context
	 * from decision evaluated in this context
	 * 
	 * @param advices a collection of advices
	 */
	void addAdvices(Collection<Advice> advices);
	
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
	 * to the {@link BagOfAttributeValues}
	 * 
	 * @param ref an attribute designator
	 * @return {@link BagOfAttributeValues}
	 * @throws EvaluationException if an error
	 * occurs while resolving given designator
	 */
	BagOfAttributeValues resolve(AttributeDesignatorKey ref) 
		throws EvaluationException;
	
	/**
	 * Resolves a given {@link AttributeSelectorKey}
	 * to the {@link BagOfAttributeValues}
	 * 
	 * @param ref an attribute selector
	 * @return {@link BagOfAttributeValues}
	 * @throws EvaluationException if an error
	 * occurs while resolving given selector
	 */
	BagOfAttributeValues resolve(AttributeSelectorKey ref) 
		throws EvaluationException;
	
	/**
	 * Evaluates a given XPath expression
	 * to a {@link NodeList}
	 * 
	 * @param xpath an XPath expression
	 * @param categoryId an attribute category
	 * @return {@link NodeList} representing an evaluation
	 * result
	 * @throws EvaluationException if an error
	 * occurs while evaluating given xpath
	 * expression
	 */
	NodeList evaluateToNodeSet(
			String xpath, 
			AttributeCategory categoryId) 
		throws EvaluationException;
	
	/**
	 * Evaluates a given XPath expression
	 * to a {@link String}
	 * 
	 * @param xpath an XPath expression
	 * @param categoryId an attribute category
	 * @return {@link String} representing an evaluation
	 * result
	 * @throws EvaluationException if an error
	 * occurs while evaluating given xpath
	 * expression
	 */
	String evaluateToString(
			String path, 
			AttributeCategory categoryId) 
		throws EvaluationException;
	
	/**
	 * Evaluates a given XPath expression
	 * to a {@link Node}
	 * 
	 * @param xpath an XPath expression
	 * @param categoryId an attribute category
	 * @return {@link Node} representing an evaluation
	 * result
	 * @throws EvaluationException if an error
	 * occurs while evaluating given xpath
	 * expression
	 */
	Node evaluateToNode(
			String path, 
			AttributeCategory categoryId) 
		throws EvaluationException;
	
	/**
	 * Evaluates a given XPath expression
	 * to a {@link Number}
	 * 
	 * @param xpath an XPath expression
	 * @param categoryId an attribute category
	 * @return {@link Number} representing an evaluation
	 * result
	 * @throws EvaluationException if an error
	 * occurs while evaluating given xpath
	 * expression
	 */
	Number evaluateToNumber(
			String path, 
			AttributeCategory categoryId) 
		throws EvaluationException;
	
	/**
	 * Resolves given {@link PolicyIDReference}
	 * reference
	 * 
	 * @param ref a policy reference
	 * @return resolved {@link Policy} instance
	 * @throws PolicyResolutionException if
	 * policy reference can not be resolved
	 */
	Policy resolve(PolicyIDReference ref) 
		throws PolicyResolutionException;
	
	/**
	 * Resolves given {@link PolicySetIDReference}
	 * reference
	 * 
	 * @param ref a policy reference
	 * @return resolved {@link PolicySet} instance
	 * @throws PolicyResolutionException if
	 * policy set reference can not be resolved
	 */
	PolicySet resolve(PolicySetIDReference ref) 
		throws PolicyResolutionException;
}