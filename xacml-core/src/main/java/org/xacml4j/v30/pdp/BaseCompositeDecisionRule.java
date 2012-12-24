package org.xacml4j.v30.pdp;

import java.util.Collection;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.Version;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.LinkedHashMultimap;
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
	protected Version version;
	private PolicyIssuer issuer;
	private Integer maxDelegationDepth;
	private Multimap<String, CombinerParameter> combinerParameters;
	
	protected BaseCompositeDecisionRule(BaseCompositeDecisionRuleBuilder<?> b){
		super(b);
		this.version = b.version;
		this.maxDelegationDepth = b.maxDelegationDepth;
		this.issuer = b.issuer;
		this.combinerParameters = ImmutableListMultimap.copyOf(b.combinerParameters);
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
	 * Gets composite decision rule combiner parameter
	 * 
	 * @param name a parameter name
	 * @return a collection of parameters
	 */
	public Collection<CombinerParameter> getCombinerParam(String name){
		return combinerParameters.get(name);
	}
	
	/**
	 * Gets composite decision rule combiner parameters
	 * 
	 * @return a collection of parameters
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
		b.add("version", version);
		b.add("issuer", issuer);
		b.add("maxDelegationDepth", maxDelegationDepth);
		b.add("combinerParameters", combinerParameters);
		super._addProperties(b);
		return b;
	}
	
	public abstract static class BaseCompositeDecisionRuleBuilder<T extends BaseCompositeDecisionRuleBuilder<?>> 
		extends BaseDecisionRuleBuilder<T>
	{
		protected Version version;
		protected Integer maxDelegationDepth;
		protected PolicyIssuer issuer;
		protected Multimap<String, CombinerParameter> combinerParameters = LinkedHashMultimap.create();
		
		protected BaseCompositeDecisionRuleBuilder(String ruleId) {
			super(ruleId);
			this.version = Version.parse("1.0");
		}
		
		public T version(Version version){
			Preconditions.checkNotNull(version, "Version can't be null");
			this.version = version;
			return getThis();
		}
		
		public T withCombinerParam(CombinerParameter p){
			Preconditions.checkNotNull(p);
			this.combinerParameters.put(p.getName(), p);
			return getThis();
		}
		
		public T combinerParams(Iterable<CombinerParameter> params){
			Preconditions.checkNotNull(params);
			for(CombinerParameter p : params){
				withCombinerParam(p);
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
