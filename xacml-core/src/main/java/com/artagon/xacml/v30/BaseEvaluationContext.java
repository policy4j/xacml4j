package com.artagon.xacml.v30;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.artagon.xacml.v30.spi.repository.PolicyReferenceResolver;
import com.google.common.base.Preconditions;

public abstract class BaseEvaluationContext implements EvaluationContext
{
	private final static Logger log = LoggerFactory.getLogger(BaseEvaluationContext.class);
	
	private EvaluationContextHandler contextHandler;
	private PolicyReferenceResolver resolver;
	
	private Collection<Advice> advices;
	private Collection<Obligation> obligations;
	
	private boolean validateAtRuntime = false;
	
	private TimeZone timezone;
	
	private List<CompositeDecisionRuleIDReference> evaluatedPolicies;
		
	private StatusCode evaluationStatus;
	private Calendar currentDateTime;
	
	private Map<AttributeDesignatorKey, BagOfAttributeValues> designCache;
	private Map<AttributeSelectorKey, BagOfAttributeValues> selectCache;
	
	private Integer decisionCacheTTL = null;
	
	/**
	 * Constructs evaluation context with a given attribute provider,
	 * policy resolver and
	 * @param attributeService
	 * @param policyResolver
	 */
	protected BaseEvaluationContext(
			EvaluationContextHandler attributeService, 
			PolicyReferenceResolver repository){
		this(false, attributeService,  repository);
	}
	
	protected BaseEvaluationContext(
			boolean validateFuncParams, 
			EvaluationContextHandler contextHandler,
			PolicyReferenceResolver repository){
		Preconditions.checkNotNull(contextHandler);

		Preconditions.checkNotNull(repository);
		this.advices = new LinkedList<Advice>();
		this.obligations = new LinkedList<Obligation>();
		//this.validateAtRuntime = validateFuncParams;
		this.contextHandler = contextHandler;
		this.resolver = repository;
		this.timezone = TimeZone.getTimeZone("UTC");
		this.currentDateTime = Calendar.getInstance(timezone);
		this.evaluatedPolicies = new LinkedList<CompositeDecisionRuleIDReference>();
		this.designCache = new HashMap<AttributeDesignatorKey, BagOfAttributeValues>(128);
		this.selectCache = new HashMap<AttributeSelectorKey, BagOfAttributeValues>(128);
	}
	
	@Override
	public StatusCode getEvaluationStatus() {
		return evaluationStatus;
	}
	
	@Override
	public void setEvaluationStatus(StatusCode status){
		this.evaluationStatus = status;
	}
	
	@Override
	public int getDecisionCacheTTL() {
		return (decisionCacheTTL == null)?0:decisionCacheTTL;
	}

	@Override
	public void setDecisionCacheTTL(int ttl) {
		if(decisionCacheTTL == null){
			this.decisionCacheTTL = ttl;
			return;
		}
		this.decisionCacheTTL = (ttl > 0)?Math.min(this.decisionCacheTTL, ttl):0;
	}

	@Override
	public TimeZone getTimeZone(){
		Preconditions.checkState(timezone != null);
		return timezone;
	}
	
	@Override
	public final Calendar getCurrentDateTime() {
		return currentDateTime;
	}

	@Override
	public final void addEvaluatedApplicablePolicy(Policy policy, Decision result) {
		this.evaluatedPolicies.add(policy.getReference());
	}
	
	@Override
	public final void addEvaluatedApplicablePolicySet(PolicySet policySet, Decision result) {
		this.evaluatedPolicies.add(policySet.getReference());
	}

	@Override
	public boolean isValidateFuncParamsAtRuntime() {
		return validateAtRuntime;
	}
	
	@Override
	public void setValidateFuncParamsAtRuntime(boolean validate){
		this.validateAtRuntime = validate;
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
	public final ValueExpression getVariableEvaluationResult(
			String variableId){
		return null;
	}
	
	@Override
	public final void setVariableEvaluationResult(String variableId, ValueExpression value) {
	}

	@Override
	public final Policy resolve(PolicyIDReference ref) 
		throws PolicyResolutionException {
		Policy p =	resolver.resolve(ref);
		if(log.isDebugEnabled()){
			log.debug("Trying to resolve " +
					"Policy reference=\"{}\"", ref);
		}
		if(p == null){			
			throw new PolicyResolutionException(this, 
					"Failed to resolve PolicySet reference");
		}
		return p;
	}

	@Override
	public final PolicySet resolve(PolicySetIDReference ref)
			throws PolicyResolutionException {
		PolicySet p = resolver.resolve(ref);
		if(log.isDebugEnabled()){
			log.debug("Trying to resolve " +
					"PolicySet reference=\"{}\"", ref);
		}
		if(p == null){
			throw new PolicyResolutionException(this, 
					"Failed to resolve PolicySet reference");
		}
		return p;
	}

	
	@Override
	public final Node evaluateToNode(String path, AttributeCategory categoryId)
			throws EvaluationException 
	{
		return contextHandler.evaluateToNode(this, path, categoryId);
	}

	@Override
	public final NodeList evaluateToNodeSet(String path, 
			AttributeCategory categoryId)
			throws EvaluationException 
	{
		return contextHandler.evaluateToNodeSet(this, path, categoryId);
	}

	@Override
	public final Number evaluateToNumber(String path, AttributeCategory categoryId)
			throws EvaluationException {
		return contextHandler.evaluateToNumber(this, path, categoryId);
	}

	@Override
	public final String evaluateToString(String path, AttributeCategory categoryId)
			throws EvaluationException 
	{
		return contextHandler.evaluateToString(this, path, categoryId);
	}
	
	@Override
	public final BagOfAttributeValues resolve(
			AttributeDesignatorKey ref) 
		throws EvaluationException
	{
		BagOfAttributeValues v = designCache.get(ref);
		if(v != null){
			if(log.isDebugEnabled()){
				log.debug("Found designator=\"{}\" " +
						"value=\"{}\" in cache", ref, v);
			}
			return v;
		}
		v = contextHandler.resolve(this, ref);
		v = (v == null)?ref.getDataType().emptyBag():v;
		// cache resolved designator
		// value internally
		setDesignatorValue(ref, v);
		return v;
	}
	
	@Override
	public final BagOfAttributeValues resolve(
			AttributeSelectorKey ref)
			throws EvaluationException 
	{
		BagOfAttributeValues v = selectCache.get(ref);
		if(v != null){
			if(log.isDebugEnabled()){
				log.debug("Found selector=\"{}\" " +
						"value=\"{}\" in cache", ref, v);
			}
			return v;
		}
		v = contextHandler.resolve(this, ref);
		v = (v == null)?ref.getDataType().emptyBag():v;
		// cache resolved selector
		// value internally
		setSelectorValue(ref, v);
		return v;
	}
	
	public void setDesignatorValue(
			AttributeDesignatorKey key, 
			BagOfAttributeValues v){
		Preconditions.checkNotNull(key);
		this.designCache.put(key, (v == null)?key.getDataType().emptyBag():v);
	}
	
	public void setSelectorValue(
			AttributeSelectorKey key, 
			BagOfAttributeValues v){
		Preconditions.checkNotNull(key);
		this.selectCache.put(key, (v == null)?key.getDataType().emptyBag():v);
	}
	
	@Override
	public Collection<CompositeDecisionRuleIDReference> getEvaluatedPolicies() {
		return Collections.unmodifiableList(evaluatedPolicies);
	}
}
