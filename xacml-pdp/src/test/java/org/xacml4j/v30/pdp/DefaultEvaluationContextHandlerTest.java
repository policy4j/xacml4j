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
import org.xacml4j.v30.AttributeReferenceEvaluationException;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.PathEvaluationException;
import org.xacml4j.v30.Status;
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
		// The handler reports the engine's syntax error with the fixed response message.
		assertEquals(Optional.of(AttributeReferenceEvaluationException.INVALID_REFERENCE_MESSAGE),
				exception.getStatus().getMessage());
	}

	@Test
	public void testSelectorPathEvaluationErrorOnRequestContentIsProcessingErrorWithFixedMessage()
	{
		// Run 061 D18: an XPath evaluation error is a processing error. The handler maps it, so a
		// caller that does not go through AttributeSelector (the attribute-selector function) gets
		// the same status and no XPath engine text.
		AttributeSelectorKey malformed = AttributeSelectorKey.builder()
				.category(CategoryId.SUBJECT_RECIPIENT).xpath("///").dataType(XacmlTypes.INTEGER).build();
		Entity withContent = Entity.builder().content(content).build();
		when(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).thenReturn(Optional.of(withContent));

		AttributeReferenceEvaluationException e = assertThrows(
				AttributeReferenceEvaluationException.class, () -> handler.resolve(context, malformed));

		assertEquals(true, e.isResolverFailure());
		assertEquals(true, e.getCause() instanceof PathEvaluationException);
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
		assertEquals(Optional.of(AttributeReferenceEvaluationException.RESOLUTION_FAILED_MESSAGE),
				e.getStatus().getMessage());
		Mockito.verifyNoInteractions(pip);
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
		AttributeSelectorKey selector = AttributeSelectorKey.builder()
				.category(CategoryId.SUBJECT_RECIPIENT).contextSelectorId("missing-selector")
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(XacmlTypes.INTEGER).build();
		when(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).thenReturn(Optional.of(entity));

		assertPathErrorIsProcessingErrorWithFixedMessage(selector);
	}

	/**
	 * The handler reports a path error of the engine as processing-error with
	 * the fixed response message and keeps the path exception as the cause.
	 */
	private void assertPathErrorIsProcessingErrorWithFixedMessage(AttributeSelectorKey ref)
	{
		AttributeReferenceEvaluationException e = assertThrows(
				AttributeReferenceEvaluationException.class, () -> handler.resolve(context, ref));

		assertEquals(true, e.getCause() instanceof PathEvaluationException);
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
		assertEquals(Optional.of(AttributeReferenceEvaluationException.RESOLUTION_FAILED_MESSAGE),
				e.getStatus().getMessage());
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
		assertPathErrorIsProcessingErrorWithFixedMessage(ref);
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
		assertPathErrorIsProcessingErrorWithFixedMessage(ref);
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
		assertPathErrorIsProcessingErrorWithFixedMessage(ref);
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
		// A PIP failure is a processing error, not a missing value (ruling 2026-10-07, run 061 D3).
		EvaluationException e = assertThrows(EvaluationException.class, () -> handler.resolve(context, ref));
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
		Mockito.verify(pip, Mockito.times(1)).resolve(context, ref);
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
		// A PIP failure is a processing error, not a missing value (ruling 2026-10-07, run 061 D3).
		EvaluationException e = assertThrows(EvaluationException.class, () -> handler.resolve(context, ref));
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
		Mockito.verify(pip, Mockito.times(1)).resolve(context, ref);
	}

	@Test
	public void testDesignatorPipFailureIsNotCachedAndDoesNotLookLikeACycle()
	{
		AttributeDesignatorKey ref = AttributeDesignatorKey
				.builder()
				.category(CategoryId.RESOURCE)
				.attributeId("testId")
				.dataType(XacmlTypes.ANYURI)
				.build();
		BagOfValues value = XacmlTypes.ANYURI.ofAny("testValue").toBag();
		when(requestContextCallback.getEntity(CategoryId.RESOURCE)).thenReturn(Optional.of(entity));
		when(pip.resolve(context, ref))
				.thenThrow(new RuntimeException("first call fails"))
				.thenReturn(Optional.of(value));

		assertThrows(EvaluationException.class, () -> handler.resolve(context, ref));
		// The resolution stack is clean after the failure, and the failure is not remembered.
		assertEquals(Optional.of(value), handler.resolve(context, ref));
		Mockito.verify(pip, Mockito.times(2)).resolve(context, ref);
	}

	@Test
	public void testDesignatorPipStatusIsNormalizedToProcessingError()
	{
		AttributeDesignatorKey ref = AttributeDesignatorKey
				.builder()
				.category(CategoryId.RESOURCE)
				.attributeId("testId")
				.dataType(XacmlTypes.ANYURI)
				.build();
		when(requestContextCallback.getEntity(CategoryId.RESOURCE)).thenReturn(Optional.of(entity));
		when(pip.resolve(context, ref)).thenThrow(new EvaluationException(
				Status.syntaxError().message("backend detail").build(), "backend detail"));

		EvaluationException e = assertThrows(EvaluationException.class, () -> handler.resolve(context, ref));

		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
		assertEquals(Optional.of(AttributeReferenceEvaluationException.RESOLUTION_FAILED_MESSAGE),
				e.getStatus().getMessage());
		Mockito.verify(pip, Mockito.times(1)).resolve(context, ref);
	}

	@Test
	public void testDesignatorPipProcessingErrorMessageIsNotPassedOn()
	{
		AttributeDesignatorKey ref = AttributeDesignatorKey
				.builder()
				.category(CategoryId.RESOURCE)
				.attributeId("testId")
				.dataType(XacmlTypes.ANYURI)
				.build();
		when(requestContextCallback.getEntity(CategoryId.RESOURCE)).thenReturn(Optional.of(entity));
		// The same exception class and status code as a mapped failure, but with the resolver's text.
		when(pip.resolve(context, ref)).thenThrow(new AttributeReferenceEvaluationException(
				Status.processingError().build(), ref, "backend detail"));

		AttributeReferenceEvaluationException e = assertThrows(
				AttributeReferenceEvaluationException.class, () -> handler.resolve(context, ref));

		assertEquals(true, e.isResolverFailure());
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
		assertEquals(Optional.of(AttributeReferenceEvaluationException.RESOLUTION_FAILED_MESSAGE),
				e.getStatus().getMessage());
		Mockito.verify(pip, Mockito.times(1)).resolve(context, ref);
	}

	@Test
	public void testSelectorPipPathExceptionWithMissingAttributeStatusIsAFailure()
	{
		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(CategoryId.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(XacmlTypes.INTEGER)
				.build();
		when(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).thenReturn(Optional.empty());
		when(pip.resolve(context, ref)).thenThrow(PathEvaluationException.invalidXpath(
				ref.getPath(), Status.missingAttribute(ref).build(), null));

		// MustBePresent=false must not be able to turn this into an empty bag.
		AttributeReferenceEvaluationException e = assertThrows(
				AttributeReferenceEvaluationException.class, () -> handler.resolve(context, ref));

		assertEquals(true, e.isResolverFailure());
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
		Mockito.verify(pip, Mockito.times(1)).resolve(context, ref);
	}

	@Test
	public void testSelectorPipCannotChooseSyntaxErrorOrTheResponseMessage()
	{
		// rv079 and rv080 round 3: a path exception thrown by the information point is not the
		// engine's own syntax error. Only the engine's evaluation of a path reports syntax-error.
		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(CategoryId.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(XacmlTypes.INTEGER)
				.build();
		when(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).thenReturn(Optional.empty());
		when(pip.resolve(context, ref)).thenThrow(PathEvaluationException.invalidXpath(
				ref.getPath(), Status.syntaxError().message("backend detail").build(), null));

		AttributeReferenceEvaluationException e = assertThrows(
				AttributeReferenceEvaluationException.class, () -> handler.resolve(context, ref));

		assertEquals(true, e.isResolverFailure());
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
		assertEquals(Optional.of(AttributeReferenceEvaluationException.RESOLUTION_FAILED_MESSAGE),
				e.getStatus().getMessage());
		Mockito.verify(pip, Mockito.times(1)).resolve(context, ref);
	}

	/**
	 * A failure type of an information point that extends the type of the
	 * engine's mapped failure and carries a status of its own choice.
	 */
	@SuppressWarnings("serial")
	private static final class SubclassedFailure extends AttributeReferenceEvaluationException
	{
		SubclassedFailure(Status status, org.xacml4j.v30.AttributeReferenceKey key) {
			super(status, key, "backend detail");
		}
	}

	@Test
	public void testDesignatorPipSubclassOfTheMappedFailureCannotKeepItsStatus()
	{
		// rv079 round 3, finding 1: only the factory marks a mapped failure. A subclass can not
		// claim the mark, so MustBePresent=false can not turn its missing-attribute into an empty bag.
		AttributeDesignatorKey ref = AttributeDesignatorKey
				.builder()
				.category(CategoryId.RESOURCE)
				.attributeId("testId")
				.dataType(XacmlTypes.ANYURI)
				.build();
		when(requestContextCallback.getEntity(CategoryId.RESOURCE)).thenReturn(Optional.of(entity));
		Status[] chosen = {
				Status.missingAttribute(ref).message("backend detail").build(),
				Status.syntaxError().message("backend detail").build()};
		when(pip.resolve(context, ref))
				.thenThrow(new SubclassedFailure(chosen[0], ref))
				.thenThrow(new SubclassedFailure(chosen[1], ref));
		for (int i = 0; i < chosen.length; i++) {
			AttributeReferenceEvaluationException e = assertThrows(
					AttributeReferenceEvaluationException.class, () -> handler.resolve(context, ref));

			assertEquals(false, e instanceof SubclassedFailure);
			assertEquals(true, e.isResolverFailure());
			assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
			assertEquals(Optional.of(AttributeReferenceEvaluationException.RESOLUTION_FAILED_MESSAGE),
					e.getStatus().getMessage());
		}
		Mockito.verify(pip, Mockito.times(2)).resolve(context, ref);
	}

	@Test
	public void testSelectorPipSubclassOfTheMappedFailureCannotKeepItsStatus()
	{
		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(CategoryId.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(XacmlTypes.INTEGER)
				.build();
		when(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).thenReturn(Optional.empty());
		Status[] chosen = {
				Status.missingAttribute(ref).message("backend detail").build(),
				Status.syntaxError().message("backend detail").build()};
		when(pip.resolve(context, ref))
				.thenThrow(new SubclassedFailure(chosen[0], ref))
				.thenThrow(new SubclassedFailure(chosen[1], ref));
		for (int i = 0; i < chosen.length; i++) {
			AttributeReferenceEvaluationException e = assertThrows(
					AttributeReferenceEvaluationException.class, () -> handler.resolve(context, ref));

			assertEquals(false, e instanceof SubclassedFailure);
			assertEquals(true, e.isResolverFailure());
			assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
			assertEquals(Optional.of(AttributeReferenceEvaluationException.RESOLUTION_FAILED_MESSAGE),
					e.getStatus().getMessage());
		}
		Mockito.verify(pip, Mockito.times(2)).resolve(context, ref);
	}

	/**
	 * A failure whose {@code getMessage()} throws an exception with a status
	 * of its own choice (rv079 round 4). The handler maps the failure before
	 * it reads or logs anything of it.
	 */
	@SuppressWarnings("serial")
	private static final class ThrowingMessageFailure extends RuntimeException
	{
		private final RuntimeException thrownByGetMessage;

		ThrowingMessageFailure(RuntimeException thrownByGetMessage) {
			this.thrownByGetMessage = thrownByGetMessage;
		}

		@Override
		public String getMessage() {
			throw thrownByGetMessage;
		}
	}

	/** Throws a checked exception from a method that does not declare it. */
	@SuppressWarnings("unchecked")
	private static <E extends Throwable> RuntimeException sneakyThrow(Throwable t) throws E
	{
		throw (E) t;
	}

	/**
	 * rv079 round 5: {@code getMessage()} of the failure throws a checked
	 * exception whose cause carries a status and a text of its own choice.
	 */
	@SuppressWarnings("serial")
	private static final class CheckedThrowingMessageFailure extends RuntimeException
	{
		private final EvaluationException chosen;

		CheckedThrowingMessageFailure(EvaluationException chosen) {
			this.chosen = chosen;
		}

		@Override
		public String getMessage() {
			throw DefaultEvaluationContextHandlerTest.<RuntimeException>sneakyThrow(
					new Exception("checked", chosen));
		}
	}

	@Test
	public void testPipFailureWhoseMessageThrowsACheckedExceptionIsStillMapped()
	{
		AttributeDesignatorKey designator = AttributeDesignatorKey
				.builder()
				.category(CategoryId.RESOURCE)
				.attributeId("testId")
				.dataType(XacmlTypes.ANYURI)
				.build();
		AttributeSelectorKey selector = AttributeSelectorKey
				.builder()
				.category(CategoryId.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(XacmlTypes.INTEGER)
				.build();
		when(requestContextCallback.getEntity(CategoryId.RESOURCE)).thenReturn(Optional.of(entity));
		when(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).thenReturn(Optional.empty());
		when(pip.resolve(context, designator)).thenThrow(new CheckedThrowingMessageFailure(
				new EvaluationException(Status.missingAttribute(designator).build(), "backend detail")));
		when(pip.resolve(context, selector)).thenThrow(new CheckedThrowingMessageFailure(
				PathEvaluationException.invalidXpath(selector.getPath(),
						Status.syntaxError().message("backend detail").build(), null)));

		for (org.xacml4j.v30.AttributeReferenceKey ref : java.util.List.of(designator, selector)) {
			AttributeReferenceEvaluationException e = assertThrows(
					AttributeReferenceEvaluationException.class, () -> handler.resolve(context, ref));

			assertEquals(true, e.isResolverFailure());
			assertEquals(true, e.getCause() instanceof CheckedThrowingMessageFailure);
			assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
			assertEquals(Optional.of(AttributeReferenceEvaluationException.RESOLUTION_FAILED_MESSAGE),
					e.getStatus().getMessage());
		}
	}

	@Test
	public void testDesignatorPipFailureWithAThrowingMessageIsStillMapped()
	{
		AttributeDesignatorKey ref = AttributeDesignatorKey
				.builder()
				.category(CategoryId.RESOURCE)
				.attributeId("testId")
				.dataType(XacmlTypes.ANYURI)
				.build();
		when(requestContextCallback.getEntity(CategoryId.RESOURCE)).thenReturn(Optional.of(entity));
		// MustBePresent=false would turn this missing-attribute into an empty bag.
		when(pip.resolve(context, ref)).thenThrow(new ThrowingMessageFailure(
				new EvaluationException(Status.missingAttribute(ref).build(), "backend detail")));

		AttributeReferenceEvaluationException e = assertThrows(
				AttributeReferenceEvaluationException.class, () -> handler.resolve(context, ref));

		assertEquals(true, e.isResolverFailure());
		assertEquals(true, e.getCause() instanceof ThrowingMessageFailure);
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
		assertEquals(Optional.of(AttributeReferenceEvaluationException.RESOLUTION_FAILED_MESSAGE),
				e.getStatus().getMessage());
		assertEquals(false, e.getMessage().contains("backend detail"));
	}

	@Test
	public void testSelectorPipFailureWithAThrowingMessageIsStillMapped()
	{
		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(CategoryId.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(XacmlTypes.INTEGER)
				.build();
		when(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).thenReturn(Optional.empty());
		// The information point must not be able to make this look like the engine's syntax error.
		when(pip.resolve(context, ref)).thenThrow(new ThrowingMessageFailure(
				PathEvaluationException.invalidXpath(ref.getPath(),
						Status.syntaxError().message("backend detail").build(), null)));

		AttributeReferenceEvaluationException e = assertThrows(
				AttributeReferenceEvaluationException.class, () -> handler.resolve(context, ref));

		assertEquals(true, e.isResolverFailure());
		assertEquals(true, e.getCause() instanceof ThrowingMessageFailure);
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
		assertEquals(Optional.of(AttributeReferenceEvaluationException.RESOLUTION_FAILED_MESSAGE),
				e.getStatus().getMessage());
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

	// One bag per request attribute and decision (change optimize-pdp-evaluation, cycle A, D5;
	// XACML 3.0 section 7.3.5: every later test of an attribute uses the bag first tested).

	private static final AttributeDesignatorKey RESOURCE_ID = AttributeDesignatorKey.builder()
			.category(CategoryId.RESOURCE).attributeId("urn:xacml4j:test:resource-id")
			.dataType(XacmlTypes.STRING).build();

	private static Entity resource(String id)
	{
		return Entity.builder()
				.attribute(Attribute.builder("urn:xacml4j:test:resource-id").value(XacmlTypes.STRING.ofAny(id)).build())
				.build();
	}

	@Test
	public void repeatedRequestDesignatorIsTakenFromTheRequestOnceAndGivesTheSameBag() throws EvaluationException
	{
		when(requestContextCallback.getEntity(CategoryId.RESOURCE)).thenReturn(Optional.of(resource("doc-1")));

		Optional<BagOfValues> first = handler.resolve(context, RESOURCE_ID);
		Optional<BagOfValues> second = handler.resolve(context, RESOURCE_ID);

		assertEquals(Optional.of(XacmlTypes.STRING.ofAny("doc-1").toBag()), first);
		org.junit.jupiter.api.Assertions.assertSame(first.get(), second.get());
		Mockito.verify(requestContextCallback, Mockito.times(1)).getEntity(CategoryId.RESOURCE);
		Mockito.verifyNoInteractions(pip);
		org.junit.jupiter.api.Assertions.assertTrue(
				((DefaultEvaluationContextHandler) handler).getResolvedDesignators().isEmpty(),
				"request attributes are not resolved attributes of the response");
	}

	@Test
	public void attributeFromTheInformationPointIsAskedOnceAndListedAsResolved() throws EvaluationException
	{
		BagOfValues supplied = XacmlTypes.STRING.ofAny("doc-2").toBag();
		when(requestContextCallback.getEntity(CategoryId.RESOURCE)).thenReturn(Optional.empty());
		when(pip.resolve(context, RESOURCE_ID)).thenReturn(Optional.of(supplied));

		assertEquals(Optional.of(supplied), handler.resolve(context, RESOURCE_ID));
		assertEquals(Optional.of(supplied), handler.resolve(context, RESOURCE_ID));

		Mockito.verify(pip, Mockito.times(1)).resolve(context, RESOURCE_ID);
		assertEquals(java.util.Map.of(RESOURCE_ID, supplied),
				((DefaultEvaluationContextHandler) handler).getResolvedDesignators());
	}

	@Test
	public void absentAttributeIsNotRemembered() throws EvaluationException
	{
		BagOfValues later = XacmlTypes.STRING.ofAny("doc-3").toBag();
		when(requestContextCallback.getEntity(CategoryId.RESOURCE)).thenReturn(Optional.of(Entity.builder().build()));
		when(pip.resolve(context, RESOURCE_ID)).thenReturn(Optional.empty(), Optional.of(later));

		assertEquals(Optional.empty(), handler.resolve(context, RESOURCE_ID));
		assertEquals(Optional.of(later), handler.resolve(context, RESOURCE_ID));
	}

	@Test
	public void failedResolutionIsNotRemembered() throws EvaluationException
	{
		BagOfValues later = XacmlTypes.STRING.ofAny("doc-4").toBag();
		when(requestContextCallback.getEntity(CategoryId.RESOURCE)).thenReturn(Optional.empty());
		when(pip.resolve(context, RESOURCE_ID)).thenThrow(new IllegalStateException("information point down"))
				.thenReturn(Optional.of(later));

		assertThrows(EvaluationException.class, () -> handler.resolve(context, RESOURCE_ID));
		assertEquals(Optional.of(later), handler.resolve(context, RESOURCE_ID));
	}

	@Test
	public void separateDecisionsResolveSeparately() throws EvaluationException
	{
		when(requestContextCallback.getEntity(CategoryId.RESOURCE)).thenReturn(Optional.of(resource("doc-5")));
		EvaluationContextHandler other = new DefaultEvaluationContextHandler(requestContextCallback, pip);

		handler.resolve(context, RESOURCE_ID);
		other.resolve(context, RESOURCE_ID);

		Mockito.verify(requestContextCallback, Mockito.times(2)).getEntity(CategoryId.RESOURCE);
	}

	/** Review RVP: two concurrent first resolutions in one decision both return the bag stored first. */
	@Test
	public void concurrentFirstResolutionsReturnTheBagStoredFirst() throws Exception
	{
		java.util.concurrent.CyclicBarrier bothInside = new java.util.concurrent.CyclicBarrier(2);
		when(requestContextCallback.getEntity(CategoryId.RESOURCE)).thenAnswer(invocation -> {
			bothInside.await(10, java.util.concurrent.TimeUnit.SECONDS);
			return Optional.of(resource("doc-6"));
		});
		java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(2);
		try{
			java.util.concurrent.Future<Optional<BagOfValues>> a = pool.submit(() -> handler.resolve(context, RESOURCE_ID));
			java.util.concurrent.Future<Optional<BagOfValues>> b = pool.submit(() -> handler.resolve(context, RESOURCE_ID));
			BagOfValues first = a.get(20, java.util.concurrent.TimeUnit.SECONDS).get();
			BagOfValues second = b.get(20, java.util.concurrent.TimeUnit.SECONDS).get();

			org.junit.jupiter.api.Assertions.assertSame(first, second);
			org.junit.jupiter.api.Assertions.assertSame(first, handler.resolve(context, RESOURCE_ID).get());
		}finally{
			pool.shutdownNow();
		}
	}

	/** Review RVP: an information point that resolves a request attribute through its context. */
	@Test
	public void informationPointCallingBackResolvesTheRequestAttributeOnce() throws EvaluationException
	{
		AttributeDesignatorKey clearance = AttributeDesignatorKey.builder()
				.category(CategoryId.SUBJECT_ACCESS).attributeId("urn:xacml4j:test:clearance")
				.dataType(XacmlTypes.STRING).build();
		BagOfValues[] seenByPip = new BagOfValues[1];
		when(requestContextCallback.getEntity(CategoryId.SUBJECT_ACCESS)).thenReturn(Optional.empty());
		when(requestContextCallback.getEntity(CategoryId.RESOURCE)).thenReturn(Optional.of(resource("doc-7")));
		when(pip.resolve(context, clearance)).thenAnswer(invocation -> {
			seenByPip[0] = handler.resolve(context, RESOURCE_ID).get();
			return Optional.of(XacmlTypes.STRING.ofAny("high").toBag());
		});

		assertEquals(Optional.of(XacmlTypes.STRING.ofAny("high").toBag()), handler.resolve(context, clearance));
		org.junit.jupiter.api.Assertions.assertSame(seenByPip[0], handler.resolve(context, RESOURCE_ID).get());
		Mockito.verify(requestContextCallback, Mockito.times(1)).getEntity(CategoryId.RESOURCE);
	}

	/** Reviews RVC1 and RVC2: a resolved-attributes map kept before the first information point bag sees it. */
	@Test
	public void resolvedMapsKeptBeforeTheFirstBagSeeLaterBags() throws EvaluationException
	{
		BagOfValues supplied = XacmlTypes.STRING.ofAny("doc-8").toBag();
		java.util.Map<AttributeDesignatorKey, BagOfValues> kept = ((DefaultEvaluationContextHandler) handler).getResolvedDesignators();
		java.util.Set<java.util.Map.Entry<AttributeDesignatorKey, BagOfValues>> keptEntries = kept.entrySet();
		java.util.Map<org.xacml4j.v30.AttributeSelectorKey, BagOfValues> keptSelectors = ((DefaultEvaluationContextHandler) handler).getResolvedSelectors();
		when(requestContextCallback.getEntity(CategoryId.RESOURCE)).thenReturn(Optional.empty());
		when(pip.resolve(context, RESOURCE_ID)).thenReturn(Optional.of(supplied));

		org.junit.jupiter.api.Assertions.assertTrue(kept.isEmpty());
		handler.resolve(context, RESOURCE_ID);

		assertEquals(java.util.Map.of(RESOURCE_ID, supplied), kept);
		assertEquals(supplied, kept.get(RESOURCE_ID));
		assertEquals(1, keptEntries.size());
		org.junit.jupiter.api.Assertions.assertTrue(keptSelectors.isEmpty());
		// the unmodifiable wrapper of a concurrent map, as before (review RVC2R)
		org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class, () -> kept.put(RESOURCE_ID, supplied));
		org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class, () -> keptSelectors.remove(RESOURCE_ID));
		org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> keptSelectors.get(null));
		org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> keptSelectors.containsValue(null));
	}
}
