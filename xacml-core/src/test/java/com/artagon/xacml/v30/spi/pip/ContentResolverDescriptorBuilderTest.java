package com.artagon.xacml.v30.spi.pip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.artagon.xacml.v30.core.AttributeCategories;

public class ContentResolverDescriptorBuilderTest 
{
	@Test
	public void testBuildDescriptor()
	{
		ContentResolverDescriptor d = ContentResolverDescriptorBuilder.bulder("id", "name", AttributeCategories.SUBJECT_ACCESS)
		.build();
		assertEquals(AttributeCategories.SUBJECT_ACCESS, d.getCategory());
		assertTrue(d.canResolve(AttributeCategories.SUBJECT_ACCESS));
		assertFalse(d.canResolve(AttributeCategories.ENVIRONMENT));
		assertFalse(d.canResolve(AttributeCategories.SUBJECT_CODEBASE));
		assertEquals("id", d.getId());
		assertEquals("name", d.getName());
	}
}
