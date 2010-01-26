package com.artagon.xacml.policy;

import java.util.Collection;

import javax.xml.xpath.XPath;

import com.artagon.xacml.Advice;
import com.artagon.xacml.CategoryId;
import com.artagon.xacml.Obligation;


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
	 * Gets variable definition by given
	 * variable identifier from this context
	 * 
	 * @param variableId an variable identifier
	 * @return {@link VariableDefinition} or
	 * <code>null</code> if variable definition
	 * does not exists for given identifier
	 */
	VariableDefinition getVariableDefinition(String variableId);
	
	/**
	 * Gets variable evaluation result for given
	 * variable identifier.
	 * 
	 * @param variableId a variable identifier
	 * @return {@link Value} instance or {@code null}
	 */
	 Value getVariableEvaluationResult(String variableId);
	
	/**
	 * Sets variable evaluation result to the 
	 * current policy evaluation context.
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
	BagOfAttributeValues<?> resolveAttributeDesignator(CategoryId category,
			String attributeId, AttributeValueType dataType, 
			String issuer);
	
	/**
	 * Resolves attribute selector
	 * 
	 * @param <T>
	 * @param location an XPath expression
	 * @param dataType an attribute data type
	 * @return a bag containing resolved values if an attribute
	 * can not be resolved an empty bag is returned to the caller
	 */
	BagOfAttributeValues<?> resolveAttributeSelector(CategoryId category, 
			XPath location, AttributeValueType dataType);
}