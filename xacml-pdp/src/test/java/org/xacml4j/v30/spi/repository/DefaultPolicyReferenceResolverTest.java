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

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.xacml4j.v30.VersionMatch;
import org.xacml4j.v30.policy.DecisionCombiningAlgorithm;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicyIDReference;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.PolicySetIDReference;


public class DefaultPolicyReferenceResolverTest
{
	private PolicyRepository repository;

	private Policy p1v1;
	private PolicySet ps1v1;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void init() throws Exception
	{
		this.repository = Mockito.mock(PolicyRepository.class);
		this.p1v1 = Policy
				.builder("id")
				.version("1.0.0")
				.combiningAlgorithm(Mockito.mock(DecisionCombiningAlgorithm.class))
				.build();
		this.ps1v1 = PolicySet
				.builder("id")
				.version("1.2.1")
				.withCombiningAlgorithm(Mockito.mock(DecisionCombiningAlgorithm.class)).build();

	}

	@Test
	public void testResolvePolicyIDReference() throws Exception
	{
		when(repository.getPolicy("id", new VersionMatch("1.0.0"), null, null)).thenReturn(p1v1);
		DefaultPolicyReferenceResolver r = new DefaultPolicyReferenceResolver(repository);
		verify(repository).addPolicyRepositoryListener(any());
		PolicyIDReference ref = PolicyIDReference.builder("id").versionAsString("1.0.0").build();
		Policy p = r.resolve(ref);
		assertSame(p1v1, p);
		r.policyRemoved(p1v1);
		p = r.resolve(ref);
		assertSame(p1v1, p);
		verify(repository, times(2)).getPolicy("id", new VersionMatch("1.0.0"), null, null);
		verifyNoMoreInteractions(repository);
	}

	@Test
	public void testResolvePolicySetIDReference() throws Exception
	{
		when(repository.getPolicySet("id", new VersionMatch("1.0.0"), null, null)).thenReturn(ps1v1);
		DefaultPolicyReferenceResolver r = new DefaultPolicyReferenceResolver(repository);
		verify(repository).addPolicyRepositoryListener(any());
		PolicySetIDReference ref = PolicySetIDReference.builder("id").versionAsString("1.0.0").build();
		PolicySet p = r.resolve(ref);
		assertSame(ps1v1, p);
		r.policySetRemoved(ps1v1);
		p = r.resolve(ref);
		assertSame(ps1v1, p);
		verify(repository, times(1)).getPolicySet("id", new VersionMatch("1.0.0"), null, null);
		verifyNoMoreInteractions(repository);
	}
}
