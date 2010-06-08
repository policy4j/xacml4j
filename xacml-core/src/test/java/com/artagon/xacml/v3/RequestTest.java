package com.artagon.xacml.v3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.types.XacmlDataTypes;

public class RequestTest 
{
	private Attributes resource0;
	private Attributes resource1;
	private Attributes subject0;
	private Attributes subject1;
	
	@Before
	public void init()
	{
		Collection<Attribute> resource0Attr = new LinkedList<Attribute>();
		resource0Attr.add(new Attribute("testId10", XacmlDataTypes.STRING.create("value0")));
		resource0Attr.add(new Attribute("testId11", XacmlDataTypes.STRING.create("value1")));
		this.resource0 = new Attributes(AttributeCategoryId.RESOURCE, resource0Attr);
		
		Collection<Attribute> resource1Attr = new LinkedList<Attribute>();
		resource1Attr.add(new Attribute("testId11", XacmlDataTypes.STRING.create("value0")));
		resource1Attr.add(new Attribute("testId22", XacmlDataTypes.STRING.create("value1")));
		resource1Attr.add(new Attribute("testId23", null, true, Arrays.asList(XacmlDataTypes.STRING.create("value2"))));
		resource1Attr.add(new Attribute("testId24","testIssuer", true, Arrays.asList(XacmlDataTypes.STRING.create("value2"))));
		this.resource1 = new Attributes(AttributeCategoryId.RESOURCE, resource1Attr);
		
		Collection<Attribute> subject0Attr = new LinkedList<Attribute>();
		subject0Attr.add(new Attribute("testId31", XacmlDataTypes.STRING.create("value0")));
		subject0Attr.add(new Attribute("testId32", XacmlDataTypes.STRING.create("value1")));
		this.subject0 =  new Attributes(AttributeCategoryId.SUBJECT_ACCESS, subject0Attr);
		
		Collection<Attribute> subject1Attr = new LinkedList<Attribute>();
		subject1Attr.add(new Attribute("testId41", XacmlDataTypes.STRING.create("value0")));
		subject1Attr.add(new Attribute("testId42", XacmlDataTypes.STRING.create("value1")));
		subject1Attr.add(new Attribute("testId43","testIssuer", true, Arrays.asList(XacmlDataTypes.STRING.create("value2"))));
		this.subject1 =  new Attributes(AttributeCategoryId.SUBJECT_CODEBASE, subject1Attr);
	}
	
	public void testHasDublicateCategories()
	{	
		Request request = new Request(false, 
				Arrays.asList(subject0, resource0));
		assertFalse(request.hasRepeatingCategories());
		request = new Request(false, 
				Arrays.asList(subject0, resource0, resource1));
		assertTrue(request.hasRepeatingCategories());
	}
	
	
	@Test
	public void testCreateRequest()
	{

		Request request1 = new Request(false, 
				Arrays.asList(subject0, resource0, resource1));
		assertFalse(request1.isReturnPolicyIdList());
		assertEquals(2, request1.getAttributes().size());
		assertTrue(request1.getAllAttributes(AttributeCategoryId.RESOURCE).contains(resource0));
		assertTrue(request1.getAllAttributes(AttributeCategoryId.RESOURCE).contains(resource1));
		assertTrue(request1.getAllAttributes(AttributeCategoryId.SUBJECT_ACCESS).contains(subject0));
		
		Request request2 = new Request(true, 
				Arrays.asList(subject0, resource0, resource1));
		
		assertTrue(request2.isReturnPolicyIdList());
		assertTrue(request1.getAllAttributes(AttributeCategoryId.RESOURCE).contains(resource0));
		assertTrue(request1.getAllAttributes(AttributeCategoryId.RESOURCE).contains(resource1));
		assertTrue(request1.getAllAttributes(AttributeCategoryId.SUBJECT_ACCESS).contains(subject0));
	}
	
	@Test
	public void testGetAttributesByCategory()
	{

		Request request = new Request(false, 
				Arrays.asList(subject0, resource0, resource1));
		Collection<Attributes> attr = request.getAllAttributes(AttributeCategoryId.RESOURCE);
		assertEquals(2, attr.size());
		assertTrue(attr.contains(resource0));
		assertTrue(attr.contains(resource1));
		attr = request.getAllAttributes(AttributeCategoryId.ENVIRONMENT);
		assertEquals(0, attr.size());		
	}
	
	@Test
	public void testGetRequestDefaults(){
		Request request = new Request(false, 
				Arrays.asList(subject0, resource0, resource1));
		assertNotNull(request.getRequestDefaults());
		assertEquals(XPathVersion.XPATH1, request.getRequestDefaults().getXPathVersion());
	}
	
	@Test
	public void testGetIncludeInResult()
	{
		
		Request request0 = new Request(false, 
				Arrays.asList(subject0, resource0));
		
		assertEquals(0, request0.getIncludeInResultAttributes().size());
		
		Request request1 = new Request(false, 
				Arrays.asList(subject0, subject1, resource0, resource1));
		
		assertEquals(2, request1.getIncludeInResultAttributes().size());
		
		Request request2 = new Request(false, 
				Arrays.asList(subject0, subject1, resource0, resource1));
		assertEquals(2, request2.getIncludeInResultAttributes().size());	
	}
}
