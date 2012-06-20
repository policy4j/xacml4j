package com.artagon.xacml.v30.pdp.profiles;

import static com.artagon.xacml.v30.types.StringType.STRING;
import static com.artagon.xacml.v30.types.XPathExpType.XPATHEXPRESSION;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.artagon.xacml.v30.Attribute;
import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.Attributes;
import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.Status;
import com.artagon.xacml.v30.StatusCode;
import com.artagon.xacml.v30.pdp.PolicyDecisionPointContext;
import com.artagon.xacml.v30.pdp.Result;
import com.artagon.xacml.v30.spi.pdp.RequestContextHandler;
import com.artagon.xacml.v30.spi.xpath.DefaultXPathProvider;
import com.artagon.xacml.v30.spi.xpath.XPathProvider;
import com.google.common.collect.Iterables;

public class MultipleResourcesViaXPathExpressionHandlerTest
{

	private String testXml = "<md:record xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
	"xmlns:md=\"urn:example:med:schemas:record\">" +
	"<md:patient>" +
	"<md:patientDoB md:attrn1=\"test\" attrn2=\"v\" >1992-03-21</md:patientDoB>" +
	"<md:patient-number>555555</md:patient-number>" +
	"</md:patient>" +
	"<md:patient>" +
	"<md:patientDoB md:attrn1=\"test1\" attrn2=\"v1\">1991-01-11</md:patientDoB>" +
	"<md:patient-number>11111</md:patient-number>" +
	"</md:patient>" +
	"</md:record>";

	private PolicyDecisionPointContext pdp;
	private RequestContextHandler profile;
	private Node content;

	private XPathProvider xpathProvider;

	@Before
	public void init() throws Exception
	{
		this.pdp = createStrictMock(PolicyDecisionPointContext.class);
		this.profile = new MultipleResourcesViaXPathExpressionHandler();
		this.xpathProvider = new DefaultXPathProvider();
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setNamespaceAware(true);
		DocumentBuilder builder = f.newDocumentBuilder();
		this.content = builder.parse(new InputSource(new StringReader(testXml)));
	}

	@Test
	public void testMultipleDecisionProfileWithSelectorInSingleCategory()
	{

		Collection<Attribute> resourceAttrs = new LinkedList<Attribute>();
		resourceAttrs.add(new Attribute("testId3", STRING.create("value0")));
		resourceAttrs.add(new Attribute("testId4", STRING.create("value1")));

		resourceAttrs.add(new Attribute(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR,
				XPATHEXPRESSION.create("//md:record/md:patient", AttributeCategories.RESOURCE)));


		Attributes resource = new Attributes(AttributeCategories.RESOURCE, content, resourceAttrs);

		Collection<Attribute> subjectAttr = new LinkedList<Attribute>();
		subjectAttr.add(new Attribute("testId7", STRING.create("value0")));
		subjectAttr.add(new Attribute("testId8", STRING.create("value1")));
		Attributes subject =  new Attributes(AttributeCategories.SUBJECT_ACCESS, subjectAttr);

		RequestContext context = new RequestContext(false,
				Arrays.asList(subject, resource));

		assertFalse(context.containsRepeatingCategories());
		Capture<RequestContext> c0 = new Capture<RequestContext>();
		Capture<RequestContext> c1 = new Capture<RequestContext>();

		expect(pdp.getXPathProvider()).andReturn(xpathProvider);

		expect(pdp.requestDecision(capture(c0))).andReturn(
				Result.createIndeterminateProcessingError().build());

		expect(pdp.requestDecision(capture(c1))).andReturn(
				Result.createIndeterminateProcessingError().build());

		replay(pdp);

		Collection<Result> results = profile.handle(context, pdp);
		Iterator<Result> it = results.iterator();
		assertEquals(new Status(StatusCode.createProcessingError()), it.next().getStatus());
		assertEquals(new Status(StatusCode.createProcessingError()), it.next().getStatus());
		RequestContext r0 = c0.getValue();
		RequestContext r1 = c1.getValue();

		Attribute selector0 = Iterables.getOnlyElement(r0.getOnlyAttributes(AttributeCategories.RESOURCE).getAttributes(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR));
		Attribute selector1 = Iterables.getOnlyElement(r1.getOnlyAttributes(AttributeCategories.RESOURCE).getAttributes(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR));

		assertEquals(XPATHEXPRESSION.create("//md:record/md:patient[1]", AttributeCategories.RESOURCE),  Iterables.getOnlyElement(selector0.getValues()));
		assertEquals(XPATHEXPRESSION.create("//md:record/md:patient[2]", AttributeCategories.RESOURCE),  Iterables.getOnlyElement(selector1.getValues()));

		verify(pdp);
	}

