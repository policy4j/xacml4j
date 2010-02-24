package com.artagon.xacml.v3.policy;

import org.junit.Test;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;

public class VersionMatchTest
{
	@Test
	public void testMatchAnySubsequentialVersions()
	{
		VersionMatch m = new VersionMatch("1.+");
		assertTrue(m.match(Version.valueOf("1.2.1")));
		assertTrue(m.match(Version.valueOf("1.1")));
		assertFalse(m.match(Version.valueOf("2.1")));
		m = new VersionMatch("1.*.+");
		assertTrue(m.match(Version.valueOf("1.0")));
	}
	
	@Test
	public void testMatchAnySingleNumber()
	{
		VersionMatch m = new VersionMatch("1.*.1");
		assertTrue(m.match(Version.valueOf("1.2.1")));
		assertTrue(m.match(Version.valueOf("1.0.1")));
		assertFalse(m.match(Version.valueOf("2.1.1")));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCreateWithSubsquentialTwoTimes()
	{
		new VersionMatch("1.+.+");
	}

	public void testCreateWithAnySingleDigitAndSubseq()
	{
		new VersionMatch("1.*.+");
	}
}
