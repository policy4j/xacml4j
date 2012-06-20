package com.artagon.xacml.v30.pdp;

import static com.artagon.xacml.v30.types.StringType.STRING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.XPathVersion;


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
		resource0Attr.add(new Attribute("testId10", STRING.create("value0")));
		resource0Attr.add(new Attribute("testId11", STRING.create("value1")));
		this.resource0 = new Attributes(AttributeCategories.RESOURCE, resource0Attr);
		
		Collection<Attribute> resource1Attr = new LinkedList<Attribute>();
		resource1Attr.add(new Attribute("testId11", STRING.create("value0")));
		resource1Attr.add(new Attribute("testId22", STRING.create("value1")));
		resource1Attr.add(new Attribute("testId23", null, true, Arrays.<AttributeExp>asList(STRING.create("value2"))));
		resource1Attr.add(new Attribute("testId24","testIssuer", true, Arrays.<AttributeExp>asList(STRING.create("value2"))));
		this.resource1 = new Attributes(AttributeCategories.RESOURCE, resource1Attr);
		
		Collection<Attribute> subject0Attr = new LinkedList<Attribute>();
		subject0Attr.add(new Attribute("testId31", STRING.create("value0")));
		subject0Attr.add(new Attribute("testId32", STRING.create("value1")));
		this.subject0 =  new Attributes(AttributeCategories.SUBJECT_ACCESS, subject0Attr);
		
		Collection<Attribute> subject1Attr = new LinkedList<Attribute>();
		subject1Attr.add(new Attribute("testId41", STRING.create("value0")));
		subject1Attr.add(new Attribute("testId42", STRING.create("value1")));
		subject1Attr.add(new Attribute("testId43","testIssuer", true, Arrays.<AttributeExp>asList(STRING.create("value2"))));
		this.subject1 =  new Attributes(AttributeCategories.SUBJECT_CODEBASE, subject1Attr);
	}
	
	@Test
	public void testHasRepeatingCategories()
	{	
		RequestContext request = new RequestContext(false, 
				Arrays.asList(subject0, resource0));
		assertFalse(request.containsRepeatingCategories());
		request = new RequestContext(false, 
				Arrays.asList(subject0, resource0, resource1));
		assertTrue(request.containsRepeatingCategories());
	}
	
	
	@Test
	public void testCreateRequest()
	{

		RequestContext request1 = new RequestContext(false, 
				Arrays.asList(subject0, resource0, resource1));
		assertFalse(request1.isReturnPolicyIdList());
		assertEquals(3, request1.getAttributes().size());
		assertTrue(request1.getAttributes(AttributeCategories.RESOURCE).contains(resource0));
		assertTrue(request1.getAttributes(AttributeCategories.RESOURCE).contains(resource1));
		assertTrue(request1.getAttributes(AttributeCategories.SUBJECT_ACCESS).contains(subject0));
		
		RequestContext request2 = new RequestContext(true, 
				Arrays.asList(subject0, resource0, resource1));
		
		assertTrue(request2.isReturnPolicyIdList());
		assertTrue(request1.getAttributes(AttributeCategories.RESOURCE).contains(resource0));
		assertTrue(request1.getAttributes(AttributeCategories.RESOURCE).contains(resource1));
		assertTrue(request1.getAttributes(AttributeCategories.SUBJECT_ACCESS).contains(subject0));
	}
	
	@Test
	public void testGetAttributesByCategory()
	{

		RequestContext request = new RequestContext(false, 
				Arrays.asList(subject0, resource0, resource1));
		Collection<Attributes> attr = request.getAttributes(AttributeCategories.RESOURCE);
		assertEquals(2, attr.size());
		assertTrue(attr.contains(resource0));
		assertTrue(attr.contains(resource1));
		attr = request.getAttributes(AttributeCategories.ENVIRONMENT);
		assertEquals(0, attr.size());		
	}
	
	@Test
	public void testGetAttributeByCategory()
	{

		RequestContext request = new RequestContext(false, 
				Arrays.asList(subject0, resource0));
		Collection<Attributes> attr = request.getAttributes(AttributeCategories.ACTION);
		assertNotNull(attr);		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testGetOnlyAttributesMultipleInstancesOfTheSameCategory()
	{
		RequestContext request = new RequestContext(false, 
				Arrays.asList(subject0, resource0, resource1));
		request.getOnlyAttributes(AttributeCategories.RESOURCE);		
	}
	
	@Test
	public void testGetOnlyAttributeSingleInstanceOfTheSameCategory()
	{
		RequestContext request = new RequestContext(false, 
				Arrays.asList(subject0, resource0));
		Attributes attr = request.getOnlyAttributes(AttributeCategories.RESOURCE);		
		assertEquals(resource0, attr);
	}
	
	@Test
	public void testGetRequestDefaults(){
		RequestContext request = new RequestContext(false, 
				Arrays.asList(subject0, resource0, resource1));
		assertNotNull(request.getRequestDefaults());
		assertEquals(XPathVersion.XPATH1, request.getRequestDefaults().getXPathVersion());
	}
	
	@Test
	public void testGetIncludeInResult()
	{
		
		RequestContext request0 = new RequestContext(false, 
				Arrays.asList(subject0, resource0));
		
		assertEquals(0, request0.getIncludeInResultAttributes().size());
		
		RequestContext request1 = new RequestContext(false, 
				Arrays.asList(subject0, subject1, resource0, resource1));
		
		assertEquals(2, request1.getIncludeInResultAttributes().size());
		
		RequestContext request2 = new RequestContext(false, 
				Arrays.asList(subject0, subject1, resource0, resource1));
		assertEquals(2, request2.getIncludeInResultAttributes().size());	
	}
}
