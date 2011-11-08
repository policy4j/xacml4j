package com.artagon.xacml.v30.pdp.profiles;

import static com.artagon.xacml.v30.types.StringType.STRING;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.Attribute;
import com.artagon.xacml.v30.Attributes;
import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.Result;
import com.artagon.xacml.v30.Status;
import com.artagon.xacml.v30.StatusCode;
import com.artagon.xacml.v30.core.AttributeCategories;
import com.artagon.xacml.v30.pdp.PolicyDecisionPointContext;
import com.artagon.xacml.v30.spi.pdp.RequestContextHandler;

public class MultipleResourcesViaRepeatingAttributesHandlerTest 
{
	private PolicyDecisionPointContext pdp;
	private RequestContextHandler profile;
	
	@Before
	public void init(){
		this.pdp = createStrictMock(PolicyDecisionPointContext.class);
		this.profile = new MultipleResourcesViaRepeatingAttributesHandler();
	}
	
	@Test
	public void testRequestWithTwoAttributesOfTheCategory()
	{
		Collection<Attribute> resource0Attr = new LinkedList<Attribute>();
		resource0Attr.add(new Attribute("testId1", STRING.create("value0")));
		resource0Attr.add(new Attribute("testId2", STRING.create("value1")));
		Attributes resource0 = new Attributes(AttributeCategories.RESOURCE, resource0Attr);
		
		Collection<Attribute> resource1Attr = new LinkedList<Attribute>();
		resource1Attr.add(new Attribute("testId3", STRING.create("value0")));
		resource1Attr.add(new Attribute("testId4", STRING.create("value1")));
		Attributes resource1 = new Attributes(AttributeCategories.RESOURCE, resource1Attr);
		
		Collection<Attribute> subjectAttr = new LinkedList<Attribute>();
		subjectAttr.add(new Attribute("testId7", STRING.create("value0")));
		subjectAttr.add(new Attribute("testId8", STRING.create("value1")));
		Attributes subject =  new Attributes(AttributeCategories.SUBJECT_ACCESS, subjectAttr);
		
		RequestContext context = new RequestContext(false, 
				Arrays.asList(subject, resource0, resource1));
		
		Capture<RequestContext> c0 = new Capture<RequestContext>();
		Capture<RequestContext> c1 = new Capture<RequestContext>();
		
		expect(pdp.requestDecision(capture(c0))).andReturn(
				new Result(Decision.INDETERMINATE, 
						new Status(StatusCode.createProcessingError()), 
						Collections.<Attributes>emptyList(), 
						Collections.<Attributes>emptyList()));
		expect(pdp.requestDecision(capture(c1))).andReturn(
				new Result(Decision.INDETERMINATE, 
						new Status(StatusCode.createProcessingError()),
				Collections.<Attributes>emptyList(), 
				Collections.<Attributes>emptyList()));
		replay(pdp);
		Collection<Result> results = profile.handle(context, pdp);
		assertEquals(2, results.size());
		assertEquals(new Status(StatusCode.createProcessingError()), results.iterator().next().getStatus());
		RequestContext r0 = c0.getValue();
		RequestContext r1 = c1.getValue();
		assertTrue(r0.getAttributes(AttributeCategories.SUBJECT_ACCESS).contains(subject));
		assertEquals(1, r0.getAttributes(AttributeCategories.RESOURCE).size());
		// order is not known so check if has 1 and at least one is in the request
		assertTrue(r0.getAttributes(AttributeCategories.RESOURCE).contains(resource0) || r0.getAttributes(AttributeCategories.RESOURCE).contains(resource1));
		assertTrue(r1.getAttributes(AttributeCategories.SUBJECT_ACCESS).contains(subject));
		// order is not known so check if has 1 and at least one is in the request
		assertEquals(1, r1.getAttributes(AttributeCategories.RESOURCE).size());
		assertTrue(r0.getAttributes(AttributeCategories.RESOURCE).contains(resource0) || r0.getAttributes(AttributeCategories.RESOURCE).contains(resource1));
		verify(pdp);
	}
	
	
	@Test
	public void testRequestWithNoAttributesOfTheSameCategory()
	{
		Collection<Attribute> resource0Attr = new LinkedList<Attribute>();
		resource0Attr.add(new Attribute("testId1", STRING.create("value0")));
		resource0Attr.add(new Attribute("testId2", STRING.create("value1")));
		Attributes resource0 = new Attributes(AttributeCategories.RESOURCE, resource0Attr);
		
		
		Collection<Attribute> subjectAttr = new LinkedList<Attribute>();
		subjectAttr.add(new Attribute("testId7", STRING.create("value0")));
		subjectAttr.add(new Attribute("testId8", STRING.create("value1")));
		Attributes subject =  new Attributes(AttributeCategories.SUBJECT_ACCESS, subjectAttr);
		
		RequestContext context = new RequestContext(false, 
				Arrays.asList(subject, resource0));
		
		Capture<RequestContext> c0 = new Capture<RequestContext>();
		
		expect(pdp.requestDecision(capture(c0))).andReturn(
				new Result(Decision.INDETERMINATE, 
						new Status(StatusCode.createProcessingError()), 
						Collections.<Attributes>emptyList(), 
						Collections.<Attributes>emptyList()));
		
		replay(pdp);
		Collection<Result> results = profile.handle(context, pdp);
		assertEquals(new Status(StatusCode.createProcessingError()), results.iterator().next().getStatus());
		assertEquals(1, results.size());
		RequestContext r0 = c0.getValue();
		assertTrue(r0.getAttributes(AttributeCategories.SUBJECT_ACCESS).contains(subject));
		assertTrue(r0.getAttributes(AttributeCategories.RESOURCE).contains(resource0));
		verify(pdp);
	}
	
	@Test
	public void testWithEmptyRequest()
	{
		RequestContext context = new RequestContext(false, 
				Collections.<Attributes>emptyList());
		
		Capture<RequestContext> c0 = new Capture<RequestContext>();
		
		expect(pdp.requestDecision(capture(c0))).andReturn(
				new Result(Decision.INDETERMINATE, 
						new Status(StatusCode.createProcessingError()),
						Collections.<Attributes>emptyList(), 
						Collections.<Attributes>emptyList()));
		
		replay(pdp);
		Collection<Result> results = profile.handle(context, pdp);
		assertEquals(new Status(StatusCode.createProcessingError()), results.iterator().next().getStatus());
		assertEquals(1, results.size());
		assertSame(context, c0.getValue());
		verify(pdp);
	}
}

