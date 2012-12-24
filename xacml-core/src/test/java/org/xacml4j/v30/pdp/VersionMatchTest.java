package org.xacml4j.v30.pdp;

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
	
	@Test(expected=IllegalArgumentException.class)
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
