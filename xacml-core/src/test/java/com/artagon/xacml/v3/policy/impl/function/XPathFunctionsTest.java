package com.artagon.xacml.v3.policy.impl.function;

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
import org.xml.sax.InputSource;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.FunctionInvocationException;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.XPathEvaluationException;
import com.artagon.xacml.v3.policy.spi.AnnotationBasedFunctionFactory;
import com.artagon.xacml.v3.policy.spi.FunctionFactory;
import com.artagon.xacml.v3.policy.spi.JDKXPathProvider;
import com.artagon.xacml.v3.policy.spi.XPathProvider;
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
	private XPathProvider xpathProvider;
	private FunctionFactory funcF;
	
	@Before
	public void init() throws Exception
	{
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setNamespaceAware(true);
		DocumentBuilder builder = f.newDocumentBuilder();
		this.context = createStrictMock(EvaluationContext.class);
		this.content = builder.parse(new InputSource(new StringReader(testXml)));
		this.xpathProvider = new JDKXPathProvider();
		this.funcF = new AnnotationBasedFunctionFactory(XPathFunctions.class);
	}
	
	@Test
	public void testValidateFunctions()
	{
		assertNotNull(funcF.getFunction("urn:oasis:names:tc:xacml:3.0:function:xpath-node-count"));
		assertNotNull(funcF.getFunction("urn:oasis:names:tc:xacml:3.0:function:xpath-node-match"));
		assertNotNull(funcF.getFunction("urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal"));
	}
	
	@Test
	public void testXPathCount() throws XPathEvaluationException, FunctionInvocationException
	{
		FunctionSpec f = funcF.getFunction("urn:oasis:names:tc:xacml:3.0:function:xpath-node-count");
		XPathExpressionValue xpath  = DataTypes.XPATHEXPRESSION.create("/md:record/md:patient", 
				AttributeCategoryId.SUBJECT_ACCESS);
		expect(context.isValidateFuncParamAtRuntime()).andReturn(true);
		expect(context.getContent(AttributeCategoryId.SUBJECT_ACCESS)).andReturn(content);
		expect(context.evaluateToNodeSet("/md:record/md:patient", content)).andAnswer(new XPathAnswer());
		replay(context);
		assertEquals(DataTypes.INTEGER.create(1), f.invoke(context, xpath));
		verify(context);
	}
	
	@Test
	public void testXPathCountExpressionReturnsEmptyNodeSet() throws XPathEvaluationException
	{
		XPathExpressionValue xpath  = DataTypes.XPATHEXPRESSION.create("/test", 
				AttributeCategoryId.SUBJECT_ACCESS);
		expect(context.getContent(AttributeCategoryId.SUBJECT_ACCESS)).andReturn(content);
		expect(context.evaluateToNodeSet("/test", content)).andAnswer(new XPathAnswer());
		replay(context);
		assertEquals(DataTypes.INTEGER.create(0), XPathFunctions.xpathCount(context, xpath));
		verify(context);
	}
	
	@Test
	public void testXPathCountContentNodeIsNull()
	{
		XPathExpressionValue xpath  = DataTypes.XPATHEXPRESSION.create(
				"/test", 
				AttributeCategoryId.SUBJECT_ACCESS);
		expect(context.getContent(AttributeCategoryId.SUBJECT_ACCESS)).andReturn(null);
		replay(context);
		assertEquals(DataTypes.INTEGER.create(0), XPathFunctions.xpathCount(context, xpath));
		verify(context);
	}
	
	@Test
	public void testXPathNodeMatch() throws XPathEvaluationException, FunctionInvocationException
	{
		FunctionSpec f = funcF.getFunction("urn:oasis:names:tc:xacml:3.0:function:xpath-node-match");
		XPathExpressionValue xpath0  = DataTypes.XPATHEXPRESSION.create("/md:record", AttributeCategoryId.SUBJECT_ACCESS);
		XPathExpressionValue xpath1  = DataTypes.XPATHEXPRESSION.create("/md:record/md:patient/md:patientDoB", AttributeCategoryId.SUBJECT_ACCESS);
		expect(context.isValidateFuncParamAtRuntime()).andReturn(true);
		expect(context.getContent(AttributeCategoryId.SUBJECT_ACCESS)).andReturn(content);
		expect(context.getContent(AttributeCategoryId.SUBJECT_ACCESS)).andReturn(content);
		expect(context.evaluateToNodeSet("/md:record", content)).andAnswer(new XPathAnswer());
		expect(context.evaluateToNodeSet("/md:record/md:patient/md:patientDoB", content)).andAnswer(new XPathAnswer());
		replay(context);
		assertEquals(DataTypes.BOOLEAN.create(true), f.invoke(context, xpath0, xpath1));
		verify(context);	
	}
	
	public class XPathAnswer implements IAnswer<NodeList>
	{
		@Override
		public NodeList answer() throws Throwable {
			Object[] args = EasyMock.getCurrentArguments();
			return xpathProvider.evaluateToNodeSet((String)args[0], (Node)args[1]);
		}
		
	}
}
