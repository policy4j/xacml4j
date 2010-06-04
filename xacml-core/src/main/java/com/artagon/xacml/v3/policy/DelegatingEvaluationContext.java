package com.artagon.xacml.v3.policy;

import java.util.Collection;
import java.util.TimeZone;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.PolicyIdentifier;
import com.artagon.xacml.v3.Value;
import com.artagon.xacml.v3.XPathVersion;
import com.artagon.xacml.v3.context.Advice;
import com.artagon.xacml.v3.context.Decision;
import com.artagon.xacml.v3.context.Obligation;
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
	public boolean isValidateFuncParamAtRuntime() {
		return delegate.isValidateFuncParamAtRuntime();
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

	
	public void addEvaluatedPolicy(Policy policy, Decision result) {
		delegate.addEvaluatedPolicy(policy, result);
	}

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
	public Value getVariableEvaluationResult(String variableId) {
		return delegate.getVariableEvaluationResult(variableId);
	}
	
	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public void setVariableEvaluationResult(String variableId, Value value) {
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
	
	
}
