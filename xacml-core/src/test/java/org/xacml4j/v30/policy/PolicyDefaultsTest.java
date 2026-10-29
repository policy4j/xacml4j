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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.XPathVersion;

public class PolicyDefaultsTest
{
	@Test
	public void genericDefaultsKeepImmutableScalarValues()
	{
		for(Object value : List.of(XPathVersion.XPATH2, "text", true, 'x', (byte)1, (short)2,
				3, 4L, 5.0f, 6.0d, BigInteger.TEN, BigDecimal.ONE, URI.create("urn:default"))){
			PolicyDefaults policy = PolicyDefaults.builder().value("custom", value).build();
			PolicySetDefaults set = PolicySetDefaults.builder().value("custom", value).build();
			assertEquals(value, policy.getValue("custom"));
			assertEquals(value, set.getValue("custom"));
		}
		assertEquals(XPathVersion.XPATH2, PolicyDefaults.builder()
				.value(PolicyDefaults.XPATH_VERSION, XPathVersion.XPATH2).build().getXPathVersion());
	}

	@Test
	public void genericDefaultsRejectMutableValues()
	{
		for(Object value : List.of(new ArrayList<>(), new StringBuilder("text"),
				new AtomicInteger(1), new byte[]{1}, ByteBuffer.wrap(new byte[]{1}))){
			assertThrows(IllegalArgumentException.class, () -> PolicyDefaults.builder().value("custom", value));
			assertThrows(IllegalArgumentException.class, () -> PolicySetDefaults.builder().value("custom", value));
		}
	}

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
