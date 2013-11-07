package org.xacml4j.v30.pdp;

import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.TimeZone;

import com.google.common.base.Objects;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.AttributeCategory;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.PolicyResolutionException;
import org.xacml4j.v30.StatusCode;
import org.xacml4j.v30.ValueExpression;
import org.xacml4j.v30.XPathVersion;

import com.google.common.base.Preconditions;
import com.google.common.base.Ticker;

/**
 * An implementation of {@link EvaluationContext} which
 * delegates all invocation to the given context
 *
 * @author Giedrius Trumpickas
 */
abstract class DelegatingEvaluationContext implements EvaluationContext
{
	private final EvaluationContext delegate;

	protected DelegatingEvaluationContext(
			EvaluationContext context){
		Preconditions.checkNotNull(context);
		this.delegate = context;
	}

	protected EvaluationContext getDelegate(){
		return delegate;
	}

	@Override
	public EvaluationContext createExtIndeterminateEvalContext() {
		return delegate.createExtIndeterminateEvalContext();
	}

	@Override
	public boolean isExtendedIndeterminateEval() {
		return delegate.isExtendedIndeterminateEval();
	}

	@Override
	public Ticker getTicker(){
		return delegate.getTicker();
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
	public void addAdvices(Decision d, Iterable<Advice> advices){
		delegate.addAdvices(d, advices);
	}

	@Override
	public void addObligations(Decision d, Iterable<Obligation> obligations){
		delegate.addObligations(d, obligations);
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public CompositeDecisionRule getCurrentPolicy() {
		return delegate.getCurrentPolicy();
	}


	@Override
	public DecisionRule getCurrentRule() {
		return delegate.getCurrentRule();
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
	public void addEvaluationResult(CompositeDecisionRule policySet, Decision result) {
		delegate.addEvaluationResult(policySet, result);
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public CompositeDecisionRule getCurrentPolicySet() {
		return delegate.getCurrentPolicySet();
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
	public CompositeDecisionRule resolve(CompositeDecisionRuleIDReference ref)
			throws PolicyResolutionException
	{
		return delegate.resolve(ref);
	}


	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public CompositeDecisionRuleIDReference getCurrentPolicyIDReference() {
		return delegate.getCurrentPolicyIDReference();
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public CompositeDecisionRuleIDReference getCurrentPolicySetIDReference() {
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
	public Calendar getCurrentDateTime() {
		return delegate.getCurrentDateTime();
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public Node evaluateToNode(String path, AttributeCategory categoryId)
			throws EvaluationException {
		return delegate.evaluateToNode(path, categoryId);
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public NodeList evaluateToNodeSet(String path, AttributeCategory categoryId)
			throws EvaluationException {
		return delegate.evaluateToNodeSet(path, categoryId);
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public Number evaluateToNumber(String path, AttributeCategory categoryId)
			throws EvaluationException {
		return delegate.evaluateToNumber(path, categoryId);
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public String evaluateToString(String path, AttributeCategory categoryId)
			throws EvaluationException {
		return delegate.evaluateToString(path, categoryId);
	}

	@Override
	public BagOfAttributeExp resolve(
			AttributeDesignatorKey ref)
			throws EvaluationException {
		return delegate.resolve(ref);
	}

	@Override
	public BagOfAttributeExp resolve(
			AttributeSelectorKey ref)
			throws EvaluationException {
		return delegate.resolve(ref);
	}

	@Override
	public Collection<CompositeDecisionRuleIDReference> getEvaluatedPolicies() {
		return delegate.getEvaluatedPolicies();
	}

	@Override
	public void setResolvedDesignatorValue(AttributeDesignatorKey ref,
			BagOfAttributeExp v) {
		delegate.setResolvedDesignatorValue(ref, v);
	}

	@Override
	public int getDecisionCacheTTL() {
		return delegate.getDecisionCacheTTL();
	}

	@Override
	public void setDecisionCacheTTL(int ttl) {
		delegate.setDecisionCacheTTL(ttl);
	}

	@Override
	public Map<AttributeDesignatorKey, BagOfAttributeExp> getResolvedDesignators() {
		return delegate.getResolvedDesignators();
	}

	@Override
	public Collection<Obligation> getMatchingObligations(Decision decision) {
		return delegate.getMatchingObligations(decision);
	}

	@Override
	public Collection<Advice> getMatchingAdvices(Decision decision) {
		return delegate.getMatchingAdvices(decision);
	}

	@Override
	public EvaluationContext getParentContext() {
		return delegate;
	}

	@Override
	public XPathVersion getXPathVersion() {
		return delegate.getXPathVersion();
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("delegate", delegate).toString();
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o == this ||
				(o instanceof DelegatingEvaluationContext) &&
						delegate.equals(((DelegatingEvaluationContext) o).getDelegate());
	}
}
