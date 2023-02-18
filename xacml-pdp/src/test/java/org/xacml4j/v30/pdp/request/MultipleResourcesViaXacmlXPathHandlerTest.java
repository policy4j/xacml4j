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

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.*;
import org.xacml4j.v30.content.XPathProvider;
import org.xacml4j.v30.content.XmlContent;
import org.xacml4j.v30.pdp.PolicyDecisionPointContext;
import org.xacml4j.v30.request.RequestContext;
import org.xacml4j.v30.spi.pdp.RequestContextHandler;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.XacmlTypes;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import javax.annotation.concurrent.Immutable;

public class MultipleResourcesViaXacmlXPathHandlerTest
{

	private String testXml = "<md:record xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
	"xmlns:md=\"urn:example:med:schemas:record\">" +
	"<md:patient>" +
	"<md:patientDoB md:attrn1=\"test\" attrn2=\"v\" >1992-03-21</md:patientDoB>" +
	"<md:patient-number>555555</md:patient-number>" +
	"</md:patient>" +
	"<md:patient>" +
	"<md:patientDoB md:attrn1=\"test1\" attrn2=\"v1\">1991-01-11</md:patientDoB>" +
	"<md:patient-number>11111</md:patient-number>" +
	"</md:patient>" +
	"</md:record>";

	private PolicyDecisionPointContext pdp;
	private RequestContextHandler profile;
	private XmlContent content;

	private XPathProvider xpathProvider;

	@Before
	public void init() throws Exception
	{
		this.pdp = createStrictMock(PolicyDecisionPointContext.class);
		this.profile = new MultipleResourcesViaXPathExpressionHandler();
		this.xpathProvider = XPathProvider.defaultProvider();
		this.content = XmlContent.of(XmlContent.fromString(testXml));
	}

	@Test
	public void testMultipleDecisionProfileWithSelectorInSingleCategory()
	{

		Category resource = Category
				.builder(CategoryId.RESOURCE)
				.entity(
						Entity.builder()
						      .content(content)
						      .attribute(
								Attribute.builder("testId3").value(XacmlTypes.STRING.of("value0")).build(),
								Attribute.builder("testId4").value(XacmlTypes.STRING.of("value1")).build(),
								Attribute.builder(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR)
								.value(XacmlTypes.XPATH.of("//md:record/md:patient", CategoryId.RESOURCE)).build())
						      .build())
				.build();

		Category subject = Category
				.builder(CategoryId.SUBJECT_ACCESS)
				.entity(
						Entity.builder()
						.attribute(
						Attribute.builder("testId7").value(XacmlTypes.STRING.of("value2")).build(),
						Attribute.builder("testId8").value(XacmlTypes.STRING.of("value3")).build())
						.build())
				.build();

		RequestContext context = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.attributes(subject, resource)
				.build();

		assertFalse(context.containsRepeatingCategories());
		Capture<RequestContext> c0 = Capture.newInstance();
		Capture<RequestContext> c1 = Capture.newInstance();


		expect(pdp.requestDecision(capture(c0))).andReturn(
				Result.indeterminate(Status.processingError().build()).build());

		expect(pdp.requestDecision(capture(c1))).andReturn(
				Result.indeterminate(Status.processingError().build()).build());

		replay(pdp);

		Collection<Result> results = profile.handle(context, pdp);
		Iterator<Result> it = results.iterator();
		assertEquals(Status.processingError().build(), it.next().getStatus());
		assertEquals(Status.processingError().build(), it.next().getStatus());
		RequestContext r0 = c0.getValue();
		RequestContext r1 = c1.getValue();

		Attribute selector0 = r0.getEntity(CategoryId.RESOURCE).get().
				getOnlyAttribute(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR).get();
		Attribute selector1 = r1.getAttribute(CategoryId.RESOURCE, MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR).get();

		Set<Value> valueSet = ImmutableSet.<Value>builder()
		                                  .addAll(selector0.getValues())
		                                  .addAll(selector1.getValues())

		                                  .build();

		assertTrue(valueSet.contains(XacmlTypes.XPATH.of("//md:record/md:patient[1]", CategoryId.RESOURCE)));
		assertTrue(valueSet.contains(XacmlTypes.XPATH.of("//md:record/md:patient[2]", CategoryId.RESOURCE)));


		verify(pdp);
	}

