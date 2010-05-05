package com.artagon.xacml.v3.policy;

import java.util.Collection;
import java.util.TimeZone;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.artagon.xacml.v3.Advice;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Obligation;
import com.artagon.xacml.v3.PolicyIdentifier;


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
	 * Tests if function parameters
	 * need to be validate at runtime
	 * every time function is invoked
	 * 
	 * @return <code>true</code> if parameters
	 * need to be validated at runtime
	 */
	boolean isValidateFuncParamAtRuntime();
		
	/**
	 * Gets parent evaluation context
	 * 
	 * @return parent evaluation context or
	 * <code>null</code>
	 */
	EvaluationContext getParentContext();
	
	/**
	 * Returns a list of all policies which were found 
	 * to be fully applicable during evaluation
	 * 
	 * @return
	 */
	Collection<PolicyIdentifier> getEvaluatedPolicies();
	
	/**
	 * Gets currently evaluated policy
	 * 
	 * @return {@link Policy}
	 */
	Policy getCurrentPolicy();
	
	/**
	 * Gets currently evaluated policy set
	 * 
	 * @return {@link PolicySet}
	 */
	PolicySet getCurrentPolicySet();
	
	/**
	 * Gets current {@link PolicyIDReference}
	 * 
	 * @return current {@link PolicyIDReference}
	 */
	PolicyIDReference getCurrentPolicyIDReference();
	
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
	 * Adds evaluated advice instances to this context
	 * from decision evaluated in this context
	 * 
	 * @param advices
	 */
	void addAdvices(Collection<Advice> advices);
	
	/**
	 * Gets variable evaluation result for given
	 * variable identifier.
	 * 
	 * @param variableId a variable identifier
	 * @return {@link Value} instance or {@code null}
	 */
	 Value getVariableEvaluationResult(String variableId);
	
	/**
	 * Caches current policy variable evaluation result.
	 * 
	 * @param variableId a variable identifier
	 * @param value a variable value
	 */
	void setVariableEvaluationResult(String variableId, Value value);
	
	
	BagOfAttributeValues<? extends AttributeValue> resolve(AttributeDesignator ref) 
		throws EvaluationException;
	
	BagOfAttributeValues<? extends AttributeValue> resolve(AttributeSelector ref) 
		throws EvaluationException;
	

	NodeList evaluateToNodeSet(
			String xpath, 
			AttributeCategoryId categoryId) 
		throws EvaluationException;
	
	
	String evaluateToString(
			String path, 
			AttributeCategoryId categoryId) 
		throws EvaluationException;
	
	Node evaluateToNode(
			String path, 
			AttributeCategoryId categoryId) 
		throws EvaluationException;
	
	Number evaluateToNumber(
			String path, 
			AttributeCategoryId categoryId) 
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
	Policy resolve(PolicyIDReference ref) throws PolicyResolutionException;
	
	/**
	 * Resolves given {@link PolicySetIDReference}
	 * reference
	 * 
	 * @param ref a policy reference
	 * @return resolved {@link PolicySet} instance
	 * @throws PolicyResolutionException if
	 * policy set reference can not be resolved
	 */
	PolicySet resolve(PolicySetIDReference ref) throws PolicyResolutionException;
	
	
}