package com.artagon.xacml.v3.spi.store;

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
import com.artagon.xacml.v3.spi.DecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v3.spi.PolicyDomain;
import com.google.common.base.Preconditions;

public abstract class AbstractPolicyStore implements PolicyDomain
{
	private final static Map<Type, String> MODE = new HashMap<PolicyDomain.Type, String>();
	
	static
	{
		MODE.put(Type.DENY_OVERRIDES, "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides");
		MODE.put(Type.FIRST_APPLICABLE, "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:first-applicable");
		MODE.put(Type.ONLY_ONE_APPLICABLE, "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:only-one-applicable");
	}
	
	private Type mode;
	private DecisionCombiningAlgorithm<CompositeDecisionRule> combineDecision;
	
	protected AbstractPolicyStore(Type mode, 
			DecisionCombiningAlgorithmProvider decisionAlgorithmProvider)
	{
		this.mode = mode;
		String algorithmId = MODE.get(mode);
		Preconditions.checkState(algorithmId != null);
		this.combineDecision = decisionAlgorithmProvider.getPolicyAlgorithm(algorithmId);
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
	public final void add(CompositeDecisionRule rule) {
		add(rule, true);
	}


	@Override
	public Type getMode(){
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
