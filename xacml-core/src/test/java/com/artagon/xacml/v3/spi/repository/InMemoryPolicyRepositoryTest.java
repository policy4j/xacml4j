package com.artagon.xacml.v3.spi.repository;


import static org.easymock.EasyMock.createControl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.Rule;
import com.artagon.xacml.v3.Version;
import com.artagon.xacml.v3.VersionMatch;

public class InMemoryPolicyRepositoryTest
{
	private Policy p1v1;
	private Policy p1v2;
	private Policy p1v3;
	private Policy p1v4;
	
	private PolicyRepository r;
	private DecisionCombiningAlgorithm<Rule> algorithm;
	
	private IMocksControl c;
	private PolicyRepositoryListener l;
	
	@SuppressWarnings("unchecked")
	@Before
	public void init() throws Exception
	{
		this.c = createControl();
		this.algorithm = c.createMock(DecisionCombiningAlgorithm.class);
		this.p1v1 = new Policy("id1", Version.parse("1"), algorithm);
		this.p1v2 = new Policy("id1", Version.parse("1.1"), algorithm);
		this.p1v3 = new Policy("id1", Version.parse("1.2.1"), algorithm);
		this.p1v4 = new Policy("id1", Version.parse("2.0.1"), algorithm);
		this.r = new InMemoryPolicyRepository();
		this.l = c.createMock(PolicyRepositoryListener.class);
		this.r.addPolicyRepositoryListener(l);
	}
	
	@Test
	public void testOnePolicyDifferentVersion() throws Exception
	{
		
		l.policyAdded(p1v1);
		l.policyAdded(p1v2);
		l.policyAdded(p1v3);
		l.policyAdded(p1v4);
		
		c.replay();
		
		r.add(p1v1);
		r.add(p1v2);
		r.add(p1v3);
		r.add(p1v4);
		
		CompositeDecisionRule p = r.get("id1", Version.parse("1.0"));
		assertEquals(p1v1, p);
		p = r.get("id1", Version.parse("1.1"));
		assertEquals(p1v2, p);
		p = r.get("id1", Version.parse("1.2.1"));
		assertEquals(p1v3, p);
		p = r.get("id1", Version.parse("2.0.1"));
		assertEquals(p1v4, p);
		
		Collection<Policy> found = r.getPolicies("id1", VersionMatch.parse("1.+"));
		assertEquals(3, found.size());
		Iterator<Policy> it = found.iterator();
		
		// sorted by version
		assertEquals(Version.parse("1"), it.next().getVersion());
		assertEquals(Version.parse("1.1"), it.next().getVersion());
		assertEquals(Version.parse("1.2.1"), it.next().getVersion());
		
		found = r.getPolicies("id1", VersionMatch.parse("1.2.+"));
		assertEquals(1, found.size());
		
		found = r.getPolicies("id1", VersionMatch.parse("1.+"), VersionMatch.parse("1.2.*"));
		assertEquals(1, found.size());
		
		Policy policy = r.getPolicy("id1", null, null, null);
		assertEquals(Version.parse("2.0.1"), policy.getVersion());
		
		c.verify();
	}
	
	@Test
	public void testAddRemove() throws Exception
	{
		l.policyAdded(p1v2);
		l.policyRemoved(p1v2);
		c.replay();
		
		r.add(p1v2);
		
		CompositeDecisionRule p = r.get("id1", Version.parse("1.0"));
		assertNull(p);
		p = r.get("id1", Version.parse("1.1"));
		assertEquals(p1v2, p);
		r.remove(p1v2);
		
		Collection<Policy> found = r.getPolicies("id1", VersionMatch.parse("1.+"));
		assertEquals(0, found.size());
		
		c.verify();
	}
	
	public void testAddPolicyWithTheSameIdAndSameVersion()
	{
		assertTrue(r.add(p1v2));
		assertFalse(r.add(p1v2));
	}

	
	@Test
	public void testFindAllPoliciesWithTheSameId() throws Exception
	{
		
		l.policyAdded(p1v2);
		l.policyAdded(p1v1);
		l.policyAdded(p1v3);
		l.policyAdded(p1v4);
		
		c.replay();
		
		r.add(p1v2);
		r.add(p1v1);
		r.add(p1v3);
		r.add(p1v4);
		
		Collection<Policy> found = r.getPolicies("id1", null, null, null);
		assertEquals(4, found.size());
		Iterator<Policy> it = found.iterator();
		
		assertEquals(Version.parse("1"), it.next().getVersion());
		assertEquals(Version.parse("1.1"), it.next().getVersion());
		assertEquals(Version.parse("1.2.1"), it.next().getVersion());
		assertEquals(Version.parse("2.0.1"), it.next().getVersion());
		
		c.verify();
		
	}
}

