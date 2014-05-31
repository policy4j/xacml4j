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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.VersionMatch;
import org.xacml4j.v30.XacmlSyntaxException;


public class VersionMatchTest
{
	@Test
	public void testMatchAnySubsequentialVersions() throws XacmlSyntaxException
	{
		VersionMatch m = new VersionMatch("1.+");
		assertEquals("1.+", m.getPattern());
		assertTrue(m.match(Version.parse("1.2.1")));
		assertTrue(m.match(Version.parse("1.1")));
		assertFalse(m.match(Version.parse("2.1")));
		m = new VersionMatch("1.*.+");
		assertTrue(m.match(Version.parse("1.0")));
	}

	@Test
	public void testMatchAnySingleNumber() throws XacmlSyntaxException
	{
		VersionMatch m = new VersionMatch("1.*.1");
		assertEquals("1.*.1", m.getPattern());
		assertTrue(m.match(Version.parse("1.2.1")));
		assertTrue(m.match(Version.parse("1.0.1")));
		assertFalse(m.match(Version.parse("2.1.1")));
	}

	@Test(expected=XacmlSyntaxException.class)
	public void testCreateWithSubsquentialTwoTimes() throws XacmlSyntaxException
	{
		new VersionMatch("1.+.+");
	}

	@Test
	public void testMatchAnySingleNumberTwoTimesInTheRow() throws XacmlSyntaxException
	{
		VersionMatch m = new VersionMatch("1.*.*.1");
		assertEquals("1.*.*.1", m.getPattern());
		assertTrue(m.match(Version.parse("1.2.1.1")));
		assertTrue(m.match(Version.parse("1.2.1.1")));
		assertTrue(m.match(Version.parse("1.0.0.1")));
		assertFalse(m.match(Version.parse("1.0.1")));
	}

	@Test
	public void testCreateWithAnySingleDigitAndSubseq() throws XacmlSyntaxException
	{
		VersionMatch m = new VersionMatch("1.*.+");
		assertTrue(m.match(Version.parse("1.2.1")));
		assertTrue(m.match(Version.parse("1.2.1.2")));
		assertTrue(m.match(Version.parse("1.0.1")));
		assertFalse(m.match(Version.parse("2.1.1")));
	}
}
