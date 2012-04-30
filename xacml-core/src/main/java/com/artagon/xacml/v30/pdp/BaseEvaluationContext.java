package com.artagon.xacml.v30.pdp;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.StatusCode;
import com.artagon.xacml.v30.spi.repository.PolicyReferenceResolver;
import com.google.common.base.Preconditions;

public abstract class BaseEvaluationContext implements EvaluationContext
{
	protected final Logger log = LoggerFactory.getLogger(BaseEvaluationContext.class);
	
	private EvaluationContextHandler contextHandler;
	private PolicyReferenceResolver resolver;
	
	private Map<String, Advice> denyAdvices;
	private Map<String, Obligation> denyObligations;
	
	private Map<String, Advice> permitAdvices;
	private Map<String, Obligation> permitObligations;
	
	private boolean validateFuncParamsAtRuntime = false;
	
	private List<CompositeDecisionRuleIDReference> evaluatedPolicies;
		
	private StatusCode evaluationStatus;
	
	private TimeZone timezone;
	private Calendar currentDateTime;
	
	private Map<AttributeDesignatorKey, BagOfAttributeExp> designCache;
	private Map<AttributeSelectorKey, BagOfAttributeExp> selectCache;
	private Map<AttributeDesignatorKey, BagOfAttributeExp> resolvedDesignators;
	
	private Integer combinedDecisionCacheTTL = null;
	
	/**
	 * Constructs evaluation context with a given attribute provider,
	 * policy resolver and
	 * @param attributeService
	 * @param policyResolver
	 */
	protected BaseEvaluationContext(
			EvaluationContextHandler attributeService, 
			PolicyReferenceResolver repository){
		this(false, 30, attributeService,  repository);
	}
	
	protected BaseEvaluationContext(
			boolean validateFuncParams, 
			int defaultDecisionCacheTTL,
			EvaluationContextHandler contextHandler,
			PolicyReferenceResolver repository){
		Preconditions.checkNotNull(contextHandler);

		Preconditions.checkNotNull(repository);
		this.denyAdvices = new LinkedHashMap<String, Advice>();
		this.denyObligations = new LinkedHashMap<String, Obligation>();
		this.permitAdvices = new LinkedHashMap<String, Advice>();
		this.permitObligations = new LinkedHashMap<String, Obligation>();
		//this.validateAtRuntime = validateFuncParams;
		this.contextHandler = contextHandler;
		this.resolver = repository;
		this.timezone = TimeZone.getTimeZone("UTC");
		this.currentDateTime = Calendar.getInstance(timezone);
		this.evaluatedPolicies = new LinkedList<CompositeDecisionRuleIDReference>();
		this.designCache = new HashMap<AttributeDesignatorKey, BagOfAttributeExp>(128);
		this.selectCache = new HashMap<AttributeSelectorKey, BagOfAttributeExp>(128);
		this.resolvedDesignators = new HashMap<AttributeDesignatorKey, BagOfAttributeExp>();
		this.combinedDecisionCacheTTL = (defaultDecisionCacheTTL > 0)?defaultDecisionCacheTTL:null;
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
		return (combinedDecisionCacheTTL == null)?0:combinedDecisionCacheTTL;
	}

