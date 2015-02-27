package org.xacml4j.v30.pdp.profiles;

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

import com.google.common.collect.Iterables;
import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xacml4j.v30.*;
import org.xacml4j.v30.pdp.PolicyDecisionPointContext;
import org.xacml4j.v30.spi.pdp.RequestContextHandler;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.XPathExp;
import org.xacml4j.v30.xpath.DefaultXPathProvider;
import org.xacml4j.v30.xpath.XPathProvider;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class MultipleResourcesViaXPathExpressionHandlerTest
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
	private Node content;

	private XPathProvider xpathProvider;

	@Before
	public void init() throws Exception
	{
		this.pdp = createStrictMock(PolicyDecisionPointContext.class);
		this.profile = new MultipleResourcesViaXPathExpressionHandler();
		this.xpathProvider = new DefaultXPathProvider();
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setNamespaceAware(true);
		DocumentBuilder builder = f.newDocumentBuilder();
		this.content = builder.parse(new InputSource(new StringReader(testXml)));
	}

	@Test
	public void testMultipleDecisionProfileWithSelectorInSingleCategory()
	{

		Category resource = Category
				.Resource()
				.entity(
                        Entity.builder()
                                .content(content)
                                .attribute(
                                        Attribute.builder("testId3").value(StringExp.of("value0")).build(),
                                        Attribute.builder("testId4").value(StringExp.of("value1")).build(),
                                        Attribute.builder(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR)
                                                .value(XPathExp.of("//md:record/md:patient", Categories.RESOURCE)).build())
                                .build())
				.build();

		Category subject = Category
				.SubjectAccess()
				.entity(
                        Entity.builder()
                                .attribute(
                                        Attribute.builder("testId7").value(StringExp.of("value0")).build(),
                                        Attribute.builder("testId8").value(StringExp.of("value1")).build())
                                .build())
				.build();

		RequestContext context = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.category(subject, resource)
				.build();

		assertFalse(context.containsRepeatingCategories());
		Capture<RequestContext> c0 = new Capture<RequestContext>();
		Capture<RequestContext> c1 = new Capture<RequestContext>();

		expect(pdp.getXPathProvider()).andReturn(xpathProvider);

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

		Attribute selector0 = r0.getOnlyEntity(Categories.RESOURCE).getOnlyAttribute(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR);
        assertNotNull(selector0);

        Attribute selector1 = r1.getOnlyEntity(Categories.RESOURCE).getOnlyAttribute(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR);
        assertNotNull(selector1);


		assertEquals(XPathExp.of("//md:record/md:patient[1]", Categories.RESOURCE),  Iterables.getOnlyElement(selector0.getValues()));
		assertEquals(XPathExp.of("//md:record/md:patient[2]", Categories.RESOURCE),  Iterables.getOnlyElement(selector1.getValues()));

		verify(pdp);
	}

	@Test
	public void testMultipleDecisionProfileWithSelectorInMultipleCategories()
	{

		Category resource = Category
				.builder(Categories.RESOURCE)
				.entity(Entity
						.builder()
						.content(content)
						.attribute(
						Attribute.builder("testId3").value(StringExp.of("value0")).build(),
						Attribute.builder("testId4").value(StringExp.of("value1")).build(),
						Attribute.builder(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR)
						.value(XPathExp.of("//md:record/md:patient", Categories.RESOURCE)).build()).build())
				.build();



		Category subject = Category
				.builder(Categories.SUBJECT_ACCESS)
				.entity(Entity
				.builder()
				.content(content)
				.attribute(
						Attribute.builder("testId7").value(StringExp.of("value0")).build(),
						Attribute.builder("testId8").value(StringExp.of("value1")).build(),
						Attribute.builder(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR)
								 .value(
										 XPathExp.of("//md:record/md:patient/md:patientDoB/@md:attrn1", Categories.SUBJECT_ACCESS)
								).build()).build()).build();

		RequestContext context = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.category(subject, resource)
				.build();

		assertFalse(context.containsRepeatingCategories());
		Capture<RequestContext> c0 = new Capture<RequestContext>();
		Capture<RequestContext> c1 = new Capture<RequestContext>();
		Capture<RequestContext> c2 = new Capture<RequestContext>();
		Capture<RequestContext> c3 = new Capture<RequestContext>();

		expect(pdp.getXPathProvider()).andReturn(xpathProvider);

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

		Attribute selector00 = r0.getOnlyEntity(Categories.RESOURCE).getOnlyAttribute(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR);
		Attribute selector01 = r0.getOnlyEntity(Categories.SUBJECT_ACCESS).getOnlyAttribute(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR);

		assertEquals(XPathExp.of("//md:record/md:patient[1]", Categories.RESOURCE),  Iterables.getOnlyElement(selector00.getValues()));
		assertEquals(XPathExp.of("//md:record/md:patient[1]/md:patientDoB[1]/@md:attrn1",
                Categories.SUBJECT_ACCESS),  Iterables.getOnlyElement(selector01.getValues()));

		Attribute selector10 = r1.getOnlyEntity(Categories.RESOURCE).getOnlyAttribute(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR);
		Attribute selector11 = r1.getOnlyEntity(Categories.SUBJECT_ACCESS).getOnlyAttribute(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR);

		assertEquals(XPathExp.of("//md:record/md:patient[2]", Categories.RESOURCE),
                Iterables.getOnlyElement(selector10.getValues()));
		assertEquals(XPathExp.of("//md:record/md:patient[1]/md:patientDoB[1]/@md:attrn1",
                Categories.SUBJECT_ACCESS),  Iterables.getOnlyElement(selector11.getValues()));

		Attribute selector20 = r2.getOnlyEntity(Categories.RESOURCE).getOnlyAttribute(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR);
		Attribute selector21 = r2.getOnlyEntity(Categories.SUBJECT_ACCESS).getOnlyAttribute(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR);

		assertEquals(XPathExp.of("//md:record/md:patient[2]", Categories.RESOURCE),  Iterables.getOnlyElement(selector20.getValues()));
		assertEquals(XPathExp.of("//md:record/md:patient[2]/md:patientDoB[1]/@md:attrn1", Categories.SUBJECT_ACCESS),  Iterables.getOnlyElement(selector21.getValues()));


		Attribute selector30 = r3.getOnlyEntity(Categories.RESOURCE).getOnlyAttribute(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR);
		Attribute selector31 = r3.getOnlyEntity(Categories.SUBJECT_ACCESS).getOnlyAttribute(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR);

		assertEquals(XPathExp.of("//md:record/md:patient[2]", Categories.RESOURCE),  Iterables.getOnlyElement(selector30.getValues()));
		assertEquals(XPathExp.of("//md:record/md:patient[2]/md:patientDoB[1]/@md:attrn1", Categories.SUBJECT_ACCESS),  Iterables.getOnlyElement(selector31.getValues()));

		verify(pdp);
	}

	@Test
	public void testMultipleDecisionRequestHasMultipleContentSelectorOfInvalidType()
	{

		Category resource = Category
				.builder(Categories.RESOURCE)
				.entity(Entity
						.builder()
						.content(content)
						.attribute(
						Attribute.builder("testId3").value(StringExp.of("value0")).build(),
						Attribute.builder("testId4").value(StringExp.of("value1")).build(),
						Attribute.builder(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR)
						.value(StringExp.of("//md:record/md:patient")).build()).build())
				.build();

		Category subject = Category
				.builder(Categories.SUBJECT_ACCESS)
				.entity(Entity
						.builder()
						.content(content)
						.attribute(
						Attribute.builder("testId7").value(StringExp.of("value0")).build(),
						Attribute.builder("testId8").value(StringExp.of("value1")).build()).build()).build();


		RequestContext request = RequestContext.builder()
				.category(resource, subject)
				.build();

		assertFalse(request.containsRepeatingCategories());
		Capture<RequestContext> c0 = new Capture<RequestContext>();

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
				.builder(Categories.RESOURCE)
				.entity(Entity
						.builder()
						.attribute(
						Attribute.builder("testId3").value(StringExp.of("value0")).build(),
						Attribute.builder("testId4").value(StringExp.of("value1")).build(),
						Attribute.builder(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR)
						.value(XPathExp.of("//md:record/md:patient", Categories.RESOURCE)).build()).build())
				.build();

		Category subject = Category
				.builder(Categories.SUBJECT_ACCESS)
				.entity(Entity
						.builder()
						.attribute(
						Attribute.builder("testId7").value(StringExp.of("value0")).build(),
						Attribute.builder("testId8").value(StringExp.of("value1")).build()
				).build()).build();


		expect(pdp.getXPathProvider()).andReturn(xpathProvider);

		RequestContext request = RequestContext.builder()
				.category(resource, subject)
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

		Capture<RequestContext> c0 = new Capture<RequestContext>();

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

