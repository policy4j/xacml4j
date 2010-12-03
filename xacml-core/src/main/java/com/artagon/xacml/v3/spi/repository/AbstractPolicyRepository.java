package com.artagon.xacml.v3.spi.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicyIDReference;
import com.artagon.xacml.v3.PolicyResolutionException;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.PolicySetIDReference;
import com.artagon.xacml.v3.VersionMatch;
import com.artagon.xacml.v3.spi.PolicyRepository;
import com.google.common.collect.MapMaker;

/**
 * A base class for {@link PolicyRepository} implementations
 * 
 * @author Giedrius Trumpickas
 */
public abstract class AbstractPolicyRepository implements PolicyRepository
{
	private final static Logger log = LoggerFactory.getLogger(AbstractPolicyRepository.class);
	
	private ConcurrentMap<PolicyIDReference, Policy> policyIDRefCache;
	private ConcurrentMap<PolicySetIDReference, PolicySet> policySetIDRefCache;
	
	private boolean enableRefCache;
	
	protected AbstractPolicyRepository(){
		this(true, 32);
	}
	
	protected AbstractPolicyRepository(boolean enabledRefCache, 
			int size)
	{
		this.enableRefCache = enabledRefCache;
		this.policyIDRefCache = new MapMaker()
		.initialCapacity(size)
		.softKeys()
		.softValues()
		.makeMap();
		this.policySetIDRefCache = new MapMaker()
		.initialCapacity(size)
		.softKeys()
		.softValues()
		.makeMap();
	}
	
	/**
	 * Implementation assumes that 
	 * {@link #getPolicies(String, VersionMatch, VersionMatch, VersionMatch)}
	 * returns unordered collection of policies
	 */
	@Override
	public Policy getPolicy(String id, VersionMatch version, 
			VersionMatch earliest, VersionMatch latest){
		Collection<Policy> found = getPolicies(id, version, earliest, latest);
		if(found.isEmpty()){
			return null;
		}
		return Collections.<Policy>max(found, new Comparator<Policy>() {
			@Override
			public int compare(Policy a, Policy b) {
				return a.getVersion().compareTo(b.getVersion());
			}
		});
	}
	
	/**
	 * Implementation assumes that 
	 * {@link #getPolicies(String, VersionMatch, VersionMatch, VersionMatch)}
	 * returns unordered collection of policies. 
	 */
	@Override
	public PolicySet getPolicySet(String id, VersionMatch version, 
			VersionMatch earliest, VersionMatch latest)
	{
		Collection<PolicySet> found = getPolicySets(id, version, earliest, latest);
		if(found.isEmpty()){
			return null;
		}
		return Collections.<PolicySet>max(found, new Comparator<PolicySet>() {
			@Override
			public int compare(PolicySet a, PolicySet b) {
				return a.getVersion().compareTo(b.getVersion());
			}
			
		});
	}
	
	@Override
	public final Collection<Policy> getPolicies(String id) {
		return getPolicies(id, null);
	}

	@Override
	public final Collection<Policy> getPolicies(String id, VersionMatch earliest,
			VersionMatch latest) {
		return getPolicies(id, null, earliest, latest);
	}

	@Override
	public final Collection<Policy> getPolicies(String id, VersionMatch version){
		return getPolicies(id, version, null, null);
	}
	
	@Override
	public final Collection<PolicySet> getPolicySets(String id, VersionMatch version){
		return getPolicySets(id, version, null, null);
	}

	/**
	 * A default implementation invokes 
	 * {@link #getPolicy(String, VersionMatch, VersionMatch, VersionMatch)
	 */
	@Override
	public Policy resolve(PolicyIDReference ref)
			throws PolicyResolutionException 
	{
		Policy p =  policyIDRefCache.get(ref);
		if(p != null){
			if(log.isDebugEnabled()){
				log.debug("Found Policy id=\"{}\" " +
						"version=\"{}\" for reference=\"{}\" in the cache", 
						new Object[]{p.getId(), p.getVersion(), ref});
			}
			return p;
		}
		p =  getPolicy(
					ref.getId(), 
					ref.getVersionMatch(), 
					ref.getEarliestVersion(), 
					ref.getLatestVersion());
		if(p != null && 
				enableRefCache){
			policyIDRefCache.put(ref, p);
		}
		return p;
	}
	
	/**
	 * A default implementation invokes 
	 * {@link #getPolicySet(String, VersionMatch, VersionMatch, VersionMatch)
	 */
	@Override
	public PolicySet resolve(PolicySetIDReference ref)
			throws PolicyResolutionException 
	{
		PolicySet p = policySetIDRefCache.get(ref);
		if(p != null){
			if(log.isDebugEnabled()){
				log.debug("Found PolicySet id=\"{}\" " +
						"version=\"{}\" for reference=\"{}\" in the cache", 
						new Object[]{p.getId(), p.getVersion(), ref});
			}
			return p;
		}
		p =  getPolicySet(
					ref.getId(), 
					ref.getVersionMatch(), 
					ref.getEarliestVersion(), 
					ref.getLatestVersion());
		if(p != null && 
				enableRefCache){
			policySetIDRefCache.put(ref, p);
		}
		return p;
	}
	
	protected final void clearRefCahce(){
		policyIDRefCache.clear();
		policySetIDRefCache.clear();
	}
}
