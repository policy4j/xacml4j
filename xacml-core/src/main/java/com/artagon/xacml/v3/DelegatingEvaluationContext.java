package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.TimeZone;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Preconditions;

/**
 * An implementation of {@link EvaluationContext} which
 * delegates all invocation to the given context
 * 
 * @author Giedrius Trumpickas
 */
class DelegatingEvaluationContext implements EvaluationContext
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
	public boolean isValidateFuncParamsAtRuntime() {
		return delegate.isValidateFuncParamsAtRuntime();
	}
	
	@Override
	public void setValidateFuncParamsAtRuntime(boolean validate){
		delegate.setValidateFuncParamsAtRuntime(validate);
	}

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
	
	

	@Override
	public StatusCode getEvaluationStatus() {
		return delegate.getEvaluationStatus();
	}

	@Override
	public void setEvaluationStatus(StatusCode code) {
		delegate.setEvaluationStatus(code);
	}

	@Override
	public void addEvaluatedPolicy(Policy policy, Decision result) {
		delegate.addEvaluatedPolicy(policy, result);
	}

	@Override
	public void addEvaluatedPolicySet(PolicySet policySet, Decision result) {
		delegate.addEvaluatedPolicySet(policySet, result);
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
	public ValueExpression getVariableEvaluationResult(String variableId) {
		return delegate.getVariableEvaluationResult(variableId);
	}
	
	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public void setVariableEvaluationResult(String variableId, ValueExpression value) {
		delegate.setVariableEvaluationResult(variableId, value);
	}
	
	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public Policy resolve(PolicyIDReference ref) throws PolicyResolutionException 
	{
		return delegate.resolve(ref);
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public PolicySet resolve(PolicySetIDReference ref) throws PolicyResolutionException {
		return delegate.resolve(ref);
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public PolicyIDReference getCurrentPolicyIDReference() {
		return delegate.getCurrentPolicyIDReference();
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public PolicySetIDReference getCurrentPolicySetIDReference() {
		return delegate.getCurrentPolicySetIDReference();
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public TimeZone getTimeZone() {
		return delegate.getTimeZone();
	}
	
	
	
	public AttributeValue getCurrentDate() {
		return delegate.getCurrentDate();
	}

	public AttributeValue getCurrentDateTime() {
		return delegate.getCurrentDateTime();
	}

	public AttributeValue getCurrentTime() {
		return delegate.getCurrentTime();
	}

	@Override
	public XPathVersion getXPathVersion() {
		return delegate.getXPathVersion();
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public Node evaluateToNode(String path, AttributeCategoryId categoryId)
			throws EvaluationException {
		return delegate.evaluateToNode(path, categoryId);
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public NodeList evaluateToNodeSet(String path, AttributeCategoryId categoryId)
			throws EvaluationException {
		return delegate.evaluateToNodeSet(path, categoryId);
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public Number evaluateToNumber(String path, AttributeCategoryId categoryId)
			throws EvaluationException {
		return delegate.evaluateToNumber(path, categoryId);
	}
	
	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public String evaluateToString(String path, AttributeCategoryId categoryId)
			throws EvaluationException {
		return delegate.evaluateToString(path, categoryId);
	}

	@Override
	public BagOfAttributeValues<AttributeValue> resolve(
			AttributeDesignator ref)
			throws EvaluationException {
		return delegate.resolve(ref);
	}
	
	@Override
	public BagOfAttributeValues<AttributeValue> resolve(
			AttributeSelector ref)
			throws EvaluationException {
		return delegate.resolve(ref);
	}

	@Override
	public Collection<PolicyIdentifier> getEvaluatedPolicies() {
		return delegate.getEvaluatedPolicies();
	}
	
	@Override
	public AttributeResolutionScope getAttributeResolutionScope() {
		return delegate.getAttributeResolutionScope();
	}	
}
