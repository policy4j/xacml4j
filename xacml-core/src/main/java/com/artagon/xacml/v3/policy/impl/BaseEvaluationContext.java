package com.artagon.xacml.v3.policy.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.artagon.xacml.util.MapMaker;
import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.util.TwoKeyIndex;
import com.artagon.xacml.util.TwoKeyMapIndex;
import com.artagon.xacml.v3.Advice;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Obligation;
import com.artagon.xacml.v3.PolicyIdentifier;
import com.artagon.xacml.v3.Version;
import com.artagon.xacml.v3.policy.AttributeValue;
import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.BagOfAttributeValues;
import com.artagon.xacml.v3.policy.ContextHandler;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.PolicyIDReference;
import com.artagon.xacml.v3.policy.PolicyReferenceResolver;
import com.artagon.xacml.v3.policy.PolicyResolutionException;
import com.artagon.xacml.v3.policy.PolicySet;
import com.artagon.xacml.v3.policy.PolicySetIDReference;
import com.artagon.xacml.v3.policy.Value;

import com.artagon.xacml.v3.policy.spi.XPathEvaluationException;
import com.artagon.xacml.v3.policy.spi.XPathProvider;

abstract class BaseEvaluationContext implements EvaluationContext
{
	private ContextHandler contextHandler;
	private PolicyReferenceResolver policyResolver;
	
	private Collection<Advice> advices;
	private Collection<Obligation> obligations;
	
	private boolean validateAtRuntime = false;
	
	private TwoKeyIndex<String, String, Value> variableEvaluationCache;

	private TimeZone timezone;
	
	private List<PolicyIdentifier> evaluatedPolicies;
	
	private XPathProvider xpathProvider;
		
	/**
	 * Constructs evaluation context with a given attribute provider,
	 * policy resolver and
	 * @param attributeService
	 * @param policyResolver
	 * @param xpathFactory
	 */
	protected BaseEvaluationContext(
			ContextHandler attributeService, 
			XPathProvider xpathProvider,
			PolicyReferenceResolver policyResolver){
		this(false, attributeService, xpathProvider, policyResolver);
	}
	
	protected BaseEvaluationContext(
			boolean validateFuncParams, 
			ContextHandler attributeService,
			XPathProvider xpathProvider,
			PolicyReferenceResolver policyResolver){
		Preconditions.checkNotNull(attributeService);
		Preconditions.checkNotNull(xpathProvider);
		Preconditions.checkNotNull(policyResolver);
		this.advices = new LinkedList<Advice>();
		this.obligations = new LinkedList<Obligation>();
		this.validateAtRuntime = validateFuncParams;
		this.contextHandler = attributeService;
		this.xpathProvider = xpathProvider;
		this.policyResolver = policyResolver;
		this.variableEvaluationCache = new TwoKeyMapIndex<String, String, Value>(
				new MapMaker() {
			@Override
			public <K, V> Map<K, V> make() {
				return new HashMap<K, V>();
			}
		});
		this.timezone = TimeZone.getTimeZone("UTC");
		this.evaluatedPolicies = new LinkedList<PolicyIdentifier>();
	}
	
	@Override
	public TimeZone getTimeZone(){
		Preconditions.checkState(timezone != null);
		return timezone;
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
	public final Node evaluateToNode(String path, AttributeCategoryId categoryId)
			throws EvaluationException {
		Node content = contextHandler.getContent(categoryId);
		if(content == null){
			return null;
		}
		try{
			return xpathProvider.evaluateToNode(
					getXPathVersion(), path, content);
		}catch(XPathEvaluationException e){
			throw new com.artagon.xacml.v3.policy.XPathEvaluationException(path, this, e);
		}
	}

	@Override
	public final NodeList evaluateToNodeSet(String path, 
			AttributeCategoryId categoryId)
			throws EvaluationException 
	{
		Node content = contextHandler.getContent(categoryId);
		if(content == null){
			return null;
		}
		try{
			return xpathProvider.evaluateToNodeSet(getXPathVersion(), path, content);
		}catch(XPathEvaluationException e){
			throw new com.artagon.xacml.v3.policy.XPathEvaluationException(path, this, e);
		}
	}

	@Override
	public final Number evaluateToNumber(String path, AttributeCategoryId categoryId)
			throws EvaluationException {
		Node content = contextHandler.getContent(categoryId);
		if(content == null){
			return null;
		}
		try{
			return xpathProvider.evaluateToNumber(getXPathVersion(), path, content);
		}catch(XPathEvaluationException e){
			throw new com.artagon.xacml.v3.policy.XPathEvaluationException(path, this, e);
		}
	}

	@Override
	public final String evaluateToString(String path, AttributeCategoryId categoryId)
			throws EvaluationException {
		Node content = contextHandler.getContent(categoryId);
		if(content == null){
			return null;
		}
		try{
			return xpathProvider.evaluateToString(
					getXPathVersion(), path, content);
		}catch(XPathEvaluationException e){
			throw new com.artagon.xacml.v3.policy.XPathEvaluationException(path, this, e);
		}
	}
	
	public BagOfAttributeValues<AttributeValue> resolve(
			AttributeCategoryId categoryId, 
			String attributeId, 
			AttributeValueType dataType,
			String issuer) throws EvaluationException
	{
		return contextHandler.resolve(this, categoryId, attributeId, dataType, issuer);
	}

	@Override
	public Collection<PolicyIdentifier> getEvaluatedPolicies() {
		return Collections.unmodifiableList(evaluatedPolicies);
	}
	
	public void addApplicablePolicy(String policyID,  Version version){
		evaluatedPolicies.add(new PolicyIdentifier(policyID, version));
	}
	
}
