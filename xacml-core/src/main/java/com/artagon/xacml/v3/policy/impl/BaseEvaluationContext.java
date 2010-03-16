package com.artagon.xacml.v3.policy.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.TimeZone;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.util.TwoKeyHashIndex;
import com.artagon.xacml.util.TwoKeyIndex;
import com.artagon.xacml.v3.Advice;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Obligation;
import com.artagon.xacml.v3.policy.AttributeDesignator;
import com.artagon.xacml.v3.policy.AttributeReferenceResolver;
import com.artagon.xacml.v3.policy.AttributeSelector;
import com.artagon.xacml.v3.policy.AttributeValue;
import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.BagOfAttributeValues;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.PolicyIDReference;
import com.artagon.xacml.v3.policy.PolicyResolutionException;
import com.artagon.xacml.v3.policy.DecisionRuleReferenceResolver;
import com.artagon.xacml.v3.policy.PolicySet;
import com.artagon.xacml.v3.policy.PolicySetIDReference;
import com.artagon.xacml.v3.policy.Value;
import com.artagon.xacml.v3.policy.XPathEvaluationException;
import com.artagon.xacml.v3.policy.spi.XPathProvider;

class BaseEvaluationContext implements EvaluationContext
{
	private AttributeReferenceResolver attributeProvider;
	private DecisionRuleReferenceResolver policyResolver;
	
	private Collection<Advice> advices;
	private Collection<Obligation> obligations;
	
	private boolean validateAtRuntime = false;
	
	private TwoKeyIndex<String, String, Value> variableEvaluationCache;
	
	private XPathProvider xpathProvider;
	private TimeZone timezone;
		
	/**
	 * Constructs evaluation context with a given attribute provider,
	 * policy resolver and
	 * @param attributeService
	 * @param policyResolver
	 * @param xpathFactory
	 */
	protected BaseEvaluationContext(AttributeReferenceResolver attributeService, 
			DecisionRuleReferenceResolver policyResolver, XPathProvider xpathFactory){
		this(false, attributeService, policyResolver, xpathFactory);
	}
	
	protected BaseEvaluationContext(
			boolean validateFuncParams, 
			AttributeReferenceResolver attributeService,
			DecisionRuleReferenceResolver policyResolver, 
			XPathProvider xpathFactory){
		Preconditions.checkNotNull(attributeService);
		Preconditions.checkNotNull(policyResolver);
		Preconditions.checkNotNull(xpathFactory);
		this.advices = new LinkedList<Advice>();
		this.obligations = new LinkedList<Obligation>();
		this.validateAtRuntime = validateFuncParams;
		this.attributeProvider = attributeService;
		this.policyResolver = policyResolver;
		this.variableEvaluationCache = new TwoKeyHashIndex<String, String, Value>();
		this.xpathProvider = xpathFactory;
		this.timezone = TimeZone.getTimeZone("UTC");
	}
	
	@Override
	public TimeZone getTimeZone(){
		Preconditions.checkState(timezone != null);
		return timezone;
	}
	
	@Override
	public Node getContent(AttributeCategoryId categoryId) {
		return attributeProvider.getContent(categoryId);
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
	 * via {@link AttributeReferenceResolver}. If attribute
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
			return attributeProvider.resolve(category, attributeId, dataType, issuer);
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
		Policy p = getCurrentPolicy();
		Preconditions.checkState(p != null);
		return variableEvaluationCache.get(p.getId(), variableId);
	}
	
	@Override
	public final void setVariableEvaluationResult(String variableId, Value value) 
	{
		Policy p = getCurrentPolicy();
		Preconditions.checkState(p != null);
		variableEvaluationCache.put(p.getId(), variableId, value);
	}

	@Override
	public final Policy resolve(PolicyIDReference ref) throws PolicyResolutionException {
		return policyResolver.resolve(this, ref);
	}

	@Override
	public final PolicySet resolve(PolicySetIDReference ref)
			throws PolicyResolutionException {
		return policyResolver.resolve(this, ref);
	}

	@Override
	public final Node evaluateToNode(String path, Node context)
			throws XPathEvaluationException {
		try{
			return xpathProvider.evaluateToNode(path, context);
		}
		catch(com.artagon.xacml.v3.policy.spi.XPathEvaluationException e){
			throw new XPathEvaluationException(path, this, e);
		}
	}

	@Override
	public final NodeList evaluateToNodeSet(String path, Node context)
			throws XPathEvaluationException 
	{
		try{
			return xpathProvider.evaluateToNodeSet(path, context);
		}catch(com.artagon.xacml.v3.policy.spi.XPathEvaluationException e){
			throw new XPathEvaluationException(path, this, e);
		}
	}

	@Override
	public final Number evaluateToNumber(String path, Node context)
			throws XPathEvaluationException {
		try{
			return xpathProvider.evaluateToNumber(path, context);
		}catch(com.artagon.xacml.v3.policy.spi.XPathEvaluationException e){
			throw new XPathEvaluationException(path, this, e);
		}
	}

	@Override
	public final String evaluateToString(String path, Node context)
			throws XPathEvaluationException {
		try{
			return xpathProvider.evaluateToString(path, context);
		}catch(com.artagon.xacml.v3.policy.spi.XPathEvaluationException e){
			throw new XPathEvaluationException(path, this, e);
		}
	}

	@Override
	public BagOfAttributeValues<AttributeValue> resolve(
			AttributeDesignator designator) throws EvaluationException {
		return null;
	}

	@Override
	public BagOfAttributeValues<AttributeValue> resolve(
			AttributeSelector selector) throws EvaluationException {
		return null;
	}
}
