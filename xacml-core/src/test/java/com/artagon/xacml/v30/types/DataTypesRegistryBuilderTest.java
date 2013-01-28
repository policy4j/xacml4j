package com.artagon.xacml.v30.types;

import org.junit.Test;

public class DataTypesRegistryBuilderTest
{
	@Test
	public void createDefaultBuilder()
	{
		Types.Builder.builder().defaultTypes().create();
	}
}
