package org.xacml4j.v30.types;

import java.util.ServiceLoader;

import org.junit.Test;
import org.xacml4j.v30.TypeCapability;

import com.google.common.collect.ImmutableList;
import com.google.common.truth.Truth;
import com.google.common.truth.Truth8;

public class TypeToStringCapabilityTest 
{
	@Test
	public void testGetCapability() {
		Truth8.assertThat(TypeToString.forType(XacmlTypes.BOOLEAN)).isPresent();
		Truth8.assertThat(TypeToString.forType(XacmlTypes.ENTITY)).isEmpty();

	}
}
