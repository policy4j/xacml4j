package com.artagon.xacml.v3.policy;

import java.util.HashMap;
import java.util.Map;

public class MockPolicyResolver implements PolicyResolver
{
	private Map<String, Policy> policies;
	private Map<String, PolicySet> policySets;
	
	public MockPolicyResolver(){
		this.policies = new HashMap<String, Policy>();
		this.policySets = new HashMap<String, PolicySet>();
	}
	
	@Override
	public Policy resolve(PolicyIDReference ref)
			throws PolicyResolutionException {
		return policies.get(ref.getId());
	}
	@Override
	public PolicySet resolve(PolicySetIDReference ref)
			throws PolicyResolutionException {
		return policySets.get(ref.getId());
	}
}
