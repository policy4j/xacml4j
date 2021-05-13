package org.xacml4j.v30.pdp;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.TimeZone;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xacml4j.v30.Advice;
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
import org.xacml4j.v30.Status;
import org.xacml4j.v30.ValueExpression;
import org.xacml4j.v30.XPathVersion;
import org.xacml4j.v30.types.XPathExp;

import com.google.common.base.Objects;
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
	public Status getEvaluationStatus() {
		return delegate.getEvaluationStatus();
	}

	@Override
	public void setEvaluationStatus(Status code) {
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
	public Node evaluateToNode(XPathExp xpath)
			throws EvaluationException {
		return delegate.evaluateToNode(xpath);
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public NodeList evaluateToNodeSet(XPathExp xpath)
			throws EvaluationException {
		return delegate.evaluateToNodeSet(xpath);
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public Number evaluateToNumber(XPathExp xpath)
			throws EvaluationException {
		return delegate.evaluateToNumber(xpath);
	}

	/**
	 * Delegates call to {@link EvaluationContext} instance
	 */
	@Override
	public String evaluateToString(XPathExp xpath)
			throws EvaluationException {
		return delegate.evaluateToString(xpath);
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
