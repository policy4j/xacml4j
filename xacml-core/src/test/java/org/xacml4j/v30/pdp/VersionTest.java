package org.xacml4j.v30.pdp;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.XacmlSyntaxException;


public class VersionTest
{
	@Test
	public void testCreateVersion() throws XacmlSyntaxException
	{
		Version v1 = Version.parse("1.0");
		Version v2 = Version.parse("1.0");
		Version v3 = Version.parse("1.0.0");
		assertTrue(v1.equals(v1));
		assertTrue(v1.equals(v2));
		assertTrue(v1.equals(v3));
		assertFalse(v1.equals(null));
		assertFalse(v1.equals("1.0.0"));
	}

	@Test
	public void testLessThanVersion() throws XacmlSyntaxException
	{
		Version v1 = Version.parse("1.1");
		Version v2 = Version.parse("1.0");
		Version v3 = Version.parse("1.0.1");
		Version v4 = Version.parse("1.0.0");
		assertTrue(v1.compareTo(v2) > 0);
		assertTrue(v3.compareTo(v1) < 0);
		assertTrue(v3.compareTo(v2) > 0);
		assertTrue(v2.compareTo(v4) == 0);
		assertTrue(v2.compareTo(v2) == 0);
		assertTrue(v4.compareTo(v2) == 0);
	}

	@Test
	public void testDefaultVersion() throws XacmlSyntaxException
	{
		Version v = Version.parse(null);
		assertEquals("1.0.0", v.getValue());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNegativeComponent() throws XacmlSyntaxException
	{
		Version.parse("-1.0");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testUnparsableVersion() throws XacmlSyntaxException
	{
		Version.parse("1.a....");
	}

	@Test
	public void sortVersion() throws Exception
	{
		Collection<Version> versions = new LinkedList<Version>();
		versions.add(Version.parse("1.0.0"));
		versions.add(Version.parse("1.0.1"));
		versions.add(Version.parse("1.1.0"));
		versions.add(Version.parse("1.2.0"));
	}
}
