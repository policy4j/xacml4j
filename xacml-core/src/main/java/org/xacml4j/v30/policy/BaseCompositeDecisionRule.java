package org.xacml4j.v30.policy;

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

import java.util.Collection;

import org.xacml4j.v30.Advice;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.Versionable;
import org.xacml4j.v30.spi.observe.DecisionObservation;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * A base class for composite decision rule. A composite decision
 * rule is a rule which contains other rules combined via decision
 * combining algorithm
 *
 * @author Giedrius Trumpickas
 */
abstract class BaseCompositeDecisionRule extends BaseDecisionRule
	implements CompositeDecisionRule, Versionable
{
	private final Version version;
	private final Entity policyIssuer;
	private final Integer maxDelegationDepth;
	private final Multimap<String, CombinerParameter> combinerParameters;

	protected BaseCompositeDecisionRule(Builder<?> b){
		super(b);
		this.version = b.version;
		this.maxDelegationDepth = b.maxDelegationDepth;
		this.policyIssuer = b.issuer;
		this.combinerParameters = b.combParamBuilder.build();
	}

	@Override
	public  Version getVersion() {
		return version;
	}

	/**
	 * Gets this rule issuer attributes
	 *
	 * @return this rule issuer attributes
	 */
	public Entity getIssuer(){
		return policyIssuer;
	}

	/**
	 * Gets composite decision rule combiner parameter by name
	 *
	 * @param name a parameter name
	 * @return parameters bound to the given name
	 */
	public Collection<CombinerParameter> getCombinerParam(String name){
		return combinerParameters.get(name);
	}

	/**
	 * Gets all composite decision rule combiner parameters
	 *
	 * @return a collection of all combiner parameters
	 */
	public Collection<CombinerParameter> getCombinerParams(){
		return combinerParameters.values();
	}

	/**
	 * Gets limits the depth of delegation which is authorized by this policy
	 *
	 * @return max delegation depth
	 */
	public Integer getMaxDelegationDepth(){
		return maxDelegationDepth;
	}

	public boolean isTrusted(){
		return (policyIssuer == null);
	}

	@Override
	final Decision evaluateRule(EvaluationContext context) {
		MatchResult r = isMatch(context);
		if(r == MatchResult.NOMATCH){
			return Decision.NOT_APPLICABLE;
		}
		if(r == MatchResult.MATCH){
			ConditionResult result = (condition == null)?ConditionResult.TRUE:evaluateCondition(context);
			if(result == ConditionResult.TRUE){
				return combine(context);
			}
			if(result == ConditionResult.FALSE){
				return Decision.NOT_APPLICABLE;
			}
		}
		return extendedIndeterminatePass(context);
	}

	/**
	 * Combines the children and adds the responses of an applicable decision. XACML 3.0, section
	 * 7.18: inside a policy set the responses wait until the decision of this rule is known and go
	 * up only when they match it (design D36). Under a built-in combining algorithm, on the thread
	 * that decides, they wait in the root of the decision, which records the staging of this rule
	 * and creates its frame on the first response that passes through the context of this rule
	 * (cycle AN, reviews R-AN, R-AN2); where custom code can run inside the subtree, a custom
	 * combining algorithm or a root of another implementation, or where the root cannot record the
	 * staging, they wait in a wrapper context.
	 */
	private Decision combine(EvaluationContext context){
		if(!(canRespond() && isEnclosed(context))){
			return respond(context, context);
		}
		if(context instanceof CompositeRuleEvaluationContext own && stagesInRoot(own) && own.openStaging()){
			RootEvaluationContext root = own.stagingRoot();
			try{
				Decision decision = respond(context, context);
				if(isApplicable(decision)){
					own.closeStaging();
					root.publishStaged(own, decision);
				}
				return decision;
			}finally{
				own.closeStaging();
				root.dropStaged(own);
			}
		}
		StagedResponses staged = new StagedResponses(context);
		Decision decision = respond(staged, context);
		if(isApplicable(decision)){
			staged.publish(decision);
		}
		return decision;
	}

	/**
	 * Tells whether a context of this rule can stage in the root of its decision: the root is the
	 * engine's, the current thread decides on it, and a built-in combining algorithm combines the
	 * children, so that no custom code runs between this rule and the staging of its children.
	 * Only the opening is limited to the deciding thread; once open, the staging holds what any
	 * thread writes through the context (review R-AN2)
	 */
	private boolean stagesInRoot(CompositeRuleEvaluationContext context){
		return hasBuiltInCombiningAlgorithm()
				&& DescendantEvaluationContext.topOf(context) instanceof RootEvaluationContext root
				&& root.isDecidingThread();
	}

	/** Whether the combining algorithm of this rule is one of the engine's own classes */
	abstract boolean hasBuiltInCombiningAlgorithm();

	/**
	 * Combines the children in the given context and adds the responses of an applicable decision;
	 * when one of them fails, the extended Indeterminate pass (XACML 3.0, section 7.18)
	 */
	private Decision respond(EvaluationContext evaluationContext, EvaluationContext context){
		Decision decision = combineDecisions(evaluationContext);
		if(!isApplicable(decision)){
			return decision;
		}
		// Also inside an extended Indeterminate pass, whose value they can change; the pass never
		// publishes them
		try{
			evaluateAdvicesAndObligations(evaluationContext, decision);
		}catch(EvaluationException e){
			return extendedIndeterminatePass(context);
		}
		// A context of this package skips a pass itself, after it asks the root whether it records
		// anything; another is asked here, as before (cycle AU)
		if(DescendantEvaluationContext.isOwn(context) || !context.isExtendedIndeterminateEval()){
			context.addEvaluationResult(this, decision);
		}
		return decision;
	}

	private static boolean isApplicable(Decision decision){
		return !decision.isIndeterminate() && decision != Decision.NOT_APPLICABLE;
	}

	/**
	 * Evaluates the children for the extended Indeterminate of this rule, in a pass whose responses
	 * are never published: the pass never gives Permit or Deny, so a rule inside a policy set gives
	 * it no obligations and no advice. The pass opens and closes the staging of its own context only
	 * (review R-AN2)
	 */
	private Decision extendedIndeterminatePass(EvaluationContext context){
		EvaluationContext pass = context.createExtIndeterminateEvalContext();
		if(!(canRespond() && isEnclosed(context))){
			return getExtendedIndeterminate(pass);
		}
		if(pass instanceof CompositeRuleEvaluationContext own && stagesInRoot(own) && own.openStaging()){
			RootEvaluationContext root = own.stagingRoot();
			try{
				return getExtendedIndeterminate(pass);
			}finally{
				own.closeStaging();
				root.dropStaged(own);
			}
		}
		return getExtendedIndeterminate(new StagedResponses(pass));
	}

	/**
	 * Tells whether the evaluation that this rule's staging holds can write responses: its children,
	 * its combining algorithm, its advice and its obligations (design D36). Its own target and
	 * condition run before the staging exists and count in {@link #canRespondToParent()}.
	 */
	boolean canRespond(){
		return true;
	}

	/**
	 * Tells whether evaluating this rule, its target and condition included, can write responses to
	 * the context of the rule that encloses it (design D36)
	 */
	final boolean canRespondToParent(){
		return canRespond() || targetOrConditionCanWrite();
	}

	/**
	 * Tests if a given context belongs to a rule which is evaluated inside a policy set.
	 * The outermost rule needs no staging: the root context is read only for the
	 * final decision, and a response of a different decision is not read
	 *
	 * @param context an evaluation context of this rule
	 * @return {@code true} if a policy set context is above the given context
	 */
	private static boolean isEnclosed(EvaluationContext context)
	{
		EvaluationContext c = context;
		while(c instanceof DescendantEvaluationContext){
			c = c.getParentContext();
			if(c instanceof PolicySet.PolicySetDescendantEvaluationContext){
				return true;
			}
		}
		return false;
	}

	/**
	 * Evaluates the children of this rule for its extended Indeterminate, as one pass of the observation of
	 * its decision (design D34)
	 *
	 * @param context an evaluation context of the pass
	 * @return the extended Indeterminate, or NotApplicable
	 */
	protected Decision getExtendedIndeterminate(EvaluationContext context)
	{
		DecisionObservation o = RootEvaluationContext.observationOf(context);
		if(!o.isEnabled()){
			return extendedIndeterminate(context);
		}
		Object parent = o.extendedIndeterminateStarted(context, this);
		Decision d;
		try{
			d = extendedIndeterminate(context);
		}catch(Throwable t){
			o.extendedIndeterminateFailed(context, this, parent, t);
			throw t;
		}
		o.extendedIndeterminateFinished(context, this, parent, d);
		return d;
	}

	private Decision extendedIndeterminate(EvaluationContext context)
	{
		Decision evaluationResult = null;
		try{
			evaluationResult = combineDecisions(context);
		}catch(Exception e){
			evaluationResult = Decision.INDETERMINATE;
		}
		return switch (evaluationResult) {
			case DENY, INDETERMINATE_D -> Decision.INDETERMINATE_D;
			case PERMIT, INDETERMINATE_P -> Decision.INDETERMINATE_P;
			case NOT_APPLICABLE -> Decision.NOT_APPLICABLE;
			default -> Decision.INDETERMINATE_DP;
		};
	}

	protected abstract Decision combineDecisions(EvaluationContext context);

	@Override
	protected MoreObjects.ToStringHelper toStringBuilder(MoreObjects.ToStringHelper b){
		return super.toStringBuilder(b)
			.add("version", version)
			.add("issuer", policyIssuer)
			.add("maxDelegationDepth", maxDelegationDepth)
			.add("combinerParameters", combinerParameters);
	}

	protected boolean equalsTo(BaseCompositeDecisionRule r) {
		return super.equalsTo(r)
			&& Objects.equal(version, r.version)
			&& Objects.equal(policyIssuer, r.policyIssuer)
			&& Objects.equal(maxDelegationDepth, r.maxDelegationDepth)
			&& Objects.equal(combinerParameters, r.combinerParameters);
	}

	@Override
	public int hashCode() {
		// Arrays.hashCode of the fields in this order, without the array (design D22)
		int base = super.hashCode();
		int h = 1;
		h = 31 * h + java.util.Objects.hashCode(version);
		h = 31 * h + java.util.Objects.hashCode(policyIssuer);
		h = 31 * h + java.util.Objects.hashCode(maxDelegationDepth);
		h = 31 * h + java.util.Objects.hashCode(combinerParameters);
		return base * 31 + h;
	}

	/**
	 * Holds the obligations and advice from the evaluation of one composite decision rule until
	 * the decision of that rule is known, where custom code can run inside the rule: a custom
	 * combining algorithm, a root context of another implementation, or another thread, and where
	 * the root cannot record the staging; under a built-in algorithm the root context of the engine
	 * stages them in frames of its own (designs D19, D36, cycle AN, reviews R-AN, R-AN2)
	 */
	private static final class StagedResponses extends DescendantEvaluationContext
	{
		private final RootEvaluationContext.Staged staged = new RootEvaluationContext.Staged();

		StagedResponses(EvaluationContext context){
			super(context);
		}

		@Override
		public boolean isExtendedIndeterminateEval(){
			return getParent().isExtendedIndeterminateEval();
		}

		@Override
		public EvaluationContext getParentContext(){
			// This wrapper replaces its delegate in ancestry walks; do not count it twice.
			return getParent().getParentContext();
		}

		@Override
		public void addAdvices(Decision d, Iterable<Advice> advices){
			staged.addAdvices(Preconditions.checkNotNull(d, "decision"), advices);
		}

		@Override
		public void addObligations(Decision d, Iterable<Obligation> obligations){
			staged.addObligations(Preconditions.checkNotNull(d, "decision"), obligations);
		}

		/** Gives the obligations and advice which match a Permit or Deny decision to the parent context */
		void publish(Decision d){
			staged.publishTo(getParent(), d);
		}
	}

	public abstract static class Builder<T extends Builder<?>>
		extends BaseDecisionRule.Builder<T>
	{
		protected Version version;
		protected Integer maxDelegationDepth;
		protected Entity issuer;
		protected ImmutableMultimap.Builder<String, CombinerParameter> combParamBuilder = ImmutableMultimap.builder();

		protected Builder(String ruleId) {
			super(ruleId);
			this.version = Version.parse("1.0");
		}

		public T version(Version version){
			Preconditions.checkNotNull(version, "Version can't be null");
			this.version = version;
			return getThis();
		}

		public T combinerParam(CombinerParameter ... params){
			Preconditions.checkNotNull(params);
			for(CombinerParameter p : params){
				this.combParamBuilder.put(p.getName(), p);
			}
			return getThis();
		}

		public T combinerParams(Iterable<CombinerParameter> params){
			Preconditions.checkNotNull(params);
			for(CombinerParameter p : params){
				combinerParam(p);
			}
			return getThis();
		}

		public T version(String version){
			return version(Version.parse(version));
		}

		public T issuer(Entity issuer){
			this.issuer = issuer;
			return getThis();
		}

		public T maxDelegationDepth(Integer maxDelegationDepth)
		{
			Preconditions.checkArgument(maxDelegationDepth == null || maxDelegationDepth >= 0);
			this.maxDelegationDepth = maxDelegationDepth;
			return getThis();
		}
	}
}
