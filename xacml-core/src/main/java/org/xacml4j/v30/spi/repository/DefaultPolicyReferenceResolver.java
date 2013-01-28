package org.xacml4j.v30.spi.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.PolicyResolutionException;
import org.xacml4j.v30.pdp.Policy;
import org.xacml4j.v30.pdp.PolicyIDReference;
import org.xacml4j.v30.pdp.PolicySet;
import org.xacml4j.v30.pdp.PolicySetIDReference;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * A default implementation of {@link PolicyReferenceResolver}.
 * Maintains a cache of resolved policies by the reference.
 * 
 * @author Giedrius Trumpickas
 */
public class DefaultPolicyReferenceResolver 
	implements PolicyReferenceResolver, PolicyRepositoryListener
{
	private final static Logger log = LoggerFactory.getLogger(DefaultPolicyReferenceResolver.class);
	
	private Cache<PolicyIDReference, Policy> policyIDRefCache;
	private Cache<PolicySetIDReference, PolicySet> policySetIDRefCache;
	
	private PolicyRepository repository;
	private boolean enableRefCache;
	
	public DefaultPolicyReferenceResolver(
			PolicyRepository repository){
		this(repository, true, 1024);
	}
	
	public DefaultPolicyReferenceResolver(
			PolicyRepository policyRepository, 
			boolean enabledRefCache, int size)
	{
		Preconditions.checkNotNull(policyRepository);
		this.repository = policyRepository;
		Preconditions.checkState(repository != null);
		this.enableRefCache = enabledRefCache;
		this.policyIDRefCache = CacheBuilder
				.newBuilder()
				.initialCapacity(1024)
				.build();
		this.policySetIDRefCache = CacheBuilder
				.newBuilder()
				.initialCapacity(1024)
				.build();
		this.repository.addPolicyRepositoryListener(this);
	}
	
	/**
	 * A default implementation invokes 
	 * {@link #getPolicy(String, VersionMatch, VersionMatch, VersionMatch)
	 */
	@Override
	public Policy resolve(PolicyIDReference ref)
			throws PolicyResolutionException 
	{
		Policy p =  policyIDRefCache.getIfPresent(ref);
		if(p != null){
			if(log.isDebugEnabled()){
				log.debug("Found Policy id=\"{}\" " +
						"version=\"{}\" for reference=\"{}\" in the cache", 
						new Object[]{p.getId(), p.getVersion(), ref});
			}
			return p;
		}
		p =  repository.getPolicy(
					ref.getId(), 
					ref.getVersion(), 
					ref.getEarliestVersion(), 
					ref.getLatestVersion());
		if(p != null && 
				enableRefCache){
			policyIDRefCache.put(ref, p);
		}
		if(p != null && 
				log.isDebugEnabled()){
			log.debug("Resolved policy id=\"{}\" " +
					"version=\"{}\" for reference=\"{}\" from repository", 
					new Object[]{p.getId(), p.getVersion(), ref});
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
		PolicySet p = policySetIDRefCache.getIfPresent(ref);
		if(p != null){
			if(log.isDebugEnabled()){
				log.debug("Found PolicySet id=\"{}\" " +
						"version=\"{}\" for reference=\"{}\" in the cache", 
						new Object[]{p.getId(), p.getVersion(), ref});
			}
			return p;
		}
		p =  repository.getPolicySet(
					ref.getId(), 
					ref.getVersion(), 
					ref.getEarliestVersion(), 
					ref.getLatestVersion());
		if(p != null && 
				enableRefCache){
			policySetIDRefCache.put(ref, p);
		}
		if(p != null && log.isDebugEnabled()){
			log.debug("Resolved policy set id=\"{}\" " +
					"version=\"{}\" for reference=\"{}\" from repository", 
					new Object[]{p.getId(), p.getVersion(), ref});
		}
		return p;
	}
	
	protected final void clearRefCahce(){
		policyIDRefCache.invalidateAll();
		policySetIDRefCache.invalidateAll();
	}

	/**
	 * Removes a cached references pointing
	 * to the given policy
	 * 
	 * @param p a policy
	 */
	private void removeCachedReferences(Policy p)
	{
		for(PolicyIDReference ref : policyIDRefCache.asMap().keySet()){
			if(ref.isReferenceTo(p)){
				if(log.isDebugEnabled()){
					log.debug("Removing=\"{}\" from cache", ref);
				}
				policyIDRefCache.invalidate(ref);
			}
		}
	}
	
	/**
	 * Removes a cached references pointing
	 * to the given policy set
	 * 
	 * @param p a policy set
	 */
	private void removeCachedReferences(PolicySet p)
	{
		for(PolicySetIDReference ref : policySetIDRefCache.asMap().keySet()){
			if(ref.isReferenceTo(p)){
				if(log.isDebugEnabled()){
					log.debug("Removing=\"{}\" from cache", ref);
				}
				policySetIDRefCache.invalidate(ref);
			}
		}
	}

	@Override
	public void policyAdded(Policy p) {
		removeCachedReferences(p);
	}

	@Override
	public void policyRemoved(Policy p) {
		removeCachedReferences(p);
	}

	@Override
	public void policySetAdded(PolicySet p) {
		removeCachedReferences(p);
	}

	@Override
	public void policySetRemoved(PolicySet p) {
		removeCachedReferences(p);		
	}
}
