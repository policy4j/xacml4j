package com.artagon.xacml.v3.spi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.policy.combine.DefaultDecisionCombiningAlgorithms;
import com.google.common.base.Preconditions;

public final class DefaultPolicyDomain implements PolicyDomain
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
	private Map<String, CompositeDecisionRule> policies;
	
	public DefaultPolicyDomain(){
		this(Type.FIRST_APPLICABLE);
	}
	
	public DefaultPolicyDomain(Type mode, 
			DecisionCombiningAlgorithmProvider decisionAlgorithmProvider)
	{
		this.mode = mode;
		String algorithmId = MODE.get(mode);
		Preconditions.checkState(algorithmId != null);
		this.combineDecision = decisionAlgorithmProvider.getPolicyAlgorithm(algorithmId);
		Preconditions.checkState(combineDecision != null);
		this.policies = new ConcurrentHashMap<String, CompositeDecisionRule>();
	}
	
	public DefaultPolicyDomain(Type mode)
	{
		this(mode, new DefaultDecisionCombiningAlgorithms());
	}
	
	@Override
	public final void add(CompositeDecisionRule policy) {
		CompositeDecisionRule oldPolicy = policies.put(policy.getId(), policy);
		Preconditions.checkState(oldPolicy == null);
	}
	
	@Override
	public Type getMode(){
		return mode;
	}
	
	@Override
	public final Decision evaluate(EvaluationContext context) {
		List<CompositeDecisionRule> policies = new LinkedList<CompositeDecisionRule>(this.policies.values());
		return combineDecision.combine(policies, context);
	}
}
