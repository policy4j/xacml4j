package com.artagon.xacml.v3;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;

public class VersionMatchTest
{
	@Test
	public void testMatchAnySubsequentialVersions() throws XacmlSyntaxException
	{
		VersionMatch m = new VersionMatch("1.+");
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
	public void testCreateWithAnySingleDigitAndSubseq() throws XacmlSyntaxException
	{
		new VersionMatch("1.*.+");
	}
}
