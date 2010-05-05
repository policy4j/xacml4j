package com.artagon.xacml.v3.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.impl.DefaultAttribute;
import com.artagon.xacml.v3.impl.DefaultAttributes;
import com.artagon.xacml.v3.impl.DefaultRequest;
import com.artagon.xacml.v3.policy.type.DataTypes;

public class DefaultRequestTest 
{
	private Attributes resource0;
	private Attributes resource1;
	private Attributes subject0;
	private Attributes subject1;
	
	@Before
	public void init()
	{
		Collection<Attribute> resource0Attr = new LinkedList<Attribute>();
		resource0Attr.add(new DefaultAttribute("testId10", DataTypes.STRING.create("value0")));
		resource0Attr.add(new DefaultAttribute("testId11", DataTypes.STRING.create("value1")));
		this.resource0 = new DefaultAttributes(AttributeCategoryId.RESOURCE, resource0Attr);
		
		Collection<Attribute> resource1Attr = new LinkedList<Attribute>();
		resource1Attr.add(new DefaultAttribute("testId11", DataTypes.STRING.create("value0")));
		resource1Attr.add(new DefaultAttribute("testId22", DataTypes.STRING.create("value1")));
		resource1Attr.add(new DefaultAttribute("testId23", null, true, Arrays.asList(DataTypes.STRING.create("value2"))));
		resource1Attr.add(new DefaultAttribute("testId24","testIssuer", true, Arrays.asList(DataTypes.STRING.create("value2"))));
		this.resource1 = new DefaultAttributes(AttributeCategoryId.RESOURCE, resource1Attr);
		
		Collection<Attribute> subject0Attr = new LinkedList<Attribute>();
		subject0Attr.add(new DefaultAttribute("testId31", DataTypes.STRING.create("value0")));
		subject0Attr.add(new DefaultAttribute("testId32", DataTypes.STRING.create("value1")));
		this.subject0 =  new DefaultAttributes(AttributeCategoryId.SUBJECT_ACCESS, subject0Attr);
		
		Collection<Attribute> subject1Attr = new LinkedList<Attribute>();
		subject1Attr.add(new DefaultAttribute("testId41", DataTypes.STRING.create("value0")));
		subject1Attr.add(new DefaultAttribute("testId42", DataTypes.STRING.create("value1")));
		subject1Attr.add(new DefaultAttribute("testId43","testIssuer", true, Arrays.asList(DataTypes.STRING.create("value2"))));
		this.subject1 =  new DefaultAttributes(AttributeCategoryId.SUBJECT_CODEBASE, subject1Attr);
	}
	
	public void testHasDublicateCategories()
	{	
		Request request = new DefaultRequest(false, 
				Arrays.asList(subject0, resource0));
		assertFalse(request.hasRepeatingCategories());
		request = new DefaultRequest(false, 
				Arrays.asList(subject0, resource0, resource1));
		assertTrue(request.hasRepeatingCategories());
	}
	
	public void testGetAttrobutesByCategoryId()
	{	
		Request request = new DefaultRequest(false, 
				Arrays.asList(subject0, resource0));
		Collection<Attributes> attr = request.getAttributes(AttributeCategoryId.RESOURCE, "testId11");
		assertEquals(2, attr.size());
	}
	
	@Test
	public void testCreateRequest()
	{

		Request request1 = new DefaultRequest(false, 
				Arrays.asList(subject0, resource0, resource1));
		assertFalse(request1.isReturnPolicyIdList());
		assertEquals(3, request1.getAttributes().size());
		assertTrue(request1.getAttributes().contains(resource0));
		assertTrue(request1.getAttributes().contains(resource1));
		assertTrue(request1.getAttributes().contains(subject0));
		
		Request request2 = new DefaultRequest(true, 
				Arrays.asList(subject0, resource0, resource1));
		
		assertTrue(request2.isReturnPolicyIdList());
		assertTrue(request1.getAttributes().contains(resource0));
		assertTrue(request1.getAttributes().contains(resource1));
		assertTrue(request1.getAttributes().contains(subject0));
	}
	
	@Test
	public void testGetAttributesByCategory()
	{

		Request request = new DefaultRequest(false, 
				Arrays.asList(subject0, resource0, resource1));
		Collection<Attributes> attr = request.getAttributes(AttributeCategoryId.RESOURCE);
		assertEquals(2, attr.size());
		assertTrue(attr.contains(resource0));
		assertTrue(attr.contains(resource1));
		attr = request.getAttributes(AttributeCategoryId.ENVIRONMENT);
		assertEquals(0, attr.size());		
	}
	
	@Test
	public void testGetIncludeInResult()
	{
		
		Request request0 = new DefaultRequest(false, 
				Arrays.asList(subject0, resource0));
		
		assertEquals(0, request0.getIncludeInResultAttributes().size());
		
		Request request1 = new DefaultRequest(false, 
				Arrays.asList(subject0, subject1, resource0, resource1));
		
		assertEquals(2, request1.getIncludeInResultAttributes().size());
		
		Request request2 = new DefaultRequest(false, 
				Arrays.asList(subject0, subject1, resource0, resource1));
		assertEquals(2, request2.getIncludeInResultAttributes().size());	
	}
}
