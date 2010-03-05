package com.artagon.xacml.v3.policy;

import java.util.Collection;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.Advice;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Obligation;


public interface EvaluationContext 
{	
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
	 * Gets  context root node from
	 * request by given attribute category
	 * identifier
	 * 
	 * @param categoryId an attribute category
	 * @return {@link Node} or <code>null</code>
	 */
	Node getContent(AttributeCategoryId categoryId);
	
	/**
	 * Gets parent evaluation context
	 * 
	 * @return parent evaluation context or
	 * <code>null</code>
	 */
	EvaluationContext getParentContext();
	
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
	
	/**
	 * Resolves attribute designator.
	 * 
	 * @param <T> a bag content type
	 * @param type a designator type
	 * @param attributeId an attribute identifier
	 * @param dataType an attribute XACML data type
	 * @param issuer an attribute issuer
	 * @return a bag containing resolved values if an attribute
	 * can not be resolved an empty bag is returned to the caller
	 */
	BagOfAttributeValues<AttributeValue> resolveAttributeDesignator(AttributeCategoryId category,
			String attributeId, AttributeValueType dataType, 
			String issuer);
	
	Policy resolve(PolicyIDReference ref) throws PolicyResolutionException;
	PolicySet resolve(PolicySetIDReference ref) throws PolicyResolutionException;
	
	/**
	 * Gets XPath factory
	 * 
	 * @return {@link XPathProvider} instance
	 */
	XPathProvider getXPathProvider();
	
}