package com.artagon.xacml.policy;

import java.util.Collection;

import javax.xml.xpath.XPath;

import org.oasis.xacml.azapi.constants.AzCategoryId;

import com.artagon.xacml.util.Preconditions;

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
	public BagOfAttributes<?> resolveAttributeDesignator(AzCategoryId category,
			String attributeId, AttributeDataType dataType,
			String issuer) {
		return delegate.resolveAttributeDesignator(category, attributeId, dataType,
				issuer);
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public BagOfAttributes<?> resolveAttributeSelector(AzCategoryId category,
			XPath location, AttributeDataType dataType) {
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