	@Test
	public void testMultipleDecisionProfileWithSelectorInMultipleCategories()
	{

		Category resource = Category
				.builder(CategoryId.RESOURCE)
				.entity(Entity
						.builder()
						.content(content)
						.attribute(
						Attribute.builder("testId3").value(XacmlTypes.STRING.of("value0")).build(),
						Attribute.builder("testId4").value(XacmlTypes.STRING.of("value1")).build(),
						Attribute.builder(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR)
						.value(XacmlTypes.XPATH.of("//md:record/md:patient", CategoryId.RESOURCE)).build()).build())
				.build();



		Category subject = Category
				.builder(CategoryId.SUBJECT_ACCESS)
				.entity(Entity
				.builder()
				.content(content)
				.attribute(
						Attribute.builder("testId7").value(XacmlTypes.STRING.of("value0")).build(),
						Attribute.builder("testId8").value(XacmlTypes.STRING.of("value1")).build(),
						Attribute.builder(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR)
								 .value(
										 XacmlTypes.XPATH.of("//md:record/md:patient/md:patientDoB/@md:attrn1", CategoryId.SUBJECT_ACCESS)
								).build()).build()).build();

		RequestContext context = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.attributes(subject, resource)
				.build();

		assertFalse(context.containsRepeatingCategories());
		Capture<RequestContext> c0 = Capture.newInstance();
		Capture<RequestContext> c1 = Capture.newInstance();
		Capture<RequestContext> c2 = Capture.newInstance();
		Capture<RequestContext> c3 = Capture.newInstance();


		expect(pdp.requestDecision(capture(c0))).andReturn(
				Result.indeterminate(Status.processingError().build()).build());
		expect(pdp.requestDecision(capture(c1))).andReturn(
				Result.indeterminate(Status.processingError().build()).build());
		expect(pdp.requestDecision(capture(c2))).andReturn(
				Result.indeterminate(Status.processingError().build()).build());
		expect(pdp.requestDecision(capture(c3))).andReturn(
				Result.indeterminate(Status.processingError().build()).build());

		replay(pdp);

		Collection<Result> results = profile.handle(context, pdp);
		Iterator<Result> it = results.iterator();
		assertEquals(Status.processingError().build(), it.next().getStatus());
		assertEquals(Status.processingError().build(), it.next().getStatus());

		RequestContext r0 = c0.getValue();
		RequestContext r1 = c1.getValue();
		RequestContext r2 = c2.getValue();
		RequestContext r3 = c3.getValue();

		Attribute selector00 = r0.getAttribute(CategoryId.RESOURCE, MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR).get();
		Attribute selector01 = r0.getAttribute(CategoryId.SUBJECT_ACCESS, MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR).get();

		Attribute selector10 = r1.getAttribute(CategoryId.RESOURCE, MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR).get();
		Attribute selector11 = r1.getAttribute(CategoryId.SUBJECT_ACCESS, MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR).get();

		Attribute selector20 = r2.getAttribute(CategoryId.RESOURCE, MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR).get();
		Attribute selector21 = r2.getAttribute(CategoryId.SUBJECT_ACCESS, MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR).get();

		Attribute selector30 = r3.getAttribute(CategoryId.RESOURCE, MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR).get();
		Attribute selector31 = r3.getAttribute(CategoryId.SUBJECT_ACCESS, MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR).get();

		Set<Value> valueSet = ImmutableSet.<Value>builder()
		                                  .addAll(selector00.getValues())
		                                  .addAll(selector01.getValues())
		                                  .addAll(selector10.getValues())
		                                  .addAll(selector11.getValues())
		                                  .addAll(selector20.getValues())
		                                  .addAll(selector21.getValues())
		                                  .addAll(selector30.getValues())
		                                  .addAll(selector31.getValues())
		                                  .build();
		assertEquals(XacmlTypes.XPATH.of("//md:record/md:patient[2]", CategoryId.RESOURCE), XacmlTypes.XPATH.of("//md:record/md:patient[2]", CategoryId.RESOURCE));
		assertTrue(valueSet.contains(XacmlTypes.XPATH.of("//md:record/md:patient[1]", CategoryId.RESOURCE)));
		assertTrue(valueSet.contains(XacmlTypes.XPATH.of("//md:record/md:patient[2]", CategoryId.RESOURCE)));
		assertTrue(valueSet.contains(XacmlTypes.XPATH.of("//md:record/md:patient[2]/md:patientDoB[1]/@md:attrn1", CategoryId.SUBJECT_ACCESS)));
		assertTrue(valueSet.contains(XacmlTypes.XPATH.of("//md:record/md:patient[1]/md:patientDoB[1]/@md:attrn1", CategoryId.SUBJECT_ACCESS)));

		verify(pdp);
	}

