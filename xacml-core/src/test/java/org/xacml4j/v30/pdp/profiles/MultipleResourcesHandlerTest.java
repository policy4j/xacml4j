package org.xacml4j.v30.pdp.profiles;


import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.xacml4j.v30.types.StringType.STRING;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.Attributes;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCode;
import org.xacml4j.v30.pdp.PolicyDecisionPointContext;
import org.xacml4j.v30.spi.pdp.RequestContextHandler;


public class MultipleResourcesHandlerTest
{
	private PolicyDecisionPointContext context;
	private RequestContextHandler profile;

	private Attributes resource0;
	private Attributes resource1;
	private Attributes subject0;
	private Attributes subject1;


	@Before
	public void init(){
		this.context = createStrictMock(PolicyDecisionPointContext.class);
		this.profile = new MultipleResourcesHandler();

		this.resource0 = Attributes.builder(AttributeCategories.RESOURCE)
				.attribute(
						Attribute.builder("testId1").value(STRING.create("value0")).build(),
						Attribute.builder("testId2").value(STRING.create("value1")).build())
				.build();
		this.resource1 = Attributes.builder(AttributeCategories.RESOURCE)
				.attribute(
						Attribute.builder("testId3").value(STRING.create("value0")).build(),
						Attribute.builder("testId4").value(STRING.create("value1")).build()).build();

		this.subject0 = Attributes.builder(AttributeCategories.SUBJECT_ACCESS)
				.attribute(
						Attribute.builder("testId7").value(STRING.create("value0")).build(),
						Attribute.builder("testId8").value(STRING.create("value1")).build()).build();


		this.subject1 = Attributes.builder(AttributeCategories.SUBJECT_ACCESS)
				.attribute(
						Attribute.builder("testId9").value(STRING.create("value0")).build(),
						Attribute.builder("testId10").value(STRING.create("value1")).build()).build();
	}

	@Test
	public void testAllResultsAreDeny()
	{

		RequestContext request = new RequestContext(false, true,
				Arrays.asList(subject0, subject1, resource0, resource1));

		Capture<RequestContext> c0 = new Capture<RequestContext>();
		Capture<RequestContext> c1 = new Capture<RequestContext>();
		Capture<RequestContext> c2 = new Capture<RequestContext>();
		Capture<RequestContext> c3 = new Capture<RequestContext>();

		expect(context.requestDecision(capture(c0))).andReturn(
						Result.createOk(Decision.DENY).build());

		expect(context.requestDecision(capture(c1))).andReturn(Result.createOk(Decision.DENY).build());

		expect(context.requestDecision(capture(c2))).andReturn(Result.createOk(Decision.DENY).build());

		expect(context.requestDecision(capture(c3))).andReturn(Result.createOk(Decision.DENY).build());

		replay(context);
		Collection<Result> results = profile.handle(request, context);
		assertEquals(1, results.size());
		Result r = results.iterator().next();

		assertEquals(Decision.DENY, r.getDecision());
		assertEquals(Status.createSuccess(), r.getStatus());

		assertEquals(0, r.getIncludeInResultAttributes().size());
		assertEquals(0, r.getObligations().size());
		assertEquals(0, r.getAssociatedAdvice().size());
		verify(context);
	}

	@Test
	public void testAllResultsArePermit()
	{


		RequestContext request = new RequestContext(false, true,
				Arrays.asList(subject0, subject1, resource0, resource1));

		Capture<RequestContext> c0 = new Capture<RequestContext>();
		Capture<RequestContext> c1 = new Capture<RequestContext>();
		Capture<RequestContext> c2 = new Capture<RequestContext>();
		Capture<RequestContext> c3 = new Capture<RequestContext>();

		expect(context.requestDecision(capture(c0))).andReturn(Result.createOk(Decision.PERMIT).build());

		expect(context.requestDecision(capture(c1))).andReturn(Result.createOk(Decision.PERMIT).build());

		expect(context.requestDecision(capture(c2))).andReturn(Result.createOk(Decision.PERMIT).build());

		expect(context.requestDecision(capture(c3))).andReturn(Result.createOk(Decision.PERMIT).build());

		replay(context);
		Collection<Result> results = profile.handle(request, context);
		assertEquals(1, results.size());
		Result r = results.iterator().next();

		assertEquals(Decision.PERMIT, r.getDecision());
		assertEquals(Status.createSuccess(), r.getStatus());

		assertEquals(0, r.getIncludeInResultAttributes().size());
		assertEquals(0, r.getObligations().size());
		assertEquals(0, r.getAssociatedAdvice().size());
		verify(context);
	}

