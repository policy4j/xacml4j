package org.xacml4j.v30.policy.function;

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
import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.XPathVersion;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.policy.function.XPathFunctions;
import org.xacml4j.v30.spi.function.AnnotiationBasedFunctionProvider;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.spi.xpath.DefaultXPathProvider;
import org.xacml4j.v30.spi.xpath.XPathProvider;
import org.xacml4j.v30.types.BooleanType;
import org.xacml4j.v30.types.IntegerType;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.StringType;
import org.xacml4j.v30.types.XPathExp;
import org.xacml4j.v30.types.XPathExpType;
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
		XPathExp xpath  = XPathExpType.XPATHEXPRESSION.create("/md:record/md:patient", 
				AttributeCategories.SUBJECT_ACCESS);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(context.evaluateToNodeSet("/md:record/md:patient", AttributeCategories.SUBJECT_ACCESS))
		.andAnswer(new XPathAnswer(content));
		replay(context);
		assertEquals(IntegerType.INTEGER.create(1), f.invoke(context, xpath));
		verify(context);
	}
	
	@Test
	public void testXPathCountXacml20() throws EvaluationException
	{
		FunctionSpec f = funcF.getFunction("urn:oasis:names:tc:xacml:1.0:function:xpath-node-count");
		StringExp xpath  = StringType.STRING.create("./xacml-context:Resource/xacml-context:ResourceContent/md:record//md:name");
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(context.evaluateToNodeSet("./md:record//md:name", AttributeCategories.RESOURCE))
		.andAnswer(new XPathAnswer(content1));
		replay(context);
		assertEquals(IntegerType.INTEGER.create(2), f.invoke(context, xpath));
		verify(context);
	}
	
	@Test
	public void testXPathCountExpressionReturnsEmptyNodeSet() throws EvaluationException
	{
		XPathExp xpath  = XPathExpType.XPATHEXPRESSION.create("/test", 
				AttributeCategories.SUBJECT_ACCESS);
		expect(context.evaluateToNodeSet("/test", AttributeCategories.SUBJECT_ACCESS)).andAnswer(new XPathAnswer(content));
		replay(context);
		assertEquals(IntegerType.INTEGER.create(0), XPathFunctions.xpathCount(context, xpath));
		verify(context);
	}
	
	@Test
	public void testXPathNodeMatch() throws EvaluationException
	{
		FunctionSpec f = funcF.getFunction("urn:oasis:names:tc:xacml:3.0:function:xpath-node-match");
		XPathExp xpath0  = XPathExpType.XPATHEXPRESSION.create("/md:record", AttributeCategories.SUBJECT_ACCESS);
		XPathExp xpath1  = XPathExpType.XPATHEXPRESSION.create("/md:record/md:patient/md:patientDoB", AttributeCategories.SUBJECT_ACCESS);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(context.evaluateToNodeSet("/md:record", AttributeCategories.SUBJECT_ACCESS)).andAnswer(new XPathAnswer(content));
		expect(context.evaluateToNodeSet("/md:record/md:patient/md:patientDoB", AttributeCategories.SUBJECT_ACCESS)).andAnswer(new XPathAnswer(content));
		replay(context);
		assertEquals(BooleanType.BOOLEAN.create(true), f.invoke(context, xpath0, xpath1));
		verify(context);	
	}
	
	@Test
	public void testXPathNodeMatchXacml20() throws EvaluationException
	{
		FunctionSpec f = funcF.getFunction("urn:oasis:names:tc:xacml:1.0:function:xpath-node-match");
		StringExp xpath0  = StringType.STRING.create(".");
		StringExp xpath1  = StringType.STRING.create("./xacml-context:Resource/xacml-context:ResourceContent/md:record");
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(context.evaluateToNodeSet(".", AttributeCategories.RESOURCE)).andAnswer(new XPathAnswer(content1));
		expect(context.evaluateToNodeSet("./md:record", AttributeCategories.RESOURCE)).andAnswer(new XPathAnswer(content1));
		replay(context);
		assertEquals(BooleanType.BOOLEAN.create(true), f.invoke(context, xpath0, xpath1));
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
