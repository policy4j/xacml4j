package org.xacml4j.v30;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.XacmlTypes;

class RequestContextEqualityTest
{
	@Test
	void equalRequestsHaveEqualHashCodes()
	{
		RequestContext left = request("subject-1", "alice");
		RequestContext right = request("subject-1", "alice");

		assertEquals(left, right);
		assertEquals(left.hashCode(), right.hashCode());
	}

	@Test
	void requestsDifferingOnlyByXmlIdAreNotEqual()
	{
		RequestContext left = request("subject-1", "alice");
		RequestContext right = request("subject-2", "alice");

		assertNotEquals(left, right);
	}

	@Test
	void requestsDifferingByEntityAreNotEqual()
	{
		RequestContext left = request("subject-1", "alice");
		RequestContext right = request("subject-1", "bob");

		assertNotEquals(left, right);
	}

	@Test
	void categoryEqualityAndHashCodeIncludeXmlId()
	{
		Category left = category("subject-1", "alice");
		Category equal = category("subject-1", "alice");
		Category differentId = category("subject-2", "alice");

		assertEquals(left, equal);
		assertEquals(left.hashCode(), equal.hashCode());
		assertNotEquals(left, differentId);
	}

	private static RequestContext request(String id, String value)
	{
		return RequestContext.builder().attributes(category(id, value)).build();
	}

	private static Category category(String id, String value)
	{
		return Category.builder(CategoryId.SUBJECT_ACCESS)
				.id(id)
				.entity(Entity.builder().attribute(Attribute.builder("subject-id")
						.value(XacmlTypes.STRING.ofAny(value))
						.build()).build())
				.build();
	}
}
