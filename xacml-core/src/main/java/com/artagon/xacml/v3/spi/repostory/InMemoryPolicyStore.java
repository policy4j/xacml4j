package com.artagon.xacml.v3.spi.repostory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.DefaultPolicyVisitor;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.PolicyVisitor;
import com.google.common.base.Preconditions;

public final class InMemoryPolicyStore extends AbstractPolicyRepository 
{	
	private final static Logger log = LoggerFactory.getLogger(InMemoryPolicyStore.class);
	
	private Map<String, Policy> policies;
	private Map<String, PolicySet> policySets;
	private Map<String, CompositeDecisionRule> topLevel;
	
	public InMemoryPolicyStore()
	{
		this.policies = new ConcurrentHashMap<String, Policy>();
		this.policySets = new ConcurrentHashMap<String, PolicySet>();
		this.topLevel = new ConcurrentHashMap<String, CompositeDecisionRule>();
	}
	
	/**
	 * Adds {@link CompositeDecisionRule} to this repository
	 * 
	 * @param rule a policy or policy set
	 */
	public void add(CompositeDecisionRule rule)
	{
		if(log.isDebugEnabled()){
			log.debug("Adding decision ruleId=\"{}\"", rule.getId());
		}
		CompositeDecisionRule old = topLevel.put(rule.getId(), rule);
		Preconditions.checkState(old == null, 
				"Decision rule with id=\"%s\" already exist in the store", rule.getId());
		rule.accept(new PolicyTreeWalker());
	}
	
	/**
	 * Adds collection of policies to this repository
	 * 
	 * @param rules a collection of policies
	 */
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
		return Collections.unmodifiableCollection(topLevel.values());
	}

	/**
	 * A {@link PolicyVisitor} implementation
	 * to index policies by policy identifier
	 */
	class PolicyTreeWalker extends DefaultPolicyVisitor
	{		
		public void visitEnter(Policy p) {
			Preconditions.checkArgument(p != null);
			if(log.isDebugEnabled()){
				log.debug("Adding child PolicyId=\"{}\"", p.getId());
			}
			Policy old = policies.put(p.getId(), p);
			Preconditions.checkState(old == null, 
					"Policy with id=\"%s\" " +
					"already exist in the store", p.getId());
		}
		
		public void visitEnter(PolicySet p) {
			Preconditions.checkArgument(p != null);
			if(log.isDebugEnabled()){
				log.debug("Adding child PolicySetId=\"{}\"", p.getId());
			}
			PolicySet old = policySets.put(p.getId(), p);
			Preconditions.checkState(old == null, 
					"PolicySet with id=\"%s\" " +
					"already exist in the store", p.getId());
		}		
	}
}
