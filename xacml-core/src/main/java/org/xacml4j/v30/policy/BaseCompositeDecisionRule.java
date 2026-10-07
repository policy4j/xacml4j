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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private final static Logger log = LoggerFactory.getLogger(BaseCompositeDecisionRule.class);

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
	public Decision evaluate(EvaluationContext context) {
		if(log.isDebugEnabled()){
			log.debug("Evaluating composite " +
					"decision rule with id=\"{}\"", id);
		}
		MatchResult r = isMatch(context);
		if(r == MatchResult.NOMATCH){
			if(log.isDebugEnabled()){
				log.debug("Composite decision rule " +
						"id=\"{}\" target is NO_MATCH, " +
						"decision result is NOT_APPLICABLE", id);
			}
			return Decision.NOT_APPLICABLE;
		}
		if(r == MatchResult.MATCH){
			ConditionResult result = (condition == null)?ConditionResult.TRUE:condition.evaluate(context);
			if(log.isDebugEnabled()){
				log.debug("Composite decision rule " +
						          "id=\"{}\" condition=\"{}\", evaluating rules", id, result);
			}
			if(result == ConditionResult.TRUE){
				// XACML 3.0, section 7.18: the obligations and advice from the evaluation of
				// this rule go to an enclosing policy set only if they match its decision
				StagedResponses staged = isEnclosed(context)?new StagedResponses(context):null;
				EvaluationContext evaluationContext = (staged == null)?context:staged;
				Decision decision = combineDecisions(evaluationContext);
				if(log.isDebugEnabled()){
					log.debug("Composite decision rule " +
							"id=\"{}\" combined  decision result is=\"{}\"", id, decision);
				}
				if(!decision.isIndeterminate() &&
						decision != Decision.NOT_APPLICABLE){
					if(!context.isExtendedIndeterminateEval()){
						try
						{
							evaluateAdvicesAndObligations(evaluationContext, decision);
						}catch(EvaluationException e){
							return getExtendedIndeterminate(
									createExtIndeterminateEvalContext(context));
						}
						if(staged != null){
							staged.publish(decision);
						}
						context.addEvaluationResult(this, decision);
					}
				}
				return decision;
			}
			if(result == ConditionResult.FALSE){
				if(log.isDebugEnabled()){
					log.debug("Composite decision rule " +
							"id=\"{}\" condition eval is FALSE, " +
							"decision result is=\"{}\"", id, Decision.NOT_APPLICABLE);
				}
				return Decision.NOT_APPLICABLE;
			}
		}
		return getExtendedIndeterminate(createExtIndeterminateEvalContext(context));
	}

	/**
	 * Creates a context to evaluate this rule for the extended indeterminate.
	 * The result of that evaluation is never Permit or Deny, so a rule which is
	 * evaluated inside a policy set gives no obligations and no advice to it
	 *
	 * @param context an evaluation context of this rule
	 * @return {@link EvaluationContext} to evaluate extended indeterminate
	 */
	private static EvaluationContext createExtIndeterminateEvalContext(EvaluationContext context)
	{
		EvaluationContext extContext = context.createExtIndeterminateEvalContext();
		return isEnclosed(context)?new StagedResponses(extContext):extContext;
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

	protected Decision getExtendedIndeterminate(EvaluationContext context)
	{
		Decision evaluationResult = null;
		try{
			evaluationResult = combineDecisions(context);
		}catch(Exception e){
			evaluationResult = Decision.INDETERMINATE;
		}
		switch(evaluationResult){
			case DENY : return Decision.INDETERMINATE_D;
			case PERMIT : return Decision.INDETERMINATE_P;
			case NOT_APPLICABLE: return Decision.NOT_APPLICABLE;

			case INDETERMINATE_D : return Decision.INDETERMINATE_D;
			case INDETERMINATE_P : return Decision.INDETERMINATE_P;
			case INDETERMINATE_DP: return Decision.INDETERMINATE_DP;
			default: return Decision.INDETERMINATE_DP;
		}
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
		return super.hashCode() * 31 +
				Objects.hashCode(version, policyIssuer, maxDelegationDepth, combinerParameters);
	}

	/**
	 * Holds the obligations and advice from the evaluation of one composite decision
	 * rule until the decision of that rule is known. The root context is not changed
	 * before that, so nothing must be removed from it when the rule is not applicable,
	 * is indeterminate, or has a different decision
	 */
	private static final class StagedResponses extends DescendantEvaluationContext
	{
		private final List<Advice> permitAdvices = new ArrayList<>();
		private final List<Advice> denyAdvices = new ArrayList<>();
		private final List<Obligation> permitObligations = new ArrayList<>();
		private final List<Obligation> denyObligations = new ArrayList<>();

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
			Preconditions.checkNotNull(d, "decision");
			if(d == Decision.PERMIT || d == Decision.DENY){
				List<Advice> staged = (d == Decision.PERMIT)?permitAdvices:denyAdvices;
				for(Advice a : advices){
					staged.add(a);
				}
			}
		}

		@Override
		public void addObligations(Decision d, Iterable<Obligation> obligations){
			Preconditions.checkNotNull(d, "decision");
			if(d == Decision.PERMIT || d == Decision.DENY){
				List<Obligation> staged = (d == Decision.PERMIT)?permitObligations:denyObligations;
				for(Obligation o : obligations){
					staged.add(o);
				}
			}
		}

		/**
		 * Gives the obligations and advice which match a given decision to the parent context
		 *
		 * @param d a Permit or Deny decision of the rule
		 */
		void publish(Decision d){
			EvaluationContext parent = getParent();
			parent.addAdvices(d, (d == Decision.PERMIT)?permitAdvices:denyAdvices);
			parent.addObligations(d, (d == Decision.PERMIT)?permitObligations:denyObligations);
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
