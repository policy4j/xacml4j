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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.XacmlTypes;

class RequestContextIncludeInResultTest
{
	private static Attribute attribute(String id, String value, boolean includeInResult)
	{
		return Attribute.builder(id).includeInResult(includeInResult).value(XacmlTypes.STRING.ofAny(value)).build();
	}

	private static Category category(CategoryId id, Attribute... attributes)
	{
		return Category.builder(id).entity(Entity.builder().attribute(attributes).build()).build();
	}

	/** Design D32: the entity knows since it was built. */
	@Test
	void entityTellsWhetherItHasIncludeInResultAttributes()
	{
		assertTrue(Entity.builder().attribute(attribute("subject-id", "user", true)).build().hasIncludeInResultAttributes());
		assertFalse(Entity.builder().attribute(attribute("subject-id", "user", false)).build().hasIncludeInResultAttributes());
		assertFalse(Entity.builder().build().hasIncludeInResultAttributes());
	}

	@Test
	void categoriesWithoutIncludedAttributesAreLeftOutAndTheOthersKeepTheirOrderAndContent()
	{
		RequestContext request = RequestContext.builder().attributes(
				category(CategoryId.SUBJECT_ACCESS, attribute("subject-id", "user", true), attribute("role", "admin", false)),
				category(CategoryId.RESOURCE, attribute("resource-id", "doc-1", false)),
				category(CategoryId.ACTION, attribute("action-id", "read", true)))
				.build();

		List<Category> included = List.copyOf(request.getIncludeInResultAttributes());

		assertEquals(List.of(CategoryId.SUBJECT_ACCESS, CategoryId.ACTION),
				included.stream().map(Category::getCategoryId).toList());
		assertEquals(List.of(attribute("subject-id", "user", true)), List.copyOf(included.get(0).getEntity().attributes()));
		assertEquals(List.of(attribute("action-id", "read", true)), List.copyOf(included.get(1).getEntity().attributes()));
		assertEquals(List.of(), List.copyOf(RequestContext.builder()
				.attributes(category(CategoryId.RESOURCE, attribute("resource-id", "doc-1", false))).build()
				.getIncludeInResultAttributes()));
	}

	/** Cycle AM: the included attributes keep the order of the index, which groups the attributes of an id. */
	@Test
	void includedAttributesKeepTheOrderOfTheIndex()
	{
		Attribute first = attribute("a", "1", true);
		Attribute other = attribute("b", "2", true);
		Attribute hidden = attribute("c", "3", false);
		Attribute second = attribute("a", "4", true);
		Category category = category(CategoryId.RESOURCE, first, other, hidden, second);

		assertEquals(List.of(first, second, other), List.copyOf(category.getIncludeInResultAttributes()));
		assertEquals(List.of(first, second, other), List.copyOf(RequestContext.builder().attributes(category).build()
				.getIncludeInResultAttributes().iterator().next().getEntity().attributes()));
	}

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
