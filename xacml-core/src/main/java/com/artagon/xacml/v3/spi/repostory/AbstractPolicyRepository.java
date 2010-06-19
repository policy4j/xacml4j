package com.artagon.xacml.v3.spi.repostory;

import java.util.LinkedHashMap;
import java.util.Map;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.MatchResult;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicyIDReference;
import com.artagon.xacml.v3.PolicyResolutionException;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.PolicySetIDReference;
import com.artagon.xacml.v3.spi.PolicyRepository;

public abstract class AbstractPolicyRepository implements PolicyRepository
{
	@Override
	public final Policy resolve(EvaluationContext context, PolicyIDReference ref)
			throws PolicyResolutionException 
	{
		try{
			Iterable<Policy> found = findPolicy(ref.getId());
			for(Policy p : found){
				if(ref.isReferenceTo(p)){
					return p;
				}
			}
			return null;
		}catch(Exception e){
			throw new PolicyResolutionException(context, e);
		}
	}

	@Override
	public final PolicySet resolve(EvaluationContext context, PolicySetIDReference ref)
			throws PolicyResolutionException {
		try{
			Iterable<PolicySet> found = findPolicySet(ref.getId());
			for(PolicySet p : found){
				if(ref.isReferenceTo(p)){
					return p;
				}
			}
			return null;
		}catch(Exception e){
			throw new PolicyResolutionException(context, e);
		}
	}
	
	@Override
	public final Map<String, CompositeDecisionRule> findApplicable(
			EvaluationContext context) 
	{
		Iterable<CompositeDecisionRule> rules = getPolicies();
		Map<String, CompositeDecisionRule> applicable = new LinkedHashMap<String, CompositeDecisionRule>();
		for(CompositeDecisionRule r : rules){
			if(r.isApplicable(context) == MatchResult.MATCH){
				applicable.put(r.getId(), r);
			}
		}
		return applicable;
	}
	
	protected abstract Iterable<Policy> findPolicy(String policyId);
	protected abstract Iterable<PolicySet> findPolicySet(String policyId);
}
