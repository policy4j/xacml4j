package com.artagon.xacml.v3.policy;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import javax.xml.xpath.XPath;

import org.w3c.dom.Node;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.Advice;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Obligation;

class BaseEvaluationContext implements EvaluationContext
{
	private AttributeResolver attributeService;
	
	private Collection<Advice> advices;
	private Collection<Obligation> obligations;
	
	private boolean validateAtRuntime = false;
	
	
	protected BaseEvaluationContext(AttributeResolver attributeService){
		this(false, attributeService);
	}
	
	protected BaseEvaluationContext(boolean validateFuncParams, 
			AttributeResolver attributeService){
		Preconditions.checkNotNull(attributeService);
		this.advices = new LinkedList<Advice>();
		this.obligations = new LinkedList<Obligation>();
		this.validateAtRuntime = validateFuncParams;
		this.attributeService = attributeService;
	}
	
	
	@Override
	public Node getContent(AttributeCategoryId categoryId) {
		return attributeService.getContent(categoryId);
	}

	@Override
	public boolean isValidateFuncParamAtRuntime() {
		return validateAtRuntime;
	}

	@Override
	public void addAdvices(Collection<Advice> advices) {
		Preconditions.checkNotNull(advices);
		this.advices.addAll(advices);
	}

	@Override
	public void addObligations(Collection<Obligation> obligations) {
		Preconditions.checkNotNull(obligations);
		this.obligations.addAll(obligations);
	}

	@Override
	public Collection<Advice> getAdvices() {
		return Collections.unmodifiableCollection(advices);
	}

	@Override
	public Collection<Obligation> getObligations() {
		return Collections.unmodifiableCollection(obligations);
	}
	
	/**
	 * Implementation tries to resolve give attribute
	 * via {@link AttributeResolver}. If attribute
	 * service was not specified during context creation
	 * {@link UnsupportedOperationException} will be thrown
	 * 
	 * @exception UnsupportedOperationException if attribute
	 * service was not specified during context construction
	 */
	@Override
	public BagOfAttributeValues<?> resolveAttributeDesignator(
			AttributeCategoryId category,
			String attributeId,
			AttributeValueType dataType, String issuer) {
			return attributeService.resolve(category, attributeId, dataType, issuer);
	}

	/**
	 * Implementation tries to resolve give attribute
	 * via {@link AttributeResolver}. If attribute
	 * service was not specified during context creation
	 * {@link UnsupportedOperationException} will be thrown
	 * 
	 * @exception UnsupportedOperationException if attribute
	 * service was not specified during context construction
	 */
	@Override
	public BagOfAttributeValues<?> resolveAttributeSelector(AttributeCategoryId category, 
			XPath location,
			AttributeValueType dataType) {
		return attributeService.resolve(category, location, dataType);
	}

	
	/**
	 * Implementation always
	 * return <code>null</code>
	 */
	@Override
	public EvaluationContext getParentContext() {
		return null;
	}

	/**
	 * Implementation always
	 * returns <code>null</code>
	 */
	@Override
	public Policy getCurrentPolicy() {
		return null;
	}

	/**
	 * Implementation always
	 * returns <code>null</code>
	 */
	@Override
	public PolicySet getCurrentPolicySet() {
		return null;
	}

	/**
	 * Throws {@link UnsupportedOperationException}
	 * 
	 * @exception UnsupportedOperationException
	 */
	@Override
	public Value getVariableEvaluationResult(String variableId) {
		throw new UnsupportedOperationException("Base implementation does not " +
		"support variable evaluation result caching");
	}

	/**
	 * Throws {@link UnsupportedOperationException}
	 * 
	 * @exception UnsupportedOperationException
	 */
	@Override
	public void setVariableEvaluationResult(String variableId, Value value) {
		throw new UnsupportedOperationException("Base implementation does not " +
		"support variable evaluation result caching");
	}	
}
