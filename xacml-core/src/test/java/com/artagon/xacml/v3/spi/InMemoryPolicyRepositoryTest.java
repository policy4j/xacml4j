package com.artagon.xacml.v3.spi;

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

public class InMemoryPolicyRepositoryTest 
{
	private Policy p1v1;
	private Policy p1v2;
	private Policy p1v3;
	
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
		this.r = new InMemoryPolicyRepository();

	}
	
	@Test
	public void testAddPolicies() throws Exception
	{
					
		replay(algorithm);
		
		r.add(p1v2);
		r.add(p1v1);
		r.add(p1v3);
		
		Collection<Policy> found = r.getPolicies("id1", VersionMatch.parse("1.+"));
		assertEquals(3, found.size());
		Iterator<Policy> it = found.iterator();
		
		assertEquals(Version.parse("1"), it.next().getVersion());
		assertEquals(Version.parse("1.1"), it.next().getVersion());
		assertEquals(Version.parse("1.2.1"), it.next().getVersion());
		
		verify(algorithm);
	}
	
	@Test
	public void testFindPolicies() throws Exception
	{
					
		replay(algorithm);
		
		r.add(p1v2);
		r.add(p1v1);
		r.add(p1v3);
		
		Collection<Policy> found = r.getPolicies("id1", null, null, null);
		assertEquals(3, found.size());
		Iterator<Policy> it = found.iterator();
		
		assertEquals(Version.parse("1"), it.next().getVersion());
		assertEquals(Version.parse("1.1"), it.next().getVersion());
		assertEquals(Version.parse("1.2.1"), it.next().getVersion());
		
		verify(algorithm);
	}
}
