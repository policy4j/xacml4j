package org.xacml4j.v30.pdp.profiles;

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
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryReference;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.RequestReference;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCode;
import org.xacml4j.v30.pdp.PolicyDecisionPointContext;
import org.xacml4j.v30.pdp.RequestSyntaxException;
import org.xacml4j.v30.spi.pdp.RequestContextHandler;
import org.xacml4j.v30.types.StringExp;

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
				.builder(Categories.RESOURCE)
				.id("resourceAttr0")
				.entity(
						Entity.builder()
						.attribute(
								Attribute.builder("testId1").value(StringExp.valueOf("value0")).build(),
								Attribute.builder("testId2").value(StringExp.valueOf("value1")).build())
						.build())
				.build();

		Category attr1 = Category
				.builder(Categories.RESOURCE)
				.id("resourceAttr1")
				.entity(
						Entity.builder()
						.attribute(
								Attribute.builder("testId3").value(StringExp.valueOf("value0")).build(),
								Attribute.builder("testId4").value(StringExp.valueOf("value1")).build())
						.build())
				.build();

		Category attr2 = Category
				.builder(Categories.ACTION)
				.id("actionAttr0")
				.entity(
						Entity.builder()
						.attribute(
								Attribute.builder("testId3").value(StringExp.valueOf("value0")).build(),
								Attribute.builder("testId4").value(StringExp.valueOf("value1")).build())
						.build())
				.build();

		Category attr3 = Category
				.builder(Categories.SUBJECT_ACCESS)
				.id("subjectAttr0")
				.entity(
						Entity.builder()
						.attribute(
						Attribute.builder("testId5").value(StringExp.valueOf("value0")).build(),
						Attribute.builder("testId6").value(StringExp.valueOf("value1")).build())
						.build())
				.build();

		Category attr4 = Category
				.builder(Categories.SUBJECT_ACCESS)
				.id("subjectAttr1")
				.entity(
						Entity.builder()
						.attribute(
						Attribute.builder("testId7").value(StringExp.valueOf("value0")).build(),
						Attribute.builder("testId8").value(StringExp.valueOf("value1")).build()).build())
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

		assertNotNull(context0.getOnlyEntity(Categories.SUBJECT_ACCESS).getAttributes("testId5"));
		assertNotNull(context0.getOnlyEntity(Categories.SUBJECT_ACCESS).getAttributes("testId6"));
		assertNotNull(context0.getOnlyEntity(Categories.RESOURCE).getAttributes("testId1"));
		assertNotNull(context0.getOnlyEntity(Categories.RESOURCE).getAttributes("testId2"));

		assertEquals(2, context0.getAttributes().size());
		assertEquals(1, context0.getAttributes(Categories.SUBJECT_ACCESS).size());
		assertEquals(1, context0.getAttributes(Categories.RESOURCE).size());
		
		assertNotNull(context1.getOnlyEntity(Categories.SUBJECT_ACCESS).getAttributes("testId7"));
		assertNotNull(context1.getOnlyEntity(Categories.SUBJECT_ACCESS).getAttributes("testId8"));
		assertNotNull(context1.getOnlyEntity(Categories.RESOURCE).getAttributes("testId3"));
		assertNotNull(context1.getOnlyEntity(Categories.RESOURCE).getAttributes("testId4"));
		assertEquals(2, context1.getAttributes().size());
		assertEquals(1, context1.getAttributes(Categories.SUBJECT_ACCESS).size());
		assertEquals(1, context1.getAttributes(Categories.RESOURCE).size());
		verify(pdp);
	}

	@Test
	public void testWithNoReferences()
	{

		Category attr0 = Category
				.builder(Categories.RESOURCE)
				.id("resourceAttr0")
				.entity(
						Entity.builder()
						.attribute(
						Attribute.builder("testId3").value(StringExp.valueOf("value0")).build(),
						Attribute.builder("testId4").value(StringExp.valueOf("value1")).build()).build())
				.build();

		Category attr1 = Category
				.builder(Categories.SUBJECT_ACCESS)
				.id("subjectAttr0")
				.entity(
						Entity.builder()
						.attribute(
						Attribute.builder("testId5").value(StringExp.valueOf("value0")).build(),
						Attribute.builder("testId6").value(StringExp.valueOf("value1")).build()).build())
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
				Collections.<Category>emptyList());

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
