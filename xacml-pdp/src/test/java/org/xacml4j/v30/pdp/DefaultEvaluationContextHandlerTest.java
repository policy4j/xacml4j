package org.xacml4j.v30.pdp;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.PathEvaluationException;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.content.XPathProvider;
import org.xacml4j.v30.content.XmlContent;
import org.xacml4j.v30.policy.EvaluationContextHandler;
import org.xacml4j.v30.policy.AttributeSelector;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.types.XacmlTypes;


public class DefaultEvaluationContextHandlerTest
{
	private String testXml = "<md:record xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
	"xmlns:md=\"urn:example:med:schemas:record\">" +
	"<md:patient>" +
	"<md:patientDoB>1992-03-21</md:patientDoB>" +
	"<md:patient-number>555555</md:patient-number>" +
	"</md:patient>" +
	"</md:record>";

	private EvaluationContext context;

	private Entity entity;
	private XmlContent content;

	private PolicyInformationPoint pip;
	private XPathProvider xpathProvider;
	private RequestContextCallback requestContextCallback;
	private EvaluationContextHandler handler;

	@BeforeEach
	public void init() throws Exception
	{

		this.context = Mockito.mock(EvaluationContext.class);
		this.requestContextCallback = Mockito.mock(RequestContextCallback.class);
		this.pip = Mockito.mock(PolicyInformationPoint.class);
		this.entity = Entity
				.builder()
				.content(XmlContent.of(XmlContent.fromString(testXml)))
				.build();
		this.xpathProvider = XPathProvider.defaultProvider();
		this.handler = new DefaultEvaluationContextHandler(requestContextCallback, pip);
		this.content = XmlContent.of(testXml);
	}


	@Test
	public void testRelativePathWithoutContextSelectorUsesDocumentNode()
	{
		// XACML 3.0 7.3.7: no ContextSelectorId means the Content document node.
		AttributeSelectorKey selector = AttributeSelectorKey.builder()
				.category(CategoryId.SUBJECT_RECIPIENT).xpath("md:patient-number/text()")
				.dataType(XacmlTypes.INTEGER).build();
		Entity selected = Entity.builder().content(content)
				.attribute(Attribute.builder("urn:oasis:names:tc:xacml:3.0:content-selector")
						.value(XacmlTypes.XPATH.ofAny("/md:record/md:patient", CategoryId.SUBJECT_RECIPIENT))
						.build()).build();
		when(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).thenReturn(Optional.of(selected));
		when(pip.resolve(context, selector)).thenReturn(Optional.empty());
		when(context.resolve(selector)).thenAnswer(invocation -> handler.resolve(context, selector));

		assertEquals(XacmlTypes.INTEGER.emptyBag(),
				AttributeSelector.builder().key(selector).mustBePresent(false).build().evaluate(context));
	}

	@Test
	public void testRelativePathWithExplicitContextSelectorUsesSelectedNode()
	{
		// XACML 3.0 7.3.7: an explicit selector sets the context to its single node.
		String selectorId = "urn:oasis:names:tc:xacml:3.0:content-selector";
		AttributeSelectorKey selector = AttributeSelectorKey.builder()
				.category(CategoryId.SUBJECT_RECIPIENT).contextSelectorId(selectorId)
				.xpath("md:patient-number/text()").dataType(XacmlTypes.INTEGER).build();
		Entity selected = Entity.builder().content(content)
				.attribute(Attribute.builder(selectorId)
						.value(XacmlTypes.XPATH.ofAny("/md:record/md:patient", CategoryId.SUBJECT_RECIPIENT))
						.build()).build();
		when(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).thenReturn(Optional.of(selected));

		assertEquals(Optional.of(XacmlTypes.INTEGER.ofAny(555555).toBag()), handler.resolve(context, selector));
	}