	@Test
	public void testMultipleDecisionProfileWithSelectorInMultipleCategories()
	{

		Collection<Attribute> resourceAttrs = new LinkedList<Attribute>();
		resourceAttrs.add(new Attribute("testId3", STRING.create("value0")));
		resourceAttrs.add(new Attribute("testId4", STRING.create("value1")));

		resourceAttrs.add(new Attribute(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR,
				XPATHEXPRESSION.create("//md:record/md:patient", AttributeCategories.RESOURCE)));


		Attributes resource = new Attributes(AttributeCategories.RESOURCE, content, resourceAttrs);

		Collection<Attribute> subjectAttr = new LinkedList<Attribute>();
		subjectAttr.add(new Attribute("testId7", STRING.create("value0")));
		subjectAttr.add(new Attribute("testId8", STRING.create("value1")));

		subjectAttr.add(new Attribute(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR,
				XPATHEXPRESSION.create("//md:record/md:patient/md:patientDoB/@md:attrn1", AttributeCategories.SUBJECT_ACCESS)));

		Attributes subject =  new Attributes(AttributeCategories.SUBJECT_ACCESS, content, subjectAttr);

		RequestContext context = new RequestContext(false,
				Arrays.asList(subject, resource));

		assertFalse(context.containsRepeatingCategories());
		Capture<RequestContext> c0 = new Capture<RequestContext>();
		Capture<RequestContext> c1 = new Capture<RequestContext>();
		Capture<RequestContext> c2 = new Capture<RequestContext>();
		Capture<RequestContext> c3 = new Capture<RequestContext>();

		expect(pdp.getXPathProvider()).andReturn(xpathProvider);

		expect(pdp.requestDecision(capture(c0))).andReturn(
				Result.createIndeterminateProcessingError().build());
		expect(pdp.requestDecision(capture(c1))).andReturn(
				Result.createIndeterminateProcessingError().build());
		expect(pdp.requestDecision(capture(c2))).andReturn(
				Result.createIndeterminateProcessingError().build());
		expect(pdp.requestDecision(capture(c3))).andReturn(
				Result.createIndeterminateProcessingError().build());

		replay(pdp);

		Collection<Result> results = profile.handle(context, pdp);
		Iterator<Result> it = results.iterator();
		assertEquals(new Status(StatusCode.createProcessingError()), it.next().getStatus());
		assertEquals(new Status(StatusCode.createProcessingError()), it.next().getStatus());

		RequestContext r0 = c0.getValue();
		RequestContext r1 = c1.getValue();
		RequestContext r2 = c2.getValue();
		RequestContext r3 = c3.getValue();

		Attribute selector00 = Iterables.getOnlyElement(r0.getOnlyAttributes(AttributeCategories.RESOURCE).getAttributes(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR));
		Attribute selector01 = Iterables.getOnlyElement(r0.getOnlyAttributes(AttributeCategories.SUBJECT_ACCESS).getAttributes(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR));

		assertEquals(XPATHEXPRESSION.create("//md:record/md:patient[1]", AttributeCategories.RESOURCE),  Iterables.getOnlyElement(selector00.getValues()));
		assertEquals(XPATHEXPRESSION.create("//md:record/md:patient[1]/md:patientDoB[1]/@md:attrn1", AttributeCategories.SUBJECT_ACCESS),  Iterables.getOnlyElement(selector01.getValues()));

		Attribute selector10 = Iterables.getOnlyElement(r1.getOnlyAttributes(AttributeCategories.RESOURCE).getAttributes(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR));
		Attribute selector11 = Iterables.getOnlyElement(r1.getOnlyAttributes(AttributeCategories.SUBJECT_ACCESS).getAttributes(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR));

		assertEquals(XPATHEXPRESSION.create("//md:record/md:patient[1]", AttributeCategories.RESOURCE),  Iterables.getOnlyElement(selector10.getValues()));
		assertEquals(XPATHEXPRESSION.create("//md:record/md:patient[2]/md:patientDoB[1]/@md:attrn1", AttributeCategories.SUBJECT_ACCESS),  Iterables.getOnlyElement(selector11.getValues()));

		Attribute selector20 = Iterables.getOnlyElement(r2.getOnlyAttributes(AttributeCategories.RESOURCE).getAttributes(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR));
		Attribute selector21 = Iterables.getOnlyElement(r2.getOnlyAttributes(AttributeCategories.SUBJECT_ACCESS).getAttributes(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR));

		assertEquals(XPATHEXPRESSION.create("//md:record/md:patient[2]", AttributeCategories.RESOURCE),  Iterables.getOnlyElement(selector20.getValues()));
		assertEquals(XPATHEXPRESSION.create("//md:record/md:patient[1]/md:patientDoB[1]/@md:attrn1", AttributeCategories.SUBJECT_ACCESS),  Iterables.getOnlyElement(selector21.getValues()));


		Attribute selector30 = Iterables.getOnlyElement(r3.getOnlyAttributes(AttributeCategories.RESOURCE).getAttributes(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR));
		Attribute selector31 = Iterables.getOnlyElement(r3.getOnlyAttributes(AttributeCategories.SUBJECT_ACCESS).getAttributes(MultipleResourcesViaXPathExpressionHandler.CONTENT_SELECTOR));

		assertEquals(XPATHEXPRESSION.create("//md:record/md:patient[2]", AttributeCategories.RESOURCE),  Iterables.getOnlyElement(selector30.getValues()));
		assertEquals(XPATHEXPRESSION.create("//md:record/md:patient[2]/md:patientDoB[1]/@md:attrn1", AttributeCategories.SUBJECT_ACCESS),  Iterables.getOnlyElement(selector31.getValues()));

		verify(pdp);
	}

