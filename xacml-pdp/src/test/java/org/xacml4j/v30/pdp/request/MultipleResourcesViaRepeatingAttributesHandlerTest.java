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
import org.xacml4j.v30.spi.pdp.RequestContextHandler;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.types.XacmlTypes;

import java.util.Collection;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;


public class MultipleResourcesViaRepeatingAttributesHandlerTest
{
	private PolicyDecisionPointContext pdp;
	private RequestContextHandler profile;

	@Before
	public void init(){
		this.pdp = createStrictMock(PolicyDecisionPointContext.class);
		this.profile = new MultipleResourcesViaRepeatingAttributesHandler();
	}

	@Test
	public void testRequestWithTwoAttributesOfTheCategory()

	{
		Category resource0 = Category
				.builder(CategoryId.RESOURCE)
				.entity(
						Entity.builder()
						      .attribute(
						Attribute.builder("testId1").value(XacmlTypes.STRING.of("value0")).build(),
						Attribute.builder("testId2").value(XacmlTypes.STRING.of("value1")).build()).build())
				.build();

		Category resource1 = Category
				.builder(CategoryId.RESOURCE)
				.entity(
						Entity.builder()
						.attribute(
						Attribute.builder("testId3").value(XacmlTypes.STRING.of("value0")).build(),
						Attribute.builder("testId4").value(XacmlTypes.STRING.of("value1")).build()).build())
				.build();

		Category subject = Category
				.builder(CategoryId.SUBJECT_ACCESS)
				.entity(
						Entity.builder()
						.attribute(
						Attribute.builder("testId7").value(XacmlTypes.STRING.of("value0")).build(),
						Attribute.builder("testId8").value(XacmlTypes.STRING.of("value1")).build()).build())
				.build();

		RequestContext context = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.attributes(subject, resource0, resource1)
				.build();

		Capture<RequestContext> c0 = Capture.newInstance();
		Capture<RequestContext> c1 = Capture.newInstance();

		expect(pdp.requestDecision(capture(c0))).andReturn(
				Result.indeterminate(Status.processingError().build()).build());
		expect(pdp.requestDecision(capture(c1))).andReturn(
				Result.indeterminate(Status.processingError().build()).build());
		replay(pdp);
		Collection<Result> results = profile.handle(context, pdp);
		assertEquals(2, results.size());
		assertEquals(Status.processingError().build(), results.iterator().next().getStatus());
		RequestContext r0 = c0.getValue();
		RequestContext r1 = c1.getValue();
		assertTrue(r0.getCategory(CategoryId.SUBJECT_ACCESS).contains(subject));
		assertEquals(1, r0.getCategory(CategoryId.RESOURCE).size());
		// order is not known so check if has 1 and at least one is in the request
		assertTrue(r0.getCategory(CategoryId.RESOURCE).contains(resource0) || r0.getCategory(CategoryId.RESOURCE).contains(resource1));
		assertTrue(r1.getCategory(CategoryId.SUBJECT_ACCESS).contains(subject));
		// order is not known so check if has 1 and at least one is in the request
		assertEquals(1, r1.getCategory(CategoryId.RESOURCE).size());
		assertTrue(r0.getCategory(CategoryId.RESOURCE).contains(resource0) || r0.getCategory(CategoryId.RESOURCE).contains(resource1));
		verify(pdp);
	}


	@Test
	public void testRequestWithNoAttributesOfTheSameCategory()
	{
		Category resource0 = Category
				.builder(CategoryId.RESOURCE)
				.entity(
						Entity.builder()
						.attribute(
								Attribute.builder("testId1").value(XacmlTypes.STRING.of("value0")).build(),
								Attribute.builder("testId2").value(XacmlTypes.STRING.of("value1")).build()).build())
				.build();


		Category subject = Category
				.builder(CategoryId.SUBJECT_ACCESS)
				.entity(Entity.builder()
						.attribute(
								Attribute.builder("testId7").value(XacmlTypes.STRING.of("value0")).build(),
								Attribute.builder("testId8").value(XacmlTypes.STRING.of("value1")).build())
						.build())
				.build();

		RequestContext context = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.attributes(subject, resource0)
				.build();

		Capture<RequestContext> c0 = Capture.newInstance();

		expect(pdp.requestDecision(capture(c0))).andReturn(
				Result.indeterminate(Status.processingError().build()).build());

		replay(pdp);
		Collection<Result> results = profile.handle(context, pdp);
		assertEquals(Status.processingError().build(), results.iterator().next().getStatus());
		assertEquals(1, results.size());
		RequestContext r0 = c0.getValue();
		assertTrue(r0.getCategory(CategoryId.SUBJECT_ACCESS).contains(subject));
		assertTrue(r0.getCategory(CategoryId.RESOURCE).contains(resource0));
		verify(pdp);
	}

	@Test
	public void testWithEmptyRequest()
	{
		RequestContext context = RequestContext.builder().build();

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

