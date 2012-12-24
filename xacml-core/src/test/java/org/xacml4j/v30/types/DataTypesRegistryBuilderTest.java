package org.xacml4j.v30.types;

import org.junit.Test;
import org.xacml4j.v30.types.DataTypeRegistry;

public class DataTypesRegistryBuilderTest
{
	@Test
	public void createDefaultBuilder()
	{
		DataTypeRegistry.Builder.builder().defaultTypes().build();
	}
}
