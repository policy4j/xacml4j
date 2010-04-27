package com.artagon.xacml.v3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;

import com.artagon.xacml.v3.policy.type.DataTypes;

public class DefaultRequestTest 
{
	@Test
	public void testCreateRequest()
	{
		Collection<Attribute> resource0Attr = new LinkedList<Attribute>();
		resource0Attr.add(new Attribute("testId1", DataTypes.STRING.create("value0")));
		resource0Attr.add(new Attribute("testId2", DataTypes.STRING.create("value1")));
		Attributes resource0 = new Attributes(AttributeCategoryId.RESOURCE, resource0Attr);
		
		Collection<Attribute> resource1Attr = new LinkedList<Attribute>();
		resource1Attr.add(new Attribute("testId3", DataTypes.STRING.create("value0")));
		resource1Attr.add(new Attribute("testId4", DataTypes.STRING.create("value1")));
		Attributes resource1 = new Attributes(AttributeCategoryId.RESOURCE, resource1Attr);
		
		Collection<Attribute> subjectAttr = new LinkedList<Attribute>();
		subjectAttr.add(new Attribute("testId7", DataTypes.STRING.create("value0")));
		subjectAttr.add(new Attribute("testId8", DataTypes.STRING.create("value1")));
		Attributes subject =  new Attributes(AttributeCategoryId.SUBJECT_ACCESS, subjectAttr);
		
		Request request1 = new DefaultRequest(false, 
				Arrays.asList(subject, resource0, resource1));
		assertFalse(request1.isReturnPolicyIdList());
		assertEquals(3, request1.getAttributes().size());
		assertTrue(request1.getAttributes().contains(resource0));
		assertTrue(request1.getAttributes().contains(resource1));
		assertTrue(request1.getAttributes().contains(subject));
		
		Request request2 = new DefaultRequest(true, 
				Arrays.asList(subject, resource0, resource1));
		
		assertTrue(request2.isReturnPolicyIdList());
	}
	
	@Test
	public void testBehavior()
	{
		Collection<Attribute> resource0Attr = new LinkedList<Attribute>();
		resource0Attr.add(new Attribute("testId11", DataTypes.STRING.create("value0")));
		resource0Attr.add(new Attribute("testId12", DataTypes.STRING.create("value1")));
		resource0Attr.add(new Attribute("testId13","testIssuer", 
				true, Arrays.asList(DataTypes.STRING.create("value2"))));
		Attributes resource0 = new Attributes(AttributeCategoryId.RESOURCE, resource0Attr);
		
		Collection<Attribute> resource1Attr = new LinkedList<Attribute>();
		resource1Attr.add(new Attribute("testId21", DataTypes.STRING.create("value0")));
		resource1Attr.add(new Attribute("testId22", DataTypes.STRING.create("value1")));
		resource1Attr.add(new Attribute("testId23","testIssuer", true, Arrays.asList(DataTypes.STRING.create("value2"))));
		Attributes resource1 = new Attributes(AttributeCategoryId.RESOURCE, resource1Attr);
		
		Collection<Attribute> subjectAttr = new LinkedList<Attribute>();
		subjectAttr.add(new Attribute("testId7", DataTypes.STRING.create("value0")));
		subjectAttr.add(new Attribute("testId8", DataTypes.STRING.create("value1")));
		Attributes subject =  new Attributes(AttributeCategoryId.SUBJECT_ACCESS, subjectAttr);
		
		Request request1 = new DefaultRequest(false, 
				Arrays.asList(subject, resource0, resource1));
		
		assertEquals(2, request1.getCategoryOccuriences(AttributeCategoryId.RESOURCE));
		assertEquals(1, request1.getCategoryOccuriences(AttributeCategoryId.SUBJECT_ACCESS));
		
		assertEquals(2, request1.getIncludeInResultAttributes().size());
		
		
	}
}
