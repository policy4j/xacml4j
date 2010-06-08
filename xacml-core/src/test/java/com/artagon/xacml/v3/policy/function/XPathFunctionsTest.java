package com.artagon.xacml.v3.policy.function;

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
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.XPathVersion;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.spi.FunctionProvider;
import com.artagon.xacml.v3.policy.spi.XPathProvider;
import com.artagon.xacml.v3.policy.spi.function.ReflectionBasedFunctionProvider;
import com.artagon.xacml.v3.policy.spi.xpath.JDKXPathProvider;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.XPathExpressionType.XPathExpressionValue;

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
	
	@Before
	public void init() throws Exception
	{
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setNamespaceAware(true);
		DocumentBuilder builder = f.newDocumentBuilder();
		this.context = createStrictMock(EvaluationContext.class);
		this.content = builder.parse(new InputSource(new StringReader(testXml)));
		this.xpathProvider = new JDKXPathProvider();
		this.funcF = new ReflectionBasedFunctionProvider(XPathFunctions.class);
	}
	
	@Test
	public void testValidateFunctions()
	{
		assertNotNull(funcF.getFunction("urn:oasis:names:tc:xacml:3.0:function:xpath-node-count"));
		assertNotNull(funcF.getFunction("urn:oasis:names:tc:xacml:3.0:function:xpath-node-match"));
		assertNotNull(funcF.getFunction("urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal"));
	}
	
	@Test
	public void testXPathCount() throws EvaluationException
	{
		FunctionSpec f = funcF.getFunction("urn:oasis:names:tc:xacml:3.0:function:xpath-node-count");
		XPathExpressionValue xpath  = XacmlDataTypes.XPATHEXPRESSION.create("/md:record/md:patient", 
				AttributeCategoryId.SUBJECT_ACCESS);
		expect(context.isValidateFuncParamAtRuntime()).andReturn(true);
		expect(context.evaluateToNodeSet("/md:record/md:patient", AttributeCategoryId.SUBJECT_ACCESS))
		.andAnswer(new XPathAnswer());
		replay(context);
		assertEquals(XacmlDataTypes.INTEGER.create(1), f.invoke(context, xpath));
		verify(context);
	}
	
	@Test
	public void testXPathCountExpressionReturnsEmptyNodeSet() throws EvaluationException
	{
		XPathExpressionValue xpath  = XacmlDataTypes.XPATHEXPRESSION.create("/test", 
				AttributeCategoryId.SUBJECT_ACCESS);
		expect(context.evaluateToNodeSet("/test", AttributeCategoryId.SUBJECT_ACCESS)).andAnswer(new XPathAnswer());
		replay(context);
		assertEquals(XacmlDataTypes.INTEGER.create(0), XPathFunctions.xpathCount(context, xpath));
		verify(context);
	}
	
	@Test
	public void testXPathNodeMatch() throws EvaluationException
	{
		FunctionSpec f = funcF.getFunction("urn:oasis:names:tc:xacml:3.0:function:xpath-node-match");
		XPathExpressionValue xpath0  = XacmlDataTypes.XPATHEXPRESSION.create("/md:record", AttributeCategoryId.SUBJECT_ACCESS);
		XPathExpressionValue xpath1  = XacmlDataTypes.XPATHEXPRESSION.create("/md:record/md:patient/md:patientDoB", AttributeCategoryId.SUBJECT_ACCESS);
		expect(context.isValidateFuncParamAtRuntime()).andReturn(true);
		expect(context.evaluateToNodeSet("/md:record", AttributeCategoryId.SUBJECT_ACCESS)).andAnswer(new XPathAnswer());
		expect(context.evaluateToNodeSet("/md:record/md:patient/md:patientDoB", AttributeCategoryId.SUBJECT_ACCESS)).andAnswer(new XPathAnswer());
		replay(context);
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), f.invoke(context, xpath0, xpath1));
		verify(context);	
	}
	
	public class XPathAnswer implements IAnswer<NodeList>
	{
		@Override
		public NodeList answer() throws Throwable {
			Object[] args = EasyMock.getCurrentArguments();
			return xpathProvider.evaluateToNodeSet(
					XPathVersion.XPATH2,
					(String)args[0],
					content);
		}
		
	}
}
