package com.artagon.xacml.v3.pdp;

import static com.artagon.xacml.v3.types.AnyURIType.ANYURI;
import static com.artagon.xacml.v3.types.DateType.DATE;
import static com.artagon.xacml.v3.types.IntegerType.INTEGER;
import static com.artagon.xacml.v3.types.XPathExpressionType.XPATHEXPRESSION;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.io.StringReader;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.artagon.xacml.v3.AttributeCategories;
import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeReferenceEvaluationException;
import com.artagon.xacml.v3.AttributeResolutionScope;
import com.artagon.xacml.v3.AttributeSelector;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationContextHandler;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.RequestContextAttributesCallback;
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
	private RequestContextAttributesCallback requestContextCallback;
	
	@Before
	public void init() throws Exception
	{
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setNamespaceAware(true);
		DocumentBuilder builder = f.newDocumentBuilder();
		this.context = createStrictMock(EvaluationContext.class);
		this.request = createStrictMock(RequestContext.class);
		this.requestContextCallback = createStrictMock(RequestContextAttributesCallback.class);
		this.pip = createStrictMock(PolicyInformationPoint.class);
		this.content = builder.parse(new InputSource(new StringReader(testXml)));
		this.xpathProvider = new DefaultXPathProvider();
	}
	
	@Test
	public void testGetContentWithCategoryContentIsInRequest()
	{
		Attributes attributes = createStrictMock(Attributes.class);
		Node content1 = createStrictMock(Node.class);
		expect(context.getRequestContextCallback()).andReturn(requestContextCallback);
		expect(requestContextCallback.getContent(AttributeCategories.RESOURCE)).andReturn(content1);
		replay(context, request, attributes, pip, requestContextCallback);
		EvaluationContextHandler handler = new DefaultEvaluationContextHandler(xpathProvider, pip);
		Node content2 = handler.getContent(context, AttributeCategories.RESOURCE);
		assertSame(content1, content2);
		verify(context, request, attributes, pip, requestContextCallback);
	}
	
	@Test
	public void testGetContentWithCategoryContentIsNotInRequestResolutionScopeRequest()
	{
		Attributes attributes = createStrictMock(Attributes.class);
		expect(context.getRequestContextCallback()).andReturn(requestContextCallback);
		expect(requestContextCallback.getContent(AttributeCategories.RESOURCE)).andReturn(null);
		expect(context.getAttributeResolutionScope()).andReturn(AttributeResolutionScope.REQUEST);
		replay(context, request, attributes, pip, requestContextCallback);
		EvaluationContextHandler handler = new DefaultEvaluationContextHandler(xpathProvider, pip);
		Node content2 = handler.getContent(context, AttributeCategories.RESOURCE);
		assertNull(content2);
		verify(context, request, attributes, pip, requestContextCallback);
	}
	
	@Test
	public void testGetContentWithCategoryContentIsNotInRequestResolutionScopeRequestExternal()
	{
		Attributes attributes = createStrictMock(Attributes.class);
		expect(context.getRequestContextCallback()).andReturn(requestContextCallback);
		expect(requestContextCallback.getContent(AttributeCategories.RESOURCE)).andReturn(null);
		expect(context.getAttributeResolutionScope()).andReturn(AttributeResolutionScope.REQUEST_EXTERNAL);
		expect(context.getRequestContextCallback()).andReturn(requestContextCallback);
		Capture<RequestContextAttributesCallback> c = new Capture<RequestContextAttributesCallback>();
		expect(pip.resolve(eq(context), eq(AttributeCategories.RESOURCE), capture(c))).andReturn(null);
		replay(context, request, attributes, pip, requestContextCallback);
		EvaluationContextHandler handler = new DefaultEvaluationContextHandler(xpathProvider, pip);
		Node content2 = handler.getContent(context, AttributeCategories.RESOURCE);
		assertNull(content2);
		verify(context, request, attributes, pip, requestContextCallback);
	}
	
	
	@Test
	public void testResolveSelectorWithCorrectNodeTypeAndNodeCanBeConvertedToInteger() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patient-number/text()", 
				INTEGER, true);
		
		expect(context.getRequestContextCallback()).andReturn(requestContextCallback);
		expect(requestContextCallback.getContent(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(content);
		
		expect(requestContextCallback.getAttributeValues(ref.getCategory(), 
				DefaultEvaluationContextHandler.CONTENT_SELECTOR , XPATHEXPRESSION)).
				andReturn(XPATHEXPRESSION.emptyBag());
		
		expect(context.getRequestContextCallback()).andReturn(requestContextCallback);
		
		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		replay(context, request, requestContextCallback, pip);
		EvaluationContextHandler handler = new DefaultEvaluationContextHandler(xpathProvider, pip);
		Expression v = handler.resolve(context, ref);
		assertEquals(v, INTEGER.bagOf(INTEGER.create(555555)));
		verify(context, request, requestContextCallback, pip);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testResolveSelectorWithXPathReturnsUnsupportedNodeType() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient", 
				INTEGER, true);
		
		expect(context.getRequestContextCallback()).andReturn(requestContextCallback);
		expect(requestContextCallback.getContent(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(content);
		
		expect(requestContextCallback.getAttributeValues(ref.getCategory(), 
				DefaultEvaluationContextHandler.CONTENT_SELECTOR , XPATHEXPRESSION)).
				andReturn(XPATHEXPRESSION.emptyBag());
		
		expect(context.getRequestContextCallback()).andReturn(requestContextCallback);
		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		replay(context, request, requestContextCallback, pip);
		EvaluationContextHandler handler = new DefaultEvaluationContextHandler(xpathProvider, pip);
		handler.resolve(context, ref);
		verify(context, request, requestContextCallback, pip);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testResolveSelectorMustBePresentFalseWithCorrectNodeTypeAndNodeFailsConvertionToDate() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patient-number/text()", 
				DATE, false);
		
		expect(context.getRequestContextCallback()).andReturn(requestContextCallback);
		expect(requestContextCallback.getContent(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(content);
		
		expect(requestContextCallback.getAttributeValues(ref.getCategory(), 
				DefaultEvaluationContextHandler.CONTENT_SELECTOR , XPATHEXPRESSION)).
				andReturn(XPATHEXPRESSION.emptyBag());
		
		expect(context.getRequestContextCallback()).andReturn(requestContextCallback);
		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		
		replay(context, request, requestContextCallback, pip);
		EvaluationContextHandler handler = new DefaultEvaluationContextHandler(xpathProvider, pip);
		handler.resolve(context, ref);
		verify(context, request, requestContextCallback, pip);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testResolveSelectorMustBePresentTrueWithCorrectNodeTypeAndNodeFailsConvertionToDate() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patient-number/text()", 
				DATE, false);
		
		expect(context.getRequestContextCallback()).andReturn(requestContextCallback);
		expect(requestContextCallback.getContent(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(content);
		
		expect(requestContextCallback.getAttributeValues(ref.getCategory(), 
				DefaultEvaluationContextHandler.CONTENT_SELECTOR , XPATHEXPRESSION)).
				andReturn(XPATHEXPRESSION.emptyBag());
		
		expect(context.getRequestContextCallback()).andReturn(requestContextCallback);
		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		
		replay(context, request, requestContextCallback, pip);
		EvaluationContextHandler handler = new DefaultEvaluationContextHandler(xpathProvider, pip);
		handler.resolve(context, ref);
		verify(context, request, requestContextCallback, pip);
	}
	
	@Test
	public void testMustBePresentFalseCategoryContentExistXPathReturnsEmptySet() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategories.SUBJECT_RECIPIENT, 
				"/test", 
				INTEGER, true);
		
		expect(context.getRequestContextCallback()).andReturn(requestContextCallback);
		expect(requestContextCallback.getContent(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(content);
		
		expect(requestContextCallback.getAttributeValues(ref.getCategory(), 
				DefaultEvaluationContextHandler.CONTENT_SELECTOR , XPATHEXPRESSION)).
				andReturn(XPATHEXPRESSION.emptyBag());
		
		expect(context.getRequestContextCallback()).andReturn(requestContextCallback);
		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		
		replay(context, request, requestContextCallback, pip);
		EvaluationContextHandler handler = new DefaultEvaluationContextHandler(xpathProvider, pip);
		Expression v = handler.resolve(context, ref);
		assertEquals(v, INTEGER.emptyBag());
		verify(context, request, requestContextCallback, pip);
	}		
	
	@Test
	public void testDesignatorResolutionAttributeInRequest() throws EvaluationException
	{
		AttributeDesignator ref = new AttributeDesignator(
				AttributeCategories.RESOURCE, "testId", null, ANYURI, false);
		
		expect(context.getRequestContextCallback()).andReturn(requestContextCallback);
		expect(requestContextCallback.getAttributeValues(
				AttributeCategories.RESOURCE, "testId", ANYURI, null)).
				andReturn(ANYURI.bagOf(ANYURI.create("testValue")));
		
		replay(context, request, pip, requestContextCallback);
		EvaluationContextHandler h = new DefaultEvaluationContextHandler(xpathProvider, pip);
		ValueExpression v = h.resolve(context, ref);
		assertEquals(ANYURI.bagOf(ANYURI.create("testValue")), v);
		verify(context, request, pip, requestContextCallback);
	}
	
	@Test
	public void testDesignatorResolutionAttributeIsNotInRequestAndScopeIsRequestAndExternal() throws EvaluationException
	{
		AttributeDesignator ref = new AttributeDesignator(
				AttributeCategories.RESOURCE, "testId", null, ANYURI, false);
		
		expect(context.getRequestContextCallback()).andReturn(requestContextCallback);
		expect(requestContextCallback.getAttributeValues(AttributeCategories.RESOURCE, "testId", ANYURI, null)).
				andReturn(ANYURI.emptyBag());
		
		expect(context.getAttributeResolutionScope()).andReturn(AttributeResolutionScope.REQUEST_EXTERNAL);
		expect(context.getRequestContextCallback()).andReturn(requestContextCallback);
		Capture<RequestContextAttributesCallback> c = new Capture<RequestContextAttributesCallback>();
		expect(pip.resolve(same(context), same(ref), capture(c))).andReturn(ANYURI.bagOf(ANYURI.create("testValue")));
		replay(context, request, pip, requestContextCallback);
		EvaluationContextHandler h = new DefaultEvaluationContextHandler(xpathProvider, pip);
		ValueExpression v = h.resolve(context, ref);
		assertEquals(ANYURI.bagOf(ANYURI.create("testValue")), v);
		verify(context, request, pip, requestContextCallback);
	}
	
	@Test
	public void testDesignatorResolutionAttributeIsNotInRequestAndScopeIsRequest() throws EvaluationException
	{
		AttributeDesignator ref = new AttributeDesignator(
				AttributeCategories.RESOURCE, "testId", null, ANYURI, false);
		
		expect(context.getRequestContextCallback()).andReturn(requestContextCallback);
		expect(requestContextCallback.getAttributeValues(
				AttributeCategories.RESOURCE, "testId", ANYURI, null)).
				andReturn(ANYURI.emptyBag());
		
		expect(context.getAttributeResolutionScope()).andReturn(AttributeResolutionScope.REQUEST);
		
		replay(context, request, requestContextCallback, pip);
		EvaluationContextHandler h = new DefaultEvaluationContextHandler(xpathProvider, pip);
		ValueExpression v = h.resolve(context, ref);
		assertEquals(ANYURI.emptyBag(), v);
		verify(context, request, requestContextCallback, pip);
	}
	
	@Test
	@Ignore
	public void testAttributeReferenceResolutionCache() throws Exception
	{
		AttributeDesignator ref = new AttributeDesignator(
				AttributeCategories.RESOURCE, "testId", null, ANYURI, false);
		
		expect(request.hasRepeatingCategories()).andReturn(false);
		expect(request.getAttributeValues(AttributeCategories.RESOURCE, "testId", null, ANYURI)).
		andReturn(Collections.<AttributeValue>emptyList());
		expect(context.getAttributeResolutionScope()).andReturn(AttributeResolutionScope.REQUEST_EXTERNAL);
		
		Capture<RequestContextAttributesCallback> c = new Capture<RequestContextAttributesCallback>();
		expect(pip.resolve(same(context), same(ref), capture(c))).andReturn(ANYURI.bagOf(ANYURI.create("testValue")));
		
		expect(request.hasRepeatingCategories()).andReturn(false);
		expect(request.getAttributeValues(AttributeCategories.RESOURCE, "testId", null, ANYURI)).
		andReturn(Collections.<AttributeValue>emptyList());
		//expect(context.getAttributeResolutionScope()).andReturn(AttributeResolutionScope.REQUEST_EXTERNAL);
		
		replay(context, request, pip);
		EvaluationContextHandler h = new DefaultEvaluationContextHandler(xpathProvider, pip);
		ValueExpression v = h.resolve(context, ref);
		assertEquals(ANYURI.bagOf(ANYURI.create("testValue")), v);
		v = h.resolve(context, ref);
		assertEquals(ANYURI.bagOf(ANYURI.create("testValue")), v);
		verify(context, request, pip);

	}
}
