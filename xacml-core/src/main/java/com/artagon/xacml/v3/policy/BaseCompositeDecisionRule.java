package com.artagon.xacml.v3.policy;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;
import com.google.common.base.Preconditions;

abstract class BaseCompositeDecisionRule extends BaseDesicionRule 
	implements CompositeDecisionRule, Versionable
{
	private final static Logger log = LoggerFactory.getLogger(BaseCompositeDecisionRule.class);
	
	private String id;
	private Version version;
	
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
			Collection<AdviceExpression> adviceExpressions,
			Collection<ObligationExpression> obligationExpressions){
		super(description, target,  adviceExpressions, obligationExpressions);
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(version);
		this.id = id;
		this.version = version;
	}
	
	protected BaseCompositeDecisionRule(
			String id, 
			Version version,
			Target target,
			Collection<AdviceExpression> adviceExpressions,
			Collection<ObligationExpression> obligationExpressions){
		this(id, version, null, target, adviceExpressions, obligationExpressions);
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
	 * Combines {@link #isApplicable(EvaluationContext)} and 
	 * {@link #evaluate(EvaluationContext)} calls to one single
	 * method invocation
	 */
	@Override
	public  Decision evaluateIfApplicable(EvaluationContext context)
	{
		if(log.isDebugEnabled()){
			log.debug("Invoking decision rule " +
					"id=\"{}\" evaluateIfApplicable", getId());
		}
		MatchResult r = isApplicable(context);
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
}
