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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.VersionMatch;
import org.xacml4j.v30.policy.DecisionCombiningAlgorithm;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.PolicyTreeVisitor;
import org.xacml4j.v30.policy.PolicyVisitorSupport;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;

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
	private final List<PolicyRepositoryListener> listeners;

	private final FunctionProvider functions;
	private final DecisionCombiningAlgorithmProvider decisionAlgorithms;
	private Executor notificationExecutor;

	protected AbstractPolicyRepository(
			String id,
			FunctionProvider functions,
			DecisionCombiningAlgorithmProvider decisionAlgorithms)
	{
		this(id, functions, decisionAlgorithms, Executors.newSingleThreadExecutor());
	}

	protected AbstractPolicyRepository(
			String id,
			FunctionProvider functions,
			DecisionCombiningAlgorithmProvider decisionAlgorithms, Executor executor)
	{
		this.id = Objects.requireNonNull(id, "id");;
		this.functions = Objects.requireNonNull(functions, "functions");
		this.decisionAlgorithms = Objects.requireNonNull(decisionAlgorithms, "decisionAlgorithms");;
		this.listeners = new CopyOnWriteArrayList<>();
		this.notificationExecutor = Objects.requireNonNull(executor, "executor");
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
		return Collections.max(found, Comparator
				.comparing((a)->a.getVersion()));
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
		return Collections.max(found, Comparator
				.comparing(a -> a.getVersion()));
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
		this.listeners.add(l);
	}

	@Override
	public final void removePolicyRepositoryListener(PolicyRepositoryListener l){
		listeners.remove(l);
	}

	private void notifyPolicyAdded(Policy p){
		for(PolicyRepositoryListener l : listeners){
			notificationExecutor.execute(
					()->l.policyAdded(p));
		}
	}

	private void notifyPolicySetAdded(PolicySet p){
		for(PolicyRepositoryListener l : listeners){
			notificationExecutor.execute(
					()->l.policySetAdded(p));
		}
	}

	private void notifyPolicyRemoved(Policy p){
		for(PolicyRepositoryListener l : listeners){
			notificationExecutor.execute(
					()->l.policyRemoved(p));
		}
	}

	private void notifyPolicySetRemoved(PolicySet p){
		for(PolicyRepositoryListener l : listeners){
			notificationExecutor.execute(
					()->l.policySetAdded(p));
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

	/**
	 * A {@link PolicyTreeVisitor} implementation
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
