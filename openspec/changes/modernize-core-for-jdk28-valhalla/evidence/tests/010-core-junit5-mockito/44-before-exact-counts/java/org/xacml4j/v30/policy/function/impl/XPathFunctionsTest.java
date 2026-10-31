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

import static org.mockito.Mockito.*;

import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Content;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.types.*;
import org.xacml4j.v30.content.XmlContent;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;


@ExtendWith(MockitoExtension.class)
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

	@BeforeEach
	public void init() throws Exception
	{

		this.context = mock(EvaluationContext.class);
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
		Path xpath  = XacmlTypes.XPATH.ofAny("/md:record/md:patient", CategoryId.SUBJECT_ACCESS);
		when(context.isValidateFuncParamsAtRuntime()).thenReturn(true);
		when(context.resolveEntity(xpath.getCategory().orElse(null)))
				.thenReturn(Optional.of(Entity.of(content)));
		InOrder order = inOrder(context);
		assertEquals(XacmlTypes.INTEGER.ofAny(1), f.<Value>invoke(context, xpath));
		order.verify(context).isValidateFuncParamsAtRuntime();
		order.verify(context).resolveEntity(xpath.getCategory().orElse(null));
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testXPathCountXacml20() throws EvaluationException
	{
		FunctionSpec f = funcF.getFunction("urn:oasis:names:tc:xacml:1.0:function:xpath-node-count").get();
		StringVal xpath  = XacmlTypes.STRING.ofAny("./xacml-context:Resource/xacml-context:ResourceContent/md:record//md:name");
		when(context.isValidateFuncParamsAtRuntime()).thenReturn(true);
		when(context.resolveEntity(CategoryId.RESOURCE))
				.thenReturn(Optional.of(Entity.of(contentDifferent)));
		InOrder order = inOrder(context);
		assertEquals(XacmlTypes.INTEGER.ofAny(2), f.<Value>invoke(context, xpath));
		order.verify(context).isValidateFuncParamsAtRuntime();
		order.verify(context).resolveEntity(CategoryId.RESOURCE);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testXPathCountExpressionReturnsEmptyNodeSet() throws EvaluationException
	{
		Path xpath  = XacmlTypes.XPATH.ofAny("/test",
				CategoryId.SUBJECT_ACCESS);
		when(context.resolveEntity(xpath.getCategory().orElse(null))).thenReturn(Optional.of(Entity.of(content)));
		InOrder order = inOrder(context);
		assertEquals(XacmlTypes.INTEGER.ofAny(0), XPathFunctions.xpathCount(context, xpath));
		order.verify(context).resolveEntity(xpath.getCategory().orElse(null));
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testXPathNodeMatch() throws EvaluationException
	{
		FunctionSpec f = funcF.getFunction("urn:oasis:names:tc:xacml:3.0:function:xpath-node-match").get();
		Path xpath0  = XacmlTypes.XPATH.ofAny("/md:record/md:patient", CategoryId.SUBJECT_ACCESS);
		Path xpath1  = XacmlTypes.XPATH.ofAny("/md:record/md:patient", CategoryId.SUBJECT_ACCESS);
		when(context.isValidateFuncParamsAtRuntime()).thenReturn(true);
		when(context.resolveContent(xpath0.getCategory().orElse(null), Content.Type.XML_UTF8))
				.thenReturn(Optional.of(content), Optional.of(contentSame));
		InOrder order = inOrder(context);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true), f.<Value>invoke(context, xpath0, xpath1));
		order.verify(context).isValidateFuncParamsAtRuntime();
		order.verify(context, times(2)).resolveContent(xpath0.getCategory().orElse(null), Content.Type.XML_UTF8);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testXPathNodeMatchXacml1()
	{
		FunctionSpec f = funcF.getFunction("urn:oasis:names:tc:xacml:1.0:function:xpath-node-match").get();
		StringVal xpath0  = XacmlTypes.STRING.ofAny("./xacml-context:Resource/xacml-context:ResourceContent/md:record");
		StringVal xpath1  = XacmlTypes.STRING.ofAny("./xacml-context:Resource/xacml-context:ResourceContent/md:record");
		when(context.isValidateFuncParamsAtRuntime()).thenReturn(true);
		when(context.resolveContent(CategoryId.RESOURCE, Content.Type.XML_UTF8))
				.thenReturn(Optional.of(content), Optional.of(contentSame));
		InOrder order = inOrder(context);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true), f.<Value>invoke(context, xpath0, xpath1));
		order.verify(context).isValidateFuncParamsAtRuntime();
		order.verify(context, times(2)).resolveContent(CategoryId.RESOURCE, Content.Type.XML_UTF8);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context);
	}

}
