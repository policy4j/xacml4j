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

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Optional;

import org.easymock.Capture;
import org.easymock.IAnswer;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.PathEvaluationException;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.content.XPathProvider;
import org.xacml4j.v30.content.XmlContent;
import org.xacml4j.v30.policy.EvaluationContextHandler;
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
	private IMocksControl c;

	private Entity entity;
	private XmlContent content;

	private PolicyInformationPoint pip;
	private XPathProvider xpathProvider;
	private RequestContextCallback requestContextCallback;
	private EvaluationContextHandler handler;

	@Before
	public void init() throws Exception
	{
		this.c = createControl();

		this.context = c.createMock(EvaluationContext.class);
		this.requestContextCallback = c.createMock(RequestContextCallback.class);
		this.pip = c.createMock(PolicyInformationPoint.class);
		this.entity = Entity
				.builder()
				.content(XmlContent.of(XmlContent.fromString(testXml)))
				.build();
		this.xpathProvider = XPathProvider.defaultProvider();
		this.handler = new DefaultEvaluationContextHandler(requestContextCallback, pip);
		this.content = XmlContent.of(testXml);
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
		expect(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).andReturn(Optional.of(entity));
		c.replay();
		Optional<BagOfValues> v = handler.resolve(context, ref);
		assertEquals(v.get(), XacmlTypes.INTEGER.of(555555).toBag());
		c.verify();
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

		expect(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).andReturn(Optional.of(entity));
		
		c.replay();

		Optional<BagOfValues> v = handler.resolve(context, ref);
		assertEquals(v.get(), XacmlTypes.INTEGER.bagBuilder()
		                                        .value(555555)
		                                        .build());
		c.verify();
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

		expect(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).andReturn(Optional.empty());

		expect(pip.resolve(context, ref)).andReturn(Optional.of(content).flatMap(v->v.resolve(ref)));

		c.replay();

		Optional<BagOfValues> v = handler.resolve(context, ref);
		// test cache
		v = handler.resolve(context, ref);
		assertEquals(XacmlTypes.INTEGER.bagBuilder().value(555555).build(), v.get());
		c.verify();
	}


	@Test(expected = PathEvaluationException.class)
	public void testSelectorResolveContentIsInRequestXPathReturnsUnsupportedNodeType() throws EvaluationException
	{
		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(CategoryId.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient")
				.dataType(XacmlTypes.INTEGER)
				.build();
		expect(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).andReturn(Optional.of(entity));
		expect(pip.resolve(context, ref)).andReturn(Optional.empty());
		Capture<Status> statusCapture = Capture.newInstance();
		context.setEvaluationStatus(capture(statusCapture));
		c.replay();
		Optional<BagOfValues> v = handler.resolve(context, ref);
		c.verify();
		assertFalse(v.isPresent());
	}
	@Test(expected = PathEvaluationException.class)
	public void testSelectorResolveContentIsInRequestXPathReturnsNonEmptySetAndNodeFailsConvertionToDate() throws EvaluationException
	{
		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(CategoryId.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(XacmlTypes.DATE)
				.build();

		expect(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).andReturn(Optional.of(entity));
		expect(pip.resolve(context, ref)).andReturn(Optional.empty());
		Capture<Status> statusCapture = Capture.newInstance();
		context.setEvaluationStatus(capture(statusCapture));
		
		c.replay();
		Optional<BagOfValues> v = handler.resolve(context, ref);
		c.verify();
		assertFalse(v.isPresent());
	}

	@Test(expected = PathEvaluationException.class)
	public void testSelectorResolveContentIsInRequestXPathReturnsNonEmptySetAndCorrectNodeTypeAndNodeFailsConvertionToDate() throws EvaluationException
	{

		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(CategoryId.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(XacmlTypes.DATE)
				.build();

		expect(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).andReturn(Optional.of(entity));
		expect(pip.resolve(context, ref)).andReturn(Optional.empty());
		Capture<Status> statusCapture = Capture.newInstance();
		context.setEvaluationStatus(capture(statusCapture));
		c.replay();
		Optional<BagOfValues> v = handler.resolve(context, ref);
		c.verify();
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
		expect(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).andReturn(Optional.of(entity));
		expect(pip.resolve(context, ref)).andReturn(Optional.empty());
		
		c.replay();
		Optional<BagOfValues> v = handler.resolve(context, ref);
		c.verify();
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

		expect(requestContextCallback.getEntity(CategoryId.SUBJECT_RECIPIENT)).andReturn(Optional.empty());

		expect(pip.resolve(context, ref)).andThrow(new RuntimeException());

		c.replay();
		Optional<BagOfValues> v = handler.resolve(context, ref);
		c.verify();
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

		expect(requestContextCallback.getEntity(CategoryId.RESOURCE)).andReturn(Optional.of(entity));
		expect(pip.resolve(context, ref)).andReturn(Optional.of(XacmlTypes.ANYURI.of("testValue").toBag()));
		c.replay();
		Optional<BagOfValues> v = handler.resolve(context, ref);
		assertEquals(XacmlTypes.ANYURI.of("testValue").toBag(), v.get());
		c.verify();
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

		expect(requestContextCallback.getEntity(CategoryId.RESOURCE)).andReturn(Optional.of(entity));
		expect(pip.resolve(context, ref)).andThrow(new RuntimeException());

		c.replay();
		Optional<BagOfValues> v = handler.resolve(context, ref);
		c.verify();
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
		expect(requestContextCallback.getEntity(CategoryId.RESOURCE)).andReturn(Optional.of(entity)).anyTimes();
		expect(pip.resolve(context, ref)).andAnswer(new IAnswer<Optional<BagOfValues>>() {
		public Optional<BagOfValues> answer() throws Throwable{
				return handler.resolve(context, ref);
			}
		});


		c.replay();
		handler.resolve(context, ref);
		c.verify();
	}
}
