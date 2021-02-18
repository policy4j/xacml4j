package org.xacml4j.v30.spi.repository;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.PolicyResolutionException;
import org.xacml4j.v30.pdp.Policy;
import org.xacml4j.v30.pdp.PolicyIDReference;
import org.xacml4j.v30.pdp.PolicySet;
import org.xacml4j.v30.pdp.PolicySetIDReference;

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

	private final Cache<PolicyIDReference, Policy> policyIDRefCache;
	private final Cache<PolicySetIDReference, PolicySet> policySetIDRefCache;

	private final PolicyRepository repository;
	private final boolean enableRefCache;

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

	protected final void clearRefCache(){
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
