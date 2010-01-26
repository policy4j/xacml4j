package com.artagon.xacml.v3.policy;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import javax.xml.xpath.XPath;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.Advice;
import com.artagon.xacml.v3.CategoryId;
import com.artagon.xacml.v3.Obligation;

class BaseEvaluationContext implements EvaluationContext
{
	private AttributeResolutionService attributeService;
	
	private Collection<Advice> advices;
	private Collection<Obligation> obligations;
	
	private boolean validateAtRuntime = false;
	
	protected BaseEvaluationContext(){
		this(true);
	}
	
	protected BaseEvaluationContext(boolean validate){
		this.advices = new LinkedList<Advice>();
		this.obligations = new LinkedList<Obligation>();
	}
	
	protected BaseEvaluationContext(AttributeResolutionService attributeService){
		this();
		Preconditions.checkNotNull(attributeService);
		this.attributeService = attributeService;
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
	 * via {@link AttributeResolutionService}. If attribute
	 * service was not specified during context creation
	 * {@link UnsupportedOperationException} will be thrown
	 * 
	 * @exception UnsupportedOperationException if attribute
	 * service was not specified during context construction
	 */
	@Override
	public BagOfAttributeValues<?> resolveAttributeDesignator(
			CategoryId category,
			String attributeId,
			AttributeValueType dataType, String issuer) {
		Preconditions.checkNotNull(attributeId);
		Preconditions.checkNotNull(dataType);
		Preconditions.checkNotNull(category);
		if(attributeService != null){
			return attributeService.resolve(category, attributeId, dataType, issuer);
		}
		throw new UnsupportedOperationException();
	}

	/**
	 * Implementation tries to resolve give attribute
	 * via {@link AttributeResolutionService}. If attribute
	 * service was not specified during context creation
	 * {@link UnsupportedOperationException} will be thrown
	 * 
	 * @exception UnsupportedOperationException if attribute
	 * service was not specified during context construction
	 */
	@Override
	public BagOfAttributeValues<?> resolveAttributeSelector(CategoryId category, 
			XPath location,
			AttributeValueType dataType) {
		Preconditions.checkNotNull(location);
		Preconditions.checkNotNull(dataType);
		return attributeService.resolve(category, location, dataType);
	}

	
	/**
	 * Throws {@link UnsupportedOperationException}
	 * 
	 * @exception UnsupportedOperationException
	 */
	@Override
	public EvaluationContext getParentContext() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Throws {@link UnsupportedOperationException}
	 * 
	 * @exception UnsupportedOperationException
	 */
	@Override
	public Policy getCurrentPolicy() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Throws {@link UnsupportedOperationException}
	 * 
	 * @exception UnsupportedOperationException
	 */
	@Override
	public PolicySet getCurrentPolicySet() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Throws {@link UnsupportedOperationException}
	 * 
	 * @exception UnsupportedOperationException
	 */
	@Override
	public VariableDefinition getVariableDefinition(String variableId) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Throws {@link UnsupportedOperationException}
	 * 
	 * @exception UnsupportedOperationException
	 */
	@Override
	public Value getVariableEvaluationResult(String variableId) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Throws {@link UnsupportedOperationException}
	 * 
	 * @exception UnsupportedOperationException
	 */
	@Override
	public void setVariableEvaluationResult(String variableId, Value value) {
		throw new UnsupportedOperationException();
	}	
}
