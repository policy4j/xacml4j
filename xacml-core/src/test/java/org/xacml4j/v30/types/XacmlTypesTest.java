package org.xacml4j.v30.types;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class XacmlTypesTest
{
	@Test
	public void assertSystemTypes(){
		assertTrue(XacmlTypes.getType("integer").isPresent());
		assertTrue(XacmlTypes.getType("Integer").isPresent());
		assertTrue(XacmlTypes.getType("double").isPresent());
	}
}
