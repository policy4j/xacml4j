package com.artagon.xacml.v3.policy;

import java.util.Collection;

import javax.xml.xpath.XPath;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.Advice;
import com.artagon.xacml.v3.CategoryId;
import com.artagon.xacml.v3.Obligation;

/**
 * An implementation of {@link EvaluationContext} which
 * delegates all invocation to the given context
 * 
 * @author Giedrius Trumpickas
 */
public class DelegatingEvaluationContext implements EvaluationContext
{
	private EvaluationContext delegate;
	
	public DelegatingEvaluationContext(EvaluationContext context){
		Preconditions.checkNotNull(context);
		this.delegate = context;
	}
	
	@Override
	public EvaluationContext getParentContext(){
		return delegate;
	}
	
	@Override
	public boolean isValidateFuncParamAtRuntime() {
		return delegate.isValidateFuncParamAtRuntime();
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public void addAdvices(Collection<Advice> advices) {
		delegate.addAdvices(advices);
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public void addObligations(Collection<Obligation> obligations) {
		delegate.addObligations(obligations);
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public Collection<Advice> getAdvices() {
		return delegate.getAdvices();
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public Policy getCurrentPolicy() {
		return delegate.getCurrentPolicy();
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public PolicySet getCurrentPolicySet() {
		return delegate.getCurrentPolicySet();
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public Collection<Obligation> getObligations() {
		return delegate.getObligations();
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public VariableDefinition getVariableDefinition(String variableId) {
		return delegate.getVariableDefinition(variableId);
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public Value getVariableEvaluationResult(String variableId) {
		return delegate.getVariableEvaluationResult(variableId);
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public BagOfAttributeValues<?> resolveAttributeDesignator(CategoryId category,
			String attributeId, AttributeValueType dataType,
			String issuer) {
		return delegate.resolveAttributeDesignator(category, attributeId, dataType,
				issuer);
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public BagOfAttributeValues<?> resolveAttributeSelector(CategoryId category,
			XPath location, AttributeValueType dataType) {
		return delegate.resolveAttributeSelector(category, location, dataType);
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public void setVariableEvaluationResult(String variableId, Value value) {
		delegate.setVariableEvaluationResult(variableId, value);
	}
}
