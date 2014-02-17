package org.xacml4j.v30.spi.repository;


import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.VersionMatch;
import org.xacml4j.v30.pdp.DecisionCombiningAlgorithm;
import org.xacml4j.v30.pdp.Policy;
import org.xacml4j.v30.pdp.Rule;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.types.Types;


public class InMemoryPolicyRepositoryTest
{
	private Policy p1v1;
	private Policy p1v2;
	private Policy p1v2DiffInstance;
	private Policy p1v3;
	private Policy p1v4;

	private PolicyRepository r;
	private DecisionCombiningAlgorithm<Rule> algorithm;

	private IMocksControl c;
	private PolicyRepositoryListener l;

	private FunctionProvider functions;
	private DecisionCombiningAlgorithmProvider decisionAlgorithms;

	@SuppressWarnings("unchecked")
	@Before
	public void init() throws Exception
	{
		this.c = createControl();
		this.algorithm = c.createMock(DecisionCombiningAlgorithm.class);
		this.functions = c.createMock(FunctionProvider.class);
		this.decisionAlgorithms = c.createMock(DecisionCombiningAlgorithmProvider.class);

		this.p1v1 = Policy.builder("id1").version("1").combiningAlgorithm(algorithm).build();
		this.p1v2 = Policy.builder("id1").version("1.1").combiningAlgorithm(algorithm).build();
		this.p1v2DiffInstance =  Policy.builder("id1").version("1.1").combiningAlgorithm(algorithm).build();
		this.p1v3 =  Policy.builder("id1").version("1.2.1").combiningAlgorithm(algorithm).build();
		this.p1v4 =  Policy.builder("id1").version("2.0.1").combiningAlgorithm(algorithm).build();
		this.r = new InMemoryPolicyRepository("testId", Types.builder().defaultTypes().create(), functions, decisionAlgorithms);
		this.l = c.createMock(PolicyRepositoryListener.class);
		this.r.addPolicyRepositoryListener(l);
	}

	@Test
	public void testOnePolicyDifferentVersion() throws Exception
	{

		expect(algorithm.getId()).andReturn("testId");
		expect(decisionAlgorithms.isRuleAgorithmProvided("testId")).andReturn(true);
		l.policyAdded(p1v1);
		expect(algorithm.getId()).andReturn("testId");
		expect(decisionAlgorithms.isRuleAgorithmProvided("testId")).andReturn(true);
		l.policyAdded(p1v2);
		expect(algorithm.getId()).andReturn("testId");
		expect(decisionAlgorithms.isRuleAgorithmProvided("testId")).andReturn(true);
		l.policyAdded(p1v3);
		expect(algorithm.getId()).andReturn("testId");
		expect(decisionAlgorithms.isRuleAgorithmProvided("testId")).andReturn(true);
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
		expect(algorithm.getId()).andReturn("testId");
		expect(decisionAlgorithms.isRuleAgorithmProvided("testId")).andReturn(true);
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

	@Test
	public void testAddPolicyWithTheSameIdAndSameVersion()
	{
		expect(algorithm.getId()).andReturn("testId");
		expect(decisionAlgorithms.isRuleAgorithmProvided("testId")).andReturn(true);
		l.policyAdded(p1v2);
		expect(algorithm.getId()).andReturn("testId");
		expect(decisionAlgorithms.isRuleAgorithmProvided("testId")).andReturn(true);
		c.replay();
		assertTrue(r.add(p1v2));
		assertFalse(r.add(p1v2DiffInstance));
		Policy p = (Policy)r.get("id1", Version.parse("1.1"));
		assertSame(p1v2, p);
		assertNotSame(p1v2DiffInstance, p);
		c.verify();
	}


	@Test
	public void testFindAllPoliciesWithTheSameId() throws Exception
	{
		expect(algorithm.getId()).andReturn("testId");
		expect(decisionAlgorithms.isRuleAgorithmProvided("testId")).andReturn(true);
		l.policyAdded(p1v2);
		expect(algorithm.getId()).andReturn("testId");
		expect(decisionAlgorithms.isRuleAgorithmProvided("testId")).andReturn(true);
		l.policyAdded(p1v1);
		expect(algorithm.getId()).andReturn("testId");
		expect(decisionAlgorithms.isRuleAgorithmProvided("testId")).andReturn(true);
		l.policyAdded(p1v3);
		expect(algorithm.getId()).andReturn("testId");
		expect(decisionAlgorithms.isRuleAgorithmProvided("testId")).andReturn(true);
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