	@Test
	public void testZeroNodeContextSelectorIsIndeterminateWithSyntaxError()
	{
		// XACML 3.0 7.3.7 requires Indeterminate with syntax-error for a non-singleton selector result.
		String selectorId = "urn:oasis:names:tc:xacml:3.0:content-selector";
		AttributeSelectorKey selector = AttributeSelectorKey.builder()
				.category(CategoryId.SUBJECT_RECIPIENT).contextSelectorId(selectorId)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(XacmlTypes.INTEGER).build();
		Entity selected = Entity.builder().content(content)
				.attribute(Attribute.builder(selectorId)
						.value(XacmlTypes.XPATH.ofAny("/md:record/md:missing", CategoryId.SUBJECT_RECIPIENT))
						.build()).build();
		when(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).thenReturn(Optional.of(selected));

		EvaluationException exception = assertThrows(EvaluationException.class,
				() -> handler.resolve(context, selector));
		assertEquals(StatusCodeId.SYNTAX_ERROR,
				exception.getStatus().getStatusCode().getValue());
	}

	@Test
	public void testTwoNodeContextSelectorIsIndeterminateWithSyntaxError()
	{
		// XACML 3.0 7.3.7 requires Indeterminate with syntax-error for a non-singleton selector result.
		String selectorId = "urn:oasis:names:tc:xacml:3.0:content-selector";
		AttributeSelectorKey selector = AttributeSelectorKey.builder()
				.category(CategoryId.SUBJECT_RECIPIENT).contextSelectorId(selectorId)
				.xpath("md:patient-number/text()").dataType(XacmlTypes.INTEGER).build();
		String twoPatients = testXml.replace("</md:record>",
				"<md:patient><md:patient-number>666666</md:patient-number></md:patient></md:record>");
		Entity selected = Entity.builder().content(XmlContent.of(twoPatients))
				.attribute(Attribute.builder(selectorId)
						.value(XacmlTypes.XPATH.ofAny("/md:record/md:patient", CategoryId.SUBJECT_RECIPIENT))
						.build()).build();
		when(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).thenReturn(Optional.of(selected));

		EvaluationException exception = assertThrows(EvaluationException.class,
				() -> handler.resolve(context, selector));
		assertEquals(StatusCodeId.SYNTAX_ERROR,
				exception.getStatus().getStatusCode().getValue());
	}

	@Test
	public void testAttributeSelectorPreservesContextSelectorCardinalitySyntaxError()
	{
		for (boolean twoNodes : new boolean[]{false, true}) {
			for (boolean mustBePresent : new boolean[]{false, true}) {
				assertAttributeSelectorCardinalitySyntaxError(twoNodes, mustBePresent);
			}
		}
	}

	private void assertAttributeSelectorCardinalitySyntaxError(boolean twoNodes, boolean mustBePresent)
	{
		String selectorId = "urn:oasis:names:tc:xacml:3.0:content-selector";
		AttributeSelectorKey key = AttributeSelectorKey.builder()
				.category(CategoryId.SUBJECT_RECIPIENT).contextSelectorId(selectorId)
				.xpath(twoNodes ? "md:patient-number/text()" : "/md:record/md:missing/text()")
				.dataType(XacmlTypes.INTEGER).build();
		String selectedXml = twoNodes
				? testXml.replace("</md:record>",
						"<md:patient><md:patient-number>666666</md:patient-number></md:patient></md:record>")
				: testXml;
		String contextXPath = twoNodes ? "/md:record/md:patient" : "/md:record/md:missing";
		Entity selected = Entity.builder().content(XmlContent.of(selectedXml))
				.attribute(Attribute.builder(selectorId)
						.value(XacmlTypes.XPATH.ofAny(contextXPath, CategoryId.SUBJECT_RECIPIENT))
						.build()).build();
		when(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).thenReturn(Optional.of(selected));
		Mockito.doAnswer(invocation -> handler.resolve(context, key)).when(context).resolve(key);

		EvaluationException exception = assertThrows(EvaluationException.class,
				() -> AttributeSelector.builder().key(key).mustBePresent(mustBePresent).build().evaluate(context));
		assertEquals(StatusCodeId.SYNTAX_ERROR, exception.getStatus().getStatusCode().getValue(),
				"twoNodes=" + twoNodes + ", mustBePresent=" + mustBePresent);
		Mockito.verifyNoInteractions(pip);
	}

