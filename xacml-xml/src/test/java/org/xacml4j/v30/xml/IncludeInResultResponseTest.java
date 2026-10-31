package org.xacml4j.v30.xml;

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

import java.io.StringWriter;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Cycle AM: a result includes the request's categories as the request keeps them, a category whose
 * attributes are all included being the category itself, and its response is the response of
 * copies with only the included attributes and no content (XACML 3.0 §5.46, §5.48).
 */
public class IncludeInResultResponseTest
{
	@Test
	public void includedCategoriesGiveTheResponseOfTheirCopies() throws Exception
	{
		Attribute subjectId = attribute("urn:oasis:names:tc:xacml:1.0:subject:subject-id", "alice", true);
		Attribute resourceId = attribute("urn:oasis:names:tc:xacml:1.0:resource:resource-id", "doc-1", true);
		Attribute owner = attribute("urn:xacml4j:test:owner", "bob", false);
		Attribute channel = attribute("urn:xacml4j:test:channel", "web", true);
		Category subject = Category.builder(CategoryId.SUBJECT_ACCESS).id("subject")
				.entity(Entity.builder().attribute(subjectId).build()).build();
		Category resource = Category.builder(CategoryId.RESOURCE)
				.entity(Entity.builder().attribute(resourceId, owner).build()).build();
		Category environment = Category.builder(CategoryId.ENVIRONMENT)
				.entity(Entity.builder().xmlContent("<context/>").attribute(channel).build()).build();
		RequestContext request = RequestContext.builder().attributes(subject, resource, environment).build();

		Result included = Result.ok(Decision.PERMIT).includeInResultAttr(request.getIncludeInResultAttributes()).build();
		Result copied = Result.ok(Decision.PERMIT)
				.includeInResult(copy(subject, subjectId))
				.includeInResult(copy(resource, resourceId))
				.includeInResult(copy(environment, channel))
				.build();

		assertEquals(xml(copied), xml(included));
		assertEquals(copied, included);
		assertEquals(copied.hashCode(), included.hashCode());
		assertFalse(included.getIncludeInResultByCategory(CategoryId.ENVIRONMENT).getEntity().hasContent());
	}

	private static Attribute attribute(String id, String value, boolean includeInResult)
	{
		return Attribute.builder(id).includeInResult(includeInResult).value(XacmlTypes.STRING.ofAny(value)).build();
	}

	/** The copy that a result held before cycle AM */
	private static Category copy(Category category, Attribute... included)
	{
		return Category.builder(category.getCategoryId()).id(category.getReferenceId().orElse(null))
				.entity(Entity.builder().attribute(included).build()).build();
	}

	private static String xml(Result result) throws Exception
	{
		StringWriter out = new StringWriter();
		new Xacml30ResponseContextMarshaller().marshal(ResponseContext.builder().result(result).build(), out);
		return out.toString();
	}
}
