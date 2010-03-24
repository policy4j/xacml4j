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
import java.util.Collections;
import java.util.LinkedList;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.AttributesReference;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestProcessingException;
import com.artagon.xacml.v3.RequestProcessingPipelineCallback;
import com.artagon.xacml.v3.RequestProcessingProfile;
import com.artagon.xacml.v3.RequestReference;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.Status;
import com.artagon.xacml.v3.StatusCode;
import com.artagon.xacml.v3.policy.type.DataTypes;

public class MultipleRequestProfileTest 
{
	private RequestProcessingPipelineCallback callback;
	private RequestProcessingProfile profile;
	
	@Before
	public void init(){
		this.profile = new MultipleAttributesByReferenceProfile();
		this.callback = createStrictMock(RequestProcessingPipelineCallback.class);
	}
	
	@Test
	public void testResolveRequestsWithValidReferences() throws RequestProcessingException
	{
		Collection<Attribute> attributes0 = new LinkedList<Attribute>();
		attributes0.add(new Attribute("testId1", DataTypes.STRING.create("value0")));
		attributes0.add(new Attribute("testId2", DataTypes.STRING.create("value1")));
		Attributes attr0 = new Attributes("resourceAttr0",  AttributeCategoryId.RESOURCE, attributes0);
		
		Collection<Attribute> attributes1 = new LinkedList<Attribute>();
		attributes1.add(new Attribute("testId3", DataTypes.STRING.create("value0")));
		attributes1.add(new Attribute("testId4", DataTypes.STRING.create("value1")));
		Attributes attr1 = new Attributes("resourceAttr1",  AttributeCategoryId.RESOURCE, attributes0);
		
		Collection<Attribute> attributes2 = new LinkedList<Attribute>();
		attributes2.add(new Attribute("testId5", DataTypes.STRING.create("value0")));
		attributes2.add(new Attribute("testId6", DataTypes.STRING.create("value1")));
		Attributes attr2 = new Attributes("subjectAttr0",  AttributeCategoryId.SUBJECT_ACCESS, attributes0);
		
		Collection<Attribute> attributes3 = new LinkedList<Attribute>();
		attributes3.add(new Attribute("testId7", DataTypes.STRING.create("value0")));
		attributes3.add(new Attribute("testId8", DataTypes.STRING.create("value1")));
		Attributes attr3 = new Attributes("subjectAttr1",  AttributeCategoryId.SUBJECT_ACCESS, attributes0);
		
		
		RequestReference reference0 = new RequestReference(
				new AttributesReference("resourceAttr0"), new AttributesReference("subjectAttr0"));
		RequestReference reference1 = new RequestReference(
				new AttributesReference("resourceAttr1"), new AttributesReference("subjectAttr1"));
		
		Request context = new Request(false, 
				Arrays.asList(attr0, attr1, attr2, attr3), 
				Arrays.asList(reference0, reference1));
		
		Capture<Request> c0 = new Capture<Request>();
		Capture<Request> c1 = new Capture<Request>();
		expect(callback.invokeNext(capture(c0))).andReturn(
				Collections.singleton(new Result(new Status(StatusCode.createProcessingError()))));
		expect(callback.invokeNext(capture(c1))).andReturn(
				Collections.singleton(new Result(new Status(StatusCode.createProcessingError()))));
		replay(callback);
		profile.process(context, callback).iterator();
		Request context0 = c0.getValue();
		Request context1 = c0.getValue();
		assertNotNull(context0.getAttributes(AttributeCategoryId.SUBJECT_ACCESS, "testId5"));
		assertNotNull(context0.getAttributes(AttributeCategoryId.SUBJECT_ACCESS, "testId6"));
		assertNotNull(context0.getAttributes(AttributeCategoryId.RESOURCE, "testId1"));
		assertNotNull(context0.getAttributes(AttributeCategoryId.RESOURCE, "testId2"));
		assertEquals(2, context0.getAttributes().size());
		assertEquals(1, context0.getAttributes(AttributeCategoryId.SUBJECT_ACCESS).size());
		assertEquals(1, context0.getAttributes(AttributeCategoryId.RESOURCE).size());
		
		assertNotNull(context1.getAttributes(AttributeCategoryId.SUBJECT_ACCESS, "testId7"));
		assertNotNull(context1.getAttributes(AttributeCategoryId.SUBJECT_ACCESS, "testId8"));
		assertNotNull(context1.getAttributes(AttributeCategoryId.RESOURCE, "testId3"));
		assertNotNull(context1.getAttributes(AttributeCategoryId.RESOURCE, "testId4"));
		assertEquals(2, context1.getAttributes().size());
		assertEquals(1, context1.getAttributes(AttributeCategoryId.SUBJECT_ACCESS).size());
		assertEquals(1, context1.getAttributes(AttributeCategoryId.RESOURCE).size());
		verify(callback);
	}
}
