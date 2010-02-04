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
	private PolicyInformationPoint attributeService;
	
	private Collection<Advice> advices;
	private Collection<Obligation> obligations;
	
	private boolean validateAtRuntime = false;
	
	protected BaseEvaluationContext(){
		this(true);
	}
	
	protected BaseEvaluationContext(boolean validate){
		this.advices = new LinkedList<Advice>();
		this.obligations = new LinkedList<Obligation>();
		this.validateAtRuntime = validate;
	}
	
	protected BaseEvaluationContext(PolicyInformationPoint attributeService){
		this(true);
		Preconditions.checkNotNull(attributeService);
		this.attributeService = attributeService;
	}
	
	
	@Override
	public Node getContent(AttributeCategoryId categoryId) {
		throw new UnsupportedOperationException("Operation is not implemented");
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
	 * via {@link PolicyInformationPoint}. If attribute
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
	 * via {@link PolicyInformationPoint}. If attribute
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
