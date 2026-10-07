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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

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
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.content.XPathProvider;
import org.xacml4j.v30.content.XmlContent;
import org.xacml4j.v30.pdp.PolicyDecisionPointContext;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.spi.pdp.RequestContextHandler;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

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

	@BeforeEach
	public void init() throws Exception
	{
		this.pdp = mock(PolicyDecisionPointContext.class);
		this.profile = new MultipleResourcesViaXPathExpressionHandler();
		this.xpathProvider = XPathProvider.defaultProvider();
		this.content = XmlContent.of(XmlContent.fromString(testXml));
	}

	@Test
	public void testExpansionPreservesSelectorMetadataAndOtherAttributes()
	{
		Attribute first = Attribute.builder("shared-id").issuer("first")
				.value(XacmlTypes.STRING.ofAny("first-value")).build();
		Attribute second = Attribute.builder("shared-id").issuer("second")
				.value(XacmlTypes.STRING.ofAny("second-value")).build();
		Category resource = Category.builder(CategoryId.RESOURCE).id("resource-ref")
				.entity(Entity.builder().content(content).attribute(first, second,
						Attribute.builder(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR)
								.issuer("selector-issuer").includeInResult(true)
								.value(XacmlTypes.XPATH.ofAny("//md:record/md:patient", CategoryId.RESOURCE))
								.build()).build()).build();
		ArgumentCaptor<RequestContext> requests = ArgumentCaptor.forClass(RequestContext.class);
		when(pdp.requestDecision(Mockito.any())).thenReturn(Result.ok(Decision.PERMIT).build());

		assertEquals(2, profile.handle(RequestContext.builder().attributes(resource).build(), pdp).size());
		InOrder order = Mockito.inOrder(pdp);
		order.verify(pdp, times(2)).requestDecision(requests.capture());
		Mockito.verifyNoMoreInteractions(pdp);
		Set<Value<?>> paths = new java.util.HashSet<>();
		for(RequestContext expanded : requests.getAllValues()){
			Category category = expanded.getCategory().iterator().next();
			Entity entity = category.getEntity();
			assertEquals(resource.getCategoryId(), category.getCategoryId());
			assertEquals(resource.getReferenceId(), category.getReferenceId());
			assertSame(content, entity.getContent().get());
			assertEquals(3, entity.attributes().size());
			assertEquals(ImmutableSet.of(first, second), ImmutableSet.copyOf(entity.find("shared-id")));
			assertTrue(entity.find(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR).isEmpty());
			Attribute selector = Iterables.getOnlyElement(entity.find(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR));
			assertEquals("selector-issuer", selector.getIssuer());
			assertTrue(selector.isIncludeInResult());
			paths.addAll(selector.values());
		}
		assertEquals(ImmutableSet.of(
				XacmlTypes.XPATH.ofAny("//md:record/md:patient[1]", CategoryId.RESOURCE),
				XacmlTypes.XPATH.ofAny("//md:record/md:patient[2]", CategoryId.RESOURCE)), paths);
	}

	@Test
	public void testDraftMultipleContentSelectorIsAcceptedAndExpanded()
	{
		String draftId = "urn:oasis:names:tc:xacml:3.0:multiple:content-selector";
		Category resource = Category.builder(CategoryId.RESOURCE)
				.entity(Entity.builder().content(content)
						.attribute(Attribute.builder(draftId)
								.value(XacmlTypes.XPATH.ofAny("//md:record/md:patient", CategoryId.RESOURCE))
								.build())
						.build())
				.build();
		ArgumentCaptor<RequestContext> requests = ArgumentCaptor.forClass(RequestContext.class);
		when(pdp.requestDecision(Mockito.any())).thenReturn(Result.ok(Decision.PERMIT).build());

		assertEquals(2, profile.handle(RequestContext.builder().attributes(resource).build(), pdp).size());
		Mockito.verify(pdp, times(2)).requestDecision(requests.capture());
		assertEquals(XacmlTypes.XPATH.ofAny("//md:record/md:patient[1]", CategoryId.RESOURCE),
				Iterables.getOnlyElement(requests.getAllValues().get(0).getCategory(CategoryId.RESOURCE)
						.iterator().next().getEntity()
						.find(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR).iterator().next().values()));
		assertEquals(XacmlTypes.XPATH.ofAny("//md:record/md:patient[2]", CategoryId.RESOURCE),
				Iterables.getOnlyElement(requests.getAllValues().get(1).getCategory(CategoryId.RESOURCE)
						.iterator().next().getEntity()
						.find(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR).iterator().next().values()));
		for(RequestContext request : requests.getAllValues()){
			Entity expanded = request.getCategory(CategoryId.RESOURCE).iterator().next().getEntity();
			assertTrue(expanded.find(draftId).isEmpty());
			assertEquals(1, expanded.find(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR).size());
		}
		Mockito.verifyNoMoreInteractions(pdp);
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
								Attribute.builder("testId3").value(XacmlTypes.STRING.ofAny("value0")).build(),
								Attribute.builder("testId4").value(XacmlTypes.STRING.ofAny("value1")).build(),
								Attribute.builder(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR)
								.value(XacmlTypes.XPATH.ofAny("//md:record/md:patient", CategoryId.RESOURCE)).build())
						      .build())
				.build();

		Category subject = Category
				.builder(CategoryId.SUBJECT_ACCESS)
				.entity(
						Entity.builder()
						.attribute(
						Attribute.builder("testId7").value(XacmlTypes.STRING.ofAny("value2")).build(),
						Attribute.builder("testId8").value(XacmlTypes.STRING.ofAny("value3")).build())
						.build())
				.build();

		RequestContext context = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.attributes(subject, resource)
				.build();

		assertFalse(context.containsRepeatingCategories());
		ArgumentCaptor<RequestContext> c0 = ArgumentCaptor.forClass(RequestContext.class);
		ArgumentCaptor<RequestContext> c1 = ArgumentCaptor.forClass(RequestContext.class);


		when(pdp.requestDecision(Mockito.any())).thenReturn(
				Result.indeterminate(Status.processingError().build()).build());

		when(pdp.requestDecision(Mockito.any())).thenReturn(
				Result.indeterminate(Status.processingError().build()).build());

		Collection<Result> results = profile.handle(context, pdp);
		Iterator<Result> it = results.iterator();
		assertEquals(Status.processingError().build(), it.next().getStatus());
		assertEquals(Status.processingError().build(), it.next().getStatus());
		InOrder order = Mockito.inOrder(pdp);
		order.verify(pdp, times(2)).requestDecision(c0.capture());
		Mockito.verifyNoMoreInteractions(pdp);
		RequestContext r0 = c0.getAllValues().get(0);
		RequestContext r1 = c0.getAllValues().get(1);

		Attribute selector0 = r0.getAttribute(CategoryId.RESOURCE, MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR).get();
		Attribute selector1 = r1.getAttribute(CategoryId.RESOURCE, MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR).get();

		Set<Value> valueSet = ImmutableSet.<Value>builder()
		                                  .addAll(selector0.values())
		                                  .addAll(selector1.values())

		                                  .build();

		assertTrue(valueSet.contains(XacmlTypes.XPATH.ofAny("//md:record/md:patient[1]", CategoryId.RESOURCE)));
		assertTrue(valueSet.contains(XacmlTypes.XPATH.ofAny("//md:record/md:patient[2]", CategoryId.RESOURCE)));




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
						Attribute.builder("testId3").value(XacmlTypes.STRING.ofAny("value0")).build(),
						Attribute.builder("testId4").value(XacmlTypes.STRING.ofAny("value1")).build(),
						Attribute.builder(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR)
						.value(XacmlTypes.XPATH.ofAny("//md:record/md:patient", CategoryId.RESOURCE)).build()).build())
				.build();



		Category subject = Category
				.builder(CategoryId.SUBJECT_ACCESS)
				.entity(Entity
				.builder()
				.content(content)
				.attribute(
						Attribute.builder("testId7").value(XacmlTypes.STRING.ofAny("value0")).build(),
						Attribute.builder("testId8").value(XacmlTypes.STRING.ofAny("value1")).build(),
						Attribute.builder(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR)
								 .value(
										 XacmlTypes.XPATH.ofAny("//md:record/md:patient/md:patientDoB/@md:attrn1", CategoryId.SUBJECT_ACCESS)
								).build()).build()).build();

		RequestContext context = RequestContext
				.builder()
				.returnPolicyIdList(false)
				.attributes(subject, resource)
				.build();

		assertFalse(context.containsRepeatingCategories());
		ArgumentCaptor<RequestContext> c0 = ArgumentCaptor.forClass(RequestContext.class);
		ArgumentCaptor<RequestContext> c1 = ArgumentCaptor.forClass(RequestContext.class);
		ArgumentCaptor<RequestContext> c2 = ArgumentCaptor.forClass(RequestContext.class);
		ArgumentCaptor<RequestContext> c3 = ArgumentCaptor.forClass(RequestContext.class);


		when(pdp.requestDecision(Mockito.any())).thenReturn(
				Result.indeterminate(Status.processingError().build()).build());
		when(pdp.requestDecision(Mockito.any())).thenReturn(
				Result.indeterminate(Status.processingError().build()).build());
		when(pdp.requestDecision(Mockito.any())).thenReturn(
				Result.indeterminate(Status.processingError().build()).build());
		when(pdp.requestDecision(Mockito.any())).thenReturn(
				Result.indeterminate(Status.processingError().build()).build());

		Collection<Result> results = profile.handle(context, pdp);
		Iterator<Result> it = results.iterator();
		assertEquals(Status.processingError().build(), it.next().getStatus());
		assertEquals(Status.processingError().build(), it.next().getStatus());

		InOrder order = Mockito.inOrder(pdp);
		order.verify(pdp, times(4)).requestDecision(c0.capture());
		Mockito.verifyNoMoreInteractions(pdp);
		RequestContext r0 = c0.getAllValues().get(0);
		RequestContext r1 = c0.getAllValues().get(1);
		RequestContext r2 = c0.getAllValues().get(2);
		RequestContext r3 = c0.getAllValues().get(3);

		Attribute selector00 = r0.getAttribute(CategoryId.RESOURCE, MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR).get();
		Attribute selector01 = r0.getAttribute(CategoryId.SUBJECT_ACCESS, MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR).get();

		Attribute selector10 = r1.getAttribute(CategoryId.RESOURCE, MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR).get();
		Attribute selector11 = r1.getAttribute(CategoryId.SUBJECT_ACCESS, MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR).get();

		Attribute selector20 = r2.getAttribute(CategoryId.RESOURCE, MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR).get();
		Attribute selector21 = r2.getAttribute(CategoryId.SUBJECT_ACCESS, MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR).get();

		Attribute selector30 = r3.getAttribute(CategoryId.RESOURCE, MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR).get();
		Attribute selector31 = r3.getAttribute(CategoryId.SUBJECT_ACCESS, MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR).get();

		Set<Value> valueSet = ImmutableSet.<Value>builder()
		                                  .addAll(selector00.values())
		                                  .addAll(selector01.values())
		                                  .addAll(selector10.values())
		                                  .addAll(selector11.values())
		                                  .addAll(selector20.values())
		                                  .addAll(selector21.values())
		                                  .addAll(selector30.values())
		                                  .addAll(selector31.values())
		                                  .build();
		assertEquals((Object) XacmlTypes.XPATH.ofAny("//md:record/md:patient[2]", CategoryId.RESOURCE), XacmlTypes.XPATH.ofAny("//md:record/md:patient[2]", CategoryId.RESOURCE));
		assertTrue(valueSet.contains(XacmlTypes.XPATH.ofAny("//md:record/md:patient[1]", CategoryId.RESOURCE)));
		assertTrue(valueSet.contains(XacmlTypes.XPATH.ofAny("//md:record/md:patient[2]", CategoryId.RESOURCE)));
		assertTrue(valueSet.contains(XacmlTypes.XPATH.ofAny("//md:record/md:patient[2]/md:patientDoB[1]/@md:attrn1", CategoryId.SUBJECT_ACCESS)));
		assertTrue(valueSet.contains(XacmlTypes.XPATH.ofAny("//md:record/md:patient[1]/md:patientDoB[1]/@md:attrn1", CategoryId.SUBJECT_ACCESS)));




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
						Attribute.builder("testId3").value(XacmlTypes.STRING.ofAny("value0")).build(),
						Attribute.builder("testId4").value(XacmlTypes.STRING.ofAny("value1")).build(),
						Attribute.builder(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR)
						.value(XacmlTypes.STRING.ofAny("//md:record/md:patient")).build()).build())
				.build();

		Category subject = Category
				.builder(CategoryId.SUBJECT_ACCESS)
				.entity(Entity
						.builder()
						.content(content)
						.attribute(
						Attribute.builder("testId7").value(XacmlTypes.STRING.ofAny("value0")).build(),
						Attribute.builder("testId8").value(XacmlTypes.STRING.ofAny("value1")).build()).build()).build();


		RequestContext request = RequestContext.builder()
				.attributes(resource, subject)
				.build();

		assertFalse(request.containsRepeatingCategories());
		ArgumentCaptor<RequestContext> c0 = ArgumentCaptor.forClass(RequestContext.class);

		when(pdp.requestDecision(Mockito.any())).thenReturn(
				Result.indeterminate(Status.processingError().build()).build());

		profile.handle(request, pdp);

		Mockito.verify(pdp).requestDecision(c0.capture());
		Mockito.verifyNoMoreInteractions(pdp);
		assertEquals(request, c0.getAllValues().get(0));
	}

	@Test
	public void testMultipleDecisionCategoryDoesNotHaveContent()
	{

		Category resource = Category
				.builder(CategoryId.RESOURCE)
				.entity(Entity
						.builder()
						.attribute(
						Attribute.builder("testId3").value(XacmlTypes.STRING.ofAny("value0")).build(),
						Attribute.builder("testId4").value(XacmlTypes.STRING.ofAny("value1")).build(),
						Attribute.builder(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR)
						.value(XacmlTypes.XPATH.ofAny("//md:record/md:patient", CategoryId.RESOURCE)).build()).build())
				.build();

		Category subject = Category
				.builder(CategoryId.SUBJECT_ACCESS)
				.entity(Entity
						.builder()
						.attribute(
						Attribute.builder("testId7").value(XacmlTypes.STRING.ofAny("value0")).build(),
						Attribute.builder("testId8").value(XacmlTypes.STRING.ofAny("value1")).build()
				).build()).build();



		RequestContext request = RequestContext.builder()
				.attributes(resource, subject)
				.build();

		assertFalse(request.containsRepeatingCategories());

		Collection<Result> results = profile.handle(request, pdp);
		assertEquals(Decision.INDETERMINATE, Iterables.getOnlyElement(results).getDecision());
		Mockito.verifyNoInteractions(pdp);
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
		Mockito.verify(pdp).requestDecision(c0.capture());
		Mockito.verifyNoMoreInteractions(pdp);
		assertSame(context, c0.getAllValues().get(0));
	}
}
