package com.artagon.xacml.v3.policy.impl;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;

import com.artagon.xacml.v3.PolicySyntaxException;
import com.artagon.xacml.v3.Version;
import com.artagon.xacml.v3.VersionMatch;

public class VersionMatchTest
{
	@Test
	public void testMatchAnySubsequentialVersions() throws PolicySyntaxException
	{
		VersionMatch m = new VersionMatch("1.+");
		assertTrue(m.match(Version.parse("1.2.1")));
		assertTrue(m.match(Version.parse("1.1")));
		assertFalse(m.match(Version.parse("2.1")));
		m = new VersionMatch("1.*.+");
		assertTrue(m.match(Version.parse("1.0")));
	}
	
	@Test
	public void testMatchAnySingleNumber() throws PolicySyntaxException
	{
		VersionMatch m = new VersionMatch("1.*.1");
		assertTrue(m.match(Version.parse("1.2.1")));
		assertTrue(m.match(Version.parse("1.0.1")));
		assertFalse(m.match(Version.parse("2.1.1")));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCreateWithSubsquentialTwoTimes() throws PolicySyntaxException
	{
		new VersionMatch("1.+.+");
	}

	public void testCreateWithAnySingleDigitAndSubseq() throws PolicySyntaxException
	{
		new VersionMatch("1.*.+");
	}
}
