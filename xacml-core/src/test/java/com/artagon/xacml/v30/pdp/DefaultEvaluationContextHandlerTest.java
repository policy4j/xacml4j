package com.artagon.xacml.v30.pdp;

import static com.artagon.xacml.v30.types.AnyURIType.ANYURI;
import static com.artagon.xacml.v30.types.DateType.DATE;
import static com.artagon.xacml.v30.types.IntegerType.INTEGER;
import static com.artagon.xacml.v30.types.XPathExpressionType.XPATHEXPRESSION;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.AttributeDesignatorKey;
import com.artagon.xacml.v30.AttributeReferenceEvaluationException;
import com.artagon.xacml.v30.AttributeSelectorKey;
import com.artagon.xacml.v30.BagOfAttributeValues;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationContextHandler;
import com.artagon.xacml.v30.EvaluationException;
import com.artagon.xacml.v30.Expression;
import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.ValueExpression;
import com.artagon.xacml.v30.XPathVersion;
import com.artagon.xacml.v30.spi.pip.PolicyInformationPoint;
import com.artagon.xacml.v30.spi.xpath.DefaultXPathProvider;
import com.artagon.xacml.v30.spi.xpath.XPathProvider;

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
	private RequestContext request;
	
	private Node content;
	
	private PolicyInformationPoint pip;
	private XPathProvider xpathProvider;
	private RequestContextCallback requestContextCallback;
	private EvaluationContextHandler handler;
	
	@Before
	public void init() throws Exception
	{
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setNamespaceAware(true);
		DocumentBuilder builder = f.newDocumentBuilder();
		this.context = createStrictMock(EvaluationContext.class);
		this.request = createStrictMock(RequestContext.class);
		this.requestContextCallback = createStrictMock(RequestContextCallback.class);
		this.pip = createStrictMock(PolicyInformationPoint.class);
		this.content = builder.parse(new InputSource(new StringReader(testXml)));
		this.xpathProvider = new DefaultXPathProvider();
		this.handler = new DefaultEvaluationContextHandler(requestContextCallback, xpathProvider, pip);
	}

	
	@Test
	public void testSelectorResolveContentIsInRequestXPathReturnsNonEmptySetResolutionScopeRequest() 
		throws EvaluationException
	{
		AttributeSelectorKey ref = new AttributeSelectorKey(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patient-number/text()", 
				INTEGER, null);
		
		expect(requestContextCallback.getContent(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(content);
		
		expect(requestContextCallback.getAttributeValue(ref.getCategory(), 
				"urn:oasis:names:tc:xacml:3.0:content-selector", XPATHEXPRESSION, null)).
				andReturn(XPATHEXPRESSION.emptyBag());
		
		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		replay(context, request, requestContextCallback, pip);
		
		Expression v = handler.resolve(context, ref);
		
		assertEquals(v, INTEGER.bagOf(INTEGER.create(555555)));
		verify(context, request, requestContextCallback, pip);
	}
	
	@Test
	public void testSelectorResolveContentIsNotInRequestXPathReturnsNonEmptyNodeSet() 
		throws Exception
	{
		AttributeSelectorKey ref = new AttributeSelectorKey(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patient-number/text()", 
				INTEGER, null);
		
		expect(requestContextCallback.getContent(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(null);
		
		expect(pip.resolve(context, AttributeCategories.SUBJECT_RECIPIENT)).andReturn(content);
		
		expect(requestContextCallback.getAttributeValue(ref.getCategory(), 
				"urn:oasis:names:tc:xacml:3.0:content-selector", XPATHEXPRESSION, null)).
				andReturn(XPATHEXPRESSION.emptyBag());
		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		
		replay(context, request, requestContextCallback, pip);
		
		Expression v = handler.resolve(context, ref);
		assertEquals(INTEGER.bagOf(INTEGER.create(555555)), v);
		verify(context, request, requestContextCallback, pip);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testSelectorResolveContentIsNotInRequestPIPCallsHandlerToResolveTheSameAttribute() 
		throws Exception
	{
		final AttributeSelectorKey ref = new AttributeSelectorKey(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patient-number/text()", 
				INTEGER, null);
		
		expect(requestContextCallback.getContent(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(null);
		
		expect(pip.resolve(context, AttributeCategories.SUBJECT_RECIPIENT)).andStubAnswer(new IAnswer<Node>() 
		{
			@Override
			public Node answer() throws Throwable {
				 handler.resolve(context, ref);
				 return content;
			}
		});
		
		replay(context, request, requestContextCallback, pip);
		
		Expression v = handler.resolve(context, ref);
		// test cache 
		v = handler.resolve(context, ref);
		assertEquals(INTEGER.bagOf(INTEGER.create(555555)), v);
		verify(context, request, requestContextCallback, pip);
	}
	
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testSelectorResolveContentIsInRequestXPathReturnsUnsupportedNodeType() throws EvaluationException
	{
		AttributeSelectorKey ref = new AttributeSelectorKey(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient", INTEGER, null);
		
		expect(requestContextCallback.getContent(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(content);
		
		expect(requestContextCallback.getAttributeValue(ref.getCategory(), 
				"urn:oasis:names:tc:xacml:3.0:content-selector", XPATHEXPRESSION, null)).
				andReturn(XPATHEXPRESSION.emptyBag());
		
		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		
		replay(context, request, requestContextCallback, pip);
		handler.resolve(context, ref);
		verify(context, request, requestContextCallback, pip);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testSelectorResolveContentIsInRequestXPathReturnsNonEmptySetAndNodeFailsConvertionToDate() throws EvaluationException
	{
		AttributeSelectorKey ref = new AttributeSelectorKey(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patient-number/text()", DATE, null);
		
		expect(requestContextCallback.getContent(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(content);
		
		expect(requestContextCallback.getAttributeValue(ref.getCategory(), 
				"urn:oasis:names:tc:xacml:3.0:content-selector", XPATHEXPRESSION, null)).
				andReturn(XPATHEXPRESSION.emptyBag());
		
		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		
		replay(context, request, requestContextCallback, pip);
		handler.resolve(context, ref);
		verify(context, request, requestContextCallback, pip);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testSelectorResolveContentIsInRequestXPathReturnsNonEmptySetAndCorrectNodeTypeAndNodeFailsConvertionToDate() throws EvaluationException
	{
		AttributeSelectorKey ref = new AttributeSelectorKey(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patient-number/text()", 
				DATE, null);
		
		expect(requestContextCallback.getContent(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(content);
		
		expect(requestContextCallback.getAttributeValue(ref.getCategory(), 
				"urn:oasis:names:tc:xacml:3.0:content-selector", XPATHEXPRESSION, null)).
				andReturn(XPATHEXPRESSION.emptyBag());
		
		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		
		replay(context, request, requestContextCallback, pip);
		handler.resolve(context, ref);
		verify(context, request, requestContextCallback, pip);
	}
	
	@Test
	public void testSelectorResolveContentInRequestExistXPathReturnsEmptySet() throws EvaluationException
	{
		AttributeSelectorKey ref = new AttributeSelectorKey(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/test", 
				INTEGER, null);
		
		expect(requestContextCallback.getContent(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(content);
		
		expect(requestContextCallback.getAttributeValue(ref.getCategory(), 
				"urn:oasis:names:tc:xacml:3.0:content-selector", XPATHEXPRESSION, null)).
				andReturn(XPATHEXPRESSION.emptyBag());
		
		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		
		replay(context, request, requestContextCallback, pip);
		Expression v = handler.resolve(context, ref);
		assertEquals(v, INTEGER.emptyBag());
		verify(context, request, requestContextCallback, pip);
	}		
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testSelectorResolveContentIsNotInRequestPIPThrowsRuntimeException() 
		throws Exception
	{
		AttributeSelectorKey ref = new AttributeSelectorKey(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patient-number/text()", 
				INTEGER, null);
		
		expect(requestContextCallback.getContent(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(null);
		
		expect(pip.resolve(context, AttributeCategories.SUBJECT_RECIPIENT)).andThrow(new RuntimeException());
		
		replay(context, request, requestContextCallback, pip);
		handler.resolve(context, ref);
		verify(context, request, requestContextCallback, pip);
	}
	
	
	@Test
	public void testDesignatorResolveAttributeIsInRequest() throws Exception
	{
		AttributeDesignatorKey ref = new AttributeDesignatorKey(
				AttributeCategories.RESOURCE, "testId", ANYURI, null);
		
		expect(requestContextCallback.getAttributeValue(
				AttributeCategories.RESOURCE, "testId", ANYURI, null)).
				andReturn(ANYURI.bagOf(ANYURI.create("testValue")));
		
		replay(context, request, pip, requestContextCallback);
		ValueExpression v = handler.resolve(context, ref);
		assertEquals(ANYURI.bagOf(ANYURI.create("testValue")), v);
		verify(context, request, pip, requestContextCallback);
	}
	
	@Test
	public void testDesignatorResolveAttributeIsNotInRequest() 
		throws Exception
	{
		AttributeDesignatorKey ref = new AttributeDesignatorKey(
				AttributeCategories.RESOURCE, "testId", ANYURI, null);
		
	
		expect(requestContextCallback.getAttributeValue(
				AttributeCategories.RESOURCE, "testId", ANYURI, null)).andReturn(ANYURI.emptyBag());
		

		expect(pip.resolve(context, ref)).andReturn(ANYURI.bagOf(ANYURI.create("testValue")));
	
		
		replay(context, request, pip, requestContextCallback);
		ValueExpression v = handler.resolve(context, ref);
		assertEquals(ANYURI.bagOf(ANYURI.create("testValue")), v);
		verify(context, request, pip, requestContextCallback);
	}
	
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testDesignatorResolveAttributeIsNotInRequestPIPThrowsRuntimeException() 
		throws Exception
	{
		AttributeDesignatorKey ref = new AttributeDesignatorKey(
				AttributeCategories.RESOURCE, "testId", ANYURI, null);
		
	
		expect(requestContextCallback.getAttributeValue(
				AttributeCategories.RESOURCE, "testId", ANYURI, null)).andReturn(ANYURI.emptyBag());
		

		expect(pip.resolve(context, ref)).andThrow(new RuntimeException());
		
		replay(context, request, pip, requestContextCallback);
		handler.resolve(context, ref);
		verify(context, request, pip, requestContextCallback);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testDesignatorResolveAttributeIsNotInRequestPIPCallsHandlerToResolveTheSameAttribute() 
		throws Exception
	{
		final AttributeDesignatorKey ref = new AttributeDesignatorKey(
				AttributeCategories.RESOURCE, "testId", ANYURI, null);
		
	
		expect(requestContextCallback.getAttributeValue(
				AttributeCategories.RESOURCE, "testId", ANYURI, null)).andReturn(ANYURI.emptyBag());
		

		expect(pip.resolve(context, ref)).andAnswer(new IAnswer<BagOfAttributeValues>() {
			public BagOfAttributeValues answer() throws Throwable{
				handler.resolve(context, ref);
				return ANYURI.emptyBag();
			}
		});
		
		expect(requestContextCallback.getAttributeValue(
				AttributeCategories.RESOURCE, "testId", ANYURI, null)).andReturn(ANYURI.emptyBag());
		
		replay(context, request, pip, requestContextCallback);
		handler.resolve(context, ref);
		verify(context, request, pip, requestContextCallback);
	}
}