	@Test
	public void testAllResultsAreNotApplicable()
	{


		RequestContext request = new RequestContext(false, true,
				Arrays.asList(subject0, subject1, resource0, resource1));

		Capture<RequestContext> c0 = new Capture<RequestContext>();
		Capture<RequestContext> c1 = new Capture<RequestContext>();
		Capture<RequestContext> c2 = new Capture<RequestContext>();
		Capture<RequestContext> c3 = new Capture<RequestContext>();

		expect(context.requestDecision(capture(c0))).andReturn(
				Result.createOk(Decision.NOT_APPLICABLE).build());

		expect(context.requestDecision(capture(c1))).andReturn(
				Result.createOk(Decision.NOT_APPLICABLE).build());

		expect(context.requestDecision(capture(c2))).andReturn(
				Result.createOk(Decision.NOT_APPLICABLE).build());

		expect(context.requestDecision(capture(c3))).andReturn(
				Result.createOk(Decision.NOT_APPLICABLE).build());

		replay(context);
		Collection<Result> results = profile.handle(request, context);
		assertEquals(1, results.size());
		Result r = results.iterator().next();

		assertEquals(Decision.NOT_APPLICABLE, r.getDecision());
		assertEquals(Status.createSuccess(), r.getStatus());

		assertEquals(0, r.getIncludeInResultAttributes().size());
		assertEquals(0, r.getObligations().size());
		assertEquals(0, r.getAssociatedAdvice().size());
		verify(context);
	}

	@Test
	public void testAllResultsNotApplicable()
	{


		RequestContext request = new RequestContext(false, true,
				Arrays.asList(subject0, subject1, resource0, resource1));

		Capture<RequestContext> c0 = new Capture<RequestContext>();
		Capture<RequestContext> c1 = new Capture<RequestContext>();
		Capture<RequestContext> c2 = new Capture<RequestContext>();
		Capture<RequestContext> c3 = new Capture<RequestContext>();

		expect(context.requestDecision(capture(c0))).andReturn(Result.createOk(Decision.NOT_APPLICABLE).build());

		expect(context.requestDecision(capture(c1))).andReturn(Result.createOk(Decision.NOT_APPLICABLE).build());

		expect(context.requestDecision(capture(c2))).andReturn(Result.createOk(Decision.NOT_APPLICABLE).build());

		expect(context.requestDecision(capture(c3))).andReturn(Result.createOk(Decision.NOT_APPLICABLE).build());

		replay(context);
		Collection<Result> results = profile.handle(request, context);
		assertEquals(1, results.size());
		Result r = results.iterator().next();

		assertEquals(Decision.NOT_APPLICABLE, r.getDecision());
		assertEquals(Status.createSuccess(), r.getStatus());

		assertEquals(0, r.getIncludeInResultAttributes().size());
		assertEquals(0, r.getObligations().size());
		assertEquals(0, r.getAssociatedAdvice().size());
		verify(context);
	}

	@Test
	public void testRequestWithSingleResultCombine()
	{
		RequestContext request = new RequestContext(false, true,
				Arrays.asList(subject0, resource0));

		Capture<RequestContext> c0 = new Capture<RequestContext>();

		expect(context.requestDecision(capture(c0))).andReturn(
				Result.createIndeterminateProcessingError().build());

		replay(context);
		Collection<Result> results = profile.handle(request, context);
		assertEquals(new Status(StatusCode.createProcessingError()), results.iterator().next().getStatus());
		assertEquals(1, results.size());
		RequestContext r0 = c0.getValue();
		assertTrue(r0.getAttributes(AttributeCategories.SUBJECT_ACCESS).contains(subject0));
		assertTrue(r0.getAttributes(AttributeCategories.RESOURCE).contains(resource0));
		verify(context);
	}

	@Test
	public void testWithEmptyRequest()
	{
		RequestContext request = new RequestContext(false, true,
				Collections.<Attributes>emptyList());

		Capture<RequestContext> c0 = new Capture<RequestContext>();

		expect(context.requestDecision(capture(c0))).andReturn(
				Result.createIndeterminateProcessingError().build());

		replay(context);
		Collection<Result> results = profile.handle(request, context);
		assertEquals(new Status(StatusCode.createProcessingError()), results.iterator().next().getStatus());
		assertEquals(1, results.size());
		assertSame(request, c0.getValue());
		verify(context);
	}
}


