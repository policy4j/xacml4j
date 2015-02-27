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

import org.easymock.IAnswer;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xacml4j.v30.*;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.types.AnyURIExp;
import org.xacml4j.v30.types.IntegerExp;
import org.xacml4j.v30.types.XacmlTypes;
import org.xacml4j.v30.xpath.DefaultXPathProvider;
import org.xacml4j.v30.xpath.XPathProvider;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;


public class DefaultEvaluationContextHandlerTest
{
	private static final String TEST_XML = "<md:record xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
	"xmlns:md=\"urn:example:med:schemas:record\">" +
	"<md:patient>" +
	"<md:patientDoB>1992-03-21</md:patientDoB>" +
	"<md:patient-number>555555</md:patient-number>" +
	"</md:patient>" +
	"</md:record>";

	private DecisionRuleEvaluationContext context;
	private IMocksControl c;

	private Entity entity;
	private Node content;

	private PolicyInformationPoint pip;
	private RequestContextCallback requestContextCallback;
	private EvaluationContextHandler handler;

	@Before
	public void init() throws Exception
	{
		this.c = createControl();
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setNamespaceAware(true);
		DocumentBuilder builder = f.newDocumentBuilder();
		this.context = c.createMock(DecisionRuleEvaluationContext.class);
		this.requestContextCallback = c.createMock(RequestContextCallback.class);
		this.pip = c.createMock(PolicyInformationPoint.class);
		this.content = builder.parse(new InputSource(new StringReader(TEST_XML)));
		this.entity = Entity
				.builder()
				.content(content).build();
		XPathProvider xpathProvider = new DefaultXPathProvider();
		this.handler = new DefaultEvaluationContextHandler(requestContextCallback, xpathProvider, pip);
	}


