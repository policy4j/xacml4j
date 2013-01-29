package org.xacml4j.util;

import org.junit.Test;

public class ConcurrentMultiKeyHashMapTest 
{
	@Test
	public void testAdd()
	{
		ConcurrentMultiKeyHashMap<String, String, String> m = new ConcurrentMultiKeyHashMap<String, String, String>();
		m.put("k1", "k2", "v1");
		m.put("k1", "k2", "v2");
	}
}
