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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.VersionMatch;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.marshal.PolicyUnmarshaller;
import org.xacml4j.v30.marshal.jaxb.XacmlPolicyUnmarshaller;
import org.xacml4j.v30.pdp.DecisionCombiningAlgorithm;
import org.xacml4j.v30.pdp.Policy;
import org.xacml4j.v30.pdp.PolicySet;
import org.xacml4j.v30.pdp.PolicyVisitorSupport;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.spi.function.FunctionProvider;

import com.google.common.base.Preconditions;

/**
 * A base class for {@link PolicyRepository} implementations.
 *
 * @author Giedrius Trumpickas
 */
public abstract class AbstractPolicyRepository
	implements PolicyRepository
{
	private final static Logger log = LoggerFactory.getLogger(AbstractPolicyRepository.class);

	private final String id;
	private final ConcurrentMap<PolicyRepositoryListener, PolicyRepositoryListener> listeners;

	private final PolicyUnmarshaller unmarshaller;

	private final FunctionProvider functions;
	private final DecisionCombiningAlgorithmProvider decisionAlgorithms;

	protected AbstractPolicyRepository(
			String id,
			FunctionProvider functions,
			DecisionCombiningAlgorithmProvider decisionAlgorithms)
		throws Exception
	{
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(functions);
		Preconditions.checkNotNull(decisionAlgorithms);
		this.id = id;
		this.functions = functions;
		this.decisionAlgorithms = decisionAlgorithms;
		this.listeners = new ConcurrentHashMap<PolicyRepositoryListener, PolicyRepositoryListener>();
		this.unmarshaller = new XacmlPolicyUnmarshaller(functions, decisionAlgorithms);
	}

	@Override
	public final String getId() {
		return id;
	}

	/**
	 * Implementation assumes that
	 * {@link PolicyRepository#getPolicies(String, VersionMatch, VersionMatch, VersionMatch)}
	 * returns unordered collection of policies
	 */
	@Override
	public Policy getPolicy(String id, VersionMatch version,
			VersionMatch earliest, VersionMatch latest){
		Collection<Policy> found = getPolicies(id, version, earliest, latest);
		if(found.isEmpty()){
			return null;
		}
		return Collections.max(found, new Comparator<Policy>() {
			@Override
			public int compare(Policy a, Policy b) {
				return a.getVersion().compareTo(b.getVersion());
			}
		});
	}

	/**
	 * Implementation assumes that
	 * {@link PolicyRepository#getPolicies(String, VersionMatch, VersionMatch, VersionMatch)}
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
		return Collections.max(found, new Comparator<PolicySet>() {
			@Override
			public int compare(PolicySet a, PolicySet b) {
				return a.getVersion().compareTo(b.getVersion());
			}

		});
	}

	/**
	 * @see AbstractPolicyRepository#getPolicySet(String, VersionMatch, VersionMatch, VersionMatch)
	 */
	@Override
	public final Collection<Policy> getPolicies(String id) {
		return getPolicies(id, null);
	}

	/**
	 * @see AbstractPolicyRepository#getPolicySet(String, VersionMatch, VersionMatch, VersionMatch)
	 */
	@Override
	public final Collection<Policy> getPolicies(String id, VersionMatch earliest,
			VersionMatch latest) {
		return getPolicies(id, null, earliest, latest);
	}

	/**
	 * @see AbstractPolicyRepository#getPolicySet(String, VersionMatch, VersionMatch, VersionMatch)
	 */
	@Override
	public final Collection<Policy> getPolicies(String id, VersionMatch version){
		return getPolicies(id, version, null, null);
	}

	/**
	 * @see AbstractPolicyRepository#getPolicySet(String, VersionMatch, VersionMatch, VersionMatch)
	 */
	@Override
	public final Collection<PolicySet> getPolicySets(String id, VersionMatch version){
		return getPolicySets(id, version, null, null);
	}

	@Override
	public final void addPolicyRepositoryListener(PolicyRepositoryListener l){
		this.listeners.put(l, l);
	}

	@Override
	public final void removePolicyRepositoryListener(PolicyRepositoryListener l){
		listeners.remove(l);
	}

	private void notifyPolicyAdded(Policy p){
		for(PolicyRepositoryListener l : listeners.keySet()){
			l.policyAdded(p);
		}
	}

	private void notifyPolicySetAdded(PolicySet p){
		for(PolicyRepositoryListener l : listeners.keySet()){
			l.policySetAdded(p);
		}
	}

	private void notifyPolicyRemoved(Policy p){
		for(PolicyRepositoryListener l : listeners.keySet()){
			l.policyRemoved(p);
		}
	}

	private void notifyPolicySetRemoved(PolicySet p){
		for(PolicyRepositoryListener l : listeners.keySet()){
			l.policySetAdded(p);
		}
	}

	/**
	 * Default implementation uses
	 * {@link PolicyRepository#getPolicies(String, VersionMatch, VersionMatch, VersionMatch)}
	 * {@link AbstractPolicyRepository#getPolicySet(String, VersionMatch, VersionMatch, VersionMatch)}
	 * to locate appropriate policy or policy set
	 *
	 * @see AbstractPolicyRepository#getPolicies(String, VersionMatch, VersionMatch, VersionMatch)
	 * @see AbstractPolicyRepository#getPolicySet(String, VersionMatch, VersionMatch, VersionMatch)
	 */
	@Override
	public CompositeDecisionRule get(String id, Version v)
	{
		VersionMatch m = new VersionMatch(v.getValue());
		Policy p = getPolicy(id, m, null, null);
		return (p != null)?p:getPolicySet(id, m, null, null);
	}

	@Override
	public final boolean add(CompositeDecisionRule r)
	{
		Preconditions.checkNotNull(r);
		r.accept(new DecisionAlgorithmValidatingVisitor());
		//r.accept(new FunctionValidatingVisitor());
		if(r instanceof Policy){
			Policy p = (Policy)r;
			if(addPolicy(p)){
				notifyPolicyAdded(p);
				return true;
			}
			if(log.isWarnEnabled()){
				log.warn("Policy with id=\"{}\" " +
						"and version=\"{}\" already exist in this repository",
						p.getId(), p.getVersion());
			}
		}
		if(r instanceof PolicySet){
			PolicySet p = (PolicySet)r;
			if(addPolicySet(p)){
				notifyPolicySetAdded(p);
				return true;
			}
			if(log.isWarnEnabled()){
				log.warn("PolicySet with id=\"{}\" " +
						"and version=\"{}\" already exist in this repository",
						p.getId(), p.getVersion());
			}
		}
		return false;
	}

	@Override
	public boolean remove(CompositeDecisionRule r) {
		if(r instanceof Policy){
			Policy p = (Policy)r;
			if(removePolicy(p)){
				notifyPolicyRemoved(p);
				return true;
			}
			return false;
		}
		if(r instanceof PolicySet){
			PolicySet p = (PolicySet)r;
			if(removePolicySet(p)){
				notifyPolicySetRemoved(p);
				return true;
			}
		}
		return false;
	}

	/**
	 * Implemented by subclass to add given
	 * policy to this repository
	 *
	 * @param p a policy
	 */
	protected abstract boolean addPolicy(Policy p);

	/**
	 * Implemented by subclass to add policy set
	 * to this repository
	 *
	 * @param p a policy set
	 */
	protected abstract boolean addPolicySet(PolicySet p);



	protected abstract  boolean removePolicy(Policy p);
	protected abstract boolean removePolicySet(PolicySet p);

	@Override
	public final CompositeDecisionRule importPolicy(InputStream source)
			throws XacmlSyntaxException, IOException {
		CompositeDecisionRule r =  unmarshaller.unmarshal(source);
		add(r);
		return r;
	}

	/**
	 * A {@link org.xacml4j.v30.pdp.PolicyVisitor} implementation
	 * to validate {@link DecisionCombiningAlgorithm} instances
	 * in a given policy or policy set
	 */
	private class DecisionAlgorithmValidatingVisitor
		extends PolicyVisitorSupport
	{
		@Override
		public void visitEnter(Policy policy) {
			String id = policy.getRuleCombiningAlgorithm().getId();
			Preconditions.checkArgument(
					decisionAlgorithms.isRuleAlgorithmProvided(id),
							"Rule combining algorithm=\"%s\" is not defined in this repository", id);
		}

		@Override
		public void visitEnter(PolicySet policySet) {
			String id = policySet.getPolicyDecisionCombiningAlgorithm().getId();
			Preconditions.checkArgument(
					decisionAlgorithms.isPolicyAlgorithmProvided(id),
					"Policy combining algorithm=\"%s\" is not defined in this repository", id);
		}
	}
}
