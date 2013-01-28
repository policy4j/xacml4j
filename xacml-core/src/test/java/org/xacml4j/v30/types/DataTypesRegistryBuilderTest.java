package org.xacml4j.v30.types;

import org.junit.Test;

public class DataTypesRegistryBuilderTest
{
	@Test
	public void createDefaultBuilder()
	{
		Types.Builder.builder().defaultTypes().create();
	}
}
