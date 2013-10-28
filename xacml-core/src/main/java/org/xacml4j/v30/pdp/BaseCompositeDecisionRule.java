package org.xacml4j.v30.pdp;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.Version;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
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
	
	protected Version version;
	private PolicyIssuer issuer;
	private Integer maxDelegationDepth;
	private Multimap<String, CombinerParameter> combinerParameters;
	
	protected BaseCompositeDecisionRule(BaseCompositeDecisionRuleBuilder<?> b){
		super(b);
		this.version = b.version;
		this.maxDelegationDepth = b.maxDelegationDepth;
		this.issuer = b.issuer;
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
	public PolicyIssuer getIssuer(){
		return issuer;
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
		return (issuer == null);
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
			if(result == ConditionResult.TRUE){
				Decision decision = combineDecisions(context);
				if(log.isDebugEnabled()){
					log.debug("Composite decision rule " +
							"id=\"{}\" condition eval is TRUE, " +
							"decision result is=\"{}\"", id, decision);
				}
				if(!decision.isIndeterminate() ||
						decision != Decision.NOT_APPLICABLE){
					if(!context.isExtendedIndeterminateEval()){
						try
						{
							evaluateAdvicesAndObligations(context, decision);
						}catch(EvaluationException e){
							return getExtendedIndeterminate(
									context.createExtIndeterminateEvalContext());
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
		return getExtendedIndeterminate(context.createExtIndeterminateEvalContext());
	}
	
	protected Decision getExtendedIndeterminate(EvaluationContext context)
	{
		Decision evaluationResult = null;
		try{
			evaluationResult = combineDecisions(context);
		}catch(Exception  e){
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
	protected ToStringHelper toStringBuilder(Objects.ToStringHelper b){
		b.add("version", version);
		b.add("issuer", issuer);
		b.add("maxDelegationDepth", maxDelegationDepth);
		b.add("combinerParameters", combinerParameters);
		super.toStringBuilder(b);
		return b;
	}
	
	public abstract static class BaseCompositeDecisionRuleBuilder<T extends BaseCompositeDecisionRuleBuilder<?>> 
		extends BaseDecisionRuleBuilder<T>
	{
		protected Version version;
		protected Integer maxDelegationDepth;
		protected PolicyIssuer issuer;
		protected ImmutableMultimap.Builder<String, CombinerParameter> combParamBuilder = ImmutableMultimap.builder();
		
		protected BaseCompositeDecisionRuleBuilder(String ruleId) {
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
		
		public T issuer(PolicyIssuer issuer){
			this.issuer = issuer;
			return getThis();
		}
		
		public T maxDelegationDepth(Integer maxDelegationdepth)
		{
			Preconditions.checkArgument(maxDelegationdepth == null || maxDelegationdepth >= 0);
			this.maxDelegationDepth = maxDelegationdepth;
			return getThis();
		}
	}
}
