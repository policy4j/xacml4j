package com.artagon.xacml.v3.policy.impl.function;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.XPathEvaluationException;
import com.artagon.xacml.v3.policy.XPathProvider;
import com.artagon.xacml.v3.policy.impl.JDKXPathProvider;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.XPathExpressionType.XPathExpressionValue;

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
	private XPathProvider provider;
	private XPathProvider realProvider;
	
	@Before
	public void init() throws Exception
	{
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setNamespaceAware(true);
		DocumentBuilder builder = f.newDocumentBuilder();
		this.context = createStrictMock(EvaluationContext.class);
		this.content = builder.parse(new InputSource(new StringReader(testXml)));
		this.provider = createStrictMock(XPathProvider.class);
		this.realProvider = new JDKXPathProvider();
	}
	
	@Test
	public void testXPathCount() throws XPathEvaluationException
	{
		XPathExpressionValue xpath  = DataTypes.XPATHEXPRESSION.create(
				"count(/md:record/md:patient)", 
				AttributeCategoryId.SUBJECT_ACCESS);
		expect(context.getXPathProvider()).andReturn(provider);
		expect(context.getContent(AttributeCategoryId.SUBJECT_ACCESS)).andReturn(content);
		expect(provider.evaluateToString("count(/md:record/md:patient)", content)).andDelegateTo(realProvider);
		replay(context, provider);
		assertEquals(DataTypes.INTEGER.create(1), XPathFunctions.xpathCount(context, xpath));
		verify(context, provider);
	}
	
	@Test
	public void testXPathCountExpressionReturnsEmptyNodeSet() throws XPathEvaluationException
	{
		XPathExpressionValue xpath  = DataTypes.XPATHEXPRESSION.create(
				"count(/test)", 
				AttributeCategoryId.SUBJECT_ACCESS);
		expect(context.getXPathProvider()).andReturn(provider);
		expect(context.getContent(AttributeCategoryId.SUBJECT_ACCESS)).andReturn(content);
		expect(provider.evaluateToString("count(/test)", content)).andDelegateTo(realProvider);
		replay(context, provider);
		assertEquals(DataTypes.INTEGER.create(0), XPathFunctions.xpathCount(context, xpath));
		verify(context, provider);
	}
	
	@Test
	public void testXPathCountContentNodeIsNull()
	{
		XPathExpressionValue xpath  = DataTypes.XPATHEXPRESSION.create(
				"count(/test)", 
				AttributeCategoryId.SUBJECT_ACCESS);
		expect(context.getXPathProvider()).andReturn(provider);
		expect(context.getContent(AttributeCategoryId.SUBJECT_ACCESS)).andReturn(null);
		replay(context, provider);
		assertEquals(DataTypes.INTEGER.create(0), XPathFunctions.xpathCount(context, xpath));
		verify(context, provider);
	}
}
