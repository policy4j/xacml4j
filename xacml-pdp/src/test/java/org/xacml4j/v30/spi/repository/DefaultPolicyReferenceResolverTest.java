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

import org.easymock.IMocksControl;
import org.junit.Before;
import org.xacml4j.v30.pdp.DecisionCombiningAlgorithm;
import org.xacml4j.v30.pdp.Policy;
import org.xacml4j.v30.pdp.PolicySet;

import static org.easymock.EasyMock.createControl;


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
				.builder("attributeId")
				.version("1.0.0")
				.combiningAlgorithm(c.createMock(DecisionCombiningAlgorithm.class))
				.build();
		this.ps1v1 = PolicySet
				.builder("attributeId")
				.version("1.2.1")
				.combiningAlgorithm(c.createMock(DecisionCombiningAlgorithm.class)).build();

	}
}
