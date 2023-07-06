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

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertSame;

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.VersionMatch;
import org.xacml4j.v30.policy.DecisionCombiningAlgorithm;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicyIDReference;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.PolicySetIDReference;


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
				.version("1.0.0")
				.combiningAlgorithm(c.createMock(DecisionCombiningAlgorithm.class))
				.build();
		this.ps1v1 = PolicySet
				.builder("id")
				.version("1.2.1")
				.withCombiningAlgorithm(c.createMock(DecisionCombiningAlgorithm.class)).build();

	}

	@Test
	public void testResolvePolicyIDReference() throws Exception
	{
		Capture<PolicyRepositoryListener> listener = Capture.newInstance();
		repository.addPolicyRepositoryListener(capture(listener));
		expect(repository.getPolicy("id", new VersionMatch("1.0.0"), null, null)).andReturn(p1v1).times(2);
		c.replay();
		DefaultPolicyReferenceResolver r = new DefaultPolicyReferenceResolver(repository);
		PolicyIDReference ref = PolicyIDReference.builder("id").versionAsString("1.0.0").build();
		Policy p = r.resolve(ref);
		assertSame(p1v1, p);
		r.policyRemoved(p1v1);
		p = r.resolve(ref);
		assertSame(p1v1, p);
		c.verify();
	}

	@Test
	public void testResolvePolicySetIDReference() throws Exception
	{
		Capture<PolicyRepositoryListener> listener = Capture.newInstance();
		repository.addPolicyRepositoryListener(capture(listener));
		expect(repository.getPolicySet("id", new VersionMatch("1.0.0"), null, null)).andReturn(ps1v1).times(1);
		c.replay();
		DefaultPolicyReferenceResolver r = new DefaultPolicyReferenceResolver(repository);
		PolicySetIDReference ref = PolicySetIDReference.builder("id").versionAsString("1.0.0").build();
		PolicySet p = r.resolve(ref);
		assertSame(ps1v1, p);
		r.policySetRemoved(ps1v1);
		p = r.resolve(ref);
		assertSame(ps1v1, p);
		c.verify();
	}
}
