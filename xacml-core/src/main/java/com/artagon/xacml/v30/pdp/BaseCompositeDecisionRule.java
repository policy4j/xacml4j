package com.artagon.xacml.v30.pdp;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;

import com.artagon.xacml.v30.Version;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Objects.ToStringHelper;

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
	protected String id;
	protected Version version;
	private PolicyIssuer policyIssuer;
	private BigInteger maxDelegationDepth;
	
	/**
	 * Constructs composite decision rule
	 * 
	 * @param id a rule identifier
	 * @param target a rule target
	 * @param adviceExpressions a rule advice 
	 * expressions
	 * @param obligationExpressions a rule obligation 
	 * expressions
	 */
	protected BaseCompositeDecisionRule(
			String id,
			Version version,
			String description,
			Target target,
			Condition condition,
			PolicyIssuer issuer,
			BigInteger maxDelegationDepth,
			Collection<AdviceExpression> adviceExpressions,
			Collection<ObligationExpression> obligationExpressions){
		super(description, target, condition, adviceExpressions, obligationExpressions);
		Preconditions.checkNotNull(id, 
				"Composite decision rule id can not be null");
		Preconditions.checkNotNull(version, 
				"Composite decision rule version can not be null");
		this.id = id;
		this.version = version;
	}
	
	protected BaseCompositeDecisionRule(
			String id, 
			Version version,
			Target target,
			Condition condition,
			Collection<AdviceExpression> adviceExpressions,
			Collection<ObligationExpression> obligationExpressions){
		this(id, version, null, target, 
				condition, null, null, adviceExpressions, obligationExpressions);
	}
	
	protected BaseCompositeDecisionRule(
			String id, 
			Version version,
			Target target){
		this(id, version, null, target, null, null, null,
				Collections.<AdviceExpression>emptyList(), 
				Collections.<ObligationExpression>emptyList());
	}
	
	@Override
	public String getId(){
		return id;
	}
	
	@Override
	public  Version getVersion() {
		return version;
	}
	
	/**
	 * Gets this composite rule issuer
	 * @return {@link PolicyIssuer}
	 */
	public PolicyIssuer getPolicyIssuer(){
		return policyIssuer;
	}
	
	public BigInteger getMaxDelegationDepth(){
		return maxDelegationDepth;
	}
	
	public boolean isTrusted(){
		return (policyIssuer == null);
	}
	
	/**
	 * Combines {@link #isMatch(EvaluationContext)} and 
	 * {@link #evaluate(EvaluationContext)} calls to one single
	 * method invocation
	 */
	@Override
	public  Decision evaluateIfMatch(EvaluationContext context)
	{
		if(log.isDebugEnabled()){
			log.debug("Invoking decision rule " +
					"id=\"{}\" evaluateIfApplicable", getId());
		}
		MatchResult r = isMatch(context);
		Preconditions.checkState(r != null);
		if(r == MatchResult.MATCH){
			if(log.isDebugEnabled()){
				log.debug("Decision rule id=\"{}\" " +
						"match result is=\"{}\", evaluating rule", 
						getId(), r);
			}
			return evaluate(context);
		}
		if(log.isDebugEnabled()){
			log.debug("Decision rule id=\"{}\" match " +
					"result is=\"{}\", not evaluating rule", getId(), r);
		}
		return (r == MatchResult.INDETERMINATE)?
				Decision.INDETERMINATE:Decision.NOT_APPLICABLE;
	}
	
	protected ToStringHelper _addProperties(Objects.ToStringHelper b){
		b.add("id", id);
		b.add("version", version);
		b.add("issuer", policyIssuer);
		super._addProperties(b);
		return b;
	}
	
	public abstract static class BaseCompositeDecisionRuleBuilder<T extends BaseCompositeDecisionRuleBuilder<?>> 
		extends BaseDecisionRuleBuilder<T>
	{
		
	}
}