	@Test
	public void testPipSuppliedContextSelectorPreservesCardinalitySyntaxError()
	{
		String selectorId = "context-selector";
		AttributeSelectorKey selector = AttributeSelectorKey.builder()
				.category(CategoryId.SUBJECT_RECIPIENT).contextSelectorId(selectorId)
				.xpath("/md:record/md:patient-number/text()")
				.dataType(XacmlTypes.INTEGER).build();
		AttributeDesignatorKey entitySelector = AttributeDesignatorKey.builder()
				.category(CategoryId.SUBJECT_RECIPIENT).attributeId(selectorId)
				.dataType(XacmlTypes.ENTITY).build();
		Entity supplied = Entity.builder().content(content)
				.attribute(Attribute.builder(selectorId)
						.value(XacmlTypes.XPATH.ofAny("/md:record/md:missing", CategoryId.SUBJECT_RECIPIENT))
						.build()).build();
		when(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).thenReturn(Optional.empty());
		when(pip.resolve(context, selector)).thenReturn(Optional.empty());
		when(pip.resolve(context, entitySelector)).thenReturn(Optional.of(supplied.toBag()));
		Mockito.doAnswer(invocation -> handler.resolve(context, selector)).when(context).resolve(selector);

		EvaluationException exception = assertThrows(EvaluationException.class,
				() -> AttributeSelector.builder().key(selector).mustBePresent(false).build().evaluate(context));
		assertEquals(StatusCodeId.SYNTAX_ERROR, exception.getStatus().getStatusCode().getValue());
		Mockito.verify(pip).resolve(context, selector);
		Mockito.verify(pip).resolve(context, entitySelector);
		Mockito.verifyNoMoreInteractions(pip);
	}

	@Test
	public void testExplicitMissingContextSelectorIsRejected()
	{
		assertThrows(PathEvaluationException.class, () -> {
			AttributeSelectorKey selector = AttributeSelectorKey.builder()
					.category(CategoryId.SUBJECT_RECIPIENT).contextSelectorId("missing-selector")
					.xpath("/md:record/md:patient/md:patient-number/text()")
					.dataType(XacmlTypes.INTEGER).build();
			when(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).thenReturn(Optional.of(entity));

			handler.resolve(context, selector);
		});
	}

	@Test
	public void testDesignatorPreservesIssuerAndDuplicateValuesAndGetEntityReturnsEntity()
	{
		AttributeDesignatorKey designator = AttributeDesignatorKey.builder()
				.category(CategoryId.RESOURCE).attributeId("test-id").issuer("requested-issuer")
				.dataType(XacmlTypes.INTEGER).build();
		Entity supplied = Entity.builder().content(content)
				.attribute(Attribute.builder("test-id").issuer("requested-issuer")
						.value(XacmlTypes.INTEGER.ofAny(7), XacmlTypes.INTEGER.ofAny(7)).build(),
						Attribute.builder("test-id").issuer("other-issuer")
						.value(XacmlTypes.INTEGER.ofAny(9)).build()).build();
		when(requestContextCallback.getEntity(CategoryId.RESOURCE)).thenReturn(Optional.of(supplied));

		assertEquals(Optional.of(XacmlTypes.INTEGER.bagBuilder().value(7, 7).build()),
				handler.resolve(context, designator));
		assertEquals(Optional.of(supplied), handler.getEntity(CategoryId.RESOURCE));
	}

