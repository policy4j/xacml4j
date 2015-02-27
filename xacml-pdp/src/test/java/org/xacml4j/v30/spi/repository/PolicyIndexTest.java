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


import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.pdp.DecisionCombiningAlgorithm;
import org.xacml4j.v30.pdp.Policy;
import org.xacml4j.v30.pdp.PolicyReference;
import org.xacml4j.v30.pdp.PolicySet;

import java.io.IOException;
import java.util.Collection;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

public class PolicyIndexTest
{
    private PolicyIndex index;
    private PolicySource source;
    private DecisionCombiningAlgorithm algo;
    private IMocksControl control;

    @Before
    public void init(){
        this.control = createControl();
        this.source = control.createMock(PolicySource.class);
        this.algo = control.createMock(DecisionCombiningAlgorithm.class);
        this.index = new PolicyIndex(source);
    }


    @Test
    public void testPoliciesInSource() throws IOException
    {
        ImmutableList.Builder<CompositeDecisionRule> b = ImmutableList.builder();
        b.add(Policy.builder("testId").combiningAlgorithm(algo).version("1.0.0").build());
        b.add(Policy.builder("testId").combiningAlgorithm(algo).version("1.1.0").build());
        b.add(Policy.builder("testId").combiningAlgorithm(algo).version("1.2.0").build());
        b.add(Policy.builder("testId").combiningAlgorithm(algo).version("1.2.1").build());
        b.add(Policy.builder("testId").combiningAlgorithm(algo).version("1.3.0").build());
        b.add(Policy.builder("testId").combiningAlgorithm(algo).version("1.3.1").build());

        Collection<CompositeDecisionRule> sourcePolicies = b.build();
        Capture<PolicySource.ChangeListener> listener = new Capture<PolicySource.ChangeListener>();

        expect(source.getPolicies()).andReturn(sourcePolicies);
        source.addListener(capture(listener));

        control.replay();
        Iterable<Policy> found = index.getPolicies(PolicyReference
                .builder("testId")
                .earliest("1.1.0")
                .latest("1.3.0").build());
        assertEquals(4, Iterables.size(found));
        control.verify();
    }

    @Test
    public void testCaseInsensitivePolicyOrPolicySetId() throws IOException
    {
        ImmutableList.Builder<CompositeDecisionRule> b = ImmutableList.builder();
        b.add(Policy.builder("testPolicyId").combiningAlgorithm(algo).version("1.0.0").build());
        b.add(PolicySet.builder("testPolicySetId").combiningAlgorithm(algo).version("1.1.0").build());

        Collection<CompositeDecisionRule> sourcePolicies = b.build();
        Capture<PolicySource.ChangeListener> listener = new Capture<PolicySource.ChangeListener>();

        expect(source.getPolicies()).andReturn(sourcePolicies);
        source.addListener(capture(listener));

        control.replay();
        Iterable<Policy> found = index.getPolicies(PolicyReference
                .builder("testPolicyId").build());
        assertEquals(1, Iterables.size(found));

        found = index.getPolicies(PolicyReference
                .builder("testPOLICYID").build());
        assertEquals(1, Iterables.size(found));
        control.verify();
    }
}
