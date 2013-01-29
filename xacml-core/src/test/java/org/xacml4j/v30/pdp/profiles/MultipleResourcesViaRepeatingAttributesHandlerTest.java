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
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCode;
import org.xacml4j.v30.pdp.PolicyDecisionPointContext;
import org.xacml4j.v30.spi.pdp.RequestContextHandler;


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
		Attributes resource0 = Attributes
				.builder(AttributeCategories.RESOURCE)
				.attribute(
						Attribute.builder("testId1").value(STRING.create("value0")).build(),
						Attribute.builder("testId2").value(STRING.create("value1")).build())
				.build();

		Attributes resource1 = Attributes
				.builder(AttributeCategories.RESOURCE)
				.attribute(
						Attribute.builder("testId3").value(STRING.create("value0")).build(),
						Attribute.builder("testId4").value(STRING.create("value1")).build())
				.build();

		Attributes subject = Attributes
				.builder(AttributeCategories.SUBJECT_ACCESS)
				.attribute(
						Attribute.builder("testId7").value(STRING.create("value0")).build(),
						Attribute.builder("testId8").value(STRING.create("value1")).build())
				.build();

		RequestContext context = new RequestContext(false,
				Arrays.asList(subject, resource0, resource1));

		Capture<RequestContext> c0 = new Capture<RequestContext>();
		Capture<RequestContext> c1 = new Capture<RequestContext>();

		expect(pdp.requestDecision(capture(c0))).andReturn(
				Result.createIndeterminateProcessingError().build());
		expect(pdp.requestDecision(capture(c1))).andReturn(
				Result.createIndeterminateProcessingError().build());
		replay(pdp);
		Collection<Result> results = profile.handle(context, pdp);
		assertEquals(2, results.size());
		assertEquals(new Status(StatusCode.createProcessingError()), results.iterator().next().getStatus());
		RequestContext r0 = c0.getValue();
		RequestContext r1 = c1.getValue();
		assertTrue(r0.getAttributes(AttributeCategories.SUBJECT_ACCESS).contains(subject));
		assertEquals(1, r0.getAttributes(AttributeCategories.RESOURCE).size());
		// order is not known so check if has 1 and at least one is in the request
		assertTrue(r0.getAttributes(AttributeCategories.RESOURCE).contains(resource0) || r0.getAttributes(AttributeCategories.RESOURCE).contains(resource1));
		assertTrue(r1.getAttributes(AttributeCategories.SUBJECT_ACCESS).contains(subject));
		// order is not known so check if has 1 and at least one is in the request
		assertEquals(1, r1.getAttributes(AttributeCategories.RESOURCE).size());
		assertTrue(r0.getAttributes(AttributeCategories.RESOURCE).contains(resource0) || r0.getAttributes(AttributeCategories.RESOURCE).contains(resource1));
		verify(pdp);
	}


	@Test
	public void testRequestWithNoAttributesOfTheSameCategory()
	{
		Attributes resource0 = Attributes
				.builder(AttributeCategories.RESOURCE)
				.attribute(
						Attribute.builder("testId1").value(STRING.create("value0")).build(),
						Attribute.builder("testId2").value(STRING.create("value1")).build())
				.build();


		Attributes subject = Attributes
				.builder(AttributeCategories.SUBJECT_ACCESS)
				.attribute(
						Attribute.builder("testId7").value(STRING.create("value0")).build(),
						Attribute.builder("testId8").value(STRING.create("value1")).build())
				.build();

		RequestContext context = new RequestContext(false,
				Arrays.asList(subject, resource0));

		Capture<RequestContext> c0 = new Capture<RequestContext>();

		expect(pdp.requestDecision(capture(c0))).andReturn(
				Result.createIndeterminateProcessingError().build());

		replay(pdp);
		Collection<Result> results = profile.handle(context, pdp);
		assertEquals(new Status(StatusCode.createProcessingError()), results.iterator().next().getStatus());
		assertEquals(1, results.size());
		RequestContext r0 = c0.getValue();
		assertTrue(r0.getAttributes(AttributeCategories.SUBJECT_ACCESS).contains(subject));
		assertTrue(r0.getAttributes(AttributeCategories.RESOURCE).contains(resource0));
		verify(pdp);
	}

	@Test
	public void testWithEmptyRequest()
	{
		RequestContext context = new RequestContext(false,
				Collections.<Attributes>emptyList());

		Capture<RequestContext> c0 = new Capture<RequestContext>();

		expect(pdp.requestDecision(capture(c0))).andReturn(
				Result.createIndeterminateProcessingError().build());

		replay(pdp);
		Collection<Result> results = profile.handle(context, pdp);
		assertEquals(new Status(StatusCode.createProcessingError()), results.iterator().next().getStatus());
		assertEquals(1, results.size());
		assertSame(context, c0.getValue());
		verify(pdp);
	}
}

