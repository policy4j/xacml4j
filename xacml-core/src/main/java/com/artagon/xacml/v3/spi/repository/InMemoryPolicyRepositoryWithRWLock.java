package com.artagon.xacml.v3.spi.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.Version;
import com.artagon.xacml.v3.VersionMatch;
import com.artagon.xacml.v3.Versionable;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class InMemoryPolicyRepositoryWithRWLock extends AbstractPolicyRepository
{
	private final static Logger log = LoggerFactory.getLogger(InMemoryPolicyRepositoryWithRWLock.class);
	
	private Map<String, Map<Version, Policy>> policies;
	private Map<String, Map<Version, PolicySet>> policySets;
	
	private ReadWriteLock policyLock;
	private ReadWriteLock policySetLock;
	
	public InMemoryPolicyRepositoryWithRWLock(){
		addCapability(PolicyReferenceResolver.class, 
				new DefaultPolicyReferenceResolver(this));
		this.policies = new HashMap<String, Map<Version, Policy>>();
		this.policySets = new HashMap<String, Map<Version, PolicySet>>();
		this.policyLock = new ReentrantReadWriteLock();
		this.policySetLock = new ReentrantReadWriteLock();
	}

	@Override
	public Collection<Policy> getPolicies(String id, VersionMatch version,
			VersionMatch earliest, VersionMatch latest) 
	{
		Map<Version, Policy> byId = policies.get(id);
		if(log.isDebugEnabled() && 
				byId != null){
			log.debug("Found=\"{}\" versions of policy with id=\"{}\"", 
					byId.size(), id);
		}
		try{
			policyLock.readLock().lock();
			return find((byId != null)?byId.values():null, version, earliest, latest);
		}finally{
			policyLock.readLock().unlock();
		}
	}

	@Override
	public Collection<PolicySet> getPolicySets(String id, VersionMatch version,
			VersionMatch earliest, VersionMatch latest) 
	{
		Map<Version, PolicySet> byId = policySets.get(id);
		if(log.isDebugEnabled() && 
				byId != null){
			log.debug("Found=\"{}\" versions of policy set with id=\"{}\"", 
					byId.size(), id);
		}
		try{
			policySetLock.readLock().lock();
			return find((byId != null)?byId.values():null, version, earliest, latest);
		}finally{
			policySetLock.readLock().unlock();
		}
	}
	
	@Override
	public void add(CompositeDecisionRule policy) {
		if(policy instanceof Policy){
			addInternal((Policy)policy);
			return;
		}
		if(policy instanceof PolicySet){
			addInternal((PolicySet)policy);
			return;
		}
	}

	private void addInternal(Policy policy) 
	{
		Preconditions.checkArgument(policy != null);
		String id = policy.getId();
		Version v = policy.getVersion();
		if(log.isDebugEnabled()){
			log.debug("Adding PolicySet with " +
					"id=\"{}\" version=\"{}\"", id, v);
			log.debug("Currently repository " +
					"contains=\"{}\" policies", policies.size());
		}
		Map<Version, Policy> versions = policies.get(id);
		try{
			policyLock.writeLock().lock();
			if(versions == null || 
					versions.isEmpty()){
				versions = new TreeMap<Version, Policy>();
				policies.put(id, versions);
			}
			Preconditions.checkArgument(!versions.containsKey(v), 
					"Repository already contains a " +
					"policy with id=\"%s\" and version=\"%s\"", 
								id, v);
			Preconditions.checkState(
					versions.put(policy.getVersion(), policy) == null);
		}finally{
			policyLock.writeLock().unlock();
		}
	}

	private void addInternal(PolicySet policySet) 
	{
		Preconditions.checkArgument(policySet != null);
		String id = policySet.getId();
		Version v = policySet.getVersion();
		if(log.isDebugEnabled()){
			log.debug("Adding Policy with " +
					"id=\"{}\" version=\"{}\"", id, v);
			log.debug("Currently repository " +
					"contains=\"{}\" policy sets", policySets.size());
		}
		Map<Version, PolicySet> versions = policySets.get(id);
		try{
			policySetLock.writeLock().lock();
			if(versions == null || 
					versions.isEmpty())
			{
				versions = new TreeMap<Version, PolicySet>();
				policySets.put(id, versions);
			}
			Preconditions.checkArgument(!versions.containsKey(v), 
					"Repository already contains a policy with id=\"%s\" and version=\"%s\"", 
					id, v);
			Preconditions.checkState(
					versions.put(policySet.getVersion(), policySet) == null);
		}finally{
			policySetLock.writeLock().unlock();
		}
		
	}

	private <T extends Versionable> Collection<T> find(
			Collection<T> c, 
			final VersionMatch version, 
			final VersionMatch earliest, 
			final VersionMatch latest)
	{
		if(c == null){
			return Collections.<T>emptyList();
		}
		return Collections2.filter(c, new Predicate<T>() {
			@Override
			public boolean apply(T p) {
				return (version == null || version.match(p.getVersion())) &&
						(earliest == null || earliest.match(p.getVersion())) &&
						(latest == null || latest.match(p.getVersion()));
			}
			
		});
	}
}