	@Test
	public void testSelectorResolveContentIsInRequestXPathReturnsNonEmptySetResolutionScopeRequest()
		throws EvaluationException
	{
		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(Categories.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(XacmlTypes.INTEGER)
				.build();

		expect(requestContextCallback.getEntity(Categories.SUBJECT_RECIPIENT)).andReturn(entity);

		c.replay();

		Expression v = handler.resolve(context, ref);

		assertEquals(v, IntegerExp.of(555555).toBag());
		c.verify();
	}

	@Test
	public void testSelectorResolveContentIsNotInRequestXPathReturnsNonEmptyNodeSet()
		throws Exception
	{
		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(Categories.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(XacmlTypes.INTEGER)
				.build();

		expect(requestContextCallback.getEntity(Categories.SUBJECT_RECIPIENT)).andReturn(entity);

		c.replay();

		Expression v = handler.resolve(context, ref);
		assertEquals(XacmlTypes.INTEGER.bagOf(IntegerExp.of(555555)), v);
		c.verify();
	}

	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testSelectorResolveContentIsNotInRequestPIPCallsHandlerToResolveTheSameAttribute()
		throws Exception
	{
		final AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(Categories.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(XacmlTypes.INTEGER)
				.build();

		expect(requestContextCallback.getEntity(Categories.SUBJECT_RECIPIENT)).andReturn(null);

		expect(pip.resolve(context, Categories.SUBJECT_RECIPIENT)).andStubAnswer(new IAnswer<Node>()
		{
			@Override
			public Node answer() throws Throwable {
				 handler.resolve(context, ref);
				 return content;
			}
		});

		c.replay();

		Expression v1 = handler.resolve(context, ref);
		// test cache
		Expression v2 = handler.resolve(context, ref);
		c.verify();

		assertEquals(IntegerExp.bag().value(555555).build(), v1);
		assertEquals(v1, v2);
	}


	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testSelectorResolveContentIsInRequestXPathReturnsUnsupportedNodeType() throws EvaluationException
	{
		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(Categories.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient")
				.dataType(XacmlTypes.INTEGER)
				.build();

		expect(requestContextCallback.getEntity(Categories.SUBJECT_RECIPIENT)).andReturn(entity);
		context.setEvaluationStatus(Status.missingAttribute(ref).build());
		c.replay();
		handler.resolve(context, ref);
		c.verify();
	}

	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testSelectorResolveContentIsInRequestXPathReturnsNonEmptySetAndNodeFailsConvertionToDate() throws EvaluationException
	{
		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(Categories.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(XacmlTypes.DATE)
				.build();

		expect(requestContextCallback.getEntity(Categories.SUBJECT_RECIPIENT)).andReturn(entity);
		context.setEvaluationStatus(Status.missingAttribute(ref).build());

		c.replay();
		handler.resolve(context, ref);
		c.verify();
	}

	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testSelectorResolveContentIsInRequestXPathReturnsNonEmptySetAndCorrectNodeTypeAndNodeFailsConvertionToDate() throws EvaluationException
	{

		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(Categories.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(XacmlTypes.DATE)
				.build();

		expect(requestContextCallback.getEntity(Categories.SUBJECT_RECIPIENT)).andReturn(entity);
		context.setEvaluationStatus(Status.missingAttribute(ref).build());

		c.replay();
		handler.resolve(context, ref);
		c.verify();
	}

	@Test
	public void testSelectorResolveContentInRequestExistXPathReturnsEmptySet() throws EvaluationException
	{
		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(Categories.SUBJECT_RECIPIENT)
				.xpath("/test")
				.dataType(XacmlTypes.INTEGER)
				.build();

		expect(requestContextCallback.getEntity(Categories.SUBJECT_RECIPIENT)).andReturn(entity);

		c.replay();
		Expression v = handler.resolve(context, ref);
		assertEquals(v, IntegerExp.emptyBag());
		c.verify();
	}

	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testSelectorResolveContentIsNotInRequestPIPThrowsRuntimeException()
		throws Exception
	{

		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(Categories.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(XacmlTypes.INTEGER)
				.build();

		expect(requestContextCallback.getEntity(Categories.SUBJECT_RECIPIENT)).andReturn(null);

		expect(pip.resolve(context, Categories.SUBJECT_RECIPIENT)).andThrow(new RuntimeException());

		c.replay();
		handler.resolve(context, ref);
		c.verify();
	}

	@Test
	public void testDesignatorResolveAttributeIsNotInRequest()
		throws Exception
	{
		AttributeDesignatorKey ref = AttributeDesignatorKey
				.builder()
				.category(Categories.RESOURCE)
				.attributeId("testId")
				.dataType(XacmlTypes.ANYURI)
				.build();

		expect(requestContextCallback.getEntity(Categories.RESOURCE)).andReturn(entity);
		expect(pip.resolve(context, ref)).andReturn(AnyURIExp.of("testValue").toBag());

		c.replay();
		ValueExpression v = handler.resolve(context, ref);
		assertEquals(AnyURIExp.of("testValue").toBag(), v);
		c.verify();
	}


	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testDesignatorResolveAttributeIsNotInRequestPIPThrowsRuntimeException()
		throws Exception
	{
		AttributeDesignatorKey ref = AttributeDesignatorKey
				.builder()
				.category(Categories.RESOURCE)
				.attributeId("testId")
				.dataType(XacmlTypes.ANYURI)
				.build();

		expect(requestContextCallback.getEntity(Categories.RESOURCE)).andReturn(entity);

		expect(pip.resolve(context, ref)).andThrow(new RuntimeException());

		c.replay();
		handler.resolve(context, ref);
		c.verify();
	}

	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testDesignatorResolveAttributeIsNotInRequestPIPCallsHandlerToResolveTheSameAttribute()
		throws Exception
	{
		final AttributeDesignatorKey ref = AttributeDesignatorKey
				.builder()
				.category(Categories.RESOURCE)
				.attributeId("testId")
				.dataType(XacmlTypes.ANYURI)
				.build();

		expect(requestContextCallback.getEntity(Categories.RESOURCE)).andReturn(entity);


		expect(pip.resolve(context, ref)).andAnswer(new IAnswer<BagOfAttributeExp>() {
			@Override
			public BagOfAttributeExp answer() throws Throwable{
				handler.resolve(context, ref);
				return AnyURIExp.emptyBag();
			}
		});

		expect(requestContextCallback.getEntity(Categories.RESOURCE)).andReturn(entity);

		c.replay();
		handler.resolve(context, ref);
		c.verify();
	}
}