	@Test
	public void testMultipleDecisionRequestHasMultipleContentSelectorOfInvalidType()
	{

		Category resource = Category
				.builder(CategoryId.RESOURCE)
				.entity(Entity
						.builder()
						.content(content)
						.attribute(
						Attribute.builder("testId3").value(XacmlTypes.STRING.of("value0")).build(),
						Attribute.builder("testId4").value(XacmlTypes.STRING.of("value1")).build(),
						Attribute.builder(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR)
						.value(XacmlTypes.STRING.of("//md:record/md:patient")).build()).build())
				.build();

		Category subject = Category
				.builder(CategoryId.SUBJECT_ACCESS)
				.entity(Entity
						.builder()
						.content(content)
						.attribute(
						Attribute.builder("testId7").value(XacmlTypes.STRING.of("value0")).build(),
						Attribute.builder("testId8").value(XacmlTypes.STRING.of("value1")).build()).build()).build();


		RequestContext request = RequestContext.builder()
				.attributes(resource, subject)
				.build();

		assertFalse(request.containsRepeatingCategories());
		Capture<RequestContext> c0 = Capture.newInstance();

		expect(pdp.requestDecision(capture(c0))).andReturn(
				Result.indeterminate(Status.processingError().build()).build());

		replay(pdp);

		profile.handle(request, pdp);

		assertEquals(request, c0.getValue());
		verify(pdp);
	}

	@Test
	public void testMultipleDecisionCategoryDoesNotHaveContent()
	{

		Category resource = Category
				.builder(CategoryId.RESOURCE)
				.entity(Entity
						.builder()
						.attribute(
						Attribute.builder("testId3").value(XacmlTypes.STRING.of("value0")).build(),
						Attribute.builder("testId4").value(XacmlTypes.STRING.of("value1")).build(),
						Attribute.builder(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR)
						.value(XacmlTypes.XPATH.of("//md:record/md:patient", CategoryId.RESOURCE)).build()).build())
				.build();

		Category subject = Category
				.builder(CategoryId.SUBJECT_ACCESS)
				.entity(Entity
						.builder()
						.attribute(
						Attribute.builder("testId7").value(XacmlTypes.STRING.of("value0")).build(),
						Attribute.builder("testId8").value(XacmlTypes.STRING.of("value1")).build()
				).build()).build();



		RequestContext request = RequestContext.builder()
				.attributes(resource, subject)
				.build();

		assertFalse(request.containsRepeatingCategories());

		replay(pdp);

		Collection<Result> results = profile.handle(request, pdp);
		assertEquals(Decision.INDETERMINATE, Iterables.getOnlyElement(results).getDecision());

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

