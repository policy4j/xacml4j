package com.artagon.xacml.v3;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.artagon.xacml.v3.spi.PolicyRepository;
import com.google.common.base.Preconditions;

public abstract class BaseEvaluationContext implements EvaluationContext
{
	private final static Logger log = LoggerFactory.getLogger(BaseEvaluationContext.class);
	
	private EvaluationContextHandler contextHandler;
	private PolicyRepository repository;
	
	private Collection<Advice> advices;
	private Collection<Obligation> obligations;
	
	private boolean validateAtRuntime = false;
	
	
	private TimeZone timezone;
	
	private List<CompositeDecisionRuleIDReference> evaluatedPolicies;
		
	private StatusCode evaluationStatus;
			
	private Calendar currentDateTime;
	
	/**
	 * Constructs evaluation context with a given attribute provider,
	 * policy resolver and
	 * @param attributeService
	 * @param policyResolver
	 */
	protected BaseEvaluationContext(
			EvaluationContextHandler attributeService, 
			PolicyRepository repository){
		this(false, attributeService,  repository);
	}
	
	protected BaseEvaluationContext(
			boolean validateFuncParams, 
			EvaluationContextHandler attributeService,
			PolicyRepository repository){
		Preconditions.checkNotNull(attributeService);

		Preconditions.checkNotNull(repository);
		this.advices = new LinkedList<Advice>();
		this.obligations = new LinkedList<Obligation>();
		this.validateAtRuntime = validateFuncParams;
		this.contextHandler = attributeService;
		this.repository = repository;
		this.timezone = TimeZone.getTimeZone("UTC");
		this.currentDateTime = Calendar.getInstance(timezone);
		this.evaluatedPolicies = new LinkedList<CompositeDecisionRuleIDReference>();
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
	public final Calendar getCurrentDateTime() {
		return currentDateTime;
	}

	@Override
	public final void addEvaluatedPolicy(Policy policy, Decision result) {
		this.evaluatedPolicies.add(policy.getReference());
	}
	
	@Override
	public final void addEvaluatedPolicySet(PolicySet policySet, Decision result) {
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
		Policy p =	repository.getPolicy(
				ref.getId(), 
				ref.getVersionMatch(), 
				ref.getEarliestVersion(), 
				ref.getLatestVersion());
		if(p == null){
			if(log.isDebugEnabled()){
				log.debug("Failed to resolve " +
						"Policy reference=\"{}\"", ref);
			}
			throw new PolicyResolutionException(this, 
					"Failed to resolve PolicySet reference, " +
					"enclosing Policy id=\"%s\"", 
					getCurrentPolicySet().getId());
		}
		return p;
	}

	@Override
	public final PolicySet resolve(PolicySetIDReference ref)
			throws PolicyResolutionException {
		PolicySet p =	repository.getPolicySet(
				ref.getId(), 
				ref.getVersionMatch(), 
				ref.getEarliestVersion(), 
				ref.getLatestVersion());
		if(p == null){
			if(log.isDebugEnabled()){
				log.debug("Failed to resolve " +
						"PolicySet reference=\"{}\"", ref);
			}
			throw new PolicyResolutionException(this, 
					"Failed to resolve PolicySet reference, " +
					"enclosing PolicySet id=\"%s\"", 
					getCurrentPolicySet().getId());
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
		BagOfAttributeValues v = contextHandler.resolve(this, ref);
		if(log.isDebugEnabled()){
			log.debug("Resolved designator=\"{}\" to value=\"{}\"", ref, v);
		}
		return (v == null)?ref.getDataType().emptyBag():v;
	}
	
	@Override
	public final BagOfAttributeValues resolve(
			AttributeSelectorKey ref)
			throws EvaluationException 
	{
		BagOfAttributeValues v = contextHandler.resolve(this, ref);
		if(log.isDebugEnabled()){
			log.debug("Resolved selector=\"{}\" to value=\"{}\"", ref, v);
		}
		return (v == null)?ref.getDataType().emptyBag():v;
	}
	
	@Override
	public Collection<CompositeDecisionRuleIDReference> getEvaluatedPolicies() {
		return Collections.unmodifiableList(evaluatedPolicies);
	}
}
