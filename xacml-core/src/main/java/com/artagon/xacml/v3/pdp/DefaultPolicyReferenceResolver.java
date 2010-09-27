
package com.artagon.xacml.v3.pdp;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicyIDReference;
import com.artagon.xacml.v3.PolicyReferenceResolver;
import com.artagon.xacml.v3.PolicyResolutionException;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.PolicySetIDReference;
import com.artagon.xacml.v3.Version;
import com.artagon.xacml.v3.spi.AbstractPolicyRepository;
import com.artagon.xacml.v3.spi.PolicyRepository;
import com.google.common.base.Preconditions;

public class DefaultPolicyReferenceResolver 
	implements PolicyReferenceResolver
{
	private final static Logger log = LoggerFactory.getLogger(AbstractPolicyRepository.class);
	
	private PolicyRepository repository;
	
	/**
	 * A cache of recently resolved policies
	 */
	private ConcurrentHashMap<String, ConcurrentHashMap<Version, Policy>> policyCache;
	
	/**
	 * A cache of recently resolved policy sets
	 */
	private ConcurrentHashMap<String, ConcurrentHashMap<Version, PolicySet>> policySetCache;
	
	/**
	 * Constructs a policy resolver with a given {@link PolicyRepository}
	 * 
	 * @param repository a policy repository
	 */
	public DefaultPolicyReferenceResolver(PolicyRepository repository)
	{
		Preconditions.checkNotNull(repository);
		this.repository = repository;
		this.policyCache = new ConcurrentHashMap<String, ConcurrentHashMap<Version,Policy>>();
		this.policySetCache = new ConcurrentHashMap<String, ConcurrentHashMap<Version,PolicySet>>();
	}
	
	@Override
	public final Policy resolve(EvaluationContext context, PolicyIDReference ref)
			throws PolicyResolutionException 
	{
		if(log.isDebugEnabled()){
			log.debug("Resolving policy reference=\"{}\"", ref);
		}
		Policy policy = doResolve(ref);
		if(log.isDebugEnabled() && 
				policy != null){
			log.debug("Resolved reference to policySet id=\"{}\" " +
					"version=\"{}\"", policy.getId(), policy.getVersion());
		}
		return policy;
	}

	@Override
	public final PolicySet resolve(EvaluationContext context, PolicySetIDReference ref)
			throws PolicyResolutionException 
	{
		if(log.isDebugEnabled()){
			log.debug("Resolving policy reference=\"{}\"", ref);
		}
		PolicySet policySet = doResolve(ref);
		if(log.isDebugEnabled() && 
				policySet != null){
			log.debug("Resolved reference to policySet id=\"{}\" " +
					"version=\"{}\"", policySet.getId(), policySet.getVersion());
		}
		return policySet;
	}
	
	private Policy doResolve(PolicyIDReference ref) 
		throws PolicyResolutionException
	{
		ConcurrentHashMap<Version, Policy> versions = policyCache.get(ref.getId());
		if(versions != null){
			return versions.get(
					Collections.max(versions.keySet()));
		}
		Policy p = repository.getPolicy(
				ref.getId(), 
				ref.getVersionMatch(), 
				ref.getEarliestVersion(), 
				ref.getLatestVersion());
		if(p != null){
			versions = new ConcurrentHashMap<Version, Policy>();
			versions.put(p.getVersion(), p);
			versions = policyCache.putIfAbsent(p.getId(), versions);
			if(versions != null){
				versions.putIfAbsent(p.getVersion(), p);
			}
		}
		return p;
	}
	
	private PolicySet doResolve(PolicySetIDReference ref) 
		throws PolicyResolutionException
	{
		ConcurrentHashMap<Version, PolicySet> versions = policySetCache.get(ref.getId());
		if(versions != null){
			return versions.get(
					Collections.max(versions.keySet()));
		}
		PolicySet p = repository.getPolicySet(
				ref.getId(), 
				ref.getVersionMatch(), 
				ref.getEarliestVersion(), 
				ref.getLatestVersion());
		if(p != null){
			versions = new ConcurrentHashMap<Version, PolicySet>();
			versions.put(p.getVersion(), p);
			versions = policySetCache.putIfAbsent(p.getId(), versions);
			if(versions != null){
				versions.putIfAbsent(p.getVersion(), p);
			}
		}
		return p;
	}
}
