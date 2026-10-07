package org.xacml4j.v30;

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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.XacmlTypes;

class RequestContextIncludeInResultTest
{
	@Test
	void preservesReferencedCategoryIdForIncludedAttributes()
	{
		Category category = Category.builder(CategoryId.SUBJECT_ACCESS)
				.id("subject1")
				.entity(Entity.builder().attribute(Attribute.builder("subject-id")
						.includeInResult(true)
						.value(XacmlTypes.STRING.ofAny("user"))
						.build()).build())
				.build();

		Category included = RequestContext.builder().attributes(category).build()
				.getIncludeInResultAttributes().iterator().next();

		assertEquals("subject1", included.getReferenceId().orElse(null));
	}
}
