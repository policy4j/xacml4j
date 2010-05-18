package com.artagon.xacml.v3.profiles;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.AttributesReference;
import com.artagon.xacml.v3.PolicyDecisionCallback;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestProfileHandler;
import com.artagon.xacml.v3.RequestReference;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.Status;
import com.artagon.xacml.v3.StatusCode;
import com.artagon.xacml.v3.impl.DefaultAttribute;
import com.artagon.xacml.v3.impl.DefaultAttributes;
import com.artagon.xacml.v3.impl.DefaultRequest;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.google.common.collect.Iterables;

public class MultipleRequestHandlerTest 
{
	private PolicyDecisionCallback pdp;
	private RequestProfileHandler profile;
	
	@Before
	public void init(){
		this.pdp = createStrictMock(PolicyDecisionCallback.class);
		this.profile = new MultipleRequestsHandler();
	}
	
	@Test
	public void testResolveRequestsWithValidReferences()
	{
		Collection<Attribute> attributes0 = new LinkedList<Attribute>();
		attributes0.add(new DefaultAttribute("testId1", DataTypes.STRING.create("value0")));
		attributes0.add(new DefaultAttribute("testId2", DataTypes.STRING.create("value1")));
		Attributes attr0 = new DefaultAttributes("resourceAttr0",  AttributeCategoryId.RESOURCE, attributes0);
		
		Collection<Attribute> attributes1 = new LinkedList<Attribute>();
		attributes1.add(new DefaultAttribute("testId3", DataTypes.STRING.create("value0")));
		attributes1.add(new DefaultAttribute("testId4", DataTypes.STRING.create("value1")));
		DefaultAttributes attr1 = new DefaultAttributes("resourceAttr1",  AttributeCategoryId.RESOURCE, attributes1);
		
		Collection<Attribute> attributes2 = new LinkedList<Attribute>();
		attributes2.add(new DefaultAttribute("testId5", DataTypes.STRING.create("value0")));
		attributes2.add(new DefaultAttribute("testId6", DataTypes.STRING.create("value1")));
		Attributes attr2 = new DefaultAttributes("subjectAttr0",  AttributeCategoryId.SUBJECT_ACCESS, attributes2);
		
		Collection<Attribute> attributes3 = new LinkedList<Attribute>();
		attributes3.add(new DefaultAttribute("testId7", DataTypes.STRING.create("value0")));
		attributes3.add(new DefaultAttribute("testId8", DataTypes.STRING.create("value1")));
		Attributes attr3 = new DefaultAttributes("subjectAttr1",  AttributeCategoryId.SUBJECT_ACCESS, attributes3);
		
		
		RequestReference reference0 = new RequestReference(
				new AttributesReference("resourceAttr0"), new AttributesReference("subjectAttr0"));
		RequestReference reference1 = new RequestReference(
				new AttributesReference("resourceAttr1"), new AttributesReference("subjectAttr1"));
		
		Request context = new DefaultRequest(false, 
				Arrays.asList(attr0, attr1, attr2, attr3), 
				Arrays.asList(reference0, reference1));
		
		Capture<DefaultRequest> c0 = new Capture<DefaultRequest>();
		Capture<DefaultRequest> c1 = new Capture<DefaultRequest>();
		
		expect(pdp.requestDecision(capture(c0))).andReturn(
				new Result(new Status(StatusCode.createProcessingError())));
		expect(pdp.requestDecision(capture(c1))).andReturn(
				new Result(new Status(StatusCode.createProcessingError())));
		replay(pdp);
		profile.handle(context, pdp).iterator();
		Request context0 = c0.getValue();
		Request context1 = c0.getValue();
		assertNotNull(Iterables.getOnlyElement(context0.getAttributes(AttributeCategoryId.SUBJECT_ACCESS)).getAttributes("testId5"));
		assertNotNull(Iterables.getOnlyElement(context0.getAttributes(AttributeCategoryId.SUBJECT_ACCESS)).getAttributes("testId6"));
		assertNotNull(Iterables.getOnlyElement(context0.getAttributes(AttributeCategoryId.RESOURCE)).getAttributes("testId1"));
		assertNotNull(Iterables.getOnlyElement(context0.getAttributes(AttributeCategoryId.RESOURCE)).getAttributes("testId2"));
		assertEquals(2, context0.getAttributes().size());
		assertEquals(1, context0.getAttributes(AttributeCategoryId.SUBJECT_ACCESS));
		assertEquals(1, context0.getAttributes(AttributeCategoryId.RESOURCE));
		
		assertNotNull(Iterables.getOnlyElement(context1.getAttributes(AttributeCategoryId.SUBJECT_ACCESS)).getAttributes("testId7"));
		assertNotNull(Iterables.getOnlyElement(context1.getAttributes(AttributeCategoryId.SUBJECT_ACCESS)).getAttributes("testId8"));
		assertNotNull(Iterables.getOnlyElement(context1.getAttributes(AttributeCategoryId.RESOURCE)).getAttributes("testId3"));
		assertNotNull(Iterables.getOnlyElement(context1.getAttributes(AttributeCategoryId.RESOURCE)).getAttributes("testId4"));
		assertEquals(2, context1.getAttributes().size());
		assertEquals(1, context1.getAttributes(AttributeCategoryId.SUBJECT_ACCESS));
		assertEquals(1, context1.getAttributes(AttributeCategoryId.RESOURCE));
		verify(pdp);
	}
	
	@Test
	public void testWithNoReferences()
	{
		Collection<Attribute> attributes0 = new LinkedList<Attribute>();
		attributes0.add(new DefaultAttribute("testId3", DataTypes.STRING.create("value0")));
		attributes0.add(new DefaultAttribute("testId4", DataTypes.STRING.create("value1")));
		Attributes attr0 = new DefaultAttributes("resourceAttr1",  AttributeCategoryId.RESOURCE, attributes0);
		
		Collection<Attribute> attributes1 = new LinkedList<Attribute>();
		attributes1.add(new DefaultAttribute("testId5", DataTypes.STRING.create("value0")));
		attributes1.add(new DefaultAttribute("testId6", DataTypes.STRING.create("value1")));
		Attributes attr1 = new DefaultAttributes("subjectAttr0",  AttributeCategoryId.SUBJECT_ACCESS, attributes1);
		
		Request request = new DefaultRequest(false, 
				Arrays.asList(attr0, attr1));
		
		expect(pdp.requestDecision(request)).andReturn(
				new Result(new Status(StatusCode.createProcessingError())));
		replay(pdp);
		Collection<Result> results = profile.handle(request, pdp);
		assertEquals(new Result(new Status(StatusCode.createProcessingError())), results.iterator().next());
		verify(pdp);
	}
}
