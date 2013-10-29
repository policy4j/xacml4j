package org.xacml4j.v30.pdp;

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
		assertFalse(v1.equals("1.0"));
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
		assertEquals("1.0", v.getValue());
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
