package com.artagon.xacml.v3.profiles;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.PolicyDecisionCallback;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestProfileHandler;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.Status;
import com.artagon.xacml.v3.StatusCode;
import com.artagon.xacml.v3.impl.DefaultAttribute;
import com.artagon.xacml.v3.impl.DefaultAttributes;
import com.artagon.xacml.v3.impl.DefaultRequest;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.google.common.collect.Iterables;

public class MultipleResourcesHandlerTest 
{
	private PolicyDecisionCallback pdp;
	private RequestProfileHandler profile;
	
	@Before
	public void init(){
		this.pdp = createStrictMock(PolicyDecisionCallback.class);
		this.profile = new MultipleResourcesHandler();
	}
	
	@Test
	public void testResolveRequestsWithValidReferences()
	{
		Collection<Attribute> resource0Attr = new LinkedList<Attribute>();
		resource0Attr.add(new DefaultAttribute("testId1", DataTypes.STRING.create("value0")));
		resource0Attr.add(new DefaultAttribute("testId2", DataTypes.STRING.create("value1")));
		Attributes resource0 = new DefaultAttributes(AttributeCategoryId.RESOURCE, resource0Attr);
		
		Collection<Attribute> resource1Attr = new LinkedList<Attribute>();
		resource1Attr.add(new DefaultAttribute("testId3", DataTypes.STRING.create("value0")));
		resource1Attr.add(new DefaultAttribute("testId4", DataTypes.STRING.create("value1")));
		Attributes resource1 = new DefaultAttributes(AttributeCategoryId.RESOURCE, resource1Attr);
		
		Collection<Attribute> subjectAttr = new LinkedList<Attribute>();
		subjectAttr.add(new DefaultAttribute("testId7", DataTypes.STRING.create("value0")));
		subjectAttr.add(new DefaultAttribute("testId8", DataTypes.STRING.create("value1")));
		Attributes subject =  new DefaultAttributes(AttributeCategoryId.SUBJECT_ACCESS, subjectAttr);
		
		Request context = new DefaultRequest(false, 
				Arrays.asList(subject, resource0, resource1));
		
		Capture<DefaultRequest> c0 = new Capture<DefaultRequest>();
		Capture<DefaultRequest> c1 = new Capture<DefaultRequest>();
		
		expect(pdp.requestDecision(capture(c0))).andReturn(
				new Result(new Status(StatusCode.createProcessingError())));
		expect(pdp.requestDecision(capture(c1))).andReturn(
				new Result(new Status(StatusCode.createProcessingError())));
		replay(pdp);
		Collection<Result> results = profile.handle(context, pdp);
		assertEquals(2, results.size());
		Request r0 = c0.getValue();
		Request r1 = c1.getValue();
		assertEquals(2, r0.getAttributes().size());
		assertTrue(r0.getAttributes().contains(subject));
		assertEquals(1, Iterables.size(r0.getAttributes(AttributeCategoryId.RESOURCE)));
		assertEquals(2, Iterables.size(r1.getAttributes()));
		assertTrue(Iterables.contains(r1.getAttributes(), subject));
		assertEquals(1, Iterables.size(r1.getAttributes(AttributeCategoryId.RESOURCE)));
		verify(pdp);
	}
}

