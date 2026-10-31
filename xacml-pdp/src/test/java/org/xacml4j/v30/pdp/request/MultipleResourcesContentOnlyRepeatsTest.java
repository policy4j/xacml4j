package org.xacml4j.v30.pdp.request;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.content.XmlContent;
import org.xacml4j.v30.pdp.PolicyDecisionPointContext;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Bead policy4j-the.4.60: repeated categories that differ only in their
 * {@code <Content>} are different categories. Each gives one individual
 * request (Multiple Decision Profile, section 3.3.3). Before the fix any
 * two XML contents compared as equal, with different hash codes, and the
 * expansion dropped some of the categories, a different number from run
 * to run.
 */
public class MultipleResourcesContentOnlyRepeatsTest
{
	@Test
	public void repeatsThatDifferOnlyInContentAreAllExpanded()
	{
		for (int count : new int[] {2, 5, 17, 64}) {
			PolicyDecisionPointContext pdp = mock(PolicyDecisionPointContext.class);
			when(pdp.requestDecision(Mockito.any())).thenReturn(Result.ok(Decision.PERMIT).build());

			int results = new MultipleResourcesViaRepeatingAttributesHandler()
					.handle(RequestContext.builder().attributes(repeats(count)).build(), pdp).size();

			assertEquals(count, results, "repeats=" + count);
			verify(pdp, times(count)).requestDecision(Mockito.any());
		}
	}

	/** {@code count} resource categories with the same attribute and different content. */
	private static List<Category> repeats(int count)
	{
		List<Category> categories = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			categories.add(Category.builder(CategoryId.RESOURCE)
					.entity(Entity.builder()
							.content(XmlContent.of("<record><id>" + i + "</id></record>"))
							.attribute(Attribute.builder("urn:test:kind")
									.value(XacmlTypes.STRING.ofAny("document")).build())
							.build())
					.build());
		}
		return categories;
	}
}
