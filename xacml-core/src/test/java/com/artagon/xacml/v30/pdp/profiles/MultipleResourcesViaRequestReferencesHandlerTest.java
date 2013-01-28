package com.artagon.xacml.v30.pdp.profiles;

import static com.artagon.xacml.v30.types.StringType.STRING;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.Attribute;
import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.Attributes;
import com.artagon.xacml.v30.AttributesReference;
import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.RequestReference;
import com.artagon.xacml.v30.Result;
import com.artagon.xacml.v30.Status;
import com.artagon.xacml.v30.StatusCode;
import com.artagon.xacml.v30.pdp.PolicyDecisionPointContext;
import com.artagon.xacml.v30.pdp.RequestSyntaxException;
import com.artagon.xacml.v30.spi.pdp.RequestContextHandler;
import com.google.common.collect.Iterables;

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
		Attributes attr0 = Attributes
				.builder(AttributeCategories.RESOURCE)
				.id("resourceAttr0")
				.attribute(
						Attribute.builder("testId1").value(STRING.create("value0")).build(),
						Attribute.builder("testId2").value(STRING.create("value1")).build())
				.build();

		Attributes attr1 = Attributes
				.builder(AttributeCategories.RESOURCE)
				.id("resourceAttr1")
				.attribute(
						Attribute.builder("testId3").value(STRING.create("value0")).build(),
						Attribute.builder("testId4").value(STRING.create("value1")).build())
				.build();

		Attributes attr2 = Attributes
				.builder(AttributeCategories.ACTION)
				.id("actionAttr0")
				.attribute(
						Attribute.builder("testId3").value(STRING.create("value0")).build(),
						Attribute.builder("testId4").value(STRING.create("value1")).build())
				.build();

		Attributes attr3 = Attributes
				.builder(AttributeCategories.SUBJECT_ACCESS)
				.id("subjectAttr0")
				.attribute(
						Attribute.builder("testId5").value(STRING.create("value0")).build(),
						Attribute.builder("testId6").value(STRING.create("value1")).build())
				.build();

		Attributes attr4 = Attributes
				.builder(AttributeCategories.SUBJECT_ACCESS)
				.id("subjectAttr1")
				.attribute(
						Attribute.builder("testId7").value(STRING.create("value0")).build(),
						Attribute.builder("testId8").value(STRING.create("value1")).build())
				.build();

		RequestReference reference0 = RequestReference.builder()
				.reference("resourceAttr0", "subjectAttr0")
				.build();


		Collection<AttributesReference> ref1 = new LinkedList<AttributesReference>();
		ref1.add(AttributesReference.builder().id("resourceAttr1").build());
		ref1.add(AttributesReference.builder().id("subjectAttr1").build());

		RequestReference reference1 = RequestReference.builder()
				.reference("resourceAttr1", "subjectAttr1")
				.build();

		RequestContext context = new RequestContext(false, false,
				Arrays.asList(attr0, attr1, attr2, attr3, attr4),
				Arrays.asList(reference0, reference1));

		Capture<RequestContext> c0 = new Capture<RequestContext>();
		Capture<RequestContext> c1 = new Capture<RequestContext>();

		expect(pdp.requestDecision(capture(c0))).andReturn(
				Result.createIndeterminateProcessingError().build());
		expect(pdp.requestDecision(capture(c1))).andReturn(
				Result.createIndeterminateProcessingError().build());
		replay(pdp);
		profile.handle(context, pdp).iterator();
		RequestContext context0 = c0.getValue();
		RequestContext context1 = c0.getValue();

		assertNotNull(Iterables.getOnlyElement(context0.getAttributes(AttributeCategories.SUBJECT_ACCESS)).getAttributes("testId5"));
		assertNotNull(Iterables.getOnlyElement(context0.getAttributes(AttributeCategories.SUBJECT_ACCESS)).getAttributes("testId6"));
		assertNotNull(Iterables.getOnlyElement(context0.getAttributes(AttributeCategories.RESOURCE)).getAttributes("testId1"));
		assertNotNull(Iterables.getOnlyElement(context0.getAttributes(AttributeCategories.RESOURCE)).getAttributes("testId2"));

		assertEquals(2, context0.getAttributes().size());
		assertEquals(1, context0.getAttributes(AttributeCategories.SUBJECT_ACCESS).size());
		assertEquals(1, context0.getAttributes(AttributeCategories.RESOURCE).size());

		assertNotNull(Iterables.getOnlyElement(context1.getAttributes(AttributeCategories.SUBJECT_ACCESS)).getAttributes("testId7"));
		assertNotNull(Iterables.getOnlyElement(context1.getAttributes(AttributeCategories.SUBJECT_ACCESS)).getAttributes("testId8"));
		assertNotNull(Iterables.getOnlyElement(context1.getAttributes(AttributeCategories.RESOURCE)).getAttributes("testId3"));
		assertNotNull(Iterables.getOnlyElement(context1.getAttributes(AttributeCategories.RESOURCE)).getAttributes("testId4"));
		assertEquals(2, context1.getAttributes().size());
		assertEquals(1, context1.getAttributes(AttributeCategories.SUBJECT_ACCESS).size());
		assertEquals(1, context1.getAttributes(AttributeCategories.RESOURCE).size());
		verify(pdp);
	}

	@Test
	public void testWithNoReferences()
	{

		Attributes attr0 = Attributes
				.builder(AttributeCategories.RESOURCE)
				.id("resourceAttr0")
				.attribute(
						Attribute.builder("testId3").value(STRING.create("value0")).build(),
						Attribute.builder("testId4").value(STRING.create("value1")).build())
				.build();

		Attributes attr1 = Attributes
				.builder(AttributeCategories.SUBJECT_ACCESS)
				.id("subjectAttr0")
				.attribute(
						Attribute.builder("testId5").value(STRING.create("value0")).build(),
						Attribute.builder("testId6").value(STRING.create("value1")).build())
				.build();

		RequestContext request = new RequestContext(false,
				Arrays.asList(attr0, attr1));

		expect(pdp.requestDecision(request)).andReturn(
				Result.createIndeterminateProcessingError().build());
		replay(pdp);
		Collection<Result> results = profile.handle(request, pdp);
		assertEquals(Result.createIndeterminateProcessingError().build(), results.iterator().next());
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