	@Override
	public void setDecisionCacheTTL(int ttl) {
		if(combinedDecisionCacheTTL == null){
			this.combinedDecisionCacheTTL = ttl;
			return;
		}
		this.combinedDecisionCacheTTL = (ttl > 0)?Math.min(this.combinedDecisionCacheTTL, ttl):0;
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
	public final void addPolicyEvaluationResult(Policy policy, Decision result) {
		this.evaluatedPolicies.add(policy.getReference());
	}
	
	@Override
	public final void addPolicySetEvaluationResult(PolicySet policySet, Decision result) {
		this.evaluatedPolicies.add(policySet.getReference());
	}

	@Override
	public boolean isValidateFuncParamsAtRuntime() {
		return validateFuncParamsAtRuntime;
	}
	
	@Override
	public void setValidateFuncParamsAtRuntime(boolean validate){
		this.validateFuncParamsAtRuntime = validate;
	}

	@Override
	public void addAdvices(Decision d, Iterable<Advice> advices) 
	{
		Preconditions.checkNotNull(d);
		if(d.isIndeterminate() || 
				d == Decision.NOT_APPLICABLE){
			return;
		}
		for(Advice a : advices){
			addAndMergeAdvice(d, a);
		}
	}
	
	@Override
	public void addObligations(Decision d, Iterable<Obligation> obligations) 
	{
		Preconditions.checkNotNull(d);
		if(d.isIndeterminate() || 
				d == Decision.NOT_APPLICABLE){
			return;
		}
		for(Obligation a : obligations){
			addAndMergeObligation(d, a);
		}
	}
	
	private void addAndMergeAdvice(Decision d, Advice a)
	{
		if(log.isDebugEnabled()){
			log.debug("Adding advice=\"{}\" for a decision=\"{}\"", a, d);
		}
		Preconditions.checkArgument(d == Decision.PERMIT || d == Decision.DENY);
		Map<String, Advice> advices = (d == Decision.PERMIT)?permitAdvices:denyAdvices;
		Advice other = advices.get(a.getId());
		if(other != null){
			advices.put(a.getId(), other.merge(a));
		}else{
			advices.put(a.getId(), a);
		}
	}
	
	private void addAndMergeObligation(Decision d, Obligation a)
	{
		if(log.isDebugEnabled()){
			log.debug("Adding obligation=\"{}\" for a decision=\"{}\"", a, d);
		}
		Preconditions.checkArgument(d == Decision.PERMIT || d == Decision.DENY);
		Map<String, Obligation> obligations = (d == Decision.PERMIT)?permitObligations:denyObligations;
		Obligation other = obligations.get(a.getId());
		if(other != null){
			obligations.put(a.getId(), other.merge(a));
		}else{
			obligations.put(a.getId(), a);
		}
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
	public final BagOfAttributeExp resolve(
			AttributeDesignatorKey ref) 
		throws EvaluationException
	{
		BagOfAttributeExp v = designCache.get(ref);
		if(v != null){
			if(log.isDebugEnabled()){
				log.debug("Found designator=\"{}\" " +
						"value=\"{}\" in cache", ref, v);
			}
			return v;
		}
		v = contextHandler.resolve(this, ref);
		v = (v == null)?ref.getDataType().emptyBag():v;
		if(log.isDebugEnabled()){
			log.debug("Resolved " +
					"designator=\"{}\" to value=\"{}\"", ref, v);
		}
		this.designCache.put(ref, (v == null)?ref.getDataType().emptyBag():v);
		return v;
	}
	
	@Override
	public final BagOfAttributeExp resolve(
			AttributeSelectorKey ref)
			throws EvaluationException 
	{
		BagOfAttributeExp v = selectCache.get(ref);
		if(v != null){
			if(log.isDebugEnabled()){
				log.debug("Found selector=\"{}\" " +
						"value=\"{}\" in cache", ref, v);
			}
			return v;
		}
		v = contextHandler.resolve(this, ref);
		v = (v == null)?ref.getDataType().emptyBag():v;
		if(log.isDebugEnabled()){
			log.debug("Resolved " +
					"selector=\"{}\" to value=\"{}\"", ref, v);
		}
		this.selectCache.put(ref, (v == null)?ref.getDataType().emptyBag():v);
		return v;
	}
	
	public void setResolvedDesignatorValue(
			AttributeDesignatorKey key, 
			BagOfAttributeExp v){
		Preconditions.checkNotNull(key);
		this.resolvedDesignators.put(key, (v == null)?key.getDataType().emptyBag():v);
		this.designCache.put(key, (v == null)?key.getDataType().emptyBag():v);
	}
	
	@Override
	public Collection<CompositeDecisionRuleIDReference> getEvaluatedPolicies() {
		return Collections.unmodifiableList(evaluatedPolicies);
	}
	
	@Override
	public Map<AttributeDesignatorKey, BagOfAttributeExp> getResolvedDesignators() {
		return Collections.unmodifiableMap(resolvedDesignators);
	}
	
	@Override
	public Iterable<Obligation> getMatchingObligations(final Decision decision) {
		return (decision == Decision.PERMIT)?permitObligations.values():denyObligations.values();
	}
	
	@Override
	public Iterable<Advice> getMatchingAdvices(final Decision decision) {
		return (decision == Decision.PERMIT)?permitAdvices.values():denyAdvices.values();
	}

	/**
	 * Clears context state
	 */
	public void clear()
	{
		this.combinedDecisionCacheTTL = null;
		this.designCache.clear();
		this.selectCache.clear();
		this.evaluatedPolicies.clear();
	}
}
