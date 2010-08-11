package com.artagon.xacml.v3;

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

import com.artagon.xacml.util.MapMaker;
import com.artagon.xacml.util.TwoKeyIndex;
import com.artagon.xacml.util.TwoKeyMapIndex;
import com.artagon.xacml.v3.spi.XPathEvaluationException;
import com.artagon.xacml.v3.spi.XPathProvider;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.google.common.base.Preconditions;

public abstract class BaseEvaluationContext implements EvaluationContext
{
	private final static Logger log = LoggerFactory.getLogger(BaseEvaluationContext.class);
	
	private EvaluationContextHandler contextHandler;
	private PolicyReferenceResolver policyResolver;
	
	private Collection<Advice> advices;
	private Collection<Obligation> obligations;
	
	private boolean validateAtRuntime = false;
	
	// TODO: remove cache from root context and move
	// it to the policy or policy set level
	// optimization for case when context
	// is re-used ti evaluate large number of policies or policy sets
	private TwoKeyIndex<String, String, ValueExpression> variableEvaluationCache;

	private TimeZone timezone;
	
	private List<PolicyIdentifier> evaluatedPolicies;
	
	private XPathProvider xpathProvider;
	
	private StatusCode evaluationStatus;
			
	private AttributeValue currentTime;
	private AttributeValue currentDateTime;
	private AttributeValue currentDate;
	
	/**
	 * Constructs evaluation context with a given attribute provider,
	 * policy resolver and
	 * @param attributeService
	 * @param policyResolver
	 * @param xpathFactory
	 */
	protected BaseEvaluationContext(
			EvaluationContextHandler attributeService, 
			XPathProvider xpathProvider,
			PolicyReferenceResolver policyResolver){
		this(false, attributeService, xpathProvider, policyResolver);
	}
	
	protected BaseEvaluationContext(
			boolean validateFuncParams, 
			EvaluationContextHandler attributeService,
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
		this.variableEvaluationCache = new TwoKeyMapIndex<String, String, ValueExpression>(
				new MapMaker() {
			@Override
			public <K, V> Map<K, V> make() {
				return new HashMap<K, V>();
			}
		});
		this.timezone = TimeZone.getTimeZone("UTC");
		Calendar now = Calendar.getInstance(timezone);
		this.currentDate = XacmlDataTypes.DATE.create(now);
		this.currentDateTime = XacmlDataTypes.DATETIME.create(now);
		this.currentTime = XacmlDataTypes.TIME.create(now);
		this.evaluatedPolicies = new LinkedList<PolicyIdentifier>();
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
	public TimeZone getTimeZone(){
		Preconditions.checkState(timezone != null);
		return timezone;
	}
	
	@Override
	public AttributeValue getCurrentDate() {
		return currentDate;
	}

	@Override
	public AttributeValue getCurrentDateTime() {
		return currentDateTime;
	}

	@Override
	public AttributeValue getCurrentTime() {
		return currentTime;
	}

	@Override
	public final void addEvaluatedPolicy(Policy policy, Decision result) {
		this.evaluatedPolicies.add(policy.getPolicyIdentifier());
	}
	
	@Override
	public final void addEvaluatedPolicySet(PolicySet policySet, Decision result) {
		this.evaluatedPolicies.add(policySet.getPolicyIdentifier());
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
	public final ValueExpression getVariableEvaluationResult(String variableId) 
	{
		Policy p = getCurrentPolicy();
		Preconditions.checkState(p != null);
		return variableEvaluationCache.get(p.getId(), variableId);
	}
	
	@Override
	public final void setVariableEvaluationResult(String variableId, ValueExpression value) 
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
			throws EvaluationException 
	{
		if(log.isDebugEnabled()){
			log.debug("Evaluating xpath=\"{}\" for category=\"{}\"", path, categoryId);
		}
		Node content = contextHandler.getContent(this, categoryId);
		if(content == null){
			log.debug("Content is not available for category=\"{}\"", categoryId);
			return null;
		}
		try{
			return xpathProvider.evaluateToNode(
					getXPathVersion(), path, content);
		}catch(XPathEvaluationException e){
			log.debug("Received exception while evaluating xpath", e);
			throw new com.artagon.xacml.v3.XPathEvaluationException(path, this, e);
		}
	}

	@Override
	public final NodeList evaluateToNodeSet(String path, 
			AttributeCategoryId categoryId)
			throws EvaluationException 
	{
		if(log.isDebugEnabled()){
			log.debug("Evaluating xpath=\"{}\" for category=\"{}\"", path, categoryId);
		}
		Node content = contextHandler.getContent(this, categoryId);
		if(content == null){
			log.debug("Content is not available for category=\"{}\"", categoryId);
			return null;
		}
		try{
			return xpathProvider.evaluateToNodeSet(getXPathVersion(), path, content);
		}catch(XPathEvaluationException e){
			throw new com.artagon.xacml.v3.XPathEvaluationException(path, this, e);
		}
	}

	@Override
	public final Number evaluateToNumber(String path, AttributeCategoryId categoryId)
			throws EvaluationException {
		if(log.isDebugEnabled()){
			log.debug("Evaluating xpath=\"{}\" for category=\"{}\"", path, categoryId);
		}
		Node content = contextHandler.getContent(this, categoryId);
		if(content == null){
			log.debug("Content is not available for category=\"{}\"", categoryId);
			return null;
		}
		try{
			return xpathProvider.evaluateToNumber(getXPathVersion(), path, content);
		}catch(XPathEvaluationException e){
			throw new com.artagon.xacml.v3.XPathEvaluationException(path, this, e);
		}
	}

	@Override
	public final String evaluateToString(String path, AttributeCategoryId categoryId)
			throws EvaluationException {
		if(log.isDebugEnabled()){
			log.debug("Evaluating xpath=\"{}\" for category=\"{}\"", path, categoryId);
		}
		Node content = contextHandler.getContent(this, categoryId);
		if(content == null){
			log.debug("Content is not available for category=\"{}\"", categoryId);
			return null;
		}
		try{
			return xpathProvider.evaluateToString(
					getXPathVersion(), path, content);
		}catch(XPathEvaluationException e){
			throw new com.artagon.xacml.v3.XPathEvaluationException(path, this, e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final BagOfAttributeValues<AttributeValue> resolve(
			AttributeDesignator ref) 
		throws EvaluationException
	{
		BagOfAttributeValues<AttributeValue> v =  (BagOfAttributeValues<AttributeValue>)contextHandler.resolve(this, ref);
		if(log.isDebugEnabled()){
			log.debug("Resolved attribute " +
					"designator=\"{}\" to value=\"{}\"", ref, v);
		}
		return v;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final BagOfAttributeValues<AttributeValue> resolve(AttributeSelector ref)
			throws EvaluationException 
	{
		BagOfAttributeValues<AttributeValue> v =  (BagOfAttributeValues<AttributeValue>)contextHandler.resolve(this, ref);
		if(log.isDebugEnabled()){
			log.debug("Resolved attribute " +
					"selector=\"{}\" to value=\"{}\"", ref, v);
		}
		return v;
	}
	
	@Override
	public Collection<PolicyIdentifier> getEvaluatedPolicies() {
		return Collections.unmodifiableList(evaluatedPolicies);
	}
	
	public void addApplicablePolicy(String policyID,  Version version){
		evaluatedPolicies.add(new PolicyIdentifier(policyID, version));
	}

	@Override
	public AttributeResolutionScope getAttributeResolutionScope() {
		return AttributeResolutionScope.REQUEST_EXTERNAL;
	}	
}
