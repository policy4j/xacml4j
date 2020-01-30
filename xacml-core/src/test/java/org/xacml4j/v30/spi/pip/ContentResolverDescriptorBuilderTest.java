package org.xacml4j.v30.spi.pip;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xacml4j.v30.CategoryId;


public class ContentResolverDescriptorBuilderTest
{
	@Test
	public void testBuildDescriptor()
	{
		ContentResolverDescriptor d = ContentResolverDescriptorBuilder.builder("id", "name", CategoryId.SUBJECT_ACCESS)
		.build();
		assertEquals(CategoryId.SUBJECT_ACCESS, d.getCategory());
		assertTrue(d.canResolve(CategoryId.SUBJECT_ACCESS));
		assertFalse(d.canResolve(CategoryId.ENVIRONMENT));
		assertFalse(d.canResolve(CategoryId.SUBJECT_CODEBASE));
		assertEquals("id", d.getId());
		assertEquals("name", d.getName());
	}
}
