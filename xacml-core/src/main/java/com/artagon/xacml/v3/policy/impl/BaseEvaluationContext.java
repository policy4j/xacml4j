package com.artagon.xacml.v3.policy.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.w3c.dom.Node;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.Advice;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Obligation;
import com.artagon.xacml.v3.policy.AttributeResolver;
import com.artagon.xacml.v3.policy.AttributeValue;
import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.BagOfAttributeValues;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.PolicyIDReference;
import com.artagon.xacml.v3.policy.PolicyResolutionException;
import com.artagon.xacml.v3.policy.PolicyResolver;
import com.artagon.xacml.v3.policy.PolicySet;
import com.artagon.xacml.v3.policy.PolicySetIDReference;
import com.artagon.xacml.v3.policy.Value;
import com.artagon.xacml.v3.policy.XPathProvider;

class BaseEvaluationContext implements EvaluationContext
{
	private AttributeResolver attributeService;
	private PolicyResolver policyResolver;
	
	private Collection<Advice> advices;
	private Collection<Obligation> obligations;
	
	private boolean validateAtRuntime = false;
	
	private Map<String, Map<String, Value>> variableEvaluationCache;
	
	private XPathProvider xpathProvider;
	
	protected BaseEvaluationContext(AttributeResolver attributeService, 
			PolicyResolver policyResolver, XPathProvider xpathFactory){
		this(false, attributeService, policyResolver, xpathFactory);
	}
	
	protected BaseEvaluationContext(
			boolean validateFuncParams, 
			AttributeResolver attributeService,
			PolicyResolver policyResolver, 
			XPathProvider xpathFactory){
		Preconditions.checkNotNull(attributeService);
		Preconditions.checkNotNull(policyResolver);
		Preconditions.checkNotNull(xpathFactory);
		this.advices = new LinkedList<Advice>();
		this.obligations = new LinkedList<Obligation>();
		this.validateAtRuntime = validateFuncParams;
		this.attributeService = attributeService;
		this.policyResolver = policyResolver;
		this.variableEvaluationCache = new HashMap<String, Map<String,Value>>();
		this.xpathProvider = xpathFactory;
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
	public BagOfAttributeValues<AttributeValue> resolveAttributeDesignator(
			AttributeCategoryId category,
			String attributeId,
			AttributeValueType dataType, String issuer) {
			return attributeService.resolve(category, attributeId, dataType, issuer);
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
	public PolicyIDReference getCurrentPolicyIDReference() {
		return null;
	}
	
	/**
	 * Implementation always
	 * returns <code>null</code>
	 */
	@Override
	public PolicySetIDReference getCurrentPolicySetIDReference() {
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
	
	@Override
	public final Value getVariableEvaluationResult(String variableId) 
	{
		Preconditions.checkState(getCurrentPolicy() != null);
		Policy p = getCurrentPolicy();
		Map<String, Value> policyCache = variableEvaluationCache.get(p.getId());
		if(policyCache != null){
			return policyCache.get(variableId);
		}
		return null;
	}
	
	@Override
	public final void setVariableEvaluationResult(String variableId, Value value) 
	{
		Preconditions.checkState(getCurrentPolicy() != null);
		Policy p = getCurrentPolicy();
		Map<String, Value> policyCache = variableEvaluationCache.get(p.getId());
		if(policyCache != null){
			policyCache.put(variableId, value);
			return;
		}
		policyCache = new HashMap<String, Value>();
		policyCache.put(variableId, value);
		variableEvaluationCache.put(p.getId(), policyCache);
	}

	@Override
	public Policy resolve(PolicyIDReference ref) throws PolicyResolutionException {
		return policyResolver.resolve(this, ref);
	}

	@Override
	public PolicySet resolve(PolicySetIDReference ref)
			throws PolicyResolutionException {
		return policyResolver.resolve(this, ref);
	}

	@Override
	public XPathProvider getXPathProvider() {
		return xpathProvider;
	}
}
