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
import org.easymock.IMocksControl;
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


public class MultipleResourcesHandlerTest
{
	private PolicyDecisionPointContext context;
	private RequestContextHandler profile;

	private Category resource0;
	private Category resource1;
	private Category subject0;
	private Category subject1;
	private IMocksControl c;


	@Before
	public void init(){
		this.c = createStrictControl();
		this.context = c.mock(PolicyDecisionPointContext.class);
		this.profile = new MultipleResourcesHandler();

		this.resource0 = Category.builder(CategoryId.RESOURCE)
				.entity(
						Entity.builder()
						      .attribute(
								Attribute.builder("testId1").value(XacmlTypes.STRING.of("value0")).build(),
								Attribute.builder("testId2").value(XacmlTypes.STRING.of("value1")).build()).build())
				.build();
		this.resource1 = Category.builder(CategoryId.RESOURCE)
				.entity(
						Entity.builder()
						.attribute(
						Attribute.builder("testId3").value(XacmlTypes.STRING.of("value0")).build(),
						Attribute.builder("testId4").value(XacmlTypes.STRING.of("value1")).build()).build())
				.build();

		this.subject0 = Category.builder(CategoryId.SUBJECT_ACCESS)
				.entity(
						Entity.builder()
						.attribute(
								Attribute.builder("testId7").value(XacmlTypes.STRING.of("value0")).build(),
								Attribute.builder("testId8").value(XacmlTypes.STRING.of("value1")).build()).build())
				.build();


		this.subject1 = Category.builder(CategoryId.SUBJECT_ACCESS)
				.entity(
						Entity.builder()
						.attribute(
						Attribute.builder("testId9").value(XacmlTypes.STRING.of("value0")).build(),
						Attribute.builder("testId10").value(XacmlTypes.STRING.of("value1")).build()).build())
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

		Capture<RequestContext> c0 = Capture.newInstance();
		Capture<RequestContext> c1 = Capture.newInstance();
		Capture<RequestContext> c2 = Capture.newInstance();
		Capture<RequestContext> c3 = Capture.newInstance();

		expect(context.requestDecision(capture(c0))).andReturn(
						Result.ok(Decision.DENY).build());

		expect(context.requestDecision(capture(c1))).andReturn(Result.ok(Decision.DENY).build());

		expect(context.requestDecision(capture(c2))).andReturn(Result.ok(Decision.DENY).build());

		expect(context.requestDecision(capture(c3))).andReturn(Result.ok(Decision.DENY).build());

		c.replay();
		Collection<Result> results = profile.handle(request, context);
		c.verify();
		assertEquals(1, results.size());
		Result r = results.iterator().next();

		assertEquals(Decision.DENY, r.getDecision());
		assertEquals(Status.ok().build(), r.getStatus());

		assertEquals(0, r.getIncludeInResultAttributes().size());
		assertEquals(0, r.getObligations().size());
		assertEquals(0, r.getAssociatedAdvice().size());

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

		Capture<RequestContext> c0 = Capture.newInstance();
		Capture<RequestContext> c1 = Capture.newInstance();
		Capture<RequestContext> c2 = Capture.newInstance();
		Capture<RequestContext> c3 = Capture.newInstance();

		expect(context.requestDecision(capture(c0))).andReturn(Result.ok(Decision.PERMIT).build());

		expect(context.requestDecision(capture(c1))).andReturn(Result.ok(Decision.PERMIT).build());

		expect(context.requestDecision(capture(c2))).andReturn(Result.ok(Decision.PERMIT).build());

		expect(context.requestDecision(capture(c3))).andReturn(Result.ok(Decision.PERMIT).build());

		c.replay();
		Collection<Result> results = profile.handle(request, context);
		c.verify();
		assertEquals(1, results.size());
		Result r = results.iterator().next();

		assertEquals(Decision.PERMIT, r.getDecision());
		assertEquals(Status.ok().build(), r.getStatus());

		assertEquals(0, r.getIncludeInResultAttributes().size());
		assertEquals(0, r.getObligations().size());
		assertEquals(0, r.getAssociatedAdvice().size());

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

		Capture<RequestContext> c0 = Capture.newInstance();
		Capture<RequestContext> c1 = Capture.newInstance();
		Capture<RequestContext> c2 = Capture.newInstance();
		Capture<RequestContext> c3 = Capture.newInstance();

		expect(context.requestDecision(capture(c0))).andReturn(
				Result.ok(Decision.NOT_APPLICABLE).build());

		expect(context.requestDecision(capture(c1))).andReturn(
				Result.ok(Decision.NOT_APPLICABLE).build());

		expect(context.requestDecision(capture(c2))).andReturn(
				Result.ok(Decision.NOT_APPLICABLE).build());

		expect(context.requestDecision(capture(c3))).andReturn(
				Result.ok(Decision.NOT_APPLICABLE).build());

		c.replay();
		Collection<Result> results = profile.handle(request, context);
		assertEquals(1, results.size());
		Result r = results.iterator().next();

		assertEquals(Decision.NOT_APPLICABLE, r.getDecision());
		assertEquals(Status.ok().build(), r.getStatus());

		assertEquals(0, r.getIncludeInResultAttributes().size());
		assertEquals(0, r.getObligations().size());
		assertEquals(0, r.getAssociatedAdvice().size());
		c.verify();
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

		Capture<RequestContext> c0 = Capture.newInstance();
		Capture<RequestContext> c1 = Capture.newInstance();
		Capture<RequestContext> c2 = Capture.newInstance();
		Capture<RequestContext> c3 = Capture.newInstance();

		expect(context.requestDecision(capture(c0))).andReturn(Result.ok(Decision.NOT_APPLICABLE).build());

		expect(context.requestDecision(capture(c1))).andReturn(Result.ok(Decision.NOT_APPLICABLE).build());

		expect(context.requestDecision(capture(c2))).andReturn(Result.ok(Decision.NOT_APPLICABLE).build());

		expect(context.requestDecision(capture(c3))).andReturn(Result.ok(Decision.NOT_APPLICABLE).build());

		c.replay();
		Collection<Result> results = profile.handle(request, context);
		assertEquals(1, results.size());
		Result r = results.iterator().next();

		assertEquals(Decision.NOT_APPLICABLE, r.getDecision());
		assertEquals(Status.ok().build(), r.getStatus());

		assertEquals(0, r.getIncludeInResultAttributes().size());
		assertEquals(0, r.getObligations().size());
		assertEquals(0, r.getAssociatedAdvice().size());
		c.verify();
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

		Capture<RequestContext> c0 = Capture.newInstance();

		expect(context.requestDecision(capture(c0))).andReturn(
				Result.indeterminate(Status.processingError().build()).build());

		c.replay();
		Collection<Result> results = profile.handle(request, context);
		assertEquals(Status.processingError().build(), results.iterator().next().getStatus());
		assertEquals(1, results.size());
		RequestContext r0 = c0.getValue();
		assertTrue(r0.getCategory(CategoryId.SUBJECT_ACCESS).contains(subject0));
		assertTrue(r0.getCategory(CategoryId.RESOURCE).contains(resource0));
		c.verify();
	}

	@Test
	public void testWithEmptyRequest()
	{
		RequestContext request = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.combineDecision(true)
				.build();

		Capture<RequestContext> c0 = Capture.newInstance();

		expect(context.requestDecision(capture(c0))).andReturn(
				Result.indeterminate(Status.processingError().build()).build());

		c.replay();
		Collection<Result> results = profile.handle(request, context);
		assertEquals(Status.processingError().build(), results.iterator().next().getStatus());
		assertEquals(1, results.size());
		assertSame(request, c0.getValue());
		c.verify();
	}
}


