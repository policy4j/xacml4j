package com.artagon.xacml.v3.spi.repostory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.DefaultPolicyVisitor;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicySet;

public final class InMemoryPolicyStore extends AbstractPolicyRepository 
{	
	private Map<String, Policy> policies;
	private Map<String, PolicySet> policySets;
	private Map<String, CompositeDecisionRule> rootPolicies;
	
	public InMemoryPolicyStore()
	{
		this.policies = new ConcurrentHashMap<String, Policy>();
		this.policySets = new ConcurrentHashMap<String, PolicySet>();
	}
	
	public void add(CompositeDecisionRule rule)
	{
		rootPolicies.put(rule.getId(), rule);
		rule.accept(new PolicyTreeWalker());
	}
	
	public void addAll(Collection<CompositeDecisionRule> rules)
	{
		for(CompositeDecisionRule r : rules){
			add(r);
		}
	}
	
	@Override
	protected Iterable<Policy> findPolicy(String policyId) {
		Policy p = policies.get(policyId);
		return (p == null)?
				Collections.<Policy>emptyList():
					Collections.<Policy>singleton(p);
	}

	@Override
	protected Iterable<PolicySet> findPolicySet(String policyId) {
		PolicySet p = policySets.get(policyId);
		return (p == null)?
				Collections.<PolicySet>emptyList():
					Collections.<PolicySet>singleton(p);
	}

	@Override
	public Collection<CompositeDecisionRule> getPolicies() {
		return Collections.unmodifiableCollection(rootPolicies.values());
	}


	class PolicyTreeWalker extends DefaultPolicyVisitor
	{		
		public void visitEnter(Policy policy) {
			policies.put(policy.getId(), policy);
		}
		
		public void visitEnter(PolicySet policySet) {
			policySets.put(policySet.getId(), policySet);
		}		
	}
}
