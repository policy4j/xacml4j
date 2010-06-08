package com.artagon.xacml.v3.profiles;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.Status;
import com.artagon.xacml.v3.StatusCode;
import com.artagon.xacml.v3.pdp.PolicyDecisionCallback;
import com.artagon.xacml.v3.policy.spi.xpath.JDKXPathProvider;
import com.artagon.xacml.v3.types.XacmlDataTypes;

public class MultipleDecisionXPathExpressionHandlerTest 
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
	
	private PolicyDecisionCallback pdp;
	private RequestProfileHandler profile;
	private Node content;
	
	@Before
	public void init() throws Exception
	{
		this.pdp = createStrictMock(PolicyDecisionCallback.class);
		this.profile = new MultipleDecisionXPathExpressionHandler(new JDKXPathProvider());
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setNamespaceAware(true);
		DocumentBuilder builder = f.newDocumentBuilder();
		this.content = builder.parse(new InputSource(new StringReader(testXml)));
	}
	
	@Test
	public void testRequestWithTwoAttributesOfTheCategory()
	{
		
		Collection<Attribute> resourceAttrs = new LinkedList<Attribute>();
		resourceAttrs.add(new Attribute("testId3", XacmlDataTypes.STRING.create("value0")));
		resourceAttrs.add(new Attribute("testId4", XacmlDataTypes.STRING.create("value1")));
		
		resourceAttrs.add(new Attribute(MultipleDecisionXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR, 
				XacmlDataTypes.XPATHEXPRESSION.create("//md:record/md:patient", AttributeCategoryId.RESOURCE)));
		
		
		Attributes resource = new Attributes(AttributeCategoryId.RESOURCE, content, resourceAttrs);
		
		Collection<Attribute> subjectAttr = new LinkedList<Attribute>();
		subjectAttr.add(new Attribute("testId7", XacmlDataTypes.STRING.create("value0")));
		subjectAttr.add(new Attribute("testId8", XacmlDataTypes.STRING.create("value1")));
		Attributes subject =  new Attributes(AttributeCategoryId.SUBJECT_ACCESS, subjectAttr);
		
		Request context = new Request(false, 
				Arrays.asList(subject, resource));
		
		Capture<Request> c0 = new Capture<Request>();
		Capture<Request> c1 = new Capture<Request>();
		
		expect(pdp.requestDecision(capture(c0))).andReturn(
				new Result(new Status(StatusCode.createProcessingError())));
		expect(pdp.requestDecision(capture(c1))).andReturn(
				new Result(new Status(StatusCode.createProcessingError())));
		replay(pdp);
		Collection<Result> results = profile.handle(context, pdp);
	
		verify(pdp);
	}
}

