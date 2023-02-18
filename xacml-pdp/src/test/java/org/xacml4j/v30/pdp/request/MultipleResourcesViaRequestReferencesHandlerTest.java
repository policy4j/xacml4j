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

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.*;
import org.xacml4j.v30.pdp.PolicyDecisionPointContext;
import org.xacml4j.v30.request.RequestContext;
import org.xacml4j.v30.request.RequestReference;
import org.xacml4j.v30.request.RequestSyntaxException;
import org.xacml4j.v30.spi.pdp.RequestContextHandler;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.XacmlTypes;

import java.util.Collection;
import java.util.LinkedList;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class MultipleResourcesViaRequestReferencesHandlerTest
{
	private PolicyDecisionPointContext pdp;
	private RequestContextHandler profile;

	@Before
	public void init()
	{
		this.pdp = createStrictMock(PolicyDecisionPointContext.class);
		this.profile = new MultipleResourcesViaRequestReferencesHandler();
	}

	@Test
	public void testResolveRequestsWithValidReferences() throws RequestSyntaxException
	{
		Category attr0 = Category
				.builder(CategoryId.RESOURCE)
				.id("resourceAttr0")
				.entity(
						Entity.builder()
						      .attribute(
								Attribute.builder("testId1").value(XacmlTypes.STRING.of("value0")).build(),
								Attribute.builder("testId2").value(XacmlTypes.STRING.of("value1")).build())
						      .build())
				.build();

		Category attr1 = Category
				.builder(CategoryId.RESOURCE)
				.id("resourceAttr1")
				.entity(
						Entity.builder()
						.attribute(
								Attribute.builder("testId3").value(XacmlTypes.STRING.of("value0")).build(),
								Attribute.builder("testId4").value(XacmlTypes.STRING.of("value1")).build())
						.build())
				.build();

		Category attr2 = Category
				.builder(CategoryId.ACTION)
				.id("actionAttr0")
				.entity(
						Entity.builder()
						.attribute(
								Attribute.builder("testId3").value(XacmlTypes.STRING.of("value0")).build(),
								Attribute.builder("testId4").value(XacmlTypes.STRING.of("value1")).build())
						.build())
				.build();

		Category attr3 = Category
				.builder(CategoryId.SUBJECT_ACCESS)
				.id("subjectAttr0")
				.entity(
						Entity.builder()
						.attribute(
						Attribute.builder("testId5").value(XacmlTypes.STRING.of("value0")).build(),
						Attribute.builder("testId6").value(XacmlTypes.STRING.of("value1")).build())
						.build())
				.build();

		Category attr4 = Category
				.builder(CategoryId.SUBJECT_ACCESS)
				.id("subjectAttr1")
				.entity(
						Entity.builder()
						.attribute(
						Attribute.builder("testId7").value(XacmlTypes.STRING.of("value0")).build(),
						Attribute.builder("testId8").value(XacmlTypes.STRING.of("value1")).build()).build())
				.build();

		RequestReference reference0 = RequestReference.builder()
		                                              .reference("resourceAttr0", "subjectAttr0")
		                                              .build();


		Collection<CategoryReference> ref1 = new LinkedList<CategoryReference>();
		ref1.add(CategoryReference.builder().id("resourceAttr1").build());
		ref1.add(CategoryReference.builder().id("subjectAttr1").build());

		RequestReference reference1 = RequestReference.builder()
				.reference("resourceAttr1", "subjectAttr1")
				.build();

		RequestContext context = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.combineDecision(false)
				.attributes(attr0, attr1, attr2, attr3, attr4)
				.reference(reference0, reference1)
				.build();

		Capture<RequestContext> c0 = Capture.newInstance();
		Capture<RequestContext> c1 = Capture.newInstance();

		expect(pdp.requestDecision(capture(c0))).andReturn(
				Result.indeterminate(Status.processingError().build()).build());
		expect(pdp.requestDecision(capture(c1))).andReturn(
				Result.indeterminate(Status.processingError().build()).build());
		replay(pdp);
		profile.handle(context, pdp).iterator();
		RequestContext context0 = c0.getValue();
		RequestContext context1 = c0.getValue();

		assertNotNull(context0.getAttribute(CategoryId.SUBJECT_ACCESS, "testId5").orElse(null));
		assertNotNull(context0.getAttribute(CategoryId.SUBJECT_ACCESS, "testId6").orElse(null));
		assertNotNull(context0.getAttribute(CategoryId.RESOURCE, "testId1").orElse(null));
		assertNotNull(context0.getAttribute(CategoryId.RESOURCE, "testId2").orElse(null));

		assertEquals(2, context0.getCategories().size());
		assertEquals(1, context0.getCategory(CategoryId.SUBJECT_ACCESS).size());
		assertEquals(1, context0.getCategory(CategoryId.RESOURCE).size());

		assertNotNull(context1.getAttribute(CategoryId.SUBJECT_ACCESS, "testId7").orElse(null));
		assertNotNull(context1.getAttribute(CategoryId.SUBJECT_ACCESS, "testId8").orElse(null));
		assertNotNull(context1.getAttribute(CategoryId.RESOURCE, "testId3").orElse(null));
		assertNotNull(context1.getAttribute(CategoryId.RESOURCE, "testId4").orElse(null));
		assertEquals(2, context1.getCategories().size());
		assertEquals(1, context1.getCategory(CategoryId.SUBJECT_ACCESS).size());
		assertEquals(1, context1.getCategory(CategoryId.RESOURCE).size());
		verify(pdp);
	}

	@Test
	public void testWithNoReferences()
	{

		Category attr0 = Category
				.builder(CategoryId.RESOURCE)
				.id("resourceAttr0")
				.entity(
						Entity.builder()
						.attribute(
						Attribute.builder("testId3").value(XacmlTypes.STRING.of("value0")).build(),
						Attribute.builder("testId4").value(XacmlTypes.STRING.of("value1")).build()).build())
				.build();

		Category attr1 = Category
				.builder(CategoryId.SUBJECT_ACCESS)
				.id("subjectAttr0")
				.entity(
						Entity.builder()
						.attribute(
						Attribute.builder("testId5").value(XacmlTypes.STRING.of("value0")).build(),
						Attribute.builder("testId6").value(XacmlTypes.STRING.of("value1")).build()).build())
				.build();

		RequestContext request = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.attributes(attr0, attr1)
				.build();

		expect(pdp.requestDecision(request)).andReturn(
				Result.indeterminate(Status.processingError().build()).build());
		replay(pdp);
		Collection<Result> results = profile.handle(request, pdp);
		assertEquals(Result.indeterminate(Status.processingError().build()).build(), results.iterator().next());
		verify(pdp);
	}

	@Test
	public void testWithEmptyRequest()
	{
		RequestContext context = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.build();

		Capture<RequestContext> c0 = Capture.newInstance();

		expect(pdp.requestDecision(capture(c0))).andReturn(
				Result.indeterminate(Status.processingError().build()).build());

		replay(pdp);
		Collection<Result> results = profile.handle(context, pdp);
		assertEquals(Status.processingError().build(), results.iterator().next().getStatus());
		assertEquals(1, results.size());
		assertSame(context, c0.getValue());
		verify(pdp);
	}
}
