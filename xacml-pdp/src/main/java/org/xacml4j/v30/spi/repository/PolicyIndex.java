package org.xacml4j.v30.spi.repository;

/*
 * #%L
 * Xacml4J PDP related classes
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
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.pdp.Policy;
import org.xacml4j.v30.pdp.PolicyReference;
import org.xacml4j.v30.pdp.PolicySet;
import org.xacml4j.v30.pdp.PolicySetReference;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Represents an in memory policy index
 * by policy identifier and version. An index
 * is initialized from a given
 * {@link org.xacml4j.v30.spi.repository.PolicySource}
 * on the first index usage
 *
 * @author Giedrius Trumpickas
 */
final class PolicyIndex
{
    private final static Logger log = LoggerFactory.getLogger(PolicyIndex.class);

    private final static int INITIAL_POLICYSET_MAP_SIZE = 128;
    private final static int INITIAL_POLICY_MAP_SIZE = 128;

    private final ConcurrentMap<String, ConcurrentNavigableMap<Version, Policy>> policies;
    private final ConcurrentMap<String, ConcurrentNavigableMap<Version, PolicySet>> policySets;

    private PolicySource source;
    private boolean indexInitialized = false;

    /**
     * Constructs policy index with a
     * given policy source
     * @param source a policy source
     */
    public PolicyIndex(PolicySource source)
    {
        Preconditions.checkNotNull(source);
        this.source = source;
        // policy and policy set id's
        // are cases insensitive in the index
        this.policies = new ConcurrentSkipListMap<String, ConcurrentNavigableMap<Version, Policy>>(String.CASE_INSENSITIVE_ORDER);
        this.policySets = new ConcurrentSkipListMap<String, ConcurrentNavigableMap<Version, PolicySet>>(String.CASE_INSENSITIVE_ORDER);
    }

    /**
     * Returns policy source identifier
     *
     * @return policy source identifier
     */
    public String getId(){
        return source.getId();
    }

    /**
     * Gets all matching policies matching specified
     * version constraints
     *
     * @param ref a policy reference
     * @return an list over all matching policies,
     * sorted by ascending version order
     */
    public Iterable<Policy> getPolicies(PolicyReference ref)
    {
        initializeIndex();
        Map<Version, Policy> byId = policies.get(ref.getId());
        if(byId == null){
            return ImmutableList.of();
        }
        return find(byId.values(), ref);
    }


    /**
     * Performs policy index initialization
     * with the policies from the source
     */
    private void initializeIndex()
    {
        if(indexInitialized){
            return;
        }
        try{
            if(log.isDebugEnabled()){
                log.debug("Initializing policy index from source");
            }
            Iterable<CompositeDecisionRule> policies = source.getPolicies();
            for(CompositeDecisionRule p : policies){
                add(p);
            }
            if(log.isDebugEnabled()){
                log.debug("Policy index initialization is done");
            }
            source.addListener(new SourceChangeListener());
            indexInitialized = true;
        }catch(IOException e){
            log.error("Failed to initialize policy " +
                    "index from given source", e);
        }

    }

    /**
     * Gets all matching policy sets matching specified
     * version constraints
     *
     * @param ref a policy reference
     * @return an list over all matching policy sets,
     * sorted by descending version order
     */
    public Iterable<PolicySet> getPolicySets(PolicySetReference ref)
    {
        initializeIndex();
        Map<Version, PolicySet> byId = policySets.get(ref.getId());
        if(byId == null){
            return ImmutableList.of();
        }
        return find(byId.values(), ref);
    }

    /**
     * Gets composite decision rule for
     * specific identifier and version
     *
     * @param id a rule identifier
     * @param v a rule version
     * @return {@link org.xacml4j.v30.CompositeDecisionRule} or
     * <code>null</code>
     */
    public CompositeDecisionRule get(String id, Version v) {
        initializeIndex();
        Map<Version, Policy> pv = policies.get(id);
        if(pv != null){
            return pv.get(v);
        }
        Map<Version, PolicySet> psv = policySets.get(id);
        return (psv != null)?psv.get(v):null;
    }

    /**
     * Adds given composite decision rule to this index
     *
     * @param r a composite decision rule
     * @return <code>true</code> if rule
     * was added successfully
     */
    private boolean add(CompositeDecisionRule r){
        if(r instanceof Policy){
            return addPolicy((Policy)r);
        }
        if(r instanceof PolicySet){
            return addPolicySet((PolicySet)r);
        }
        return false;
    }

    /**
     * Removes composite decision rule from this index
     *
     * @param r a composite decision rule
     * @return <code>true</code> if rule was removed
     * successfully
     */
    private boolean remove(CompositeDecisionRule r){
        if(r instanceof Policy){
            return removePolicy((Policy)r);
        }
        if(r instanceof PolicySet){
            return removePolicySet((PolicySet)r);
        }
        return false;
    }

