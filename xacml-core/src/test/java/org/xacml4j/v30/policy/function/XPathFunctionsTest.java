package org.xacml4j.v30.policy.function;

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
import static org.junit.Assert.assertNotNull;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.spi.function.AnnotationBasedFunctionProvider;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.spi.xpath.DefaultXPathProvider;
import org.xacml4j.v30.spi.xpath.XPathProvider;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.IntegerExp;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.XPathExp;
import org.xml.sax.InputSource;


public class XPathFunctionsTest
{
	private String testXml = "<md:record xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
	"xmlns:md=\"urn:example:med:schemas:record\">" +
	"<md:patient>" +
	"<md:patientDoB>1992-03-21</md:patientDoB>" +
	"<md:patient-number>555555</md:patient-number>" +
	"</md:patient>" +
	"</md:record>";

	private EvaluationContext context;
	private Node content;
	private XPathProvider xpathProvider;
	private FunctionProvider funcF;

	private Node content1;

	@Before
	public void init() throws Exception
	{
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setNamespaceAware(true);
		DocumentBuilder builder = f.newDocumentBuilder();
		this.context = createStrictMock(EvaluationContext.class);
		this.content = builder.parse(new InputSource(new StringReader(testXml)));
		this.content1 = builder.parse(new InputSource(
				Thread.currentThread().getContextClassLoader().getResourceAsStream("./testContentXPathFunctions.xml")));
		this.xpathProvider = new DefaultXPathProvider();
		this.funcF = new AnnotationBasedFunctionProvider(
				XPathFunctions.class);
	}

	@Test
	public void testValidateFunctions()
	{
		assertNotNull(funcF.getFunction("urn:oasis:names:tc:xacml:3.0:function:xpath-node-count"));
		assertNotNull(funcF.getFunction("urn:oasis:names:tc:xacml:3.0:function:xpath-node-match"));
		assertNotNull(funcF.getFunction("urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal"));
		assertNotNull(funcF.getFunction("urn:oasis:names:tc:xacml:1.0:function:xpath-node-count"));
		assertNotNull(funcF.getFunction("urn:oasis:names:tc:xacml:1.0:function:xpath-node-match"));
		assertNotNull(funcF.getFunction("urn:oasis:names:tc:xacml:1.0:function:xpath-node-equal"));
	}

	@Test
	public void testXpathEvaluation() throws Exception
	{
		NodeList result = xpathProvider.evaluateToNodeSet("./md:record", content1);
		NodeList result1 = xpathProvider.evaluateToNodeSet("./md:record//md:name", content1);
		NodeList result2 = xpathProvider.evaluateToNodeSet("//*[local-name()='record'][namespace-uri()='urn:example:med:schemas:record']", content);
		assertEquals(1, result.getLength());
		assertEquals(2, result1.getLength());
		assertEquals(1, result2.getLength());
	}

	@Test
	public void testXPathCount() throws EvaluationException
	{
		FunctionSpec f = funcF.getFunction("urn:oasis:names:tc:xacml:3.0:function:xpath-node-count");
		XPathExp xpath  = XPathExp.valueOf("/md:record/md:patient", Categories.SUBJECT_ACCESS);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(context.evaluateToNodeSet(xpath))
		.andAnswer(new XPathAnswer(content));
		replay(context);
		assertEquals(IntegerExp.valueOf(1), f.invoke(context, xpath));
		verify(context);
	}

	@Test
	public void testXPathCountXacml20() throws EvaluationException
	{
		FunctionSpec f = funcF.getFunction("urn:oasis:names:tc:xacml:1.0:function:xpath-node-count");
		StringExp xpath  = StringExp.valueOf("./xacml-context:Resource/xacml-context:ResourceContent/md:record//md:name");
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(context.evaluateToNodeSet(XPathExp.valueOf("./md:record//md:name", Categories.RESOURCE)))
		.andAnswer(new XPathAnswer(content1));
		replay(context);
		assertEquals(IntegerExp.valueOf(2), f.invoke(context, xpath));
		verify(context);
	}

	@Test
	public void testXPathCountExpressionReturnsEmptyNodeSet() throws EvaluationException
	{
		XPathExp xpath  = XPathExp.valueOf("/test",
				Categories.SUBJECT_ACCESS);
		expect(context.evaluateToNodeSet(XPathExp.valueOf("/test", Categories.SUBJECT_ACCESS)))
		.andAnswer(new XPathAnswer(content));
		replay(context);
		assertEquals(IntegerExp.valueOf(0), XPathFunctions.xpathCount(context, xpath));
		verify(context);
	}

	@Test
	public void testXPathNodeMatch() throws EvaluationException
	{
		FunctionSpec f = funcF.getFunction("urn:oasis:names:tc:xacml:3.0:function:xpath-node-match");
		XPathExp xpath0  = XPathExp.valueOf("/md:record", Categories.SUBJECT_ACCESS);
		XPathExp xpath1  = XPathExp.valueOf("/md:record/md:patient/md:patientDoB", Categories.SUBJECT_ACCESS);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(context.evaluateToNodeSet(XPathExp.valueOf("/md:record", Categories.SUBJECT_ACCESS))).andAnswer(new XPathAnswer(content));
		expect(context.evaluateToNodeSet(XPathExp.valueOf("/md:record/md:patient/md:patientDoB", Categories.SUBJECT_ACCESS))).andAnswer(new XPathAnswer(content));
		replay(context);
		assertEquals(BooleanExp.valueOf(true), f.invoke(context, xpath0, xpath1));
		verify(context);
	}

	@Test
	public void testXPathNodeMatchXacml20() throws EvaluationException
	{
		FunctionSpec f = funcF.getFunction("urn:oasis:names:tc:xacml:1.0:function:xpath-node-match");
		StringExp xpath0  = StringExp.valueOf(".");
		StringExp xpath1  = StringExp.valueOf("./xacml-context:Resource/xacml-context:ResourceContent/md:record");
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(context.evaluateToNodeSet(XPathExp.valueOf(".", Categories.RESOURCE))).andAnswer(new XPathAnswer(content1));
		expect(context.evaluateToNodeSet(XPathExp.valueOf("./md:record", Categories.RESOURCE))).andAnswer(new XPathAnswer(content1));
		replay(context);
		assertEquals(BooleanExp.valueOf(true), f.invoke(context, xpath0, xpath1));
		verify(context);
	}


	public class XPathAnswer implements IAnswer<NodeList>
	{
		private Node xml;

		public XPathAnswer(Node content){
			this.xml = content;
		}

		@Override
		public NodeList answer() throws Throwable {
			Object[] args = EasyMock.getCurrentArguments();
			return xpathProvider.evaluateToNodeSet(
					((XPathExp)args[0]).getPath(),
					xml);
		}
	}
}
