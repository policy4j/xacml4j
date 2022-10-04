package org.xacml4j.v30.policy;

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

import org.junit.Test;
import org.xacml4j.v30.XPathVersion;

import static org.junit.Assert.assertEquals;

public class PolicyDefaultsTest
{
	@Test
	public void testCreatePolicyDefaults()
	{
		PolicyDefaults d1 = PolicyDefaults.builder().build();
		PolicyDefaults d2 = PolicyDefaults.builder().build();
		assertEquals(d1, d2);
	}

	@Test
	public void testCreatePolicyDefaultsWithXPath()
	{
		PolicyDefaults d1 = PolicyDefaults.builder().xpathVersion(XPathVersion.XPATH1.toString()).build();
		PolicyDefaults d2 = PolicyDefaults.builder().xpathVersion(null).build();
		assertEquals(d1, d2);
		assertEquals(XPathVersion.XPATH1, d1.<XPathVersion>getValue(PolicyDefaults.XPATH_VERSION));
		assertEquals(XPathVersion.XPATH1, d2.<XPathVersion>getValue(PolicyDefaults.XPATH_VERSION));
	}
}
