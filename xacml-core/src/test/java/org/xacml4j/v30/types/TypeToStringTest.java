package org.xacml4j.v30.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

public class TypeToStringTest
{
	@Test
	public void testBasicTypes(){
		Optional<TypeToString> c = TypeToString.forType(XacmlTypes.INTEGER);
		assertTrue(c.isPresent());
		c = TypeToString.forType(XacmlTypes.ENTITY);
		assertFalse(c.isPresent());
	}
}
