package org.xacml4j.v30.policy.function.impl;

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

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Content;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Value;
import org.xacml4j.v30.content.XmlContent;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.PathValue;
import org.xacml4j.v30.types.XacmlTypes;



public class XPathFunctionsTest
{
	private String testXml = "<md:record xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
	"xmlns:md=\"urn:example:med:schemas:record\">" +
	"<md:patient>" +
	"<md:patientDoB>1992-03-21</md:patientDoB>" +
	"<md:patient-number>555555</md:patient-number>" +
	"</md:patient>" +
	"</md:record>";

	private String testXmlSame = "<md:record xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
			"xmlns:md=\"urn:example:med:schemas:record\">" +
			"<md:patient>" +
			"<md:patientDoB>1992-03-21</md:patientDoB>" +
			"<md:patient-number>555555</md:patient-number>" +
			"</md:patient>" +
			"</md:record>";

	private String testXmlDiff = "<md:record xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
			"xmlns:md=\"urn:example:med:schemas:record\">" +
			"<md:patient>" +
			"<md:patientDoB>1976-13-21</md:patientDoB>" +
			"<md:patient-number>109293</md:patient-number>" +
			"</md:patient>" +
			"</md:record>";

	private EvaluationContext context;
	private XmlContent content;
	private XmlContent contentSame;
	private XmlContent contentDiff;
	private FunctionProvider funcF;

	private XmlContent contentDifferent;

	@Before
	public void init() throws Exception
	{

		this.context = createStrictMock(EvaluationContext.class);
		this.content = XmlContent.of(XmlContent.fromString(testXml));
		this.contentSame = XmlContent.of(XmlContent.fromString(testXmlSame));
		this.contentDiff = XmlContent.of(XmlContent.fromString(testXmlDiff));
		this.contentDifferent = XmlContent.of(
				XmlContent
						.fromStream(
								Thread.currentThread()
										.getContextClassLoader()
										.getResourceAsStream("./testContentXPathFunctions.xml")));

		this.funcF = FunctionProviderBuilder
				.builder()
				.fromClass(XPathFunctions.class)
				.build();
	}

	@Test
	public void testValidateFunctions()
	{
		assertTrue(funcF.getFunction("urn:oasis:names:tc:xacml:3.0:function:xpath-node-count").isPresent());
		assertTrue(funcF.getFunction("urn:oasis:names:tc:xacml:3.0:function:xpath-node-match").isPresent());
		assertTrue(funcF.getFunction("urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal").isPresent());
		assertTrue(funcF.getFunction("urn:oasis:names:tc:xacml:1.0:function:xpath-node-count").isPresent());
		assertTrue(funcF.getFunction("urn:oasis:names:tc:xacml:1.0:function:xpath-node-match").isPresent());
		assertTrue(funcF.getFunction("urn:oasis:names:tc:xacml:1.0:function:xpath-node-equal").isPresent());
	}


	@Test
	public void testXPathCount() throws EvaluationException
	{
		FunctionSpec f = funcF.getFunction("urn:oasis:names:tc:xacml:3.0:function:xpath-node-count").get();
		PathValue xpath  = XacmlTypes.XPATH.of("/md:record/md:patient", CategoryId.SUBJECT_ACCESS);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(context.resolve(xpath.getCategory().orElse(null), xpath.getContentType())).andReturn(Optional.of(content));
		replay(context);
		assertEquals(XacmlTypes.INTEGER.of(1), f.invoke(context, xpath));
		verify(context);
	}

	@Test
	public void testXPathCountXacml20() throws EvaluationException
	{
		FunctionSpec f = funcF.getFunction("urn:oasis:names:tc:xacml:1.0:function:xpath-node-count").get();
		Value xpath  = XacmlTypes.STRING.of("./xacml-context:Resource/xacml-context:ResourceContent/md:record//md:name");
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(context.resolve(CategoryId.RESOURCE, Content.Type.XML_UTF8))
				.andReturn(Optional.of(contentDifferent));
		replay(context);
		assertEquals(XacmlTypes.INTEGER.of(2), f.invoke(context, xpath));
		verify(context);
	}

	@Test
	public void testXPathCountExpressionReturnsEmptyNodeSet() throws EvaluationException
	{
		PathValue xpath  = XacmlTypes.XPATH.of("/test",
				CategoryId.SUBJECT_ACCESS);
		expect(context.resolve(xpath.getCategory().orElse(null), xpath.getContentType())).andReturn(Optional.of(content));
		replay(context);
		assertEquals(XacmlTypes.INTEGER.of(0), XPathFunctions.xpathCount(context, xpath));
		verify(context);
	}

	@Test
	public void testXPathNodeMatch() throws EvaluationException
	{
		FunctionSpec f = funcF.getFunction("urn:oasis:names:tc:xacml:3.0:function:xpath-node-match").get();
		PathValue xpath0  = XacmlTypes.XPATH.of("/md:record/md:patient", CategoryId.SUBJECT_ACCESS);
		PathValue xpath1  = XacmlTypes.XPATH.of("/md:record/md:patient", CategoryId.SUBJECT_ACCESS);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(context.resolve(xpath0.getCategory().get(), xpath0.getContentType())).andReturn(Optional.of(content));
		expect(context.resolve(xpath1.getCategory().get(), xpath1.getContentType())).andReturn(Optional.of(contentSame));
		replay(context);
		assertEquals(XacmlTypes.BOOLEAN.of(true), f.invoke(context, xpath0, xpath1));
		verify(context);
	}

	@Test
	public void testXPathNodeMatchXacml1()
	{
		FunctionSpec f = funcF.getFunction("urn:oasis:names:tc:xacml:1.0:function:xpath-node-match").get();
		Value xpath0  = XacmlTypes.STRING.of("./xacml-context:Resource/xacml-context:ResourceContent/md:record");
		Value xpath1  = XacmlTypes.STRING.of("./xacml-context:Resource/xacml-context:ResourceContent/md:record");
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(context.resolve(CategoryId.RESOURCE, Content.Type.XML_UTF8))
				.andReturn(Optional.of(content));
		expect(context.resolve(CategoryId.RESOURCE, Content.Type.XML_UTF8))
				.andReturn(Optional.of(contentSame));
		replay(context);
		assertEquals(XacmlTypes.BOOLEAN.of(true), f.invoke(context, xpath0, xpath1));
		verify(context);
	}

}
