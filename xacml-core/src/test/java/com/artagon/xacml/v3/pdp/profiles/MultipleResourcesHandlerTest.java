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

import com.artagon.xacml.v3.AttributeCategories;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.context.Attribute;
import com.artagon.xacml.v3.context.Attributes;
import com.artagon.xacml.v3.context.RequestContext;
import com.artagon.xacml.v3.context.RequestContextHandler;
import com.artagon.xacml.v3.context.Result;
import com.artagon.xacml.v3.context.Status;
import com.artagon.xacml.v3.context.StatusCode;
import com.artagon.xacml.v3.pdp.PolicyDecisionPointContext;

public class MultipleResourcesHandlerTest 
{
	private PolicyDecisionPointContext context;
	private RequestContextHandler profile;
	
	private Attributes resource0;
	private Attributes resource1;
	private Attributes subject0;
	private Attributes subject1;
	
	
	@Before
	public void init(){
		this.context = createStrictMock(PolicyDecisionPointContext.class);
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
	public void testAllResultsAreDeny()
	{
		
		RequestContext request = new RequestContext(false, true,
				Arrays.asList(subject0, subject1, resource0, resource1));
		
		Capture<RequestContext> c0 = new Capture<RequestContext>();
		Capture<RequestContext> c1 = new Capture<RequestContext>();
		Capture<RequestContext> c2 = new Capture<RequestContext>();
		Capture<RequestContext> c3 = new Capture<RequestContext>();
		
		expect(context.requestDecision(capture(c0))).andReturn(
					new Result(Decision.DENY, Status.createSuccess(), 
						Collections.<Attributes>emptyList()));
		
		expect(context.requestDecision(capture(c1))).andReturn(
				new Result(Decision.DENY, Status.createSuccess(), 
						Collections.<Attributes>emptyList()));
		
		expect(context.requestDecision(capture(c2))).andReturn(
				new Result(Decision.DENY, Status.createSuccess(),
				Collections.<Attributes>emptyList()));
		
		expect(context.requestDecision(capture(c3))).andReturn(
				new Result(Decision.DENY, Status.createSuccess(),
				Collections.<Attributes>emptyList()));
		
		replay(context);
		Collection<Result> results = profile.handle(request, context);
		assertEquals(1, results.size());
		Result r = results.iterator().next();
	
		assertEquals(Decision.DENY, r.getDecision());
		assertEquals(Status.createSuccess(), r.getStatus());
		
		assertEquals(0, r.getAttributes().size());
		assertEquals(0, r.getObligations().size());
		assertEquals(0, r.getAssociatedAdvice().size());
		verify(context);
	}
	
	@Test
	public void testAllResultsArePermit()
	{
		
		
		RequestContext request = new RequestContext(false, true,
				Arrays.asList(subject0, subject1, resource0, resource1));
		
		Capture<RequestContext> c0 = new Capture<RequestContext>();
		Capture<RequestContext> c1 = new Capture<RequestContext>();
		Capture<RequestContext> c2 = new Capture<RequestContext>();
		Capture<RequestContext> c3 = new Capture<RequestContext>();
		
		expect(context.requestDecision(capture(c0))).andReturn(
					new Result(Decision.PERMIT, Status.createSuccess(), 
						Collections.<Attributes>emptyList()));
		
		expect(context.requestDecision(capture(c1))).andReturn(
				new Result(Decision.PERMIT, Status.createSuccess(), 
						Collections.<Attributes>emptyList()));
		
		expect(context.requestDecision(capture(c2))).andReturn(
				new Result(Decision.PERMIT, Status.createSuccess(),
				Collections.<Attributes>emptyList()));
		
		expect(context.requestDecision(capture(c3))).andReturn(
				new Result(Decision.PERMIT, Status.createSuccess(),
				Collections.<Attributes>emptyList()));
		
		replay(context);
		Collection<Result> results = profile.handle(request, context);
		assertEquals(1, results.size());
		Result r = results.iterator().next();
	
		assertEquals(Decision.PERMIT, r.getDecision());
		assertEquals(Status.createSuccess(), r.getStatus());
		
		assertEquals(0, r.getAttributes().size());
		assertEquals(0, r.getObligations().size());
		assertEquals(0, r.getAssociatedAdvice().size());
		verify(context);
	}
	
	@Test
	public void testAllResultsAreNotApplicable()
	{
		
		
		RequestContext request = new RequestContext(false, true,
				Arrays.asList(subject0, subject1, resource0, resource1));
		
		Capture<RequestContext> c0 = new Capture<RequestContext>();
		Capture<RequestContext> c1 = new Capture<RequestContext>();
		Capture<RequestContext> c2 = new Capture<RequestContext>();
		Capture<RequestContext> c3 = new Capture<RequestContext>();
		
		expect(context.requestDecision(capture(c0))).andReturn(
					new Result(Decision.NOT_APPLICABLE, Status.createSuccess(), 
						Collections.<Attributes>emptyList()));
		
		expect(context.requestDecision(capture(c1))).andReturn(
				new Result(Decision.NOT_APPLICABLE, Status.createSuccess(), 
						Collections.<Attributes>emptyList()));
		
		expect(context.requestDecision(capture(c2))).andReturn(
				new Result(Decision.NOT_APPLICABLE, Status.createSuccess(),
				Collections.<Attributes>emptyList()));
		
		expect(context.requestDecision(capture(c3))).andReturn(
				new Result(Decision.NOT_APPLICABLE, Status.createSuccess(),
				Collections.<Attributes>emptyList()));
		
		replay(context);
		Collection<Result> results = profile.handle(request, context);
		assertEquals(1, results.size());
		Result r = results.iterator().next();
	
		assertEquals(Decision.NOT_APPLICABLE, r.getDecision());
		assertEquals(Status.createSuccess(), r.getStatus());
		
		assertEquals(0, r.getAttributes().size());
		assertEquals(0, r.getObligations().size());
		assertEquals(0, r.getAssociatedAdvice().size());
		verify(context);
	}
	
	@Test
	public void testVariousResults()
	{
		
		
		RequestContext request = new RequestContext(false, true,
				Arrays.asList(subject0, subject1, resource0, resource1));
		
		Capture<RequestContext> c0 = new Capture<RequestContext>();
		Capture<RequestContext> c1 = new Capture<RequestContext>();
		Capture<RequestContext> c2 = new Capture<RequestContext>();
		Capture<RequestContext> c3 = new Capture<RequestContext>();
		
		expect(context.requestDecision(capture(c0))).andReturn(
					new Result(Decision.NOT_APPLICABLE, Status.createSuccess(), 
						Collections.<Attributes>emptyList()));
		
		expect(context.requestDecision(capture(c1))).andReturn(
				new Result(Decision.DENY, Status.createSuccess(), 
						Collections.<Attributes>emptyList()));
		
		expect(context.requestDecision(capture(c2))).andReturn(
				new Result(Decision.PERMIT, Status.createSuccess(),
				Collections.<Attributes>emptyList()));
		
		expect(context.requestDecision(capture(c3))).andReturn(
				new Result(Decision.PERMIT, Status.createSuccess(),
				Collections.<Attributes>emptyList()));
		
		replay(context);
		Collection<Result> results = profile.handle(request, context);
		assertEquals(1, results.size());
		Result r = results.iterator().next();
	
		assertEquals(Decision.INDETERMINATE, r.getDecision());
		assertEquals(Status.createProcessingError(), r.getStatus());
		
		assertEquals(0, r.getAttributes().size());
		assertEquals(0, r.getObligations().size());
		assertEquals(0, r.getAssociatedAdvice().size());
		verify(context);
	}
	
	@Test
	public void testRequestWithSingleResultCombine()
	{
		RequestContext request = new RequestContext(false, true,
				Arrays.asList(subject0, resource0));
		
		Capture<RequestContext> c0 = new Capture<RequestContext>();
		
		expect(context.requestDecision(capture(c0))).andReturn(
				new Result(Decision.INDETERMINATE, 
						new Status(StatusCode.createProcessingError()), 
						Collections.<Attributes>emptyList()));
		
		replay(context);
		Collection<Result> results = profile.handle(request, context);
		assertEquals(new Status(StatusCode.createProcessingError()), results.iterator().next().getStatus());
		assertEquals(1, results.size());
		RequestContext r0 = c0.getValue();
		assertTrue(r0.getAttributes(AttributeCategories.SUBJECT_ACCESS).contains(subject0));
		assertTrue(r0.getAttributes(AttributeCategories.RESOURCE).contains(resource0));
		verify(context);
	}
	
	@Test
	public void testWithEmptyRequest()
	{
		RequestContext request = new RequestContext(false, true,
				Collections.<Attributes>emptyList());
		
		Capture<RequestContext> c0 = new Capture<RequestContext>();
		
		expect(context.requestDecision(capture(c0))).andReturn(
				new Result(Decision.INDETERMINATE, 
						new Status(StatusCode.createProcessingError()),
						Collections.<Attributes>emptyList()));
		
		replay(context);
		Collection<Result> results = profile.handle(request, context);
		assertEquals(new Status(StatusCode.createProcessingError()), results.iterator().next().getStatus());
		assertEquals(1, results.size());
		assertSame(request, c0.getValue());
		verify(context);
	}
}


