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


import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.pdp.PolicyDecisionPointContext;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.spi.pdp.RequestContextHandler;
import org.xacml4j.v30.types.XacmlTypes;


public class MultipleResourcesHandlerTest
{
	private PolicyDecisionPointContext context;
	private RequestContextHandler profile;

	private Category resource0;
	private Category resource1;
	private Category subject0;
	private Category subject1;


	@BeforeEach
	public void init(){
		this.context = Mockito.mock(PolicyDecisionPointContext.class);
		this.profile = new MultipleResourcesHandler();

		this.resource0 = Category.builder(CategoryId.RESOURCE)
				.entity(
						Entity.builder()
						      .attribute(
								Attribute.builder("testId1").value(XacmlTypes.STRING.ofAny("value0")).build(),
								Attribute.builder("testId2").value(XacmlTypes.STRING.ofAny("value1")).build()).build())
				.build();
		this.resource1 = Category.builder(CategoryId.RESOURCE)
				.entity(
						Entity.builder()
						.attribute(
						Attribute.builder("testId3").value(XacmlTypes.STRING.ofAny("value0")).build(),
						Attribute.builder("testId4").value(XacmlTypes.STRING.ofAny("value1")).build()).build())
				.build();

		this.subject0 = Category.builder(CategoryId.SUBJECT_ACCESS)
				.entity(
						Entity.builder()
						.attribute(
								Attribute.builder("testId7").value(XacmlTypes.STRING.ofAny("value0")).build(),
								Attribute.builder("testId8").value(XacmlTypes.STRING.ofAny("value1")).build()).build())
				.build();


		this.subject1 = Category.builder(CategoryId.SUBJECT_ACCESS)
				.entity(
						Entity.builder()
						.attribute(
						Attribute.builder("testId9").value(XacmlTypes.STRING.ofAny("value0")).build(),
						Attribute.builder("testId10").value(XacmlTypes.STRING.ofAny("value1")).build()).build())
				.build();
	}

	@Test
	public void testAllResultsAreDeny()
	{

		RequestContext request = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.combineDecision(true)
				.attributes(subject0, subject1, resource0, resource1)
				.build();

		ArgumentCaptor<RequestContext> requests = ArgumentCaptor.forClass(RequestContext.class);

		when(context.requestDecision(Mockito.any())).thenReturn(Result.ok(Decision.DENY).build());
		Collection<Result> results = profile.handle(request, context);
		assertEquals(1, results.size());
		Result r = results.iterator().next();

		assertEquals(Decision.DENY, r.getDecision());
		assertEquals(Status.ok().build(), r.getStatus());

		assertEquals(0, r.getIncludeInResultAttributes().size());
		assertEquals(0, r.getObligations().size());
		assertEquals(0, r.getAssociatedAdvice().size());
		InOrder order = Mockito.inOrder(context);
		order.verify(context, Mockito.times(4)).requestDecision(requests.capture());
		Mockito.verifyNoMoreInteractions(context);

	}

	@Test
	public void testAllResultsArePermit()
	{
		RequestContext request = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.combineDecision(true)
				.attributes(subject0, subject1, resource0, resource1)
				.build();

		ArgumentCaptor<RequestContext> requests = ArgumentCaptor.forClass(RequestContext.class);

		when(context.requestDecision(Mockito.any())).thenReturn(Result.ok(Decision.PERMIT).build());
		Collection<Result> results = profile.handle(request, context);
		assertEquals(1, results.size());
		Result r = results.iterator().next();

		assertEquals(Decision.PERMIT, r.getDecision());
		assertEquals(Status.ok().build(), r.getStatus());

		assertEquals(0, r.getIncludeInResultAttributes().size());
		assertEquals(0, r.getObligations().size());
		assertEquals(0, r.getAssociatedAdvice().size());
		InOrder order = Mockito.inOrder(context);
		order.verify(context, Mockito.times(4)).requestDecision(requests.capture());
		Mockito.verifyNoMoreInteractions(context);

	}

