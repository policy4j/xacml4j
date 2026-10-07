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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.pdp.PolicyDecisionPointContext;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.spi.pdp.RequestContextHandler;
import org.xacml4j.v30.types.XacmlTypes;


public class MultipleResourcesViaRepeatingAttributesHandlerTest
{
	private PolicyDecisionPointContext pdp;
	private RequestContextHandler profile;

	@BeforeEach
	public void init(){
		this.pdp = mock(PolicyDecisionPointContext.class);
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
						Attribute.builder("testId1").value(XacmlTypes.STRING.ofAny("value0")).build(),
						Attribute.builder("testId2").value(XacmlTypes.STRING.ofAny("value1")).build()).build())
				.build();

		Category resource1 = Category
				.builder(CategoryId.RESOURCE)
				.entity(
						Entity.builder()
						.attribute(
						Attribute.builder("testId3").value(XacmlTypes.STRING.ofAny("value0")).build(),
						Attribute.builder("testId4").value(XacmlTypes.STRING.ofAny("value1")).build()).build())
				.build();

		Category subject = Category
				.builder(CategoryId.SUBJECT_ACCESS)
				.entity(
						Entity.builder()
						.attribute(
						Attribute.builder("testId7").value(XacmlTypes.STRING.ofAny("value0")).build(),
						Attribute.builder("testId8").value(XacmlTypes.STRING.ofAny("value1")).build()).build())
				.build();

		RequestContext context = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.attributes(subject, resource0, resource1)
				.build();

		ArgumentCaptor<RequestContext> requests = ArgumentCaptor.forClass(RequestContext.class);

		when(pdp.requestDecision(Mockito.any())).thenReturn(
				Result.indeterminate(Status.processingError().build()).build());
		Collection<Result> results = profile.handle(context, pdp);
		assertEquals(2, results.size());
		assertEquals(Status.processingError().build(), results.iterator().next().getStatus());
		InOrder order = Mockito.inOrder(pdp);
		order.verify(pdp, times(2)).requestDecision(requests.capture());
		Mockito.verifyNoMoreInteractions(pdp);
		RequestContext r0 = requests.getAllValues().get(0);
		RequestContext r1 = requests.getAllValues().get(1);
		assertTrue(r0.getCategory(CategoryId.SUBJECT_ACCESS).contains(subject));
		assertEquals(1, r0.getCategory(CategoryId.RESOURCE).size());
		// order is not known so check if has 1 and at least one is in the request
		assertTrue(r0.getCategory(CategoryId.RESOURCE).contains(resource0) || r0.getCategory(CategoryId.RESOURCE).contains(resource1));
		assertTrue(r1.getCategory(CategoryId.SUBJECT_ACCESS).contains(subject));
		// order is not known so check if has 1 and at least one is in the request
		assertEquals(1, r1.getCategory(CategoryId.RESOURCE).size());
		assertTrue(r0.getCategory(CategoryId.RESOURCE).contains(resource0) || r0.getCategory(CategoryId.RESOURCE).contains(resource1));
	}


	@Test
	public void testRequestWithNoAttributesOfTheSameCategory()
	{
		Category resource0 = Category
				.builder(CategoryId.RESOURCE)
				.entity(
						Entity.builder()
						.attribute(
								Attribute.builder("testId1").value(XacmlTypes.STRING.ofAny("value0")).build(),
								Attribute.builder("testId2").value(XacmlTypes.STRING.ofAny("value1")).build()).build())
				.build();


		Category subject = Category
				.builder(CategoryId.SUBJECT_ACCESS)
				.entity(Entity.builder()
						.attribute(
								Attribute.builder("testId7").value(XacmlTypes.STRING.ofAny("value0")).build(),
								Attribute.builder("testId8").value(XacmlTypes.STRING.ofAny("value1")).build())
						.build())
				.build();

		RequestContext context = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.attributes(subject, resource0)
				.build();

		ArgumentCaptor<RequestContext> c0 = ArgumentCaptor.forClass(RequestContext.class);

		when(pdp.requestDecision(Mockito.any())).thenReturn(
				Result.indeterminate(Status.processingError().build()).build());
		Collection<Result> results = profile.handle(context, pdp);
		assertEquals(Status.processingError().build(), results.iterator().next().getStatus());
		assertEquals(1, results.size());
		verify(pdp).requestDecision(c0.capture());
		Mockito.verifyNoMoreInteractions(pdp);
		RequestContext r0 = c0.getAllValues().get(0);
		assertTrue(r0.getCategory(CategoryId.SUBJECT_ACCESS).contains(subject));
		assertTrue(r0.getCategory(CategoryId.RESOURCE).contains(resource0));
	}

	@Test
	public void testWithEmptyRequest()
	{
		RequestContext context = RequestContext.builder().build();

		ArgumentCaptor<RequestContext> c0 = ArgumentCaptor.forClass(RequestContext.class);

		when(pdp.requestDecision(Mockito.any())).thenReturn(
				Result.indeterminate(Status.processingError().build()).build());
		Collection<Result> results = profile.handle(context, pdp);
		assertEquals(Status.processingError().build(), results.iterator().next().getStatus());
		assertEquals(1, results.size());
		verify(pdp).requestDecision(c0.capture());
		Mockito.verifyNoMoreInteractions(pdp);
		assertSame(context, c0.getAllValues().get(0));
	}
}
