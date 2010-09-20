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
import com.artagon.xacml.v3.FunctionSpec;
import com.artagon.xacml.v3.XPathVersion;
import com.artagon.xacml.v3.spi.FunctionProvider;
import com.artagon.xacml.v3.spi.XPathProvider;
import com.artagon.xacml.v3.spi.function.AnnotiationBasedFunctionProvider;
import com.artagon.xacml.v3.spi.xpath.DefaultXPathProvider;
import com.artagon.xacml.v3.types.StringType;
import com.artagon.xacml.v3.types.StringType.StringValue;
import com.artagon.xacml.v3.types.XPathExpressionType;
import com.artagon.xacml.v3.types.XPathExpressionType.XPathExpressionValue;
import com.artagon.xacml.v3.types.XacmlDataTypes;

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
		this.funcF = new AnnotiationBasedFunctionProvider(XPathFunctions.class);
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
		NodeList result = xpathProvider.evaluateToNodeSet(XPathVersion.XPATH1, "./md:record", content1); 
		NodeList result1 = xpathProvider.evaluateToNodeSet(XPathVersion.XPATH1, "./md:record//md:name", content1);
		NodeList result2 = xpathProvider.evaluateToNodeSet(XPathVersion.XPATH1, "//*[local-name()='record'][namespace-uri()='urn:example:med:schemas:record']", content);
		assertEquals(1, result.getLength());
		assertEquals(2, result1.getLength());
		assertEquals(1, result2.getLength());
	}
	
	@Test
	public void testXPathCount() throws EvaluationException
	{
		FunctionSpec f = funcF.getFunction("urn:oasis:names:tc:xacml:3.0:function:xpath-node-count");
		XPathExpressionValue xpath  = XPathExpressionType.Factory.create("/md:record/md:patient", 
				AttributeCategoryId.SUBJECT_ACCESS);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(context.evaluateToNodeSet("/md:record/md:patient", AttributeCategoryId.SUBJECT_ACCESS))
		.andAnswer(new XPathAnswer(content));
		replay(context);
		assertEquals(XacmlDataTypes.INTEGER.create(1), f.invoke(context, xpath));
		verify(context);
	}
	
	@Test
	public void testXPathCountXacml20() throws EvaluationException
	{
		FunctionSpec f = funcF.getFunction("urn:oasis:names:tc:xacml:1.0:function:xpath-node-count");
		StringValue xpath  = StringType.Factory.create("./xacml-context:Resource/xacml-context:ResourceContent/md:record//md:name");
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(context.evaluateToNodeSet("./md:record//md:name", AttributeCategoryId.RESOURCE))
		.andAnswer(new XPathAnswer(content1));
		replay(context);
		assertEquals(XacmlDataTypes.INTEGER.create(2), f.invoke(context, xpath));
		verify(context);
	}
	
	@Test
	public void testXPathCountExpressionReturnsEmptyNodeSet() throws EvaluationException
	{
		XPathExpressionValue xpath  = XPathExpressionType.Factory.create("/test", 
				AttributeCategoryId.SUBJECT_ACCESS);
		expect(context.evaluateToNodeSet("/test", AttributeCategoryId.SUBJECT_ACCESS)).andAnswer(new XPathAnswer(content));
		replay(context);
		assertEquals(XacmlDataTypes.INTEGER.create(0), XPathFunctions.xpathCount(context, xpath));
		verify(context);
	}
	
	@Test
	public void testXPathNodeMatch() throws EvaluationException
	{
		FunctionSpec f = funcF.getFunction("urn:oasis:names:tc:xacml:3.0:function:xpath-node-match");
		XPathExpressionValue xpath0  = XPathExpressionType.Factory.create("/md:record", AttributeCategoryId.SUBJECT_ACCESS);
		XPathExpressionValue xpath1  = XPathExpressionType.Factory.create("/md:record/md:patient/md:patientDoB", AttributeCategoryId.SUBJECT_ACCESS);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(context.evaluateToNodeSet("/md:record", AttributeCategoryId.SUBJECT_ACCESS)).andAnswer(new XPathAnswer(content));
		expect(context.evaluateToNodeSet("/md:record/md:patient/md:patientDoB", AttributeCategoryId.SUBJECT_ACCESS)).andAnswer(new XPathAnswer(content));
		replay(context);
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), f.invoke(context, xpath0, xpath1));
		verify(context);	
	}
	
	@Test
	public void testXPathNodeMatchXacml20() throws EvaluationException
	{
		FunctionSpec f = funcF.getFunction("urn:oasis:names:tc:xacml:1.0:function:xpath-node-match");
		StringValue xpath0  = StringType.Factory.create(".");
		StringValue xpath1  = StringType.Factory.create("./xacml-context:Resource/xacml-context:ResourceContent/md:record");
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(context.evaluateToNodeSet(".", AttributeCategoryId.RESOURCE)).andAnswer(new XPathAnswer(content1));
		expect(context.evaluateToNodeSet("./md:record", AttributeCategoryId.RESOURCE)).andAnswer(new XPathAnswer(content1));
		replay(context);
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), f.invoke(context, xpath0, xpath1));
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
					XPathVersion.XPATH1,
					(String)args[0],
					xml);
		}
		
	}
}
