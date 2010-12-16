package com.artagon.xacml.v3.spi.repository;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertSame;

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicyIDReference;
import com.artagon.xacml.v3.Version;
import com.artagon.xacml.v3.VersionMatch;

public class DefaultPolicyReferenceResolverTest
{	
	private PolicyRepository repository;
	private IMocksControl c;
	
	private Policy p1v1;
	private Policy p1v2;
	private Policy p1v3;
	private Policy p1v4;
	
	@SuppressWarnings("unchecked")
	@Before
	public void init() throws Exception
	{
		this.c = createControl();
		this.repository = c.createMock(PolicyRepository.class);
		this.p1v1 = new Policy("id", Version.parse("1.0.0"), c.createMock(DecisionCombiningAlgorithm.class));
		this.p1v2 = new Policy("id", Version.parse("1.1"), c.createMock(DecisionCombiningAlgorithm.class));
		this.p1v3 = new Policy("id", Version.parse("1.2.1"), c.createMock(DecisionCombiningAlgorithm.class));
		this.p1v4 = new Policy("id", Version.parse("2.0.0"), c.createMock(DecisionCombiningAlgorithm.class));
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
}
