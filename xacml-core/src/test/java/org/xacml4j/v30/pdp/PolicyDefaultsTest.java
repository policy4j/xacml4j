package org.xacml4j.v30.pdp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xacml4j.v30.XPathVersion;

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
