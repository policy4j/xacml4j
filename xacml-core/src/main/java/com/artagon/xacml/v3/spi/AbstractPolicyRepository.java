package com.artagon.xacml.v3.spi;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicyIDReference;
import com.artagon.xacml.v3.PolicyResolutionException;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.PolicySetIDReference;
import com.artagon.xacml.v3.VersionMatch;
import com.google.common.collect.Iterables;

/**
 * A base class for {@link PolicyRepository} implementations
 * 
 * @author Giedrius Trumpickas
 */
public abstract class AbstractPolicyRepository implements PolicyRepository
{
	private final static Logger log = LoggerFactory.getLogger(AbstractPolicyRepository.class);
	
	@Override
	public final Policy resolve(EvaluationContext context, PolicyIDReference ref)
			throws PolicyResolutionException {
		if(log.isDebugEnabled()){
			log.debug("Resolving policy reference=\"{}\"", ref);
		}
		Collection<Policy> found = getPolicies(ref.getId(), ref.getVersionMatch(), 
				ref.getEarliestVersion(), ref.getLatestVersion());
		Policy policy =  found.isEmpty()?null:Iterables.getLast(found);
		if(log.isDebugEnabled() && 
				policy != null){
			log.debug("Resolved reference to policySet id=\"{}\" " +
					"version=\"{}\"", policy.getId(), policy.getVersion());
		}
		return policy;
	}

	@Override
	public final PolicySet resolve(EvaluationContext context, PolicySetIDReference ref)
			throws PolicyResolutionException {
		Collection<PolicySet> found = getPolicySets(ref.getId(), ref.getVersionMatch(), 
				ref.getEarliestVersion(), ref.getLatestVersion());
		if(log.isDebugEnabled()){
			log.debug("Resolving policy reference=\"{}\"", ref);
		}
		PolicySet policySet = found.isEmpty()?null:Iterables.getLast(found);
		if(log.isDebugEnabled() && 
				policySet != null){
			log.debug("Resolved reference to policySet id=\"{}\" " +
					"version=\"{}\"", policySet.getId(), policySet.getVersion());
		}
		return policySet;
	}
	
	@Override
	public final Collection<Policy> getPolicies(String id, VersionMatch version){
		return getPolicies(id, version, null, null);
	}
	
	@Override
	public final Collection<PolicySet> getPolicySets(String id, VersionMatch version){
		return getPolicySets(id, version, null, null);
	}
	
}