    /**
     * Adds policy to the index
     *
     * @param policy a policy to be added
     * @return <code>true</code> if policy was
     * added successfully, <code>false</code>
     * if policy with given id and version
     * already exist in the index
     */
    private boolean addPolicy(Policy policy)
    {
        if(policy == null){
            return false;
        }
        String id = policy.getId();
        Version v = policy.getVersion();
        if(log.isDebugEnabled()){
            log.debug("Adding Policy with " +
                    "attributeId=\"{}\" version=\"{}\" index", id, v);
        }
        ConcurrentNavigableMap<Version, Policy> versions = policies.get(id);
        if(versions == null){
            versions = new ConcurrentSkipListMap<Version, Policy>(Collections.<Version>reverseOrder());
            ConcurrentNavigableMap<Version, Policy> existing = policies.putIfAbsent(id, versions);
            if(existing != null){
                versions = existing;
            }
        }
        return versions.putIfAbsent(v, policy) == null;
    }

    /**
     * Adds policy set to the index
     *
     * @return <code>true</code> if policy set
     * was added successfully, <code>false</code>
     * if policy set with given id and version
     * already exist in the index
     */
    public boolean addPolicySet(PolicySet policySet)
    {
        if(policySet == null){
            return false;
        }
        String id = policySet.getId();
        Version v = policySet.getVersion();
        if(log.isDebugEnabled()){
            log.debug("Adding PolicySet with " +
                    "attributeId=\"{}\" version=\"{}\" to the index", id, v);
        }
        ConcurrentNavigableMap<Version, PolicySet> versions = policySets.get(id);
        if(versions == null){
            versions = new ConcurrentSkipListMap<Version, PolicySet>(Collections.<Version>reverseOrder());
            ConcurrentNavigableMap<Version, PolicySet> existing = policySets.putIfAbsent(id, versions);
            if(existing != null){
                versions = existing;
            }
        }
        return versions.putIfAbsent(v, policySet) == null;
    }

    /**
     * Removes policy from the index
     *
     * @param p a policy to be removed
     * @return <code>true</code> if policy
     * was removed from the index
     */
    public boolean removePolicy(Policy p)
    {
        if(p == null){
            return false;
        }
        String id = p.getId();
        Version v = p.getVersion();
        if(log.isDebugEnabled()){
            log.debug("Removing Policy with " +
                    "attributeId=\"{}\" version=\"{}\" from index", id, v);
        }
        ConcurrentNavigableMap<Version, Policy> versions = policies.get(id);
        if(versions != null){
            return versions.remove(v) != null;
        }
        return (versions != null) && (versions.remove(v) != null);
    }

    /**
     * Removes policy set from the index
     *
     * @param p a policy set to be removed
     * @return <code>true</code> if policy
     * set was removed from the index
     */
    public boolean removePolicySet(PolicySet p)
    {
        if(p == null){
            return false;
        }
        String id = p.getId();
        Version v = p.getVersion();
        if(log.isDebugEnabled()){
            log.debug("Removing PolicySet with " +
                    "attributeId=\"{}\" version=\"{}\" from index", id, v);
        }
        ConcurrentNavigableMap<Version, PolicySet> versions = policySets.get(id);
        if(versions != null){
            return versions.remove(v) != null;
        }
        return (versions != null) && (versions.remove(v) != null);
    }

    /**
     * A helper method to filter given iterable
     *
     * @param id an iterable over decision rules
     * @param ref a decision rule reference
     * @param <T>
     * @return an iterable over matching decision rules
     */
    private <T extends CompositeDecisionRule> Iterable<T> find(
            Iterable<T> id,
            final CompositeDecisionRuleIDReference ref) {
        if (id == null) {
            return ImmutableList.of();
        }
        return FluentIterable.from(id).filter(new Predicate<T>() {
            @Override
            public boolean apply(T p) {
                boolean isRefTo = ref.isReferenceTo(p);
                if(log.isDebugEnabled()){
                    log.debug("Matching policy " +
                            "decision rule id=\"{}\" version=\"{}\" " +
                            "with reference=\"{}\" result is=\"{}\"",
                            new Object[]{p.getId(), p.getVersion(), ref, isRefTo});
                }
                return isRefTo;
            }
        }).toList();
    }

    /**
     * A change listener for policy source
     */
    private class SourceChangeListener
            implements PolicySource.ChangeListener
    {
        @Override
        public void policyAdded(Policy... policies) {
            for(Policy p : policies){
                addPolicy(p);
            }
        }

        @Override
        public void policyRemoved(Policy... policies) {
            for(Policy p : policies){
                removePolicy(p);
            }
        }

        @Override
        public void policySetAdded(PolicySet... policies) {
            for(PolicySet p : policies){
                addPolicySet(p);
            }
        }

        @Override
        public void policySetRemoved(PolicySet... policies) {
            for(PolicySet p : policies){
                removePolicySet(p);
            }
        }
    }
}
