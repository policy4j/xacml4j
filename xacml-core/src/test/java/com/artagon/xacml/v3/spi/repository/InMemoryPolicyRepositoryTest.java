package com.artagon.xacml.v3.spi.repository;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.Rule;
import com.artagon.xacml.v3.Version;
import com.artagon.xacml.v3.VersionMatch;
import com.artagon.xacml.v3.spi.PolicyRepository;
import com.artagon.xacml.v3.spi.repository.InMemoryPolicyRepository;

public class InMemoryPolicyRepositoryTest 
{
	private Policy p1v1;
	private Policy p1v2;
	private Policy p1v3;
	private Policy p1v4;
	
	private PolicyRepository r;
	private DecisionCombiningAlgorithm<Rule> algorithm;
	
	@SuppressWarnings("unchecked")
	@Before
	public void init() throws Exception
	{
		this.algorithm = createStrictMock(DecisionCombiningAlgorithm.class);
		this.p1v1 = new Policy("id1", Version.parse("1"), algorithm);
		this.p1v2 = new Policy("id1", Version.parse("1.1"), algorithm);
		this.p1v3 = new Policy("id1", Version.parse("1.2.1"), algorithm);
		this.p1v4 = new Policy("id1", Version.parse("2.0.1"), algorithm);
		this.r = new InMemoryPolicyRepository();
	}
	
	@Test
	public void testOnePolicyDifferentVersion() throws Exception
	{
		r.add(p1v2);
		r.add(p1v1);
		r.add(p1v3);
		r.add(p1v4);
		
		replay(algorithm);
		
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
		
		Policy p = r.getPolicy("id1", null, null, null);
		assertEquals(Version.parse("2.0.1"), p.getVersion());
		
		verify(algorithm);
	}
	
	@Test
	public void testFindAllPoliciesWithTheSameId() throws Exception
	{
		
		replay(algorithm);
		
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
		
		verify(algorithm);
		
		
	}
}
