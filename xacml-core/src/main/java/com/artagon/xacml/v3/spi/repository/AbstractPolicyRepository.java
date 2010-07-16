package com.artagon.xacml.v3.spi.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicyIDReference;
import com.artagon.xacml.v3.PolicyResolutionException;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.PolicySetIDReference;
import com.artagon.xacml.v3.policy.combine.DenyOverridesPolicyCombiningAlgorithm;
import com.artagon.xacml.v3.policy.combine.FirstApplicablePolicyCombiningAlgorithm;
import com.artagon.xacml.v3.policy.combine.OnlyOneApplicablePolicyCombingingAlgorithm;
import com.artagon.xacml.v3.spi.PolicyStore;
import com.google.common.base.Preconditions;

public abstract class AbstractPolicyRepository implements PolicyStore
{
	private final static Map<Mode, DecisionCombiningAlgorithm<CompositeDecisionRule>> MODE = new HashMap<PolicyStore.Mode, DecisionCombiningAlgorithm<CompositeDecisionRule>>();
	
	static
	{
		MODE.put(Mode.DENY_OVERRIDES, new DenyOverridesPolicyCombiningAlgorithm());
		MODE.put(Mode.FIRST_APPLICABLE, new FirstApplicablePolicyCombiningAlgorithm());
		MODE.put(Mode.ONLY_ONE_APPLICABLE, new OnlyOneApplicablePolicyCombingingAlgorithm());
	}
	
	private Mode mode;
	private DecisionCombiningAlgorithm<CompositeDecisionRule> combineDecision;
	
	protected AbstractPolicyRepository(Mode mode)
	{
		this.mode = mode;
		this.combineDecision = MODE.get(mode);
		Preconditions.checkState(combineDecision != null);
	}
	
	
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
	public Mode getMode(){
		return mode;
	}
	
	@Override
	public final Decision evaluate(EvaluationContext context) {
		List<CompositeDecisionRule> rules = new ArrayList<CompositeDecisionRule>(getPolicies());
		return combineDecision.combine(rules, context);
	}

	protected abstract Iterable<Policy> findPolicy(String policyId);
	protected abstract Iterable<PolicySet> findPolicySet(String policyId);
}
