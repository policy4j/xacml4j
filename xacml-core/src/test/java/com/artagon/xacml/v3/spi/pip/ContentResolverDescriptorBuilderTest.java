package com.artagon.xacml.v3.spi.pip;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.artagon.xacml.v3.AttributeCategories;

public class ContentResolverDescriptorBuilderTest 
{
	@Test
	public void testBuildDescriptor()
	{
		ContentResolverDescriptor d = ContentResolverDescriptorBuilder.create().
		category(AttributeCategories.ENVIRONMENT).category(AttributeCategories.SUBJECT_INTERMEDIARY).build();
		assertTrue(d.canResolve(AttributeCategories.SUBJECT_INTERMEDIARY));
		assertTrue(d.canResolve(AttributeCategories.ENVIRONMENT));
		assertFalse(d.canResolve(AttributeCategories.SUBJECT_CODEBASE));
	}
}
