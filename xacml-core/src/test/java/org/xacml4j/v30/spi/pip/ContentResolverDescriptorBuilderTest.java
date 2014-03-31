package org.xacml4j.v30.spi.pip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xacml4j.v30.Categories;


public class ContentResolverDescriptorBuilderTest
{
	@Test
	public void testBuildDescriptor()
	{
		ContentResolverDescriptor d = ContentResolverDescriptorBuilder.bulder("id", "name", Categories.SUBJECT_ACCESS)
		.build();
		assertEquals(Categories.SUBJECT_ACCESS, d.getCategory());
		assertTrue(d.canResolve(Categories.SUBJECT_ACCESS));
		assertFalse(d.canResolve(Categories.ENVIRONMENT));
		assertFalse(d.canResolve(Categories.SUBJECT_CODEBASE));
		assertEquals("id", d.getId());
		assertEquals("name", d.getName());
	}
}