	@Test
	public void testAllResultsAreNotApplicable()
	{

		RequestContext request = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.combineDecision(true)
				.attributes(subject0, subject1, resource0, resource1)
				.build();

		ArgumentCaptor<RequestContext> requests = ArgumentCaptor.forClass(RequestContext.class);

		when(context.requestDecision(Mockito.any())).thenReturn(
				Result.ok(Decision.NOT_APPLICABLE).build());
		Collection<Result> results = profile.handle(request, context);
		assertEquals(1, results.size());
		Result r = results.iterator().next();

		assertEquals(Decision.NOT_APPLICABLE, r.getDecision());
		assertEquals(Status.ok().build(), r.getStatus());

		assertEquals(0, r.getIncludeInResultAttributes().size());
		assertEquals(0, r.getObligations().size());
		assertEquals(0, r.getAssociatedAdvice().size());
		InOrder order = Mockito.inOrder(context);
		order.verify(context, Mockito.times(4)).requestDecision(requests.capture());
		Mockito.verifyNoMoreInteractions(context);
	}

	@Test
	public void testAllResultsNotApplicable()
	{


		RequestContext request = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.combineDecision(true)
				.attributes(subject0, subject1, resource0, resource1)
				.build();

		ArgumentCaptor<RequestContext> requests = ArgumentCaptor.forClass(RequestContext.class);

		when(context.requestDecision(Mockito.any())).thenReturn(Result.ok(Decision.NOT_APPLICABLE).build());
		Collection<Result> results = profile.handle(request, context);
		assertEquals(1, results.size());
		Result r = results.iterator().next();

		assertEquals(Decision.NOT_APPLICABLE, r.getDecision());
		assertEquals(Status.ok().build(), r.getStatus());

		assertEquals(0, r.getIncludeInResultAttributes().size());
		assertEquals(0, r.getObligations().size());
		assertEquals(0, r.getAssociatedAdvice().size());
		InOrder order = Mockito.inOrder(context);
		order.verify(context, Mockito.times(4)).requestDecision(requests.capture());
		Mockito.verifyNoMoreInteractions(context);
	}

	@Test
	public void testRequestWithSingleResultCombine()
	{
		RequestContext request = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.combineDecision(true)
				.attributes(subject0, resource0)
				.build();

		ArgumentCaptor<RequestContext> c0 = ArgumentCaptor.forClass(RequestContext.class);

		when(context.requestDecision(Mockito.any())).thenReturn(
				Result.indeterminate(Status.processingError().build()).build());
		Collection<Result> results = profile.handle(request, context);
		assertEquals(Status.processingError().build(), results.iterator().next().getStatus());
		assertEquals(1, results.size());
		InOrder order = Mockito.inOrder(context);
		order.verify(context).requestDecision(c0.capture());
		Mockito.verifyNoMoreInteractions(context);
		RequestContext r0 = c0.getAllValues().get(0);
		assertTrue(r0.getCategory(CategoryId.SUBJECT_ACCESS).contains(subject0));
		assertTrue(r0.getCategory(CategoryId.RESOURCE).contains(resource0));
	}

	@Test
	public void testWithEmptyRequest()
	{
		RequestContext request = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.combineDecision(true)
				.build();

		ArgumentCaptor<RequestContext> c0 = ArgumentCaptor.forClass(RequestContext.class);

		when(context.requestDecision(Mockito.any())).thenReturn(
				Result.indeterminate(Status.processingError().build()).build());
		Collection<Result> results = profile.handle(request, context);
		assertEquals(Status.processingError().build(), results.iterator().next().getStatus());
		assertEquals(1, results.size());
		InOrder order = Mockito.inOrder(context);
		order.verify(context).requestDecision(c0.capture());
		Mockito.verifyNoMoreInteractions(context);
		assertSame(request, c0.getAllValues().get(0));
	}
}
