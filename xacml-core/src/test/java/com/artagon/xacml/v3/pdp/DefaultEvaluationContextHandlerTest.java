package com.artagon.xacml.v3.pdp;

import static com.artagon.xacml.v3.types.AnyURIType.ANYURI;
import static com.artagon.xacml.v3.types.DateType.DATE;
import static com.artagon.xacml.v3.types.IntegerType.INTEGER;
import static com.artagon.xacml.v3.types.XPathExpressionType.XPATHEXPRESSION;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.artagon.xacml.v3.AttributeCategories;
import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeReferenceEvaluationException;
import com.artagon.xacml.v3.AttributeSelector;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationContextHandler;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.RequestContextCallback;
import com.artagon.xacml.v3.ResolutionScope;
import com.artagon.xacml.v3.ValueExpression;
import com.artagon.xacml.v3.XPathVersion;
import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.artagon.xacml.v3.spi.XPathProvider;
import com.artagon.xacml.v3.spi.xpath.DefaultXPathProvider;

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
	public void testSelectorResolveContentIsInRequestXPathReturnsNonEmptySetResolutionScopeRequest() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patient-number/text()", 
				INTEGER, true);
		
		expect(requestContextCallback.getContent(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(content);
		
		expect(requestContextCallback.getAttributeValues(ref.getCategory(), 
				"urn:oasis:names:tc:xacml:3.0:content-selector", XPATHEXPRESSION)).
				andReturn(XPATHEXPRESSION.emptyBag());
		
		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		replay(context, request, requestContextCallback, pip);
		
		Expression v = handler.resolve(context, ref);
		// test cache 
		v = handler.resolve(context, ref);
		assertEquals(v, INTEGER.bagOf(INTEGER.create(555555)));
		verify(context, request, requestContextCallback, pip);
	}
	
	@Test
	public void testSelectorResolveContentIsNotInRequestXPathReturnsNonEmptySetResolutionScopeRequest() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patient-number/text()", 
				INTEGER, true);
		
		expect(requestContextCallback.getContent(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(null);
		
		expect(context.getResolutionScope()).andReturn(ResolutionScope.REQUEST);
		replay(context, request, requestContextCallback, pip);
		
		Expression v = handler.resolve(context, ref);
		// test cache 
		v = handler.resolve(context, ref);
		assertEquals(v, INTEGER.emptyBag());
		verify(context, request, requestContextCallback, pip);
	}
	
	@Test
	public void testSelectorResolveContentIsInNotRequestXPathReturnsNonEmptySetResolutionScopeRequestExternal() 
		throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patient-number/text()", 
				INTEGER, true);
		
		expect(requestContextCallback.getContent(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(null);
		
		expect(requestContextCallback.getAttributeValues(ref.getCategory(), 
				"urn:oasis:names:tc:xacml:3.0:content-selector", XPATHEXPRESSION)).
				andReturn(XPATHEXPRESSION.emptyBag());
		
		expect(context.getResolutionScope()).andReturn(ResolutionScope.REQUEST_EXTERNAL);
		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		expect(pip.resolve(context, AttributeCategories.SUBJECT_RECIPIENT, requestContextCallback)).andReturn(content);
		replay(context, request, requestContextCallback, pip);
		
		Expression v = handler.resolve(context, ref);
		// test cache 
		v = handler.resolve(context, ref);
		assertEquals(v, INTEGER.bagOf(INTEGER.create(555555)));
		verify(context, request, requestContextCallback, pip);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testSelectorResolveContentIsInRequestXPathReturnsUnsupportedNodeType() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient", 
				INTEGER, true);
		
		expect(requestContextCallback.getContent(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(content);
		
		expect(requestContextCallback.getAttributeValues(ref.getCategory(), 
				"urn:oasis:names:tc:xacml:3.0:content-selector", XPATHEXPRESSION)).
				andReturn(XPATHEXPRESSION.emptyBag());
		
		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		
		replay(context, request, requestContextCallback, pip);
		handler.resolve(context, ref);
		verify(context, request, requestContextCallback, pip);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testSelectorResolveContentIsInRequestXPathReturnsNonEmptySetAndNodeFailsConvertionToDate() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patient-number/text()", 
				DATE, false);
		
		expect(requestContextCallback.getContent(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(content);
		
		expect(requestContextCallback.getAttributeValues(ref.getCategory(), 
				"urn:oasis:names:tc:xacml:3.0:content-selector", XPATHEXPRESSION)).
				andReturn(XPATHEXPRESSION.emptyBag());
		
		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		
		replay(context, request, requestContextCallback, pip);
		handler.resolve(context, ref);
		verify(context, request, requestContextCallback, pip);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testSelectorResolveContentIsInRequestXPathReturnsNonEmptySetAndCorrectNodeTypeAndNodeFailsConvertionToDate() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patient-number/text()", 
				DATE, false);
		
		expect(requestContextCallback.getContent(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(content);
		
		expect(requestContextCallback.getAttributeValues(ref.getCategory(), 
				"urn:oasis:names:tc:xacml:3.0:content-selector", XPATHEXPRESSION)).
				andReturn(XPATHEXPRESSION.emptyBag());
		
		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		
		replay(context, request, requestContextCallback, pip);
		handler.resolve(context, ref);
		verify(context, request, requestContextCallback, pip);
	}
	
	@Test
	public void testSelectorResolveContentInRequestExistXPathReturnsEmptySet() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/test", 
				INTEGER, true);
		
		expect(requestContextCallback.getContent(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(content);
		
		expect(requestContextCallback.getAttributeValues(ref.getCategory(), 
				"urn:oasis:names:tc:xacml:3.0:content-selector", XPATHEXPRESSION)).
				andReturn(XPATHEXPRESSION.emptyBag());
		
		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		
		replay(context, request, requestContextCallback, pip);
		Expression v = handler.resolve(context, ref);
		assertEquals(v, INTEGER.emptyBag());
		verify(context, request, requestContextCallback, pip);
	}		
	
	@Test
	public void testDesignatorResolveAttributeIsInRequest() throws EvaluationException
	{
		AttributeDesignator ref = new AttributeDesignator(
				AttributeCategories.RESOURCE, "testId", null, ANYURI, false);
		
		expect(requestContextCallback.getAttributeValues(
				AttributeCategories.RESOURCE, "testId", ANYURI, null)).
				andReturn(ANYURI.bagOf(ANYURI.create("testValue")));
		
		replay(context, request, pip, requestContextCallback);
		ValueExpression v = handler.resolve(context, ref);
		assertEquals(ANYURI.bagOf(ANYURI.create("testValue")), v);
		verify(context, request, pip, requestContextCallback);
	}
	
	@Test
	public void testDesignatorResolveAttributeIsNotInRequestAndScopeIsRequestAndExternal() throws EvaluationException
	{
		AttributeDesignator ref = new AttributeDesignator(
				AttributeCategories.RESOURCE, "testId", null, ANYURI, false);
		
	
		expect(requestContextCallback.getAttributeValues(
				AttributeCategories.RESOURCE, "testId", ANYURI, null)).andReturn(ANYURI.emptyBag());
		
		expect(context.getResolutionScope()).andReturn(ResolutionScope.REQUEST_EXTERNAL);
	
		Capture<RequestContextCallback> c = new Capture<RequestContextCallback>();
		expect(pip.resolve(same(context), same(ref), capture(c))).andReturn(ANYURI.bagOf(ANYURI.create("testValue")));
		expect(requestContextCallback.getAttributeValues(
				AttributeCategories.RESOURCE, "testId", ANYURI, null)).andReturn(ANYURI.emptyBag());
		
		expect(context.getResolutionScope()).andReturn(ResolutionScope.REQUEST_EXTERNAL);
		
		replay(context, request, pip, requestContextCallback);
		ValueExpression v = handler.resolve(context, ref);
		// test if designator resolution result is cached
		v = handler.resolve(context, ref);
		assertEquals(ANYURI.bagOf(ANYURI.create("testValue")), v);
		verify(context, request, pip, requestContextCallback);
	}
	
	@Test
	public void testDesignatorResolveAttributeIsNotInRequestAndScopeIsRequest() throws EvaluationException
	{
		AttributeDesignator ref = new AttributeDesignator(
				AttributeCategories.RESOURCE, "testId", null, ANYURI, false);
		

		expect(requestContextCallback.getAttributeValues(
				AttributeCategories.RESOURCE, "testId", ANYURI, null)).
				andReturn(ANYURI.emptyBag());
		
		expect(context.getResolutionScope()).andReturn(ResolutionScope.REQUEST);
		
		replay(context, request, requestContextCallback, pip);

		ValueExpression v = handler.resolve(context, ref);
		assertEquals(ANYURI.emptyBag(), v);
		verify(context, request, requestContextCallback, pip);
	}
}
