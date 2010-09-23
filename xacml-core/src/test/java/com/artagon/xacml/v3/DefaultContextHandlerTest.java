package com.artagon.xacml.v3;

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
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.artagon.xacml.v3.spi.XPathProvider;
import com.artagon.xacml.v3.spi.xpath.DefaultXPathProvider;
import com.artagon.xacml.v3.types.AnyURIType;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.XacmlDataTypes;

public class DefaultContextHandlerTest
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
	
	@Before
	public void init() throws Exception
	{
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setNamespaceAware(true);
		DocumentBuilder builder = f.newDocumentBuilder();
		this.context = createStrictMock(EvaluationContext.class);
		this.request = createStrictMock(RequestContext.class);
		this.pip = createStrictMock(PolicyInformationPoint.class);
		this.content = builder.parse(new InputSource(new StringReader(testXml)));
		this.xpathProvider = new DefaultXPathProvider();
	}
	
	@Test
	public void testGetContentWithCategoryContentIsInRequest()
	{
		Attributes attributes = createStrictMock(Attributes.class);
		Node content1 = createStrictMock(Node.class);
		expect(request.hasRepeatingCategories()).andReturn(false);
		expect(request.getOnlyAttributes(AttributeCategoryId.RESOURCE)).andReturn(attributes);
		expect(attributes.getContent()).andReturn(content1).times(2);
		replay(context, request, attributes, pip);
		EvaluationContextHandler handler = new DefaultContextHandler(xpathProvider, request, pip);
		Node content2 = handler.getContent(context, AttributeCategoryId.RESOURCE);
		assertSame(content1, content2);
		verify(context, request, attributes, pip);
	}
	
	@Test
	public void testGetContentWithCategoryContentIsNotInRequestResolutionScopeRequest()
	{
		Attributes attributes = createStrictMock(Attributes.class);
		expect(request.hasRepeatingCategories()).andReturn(false);
		expect(request.getOnlyAttributes(AttributeCategoryId.RESOURCE)).andReturn(attributes);
		expect(attributes.getContent()).andReturn(null).times(2);
		expect(context.getAttributeResolutionScope()).andReturn(AttributeResolutionScope.REQUEST);
		replay(context, request, attributes, pip);
		EvaluationContextHandler handler = new DefaultContextHandler(xpathProvider, request, pip);
		Node content2 = handler.getContent(context, AttributeCategoryId.RESOURCE);
		assertNull(content2);
		verify(context, request, attributes, pip);
	}
	
	@Test
	public void testGetContentWithCategoryContentIsNotInRequestResolutionScopeRequestExternal()
	{
		Attributes attributes = createStrictMock(Attributes.class);
		expect(request.hasRepeatingCategories()).andReturn(false);
		expect(request.getOnlyAttributes(AttributeCategoryId.RESOURCE)).andReturn(attributes);
		expect(attributes.getContent()).andReturn(null);
		expect(context.getAttributeResolutionScope()).andReturn(AttributeResolutionScope.REQUEST_EXTERNAL);
		Capture<RequestContextAttributesCallback> c = new Capture<RequestContextAttributesCallback>();
		expect(pip.resolve(eq(context), eq(AttributeCategoryId.RESOURCE), capture(c))).andReturn(null);
		replay(context, request, attributes, pip);
		EvaluationContextHandler handler = new DefaultContextHandler(xpathProvider, request, pip);
		Node content2 = handler.getContent(context, AttributeCategoryId.RESOURCE);
		assertNull(content2);
		verify(context, request, attributes, pip);
	}
	
	
	@Test
	public void testResolveSelectorWithCorrectNodeTypeAndNodeCanBeConvertedToInteger() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patient-number/text()", 
				XacmlDataTypes.INTEGER.getDataType(), true);
		
		Attributes attributes = createStrictMock(Attributes.class);
		expect(request.hasRepeatingCategories()).andReturn(false);
		expect(request.getOnlyAttributes(AttributeCategoryId.SUBJECT_RECIPIENT)).andReturn(attributes);
		expect(attributes.getContent()).andReturn(content).times(2);
		expect(request.getAttributeValues(ref.getCategory(), 
				DefaultContextHandler.CONTENT_SELECTOR , XacmlDataTypes.XPATHEXPRESSION.getDataType())).
				andReturn(Collections.<AttributeValue>emptyList());
		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		replay(context, request, attributes, pip);
		EvaluationContextHandler handler = new DefaultContextHandler(xpathProvider, request, pip);
		Expression v = handler.resolve(context, ref);
		assertEquals(v, IntegerType.Factory.bagOf(IntegerType.Factory.create(555555)));
		verify(context, request, attributes, pip);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testResolveSelectorWithXPathReturnsUnsupportedNodeType() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, 
				"/md:record/md:patient", 
				XacmlDataTypes.INTEGER.getDataType(), true);
		
		Attributes attributes = createStrictMock(Attributes.class);
		expect(request.hasRepeatingCategories()).andReturn(false);
		expect(request.getOnlyAttributes(AttributeCategoryId.SUBJECT_RECIPIENT)).andReturn(attributes);
		expect(attributes.getContent()).andReturn(content).times(2);
		expect(request.getAttributeValues(ref.getCategory(), 
				DefaultContextHandler.CONTENT_SELECTOR , XacmlDataTypes.XPATHEXPRESSION.getDataType())).
				andReturn(Collections.<AttributeValue>emptyList());
		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		replay(context, request, attributes, pip);
		EvaluationContextHandler handler = new DefaultContextHandler(xpathProvider, request, pip);
		handler.resolve(context, ref);
		verify(context, request, attributes, pip);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testResolveSelectorMustBePresentFalseWithCorrectNodeTypeAndNodeFailsConvertionToDate() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patient-number/text()", 
				XacmlDataTypes.DATE.getDataType(), false);
		
		Attributes attributes = createStrictMock(Attributes.class);
		expect(request.hasRepeatingCategories()).andReturn(false);
		expect(request.getOnlyAttributes(AttributeCategoryId.SUBJECT_RECIPIENT)).andReturn(attributes);
		expect(attributes.getContent()).andReturn(content).times(2);
		expect(request.getAttributeValues(ref.getCategory(), 
				DefaultContextHandler.CONTENT_SELECTOR , XacmlDataTypes.XPATHEXPRESSION.getDataType())).
				andReturn(Collections.<AttributeValue>emptyList());
		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		replay(context, request, attributes, pip);
		EvaluationContextHandler handler = new DefaultContextHandler(xpathProvider, request, pip);
		handler.resolve(context, ref);
		verify(context, request, attributes, pip);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testResolveSelectorMustBePresentTrueWithCorrectNodeTypeAndNodeFailsConvertionToDate() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patient-number/text()", 
				XacmlDataTypes.DATE.getDataType(), false);
		
		Attributes attributes = createStrictMock(Attributes.class);
		expect(request.hasRepeatingCategories()).andReturn(false);
		expect(request.getOnlyAttributes(AttributeCategoryId.SUBJECT_RECIPIENT)).andReturn(attributes);
		expect(attributes.getContent()).andReturn(content).times(2);
		expect(request.getAttributeValues(ref.getCategory(), 
				DefaultContextHandler.CONTENT_SELECTOR , XacmlDataTypes.XPATHEXPRESSION.getDataType())).
				andReturn(Collections.<AttributeValue>emptyList());
		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		replay(context, request, attributes, pip);
		EvaluationContextHandler handler = new DefaultContextHandler(xpathProvider, request, pip);
		handler.resolve(context, ref);
		verify(context, request, attributes, pip);
	}
	
	@Test
	public void testMustBePresentFalseCategoryContentExistXPathReturnsEmptySet() throws EvaluationException
	{
		AttributeSelector ref = new AttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, 
				"/test", 
				XacmlDataTypes.INTEGER.getDataType(), true);
		
		Attributes attributes = createStrictMock(Attributes.class);
		expect(request.hasRepeatingCategories()).andReturn(false);
		expect(request.getOnlyAttributes(AttributeCategoryId.SUBJECT_RECIPIENT)).andReturn(attributes);
		expect(attributes.getContent()).andReturn(content).times(2);
		expect(request.getAttributeValues(ref.getCategory(), 
				DefaultContextHandler.CONTENT_SELECTOR , XacmlDataTypes.XPATHEXPRESSION.getDataType())).
				andReturn(Collections.<AttributeValue>emptyList());
		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		replay(context, request, attributes, pip);
		EvaluationContextHandler handler = new DefaultContextHandler(xpathProvider, request, pip);
		Expression v = handler.resolve(context, ref);
		assertEquals(v, XacmlDataTypes.INTEGER.emptyBag());
		verify(context, request, attributes, pip);
	}		
	
	@Test
	public void testDesignatorResolutionAttributeInRequest() throws EvaluationException
	{
		AttributeDesignator ref = new AttributeDesignator(
				AttributeCategoryId.RESOURCE, "testId", null, XacmlDataTypes.ANYURI.getDataType(), false);
		
		Attributes attributes = createStrictMock(Attributes.class);
		expect(request.hasRepeatingCategories()).andReturn(false);
		expect(request.getAttributeValues(AttributeCategoryId.RESOURCE, "testId", null, XacmlDataTypes.ANYURI.getDataType())).
		andReturn(Collections.singleton(XacmlDataTypes.ANYURI.create("testValue")));
		
		replay(context, request, attributes, pip);
		EvaluationContextHandler h = new DefaultContextHandler(xpathProvider, request, pip);
		ValueExpression v = h.resolve(context, ref);
		assertEquals(AnyURIType.Factory.bagOf(XacmlDataTypes.ANYURI.create("testValue")), v);
		verify(context, request, attributes, pip);
	}
	
	@Test
	public void testDesignatorResolutionAttributeIsNotInRequestAndScopeIsRequestAndExternal() throws EvaluationException
	{
		AttributeDesignator ref = new AttributeDesignator(
				AttributeCategoryId.RESOURCE, "testId", null, XacmlDataTypes.ANYURI.getDataType(), false);
		
		Attributes attributes = createStrictMock(Attributes.class);
		expect(request.hasRepeatingCategories()).andReturn(false);
		expect(request.getAttributeValues(AttributeCategoryId.RESOURCE, "testId", null, AnyURIType.Factory.getInstance())).
		andReturn(Collections.<AttributeValue>emptyList());
		expect(context.getAttributeResolutionScope()).andReturn(AttributeResolutionScope.REQUEST_EXTERNAL);
		Capture<RequestContextAttributesCallback> c = new Capture<RequestContextAttributesCallback>();
		expect(pip.resolve(same(context), same(ref), capture(c))).andReturn(AnyURIType.Factory.bagOf(AnyURIType.Factory.create("testValue")));
		replay(context, request, attributes, pip);
		EvaluationContextHandler h = new DefaultContextHandler(xpathProvider, request, pip);
		ValueExpression v = h.resolve(context, ref);
		assertEquals(AnyURIType.Factory.bagOf(XacmlDataTypes.ANYURI.create("testValue")), v);
		verify(context, request, attributes, pip);
	}
	
	@Test
	public void testDesignatorResolutionAttributeIsNotInRequestAndScopeIsRequest() throws EvaluationException
	{
		AttributeDesignator ref = new AttributeDesignator(
				AttributeCategoryId.RESOURCE, "testId", null, XacmlDataTypes.ANYURI.getDataType(), false);
		
		Attributes attributes = createStrictMock(Attributes.class);
		expect(request.hasRepeatingCategories()).andReturn(false);
		expect(request.getAttributeValues(AttributeCategoryId.RESOURCE, "testId", null, XacmlDataTypes.ANYURI.getDataType())).
		andReturn(Collections.<AttributeValue>emptyList());
		expect(context.getAttributeResolutionScope()).andReturn(AttributeResolutionScope.REQUEST);
		replay(context, request, attributes, pip);
		EvaluationContextHandler h = new DefaultContextHandler(xpathProvider, request, pip);
		ValueExpression v = h.resolve(context, ref);
		assertEquals(XacmlDataTypes.ANYURI.emptyBag(), v);
		verify(context, request, attributes, pip);
	}
}
