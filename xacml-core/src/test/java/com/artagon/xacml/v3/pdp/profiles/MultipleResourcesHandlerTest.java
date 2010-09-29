package com.artagon.xacml.v3.pdp.profiles;


import static com.artagon.xacml.v3.types.StringType.STRING;
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

import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeCategories;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.Status;
import com.artagon.xacml.v3.StatusCode;
import com.artagon.xacml.v3.pdp.PolicyDecisionCallback;
import com.artagon.xacml.v3.pdp.RequestContextHandler;

public class MultipleResourcesHandlerTest 
{
	private PolicyDecisionCallback pdp;
	private RequestContextHandler profile;
	
	private Attributes resource0;
	private Attributes resource1;
	private Attributes subject0;
	private Attributes subject1;
	
	
	@Before
	public void init(){
		this.pdp = createStrictMock(PolicyDecisionCallback.class);
		this.profile = new MultipleResourcesHandler();
		
		Collection<Attribute> resource0Attr = new LinkedList<Attribute>();
		resource0Attr.add(new Attribute("testId1", STRING.create("value0")));
		resource0Attr.add(new Attribute("testId2", STRING.create("value1")));
		this.resource0 = new Attributes(AttributeCategories.RESOURCE, resource0Attr);
		
		Collection<Attribute> resource1Attr = new LinkedList<Attribute>();
		resource1Attr.add(new Attribute("testId3", STRING.create("value0")));
		resource1Attr.add(new Attribute("testId4", STRING.create("value1")));
		this.resource1 = new Attributes(AttributeCategories.RESOURCE, resource1Attr);
		
		Collection<Attribute> subject0Attr = new LinkedList<Attribute>();
		subject0Attr.add(new Attribute("testId7", STRING.create("value0")));
		subject0Attr.add(new Attribute("testId8", STRING.create("value1")));
		this.subject0 =  new Attributes(AttributeCategories.SUBJECT_ACCESS, subject0Attr);
		
		Collection<Attribute> subject1Attr = new LinkedList<Attribute>();
		subject1Attr.add(new Attribute("testId9", STRING.create("value0")));
		subject1Attr.add(new Attribute("testId10", STRING.create("value1")));
		this.subject1 =  new Attributes(AttributeCategories.SUBJECT_ACCESS, subject1Attr);
	}
	
	@Test
	public void testRequestWithTwoAttributesOfTheCategory()
	{
		
		
		RequestContext context = new RequestContext(false, true,
				Arrays.asList(subject0, subject1, resource0, resource1));
		
		Capture<RequestContext> c0 = new Capture<RequestContext>();
		Capture<RequestContext> c1 = new Capture<RequestContext>();
		Capture<RequestContext> c2 = new Capture<RequestContext>();
		Capture<RequestContext> c3 = new Capture<RequestContext>();
		
		expect(pdp.requestDecision(capture(c0))).andReturn(
				new Result(Decision.DENY, 
						new Status(StatusCode.createProcessingError()), 
						Collections.<Attributes>emptyList()));
		
		expect(pdp.requestDecision(capture(c1))).andReturn(
				new Result(Decision.DENY, 
						new Status(StatusCode.createProcessingError()), 
						Collections.<Attributes>emptyList()));
		
		expect(pdp.requestDecision(capture(c2))).andReturn(
				new Result(Decision.DENY, 
						new Status(StatusCode.createOk()),
				Collections.<Attributes>emptyList()));
		
		expect(pdp.requestDecision(capture(c3))).andReturn(
				new Result(Decision.DENY, 
						new Status(StatusCode.createProcessingError()),
				Collections.<Attributes>emptyList()));
		
		replay(pdp);
		Collection<Result> results = profile.handle(context, pdp);
		assertEquals(1, results.size());
		Result r = results.iterator().next();
	
		assertEquals(Decision.INDETERMINATE, r.getDecision());
		assertEquals(Status.createProcessingError(), r.getStatus());
		
		assertEquals(0, r.getAttributes().size());
		assertEquals(0, r.getObligations().size());
		assertEquals(0, r.getAssociatedAdvice().size());
		verify(pdp);
	}
	
	
	@Test
	public void testRequestWithSingleResultCombine()
	{
		RequestContext context = new RequestContext(false, true,
				Arrays.asList(subject0, resource0));
		
		Capture<RequestContext> c0 = new Capture<RequestContext>();
		
		expect(pdp.requestDecision(capture(c0))).andReturn(
				new Result(Decision.INDETERMINATE, 
						new Status(StatusCode.createProcessingError()), 
						Collections.<Attributes>emptyList()));
		
		replay(pdp);
		Collection<Result> results = profile.handle(context, pdp);
		assertEquals(new Status(StatusCode.createProcessingError()), results.iterator().next().getStatus());
		assertEquals(1, results.size());
		RequestContext r0 = c0.getValue();
		assertTrue(r0.getAttributes(AttributeCategories.SUBJECT_ACCESS).contains(subject0));
		assertTrue(r0.getAttributes(AttributeCategories.RESOURCE).contains(resource0));
		verify(pdp);
	}
	
	@Test
	public void testWithEmptyRequest()
	{
		RequestContext context = new RequestContext(false, true,
				Collections.<Attributes>emptyList());
		
		Capture<RequestContext> c0 = new Capture<RequestContext>();
		
		expect(pdp.requestDecision(capture(c0))).andReturn(
				new Result(Decision.INDETERMINATE, 
						new Status(StatusCode.createProcessingError()),
						Collections.<Attributes>emptyList()));
		
		replay(pdp);
		Collection<Result> results = profile.handle(context, pdp);
		assertEquals(new Status(StatusCode.createProcessingError()), results.iterator().next().getStatus());
		assertEquals(1, results.size());
		assertSame(context, c0.getValue());
		verify(pdp);
	}
}