	@Test
	public void testMultipleDecisionRequestHasMultipleContentSelectorOfInvalidType()
	{

		Collection<Attribute> resourceAttrs = new LinkedList<Attribute>();
		resourceAttrs.add(new Attribute("testId3", STRING.create("value0")));
		resourceAttrs.add(new Attribute("testId4", STRING.create("value1")));

		resourceAttrs.add(new Attribute(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR,
				STRING.create("//md:record/md:patient")));


		Attributes resource = new Attributes(AttributeCategories.RESOURCE, content, resourceAttrs);

		Collection<Attribute> subjectAttr = new LinkedList<Attribute>();
		subjectAttr.add(new Attribute("testId7", STRING.create("value0")));
		subjectAttr.add(new Attribute("testId8", STRING.create("value1")));

		Attributes subject =  new Attributes(AttributeCategories.SUBJECT_ACCESS, content, subjectAttr);

		RequestContext request = new RequestContext(false,
				Arrays.asList(subject, resource));

		assertFalse(request.containsRepeatingCategories());
		Capture<RequestContext> c0 = new Capture<RequestContext>();

		expect(pdp.requestDecision(capture(c0))).andReturn(
				Result.createIndeterminateProcessingError().build());

		replay(pdp);

		profile.handle(request, pdp);

		assertEquals(request, c0.getValue());
		verify(pdp);
	}

	@Test
	public void testMultipleDecisionCategoryDoesNotHaveContent()
	{

		Collection<Attribute> resourceAttrs = new LinkedList<Attribute>();
		resourceAttrs.add(new Attribute("testId3", STRING.create("value0")));
		resourceAttrs.add(new Attribute("testId4", STRING.create("value1")));

		resourceAttrs.add(new Attribute(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR,
				XPATHEXPRESSION.create("//md:record/md:patient", AttributeCategories.RESOURCE)));

		Attributes resource = new Attributes(AttributeCategories.RESOURCE, resourceAttrs);

		Collection<Attribute> subjectAttr = new LinkedList<Attribute>();
		subjectAttr.add(new Attribute("testId7", STRING.create("value0")));
		subjectAttr.add(new Attribute("testId8", STRING.create("value1")));

		Attributes subject =  new Attributes(AttributeCategories.SUBJECT_ACCESS, subjectAttr);

		expect(pdp.getXPathProvider()).andReturn(xpathProvider);

		RequestContext request = new RequestContext(false,
				Arrays.asList(subject, resource));

		assertFalse(request.containsRepeatingCategories());

		replay(pdp);

		Collection<Result> results = profile.handle(request, pdp);
		assertEquals(Decision.INDETERMINATE, Iterables.getOnlyElement(results).getDecision());

		verify(pdp);
	}

	@Test
	public void testWithEmptyRequest()
	{
		RequestContext context = new RequestContext(false,
				Collections.<Attributes>emptyList());

		Capture<RequestContext> c0 = new Capture<RequestContext>();

		expect(pdp.requestDecision(capture(c0))).andReturn(
				Result.createIndeterminateProcessingError().build());

		replay(pdp);
		Collection<Result> results = profile.handle(context, pdp);
		assertEquals(new Status(StatusCode.createProcessingError()), results.iterator().next().getStatus());
		assertEquals(1, results.size());
		assertSame(context, c0.getValue());
		verify(pdp);
	}
}

