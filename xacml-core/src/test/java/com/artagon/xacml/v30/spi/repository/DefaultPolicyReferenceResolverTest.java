package com.artagon.xacml.v30.spi.repository;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertSame;

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.pdp.DecisionCombiningAlgorithm;
import com.artagon.xacml.v30.pdp.Policy;
import com.artagon.xacml.v30.pdp.PolicyIDReference;
import com.artagon.xacml.v30.pdp.PolicySet;
import com.artagon.xacml.v30.pdp.PolicySetIDReference;
import com.artagon.xacml.v30.pdp.VersionMatch;

public class DefaultPolicyReferenceResolverTest
{	
	private PolicyRepository repository;
	private IMocksControl c;
	
	private Policy p1v1;
	private PolicySet ps1v1;
	
	@SuppressWarnings("unchecked")
	@Before
	public void init() throws Exception
	{
		this.c = createControl();
		this.repository = c.createMock(PolicyRepository.class);
		this.p1v1 = Policy
				.builder("id")
				.withVersion("1.0.0")
				.withCombiningAlgorithm(c.createMock(DecisionCombiningAlgorithm.class))
				.create();
		this.ps1v1 = PolicySet
				.builder("id")
				.withVersion("1.2.1")
				.withCombiningAlgorithm(c.createMock(DecisionCombiningAlgorithm.class)).create();
	
	}
	
	@Test
	public void testResolvePolicyIDReference() throws Exception
	{
		Capture<PolicyRepositoryListener> listener = new Capture<PolicyRepositoryListener>();
		repository.addPolicyRepositoryListener(capture(listener));
		expect(repository.getPolicy("id", new VersionMatch("1.0.0"), null, null)).andReturn(p1v1).times(2);
		c.replay();
		DefaultPolicyReferenceResolver r = new DefaultPolicyReferenceResolver(repository);
		PolicyIDReference ref = new PolicyIDReference("id", new VersionMatch("1.0.0"));
		Policy p = r.resolve(ref);
		p = r.resolve(ref);
		assertSame(p1v1, p);
		r.policyRemoved(p1v1);
		p = r.resolve(ref);
		assertSame(p1v1, p);
		c.verify();
	}
	
	@Test
	public void testResolvePolicySetIDReference() throws Exception
	{
		Capture<PolicyRepositoryListener> listener = new Capture<PolicyRepositoryListener>();
		repository.addPolicyRepositoryListener(capture(listener));
		expect(repository.getPolicySet("id", new VersionMatch("1.0.0"), null, null)).andReturn(ps1v1).times(1);
		c.replay();
		DefaultPolicyReferenceResolver r = new DefaultPolicyReferenceResolver(repository);
		PolicySetIDReference ref = new PolicySetIDReference("id", new VersionMatch("1.0.0"));
		PolicySet p = r.resolve(ref);
		p = r.resolve(ref);
		assertSame(ps1v1, p);
		r.policySetRemoved(ps1v1);
		p = r.resolve(ref);
		assertSame(ps1v1, p);
		c.verify();
	}
}
