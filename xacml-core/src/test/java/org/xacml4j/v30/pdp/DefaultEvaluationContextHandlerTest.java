package org.xacml4j.v30.pdp;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.xacml4j.v30.types.AnyURIType.ANYURI;
import static org.xacml4j.v30.types.DateType.DATE;
import static org.xacml4j.v30.types.IntegerType.INTEGER;
import static org.xacml4j.v30.types.XPathExpType.XPATHEXPRESSION;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.ValueExpression;
import org.xacml4j.v30.XPathVersion;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.xpath.DefaultXPathProvider;
import org.xacml4j.v30.spi.xpath.XPathProvider;
import org.xacml4j.v30.types.EntityExp;
import org.xacml4j.v30.types.IntegerExp;
import org.xacml4j.v30.types.XPathExp;
import org.xacml4j.v30.types.XacmlTypes;
import org.xml.sax.InputSource;


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

	private Entity entity;

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
		this.entity = Entity
				.builder()
				.content(builder.parse
						(new InputSource(new StringReader(testXml)))).build();
		this.xpathProvider = new DefaultXPathProvider();
		this.handler = new DefaultEvaluationContextHandler(requestContextCallback, xpathProvider, pip);
	}


	@Test
	public void testSelectorResolveContentIsInRequestXPathReturnsNonEmptySetResolutionScopeRequest()
		throws EvaluationException
	{
		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(AttributeCategories.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(XacmlTypes.INTEGER)
				.build();

		expect(requestContextCallback.getEntity(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(entity);


		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);

		replay(context, request, requestContextCallback, pip);

		Expression v = handler.resolve(context, ref);

		assertEquals(v, IntegerExp.valueOf(555555).toBag());
		verify(context, request, requestContextCallback, pip);
	}

	@Test
	public void testSelectorResolveContentIsNotInRequestXPathReturnsNonEmptyNodeSet()
		throws Exception
	{
		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(AttributeCategories.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(XacmlTypes.INTEGER)
				.build();

		expect(requestContextCallback.getEntity(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(entity);


		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		replay(context, request, requestContextCallback, pip);

		Expression v = handler.resolve(context, ref);
		assertEquals(XacmlTypes.INTEGER.bagOf(IntegerExp.valueOf(555555)), v);
		verify(context, request, requestContextCallback, pip);
	}

	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testSelectorResolveContentIsNotInRequestPIPCallsHandlerToResolveTheSameAttribute()
		throws Exception
	{
		final AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(AttributeCategories.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(XacmlTypes.INTEGER)
				.build();

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
		assertEquals(IntegerExp.bag().value(555555).build(), v);
		verify(context, request, requestContextCallback, pip);
	}


	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testSelectorResolveContentIsInRequestXPathReturnsUnsupportedNodeType() throws EvaluationException
	{
		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(AttributeCategories.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient")
				.dataType(XacmlTypes.INTEGER)
				.build();

		expect(requestContextCallback.getContent(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(content);

		expect(requestContextCallback.getAttributeValue(ref.getCategory(),
				"urn:oasis:names:tc:xacml:3.0:content-selector", XacmlTypes.XPATH, null)).
				andReturn(XPathExp.emptyBag());

		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);

		replay(context, request, requestContextCallback, pip);
		handler.resolve(context, ref);
		verify(context, request, requestContextCallback, pip);
	}

	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testSelectorResolveContentIsInRequestXPathReturnsNonEmptySetAndNodeFailsConvertionToDate() throws EvaluationException
	{
		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(AttributeCategories.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(DATE)
				.build();

		expect(requestContextCallback.getContent(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(content);

		expect(requestContextCallback.getAttributeValue(ref.getCategory(),
				"urn:oasis:names:tc:xacml:3.0:content-selector", XPATHEXPRESSION, null)).
				andReturn(XPATHEXPRESSION.emptyBag());
		
		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		expect(context.getTypes()).andReturn(XacmlTypes.builder().defaultTypes().create());
		
		replay(context, request, requestContextCallback, pip);
		handler.resolve(context, ref);
		verify(context, request, requestContextCallback, pip);
	}

	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testSelectorResolveContentIsInRequestXPathReturnsNonEmptySetAndCorrectNodeTypeAndNodeFailsConvertionToDate() throws EvaluationException
	{

		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(AttributeCategories.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(DATE)
				.build();

		expect(requestContextCallback.getContent(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(content);

		expect(requestContextCallback.getAttributeValue(ref.getCategory(),
				"urn:oasis:names:tc:xacml:3.0:content-selector", XPATHEXPRESSION, null)).
				andReturn(XPATHEXPRESSION.emptyBag());

		expect(context.getXPathVersion()).andReturn(XPathVersion.XPATH1);
		expect(context.getTypes()).andReturn(XacmlTypes.builder().defaultTypes().create());
		
		replay(context, request, requestContextCallback, pip);
		handler.resolve(context, ref);
		verify(context, request, requestContextCallback, pip);
	}

	@Test
	public void testSelectorResolveContentInRequestExistXPathReturnsEmptySet() throws EvaluationException
	{
		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(AttributeCategories.SUBJECT_RECIPIENT)
				.xpath("/test")
				.dataType(INTEGER)
				.build();

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

		AttributeSelectorKey ref = AttributeSelectorKey
				.builder()
				.category(AttributeCategories.SUBJECT_RECIPIENT)
				.xpath("/md:record/md:patient/md:patient-number/text()")
				.dataType(INTEGER)
				.build();

		expect(requestContextCallback.getContent(AttributeCategories.SUBJECT_RECIPIENT)).andReturn(null);

		expect(pip.resolve(context, AttributeCategories.SUBJECT_RECIPIENT)).andThrow(new RuntimeException());

		replay(context, request, requestContextCallback, pip);
		handler.resolve(context, ref);
		verify(context, request, requestContextCallback, pip);
	}


	@Test
	public void testDesignatorResolveAttributeIsInRequest() throws Exception
	{
		AttributeDesignatorKey ref = AttributeDesignatorKey
				.builder()
				.category(AttributeCategories.RESOURCE)
				.attributeId("testId")
				.dataType(ANYURI)
				.build();

		expect(requestContextCallback.getAttributeValue(
				AttributeCategories.RESOURCE, "testId", ANYURI, null)).
				andReturn(ANYURI.bagOf(ANYURI.fromAny("testValue")));

		replay(context, request, pip, requestContextCallback);
		ValueExpression v = handler.resolve(context, ref);
		assertEquals(ANYURI.bagOf(ANYURI.fromAny("testValue")), v);
		verify(context, request, pip, requestContextCallback);
	}

	@Test
	public void testDesignatorResolveAttributeIsNotInRequest()
		throws Exception
	{
		AttributeDesignatorKey ref = AttributeDesignatorKey
				.builder()
				.category(AttributeCategories.RESOURCE)
				.attributeId("testId")
				.dataType(ANYURI)
				.build();

		expect(requestContextCallback.getAttributeValue(
				AttributeCategories.RESOURCE, "testId", ANYURI, null)).andReturn(ANYURI.emptyBag());


		expect(pip.resolve(context, ref)).andReturn(ANYURI.bagOf(ANYURI.fromAny("testValue")));


		replay(context, request, pip, requestContextCallback);
		ValueExpression v = handler.resolve(context, ref);
		assertEquals(ANYURI.bagOf(ANYURI.fromAny("testValue")), v);
		verify(context, request, pip, requestContextCallback);
	}


	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testDesignatorResolveAttributeIsNotInRequestPIPThrowsRuntimeException()
		throws Exception
	{
		AttributeDesignatorKey ref = AttributeDesignatorKey
				.builder()
				.category(AttributeCategories.RESOURCE)
				.attributeId("testId")
				.dataType(ANYURI)
				.build();

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
		final AttributeDesignatorKey ref = AttributeDesignatorKey
				.builder()
				.category(AttributeCategories.RESOURCE)
				.attributeId("testId")
				.dataType(ANYURI)
				.build();

		expect(requestContextCallback.getAttributeValue(
				AttributeCategories.RESOURCE, "testId", ANYURI, null)).andReturn(ANYURI.emptyBag());


		expect(pip.resolve(context, ref)).andAnswer(new IAnswer<BagOfAttributeExp>() {
			@Override
			public BagOfAttributeExp answer() throws Throwable{
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