	@Test
	public void testSelectorFallsBackToEntityValueFromPipAndCachesResult()
	{
		String selectorId = "entity-selector";
		AttributeSelectorKey selector = AttributeSelectorKey.builder()
				.category(CategoryId.SUBJECT_RECIPIENT).contextSelectorId(selectorId)
				.xpath("md:patient-number/text()").dataType(XacmlTypes.INTEGER).build();
		AttributeDesignatorKey designator = AttributeDesignatorKey.builder()
				.category(CategoryId.SUBJECT_RECIPIENT).attributeId(selectorId)
				.dataType(XacmlTypes.ENTITY).build();
		Entity supplied = Entity.builder().content(content)
				.attribute(Attribute.builder(selectorId)
						.value(XacmlTypes.XPATH.ofAny("/md:record/md:patient", CategoryId.SUBJECT_RECIPIENT))
						.build()).build();
		when(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT))
				.thenReturn(Optional.empty());
		when(pip.resolve(context, selector)).thenReturn(Optional.empty());
		when(pip.resolve(context, designator)).thenReturn(Optional.of(supplied.toBag()));

		Optional<BagOfValues> expected = Optional.of(XacmlTypes.INTEGER.ofAny(555555).toBag());
		assertEquals(expected, handler.resolve(context, selector));
		assertEquals(expected, handler.resolve(context, selector));
		Mockito.verify(requestContextCallback, Mockito.times(2)).getEntity(CategoryId.SUBJECT_RECIPIENT);
		Mockito.verify(pip).resolve(context, selector);
		Mockito.verify(pip).resolve(context, designator);
		Mockito.verifyNoMoreInteractions(requestContextCallback, pip);
	}

	@Test
	public void testSelectorResolveContentIsInRequestXPathReturnsNonEmptySetResolutionScopeRequest()
		throws EvaluationException
	{
		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(CategoryId.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(XacmlTypes.INTEGER)
				.build();
		when(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).thenReturn(Optional.of(entity));
		Optional<BagOfValues> v = handler.resolve(context, ref);
		assertEquals(v.get(), XacmlTypes.INTEGER.ofAny(555555).toBag());
	}

	@Test
	public void testSelectorResolveContentIsNotInRequestXPathReturnsNonEmptyNodeSet()
	{
		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(CategoryId.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(XacmlTypes.INTEGER)
				.build();

		when(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).thenReturn(Optional.of(entity));

		Optional<BagOfValues> v = handler.resolve(context, ref);
		assertEquals(v.get(), XacmlTypes.INTEGER.bagBuilder()
		                                        .value(555555)
		                                        .build());
	}

	@Test
	public void testSelectorResolveContentIsNotInRequestPIPCallsHandlerToResolveTheSameAttribute()
		throws Exception
	{
		final AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(CategoryId.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(XacmlTypes.INTEGER)
				.build();

		when(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).thenReturn(Optional.empty());

		when(pip.resolve(context, ref)).thenReturn(Optional.of(content).flatMap(v->v.resolve(ref)));

		Optional<BagOfValues> v = handler.resolve(context, ref);
		// test cache
		v = handler.resolve(context, ref);
		assertEquals(XacmlTypes.INTEGER.bagBuilder().value(555555).build(), v.get());
		Mockito.verify(requestContextCallback, Mockito.times(1)).getEntity(CategoryId.SUBJECT_RECIPIENT);
		Mockito.verify(pip).resolve(context, ref);
		Mockito.verifyNoMoreInteractions(requestContextCallback, pip);
	}


	@Test
	public void testSelectorResolveContentIsInRequestXPathReturnsUnsupportedNodeType() throws EvaluationException
	{
		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(CategoryId.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient")
				.dataType(XacmlTypes.INTEGER)
				.build();
		when(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).thenReturn(Optional.of(entity));
		assertThrows(PathEvaluationException.class, () -> handler.resolve(context, ref));
		// The baseline status expectation was unreachable after the throw.
		Mockito.verify(requestContextCallback).getEntity(CategoryId.SUBJECT_RECIPIENT);
	}
	@Test
	public void testSelectorResolveContentIsInRequestXPathReturnsNonEmptySetAndNodeFailsConvertionToDate() throws EvaluationException
	{
		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(CategoryId.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(XacmlTypes.DATE)
				.build();
		when(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).thenReturn(Optional.of(entity));
		assertThrows(PathEvaluationException.class, () -> handler.resolve(context, ref));
		// The baseline status expectation was unreachable after the throw.
		Mockito.verify(requestContextCallback).getEntity(CategoryId.SUBJECT_RECIPIENT);
	}

	@Test
	public void testSelectorResolveContentIsInRequestXPathReturnsNonEmptySetAndCorrectNodeTypeAndNodeFailsConvertionToDate() throws EvaluationException
	{
		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(CategoryId.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(XacmlTypes.DATE)
				.build();
		when(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).thenReturn(Optional.of(entity));
		assertThrows(PathEvaluationException.class, () -> handler.resolve(context, ref));
		// The baseline status expectation was unreachable after the throw.
		Mockito.verify(requestContextCallback).getEntity(CategoryId.SUBJECT_RECIPIENT);
	}

	@Test
	public void testSelectorResolveContentInRequestExistXPathReturnsEmptySet() throws EvaluationException
	{
		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(CategoryId.SUBJECT_RECIPIENT)
				.xpath("/test")
				.dataType(XacmlTypes.INTEGER)
				.build();
		when(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).thenReturn(Optional.of(entity));
		when(pip.resolve(context, ref)).thenReturn(Optional.empty());
		Optional<BagOfValues> v = handler.resolve(context, ref);
		assertFalse(v.isPresent());
	}

	@Test
	public void testSelectorResolveContentIsNotInRequestPIPThrowsRuntimeException()
		throws Exception
	{

		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(CategoryId.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(XacmlTypes.INTEGER)
				.build();

		when(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).thenReturn(Optional.empty());

		when(pip.resolve(context, ref)).thenThrow(new RuntimeException());
		Optional<BagOfValues> v = handler.resolve(context, ref);
		assertFalse(v.isPresent());
	}
	
	@Test
	public void testDesignatorResolveAttributeIsNotInRequest()
		throws Exception
	{
		AttributeDesignatorKey ref = AttributeDesignatorKey
				.builder()
				.category(CategoryId.RESOURCE)
				.attributeId("testId")
				.dataType(XacmlTypes.ANYURI)
				.build();

		when(requestContextCallback.getEntity(CategoryId.RESOURCE)).thenReturn(Optional.of(entity));
		when(pip.resolve(context, ref)).thenReturn(Optional.of(XacmlTypes.ANYURI.ofAny("testValue").toBag()));
		Optional<BagOfValues> v = handler.resolve(context, ref);
		assertEquals(XacmlTypes.ANYURI.ofAny("testValue").toBag(), v.get());
	}


	@Test
	public void testDesignatorResolveAttributeIsNotInRequestPIPThrowsRuntimeException()
		throws Exception
	{
		AttributeDesignatorKey ref = AttributeDesignatorKey
				.builder()
				.category(CategoryId.RESOURCE)
				.attributeId("testId")
				.dataType(XacmlTypes.ANYURI)
				.build();

		when(requestContextCallback.getEntity(CategoryId.RESOURCE)).thenReturn(Optional.of(entity));
		when(pip.resolve(context, ref)).thenThrow(new RuntimeException());
		Optional<BagOfValues> v = handler.resolve(context, ref);
		assertFalse(v.isPresent());
	}

	@Test
	public void testDesignatorResolveAttributeIsNotInRequestPIPCallsHandlerToResolveTheSameAttribute()
		throws Exception
	{
		final AttributeDesignatorKey ref = AttributeDesignatorKey
				.builder()
				.category(CategoryId.RESOURCE)
				.attributeId("testId")
				.dataType(XacmlTypes.ANYURI)
				.build();
		when(requestContextCallback.getEntity(CategoryId.RESOURCE)).thenReturn(Optional.of(entity));
		when(pip.resolve(context, ref)).thenAnswer(invocation -> handler.resolve(context, ref));
		handler.resolve(context, ref);
		Mockito.verify(pip, Mockito.times(1)).resolve(context, ref);
	}
}
