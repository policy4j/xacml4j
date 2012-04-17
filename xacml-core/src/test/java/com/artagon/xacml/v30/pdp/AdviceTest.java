package com.artagon.xacml.v30.pdp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;

import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.types.IntegerType;

public class AdviceTest 
{
	@Test
	public void testCreateAttributeAssignmentsWithDifferentIds()
	{
		AttributeAssignment attr1 = new AttributeAssignment("testId1", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(0));
		AttributeAssignment attr2 = new AttributeAssignment("testId2", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(0));
		Advice a = Advice.builder("testId", Effect.DENY)
				.attribute(attr1)
				.attribute(attr2)
				.create();
		assertEquals("testId", a.getId());
		assertTrue(a.getAttribute("testId1").contains(new AttributeAssignment("testId1", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(0))));
		assertFalse(a.getAttribute("testId1").contains(attr2));
		assertFalse(a.getAttribute("testId0").contains(attr1));
	}
	
	@Test
	public void testAdviceMergeDifferentAttributeIds()
	{
		AttributeAssignment attr1 = new AttributeAssignment("testId1", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(0));
		AttributeAssignment attr2 = new AttributeAssignment("testId2", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(0));
		Advice a1 = Advice.builder("testId", Effect.DENY)
				.attribute(attr1)
				.attribute(attr2)
				.create();
		AttributeAssignment attr3 = new AttributeAssignment("testId3", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(0));
		AttributeAssignment attr4 = new AttributeAssignment("testId4", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(0));
		Advice a2 = Advice.builder("testId", Effect.DENY)
				.attribute(attr3)
				.attribute(attr4)
				.create();
		Advice a = a1.merge(a2);
		assertEquals(a1.getId(), a.getId());
		assertTrue(a.getAttribute("testId1").contains(attr1));
		assertTrue(a.getAttribute("testId2").contains(attr2));
		assertTrue(a.getAttribute("testId3").contains(attr3));
		assertTrue(a.getAttribute("testId4").contains(attr4));
	}
	
	@Test
	public void testAdviceMergeHasSameAttributeIds()
	{
		AttributeAssignment attr1 = new AttributeAssignment("testId1", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(0));
		AttributeAssignment attr2 = new AttributeAssignment("testId2", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(0));
		Advice a1 = Advice.builder("testId", Effect.DENY)
				.attribute(attr1)
				.attribute(attr2)
				.create();
		AttributeAssignment attr4 = new AttributeAssignment("testId4", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(0));
		Advice a2 = Advice.builder("testId", Effect.DENY)
				.attribute(attr1)
				.attribute(attr4)
				.create();
		Advice a = a1.merge(a2);
		assertEquals(a1.getId(), a.getId());
		assertTrue(a.getAttribute("testId1").contains(attr1));
		assertTrue(a.getAttribute("testId2").contains(attr2));
		assertTrue(a.getAttribute("testId4").contains(attr4));
	}
	
	@Test
	public void testEquals()
	{
		AttributeAssignment attr1 = new AttributeAssignment("testId1", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(0));
		AttributeAssignment attr2 = new AttributeAssignment("testId2", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(0));
		AttributeAssignment attr3 = new AttributeAssignment("testId2", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(1));
		
		Advice a1 = Advice.builder("testId", Effect.DENY)
				.attribute(attr1)
				.attribute(attr2)
				.create();
		Advice a2 = Advice.builder("testId", Effect.PERMIT)
				.attribute(attr1)
				.attribute(attr2)
				.create();
		Advice a3 = Advice.builder("testId", Effect.PERMIT)
				.attribute(attr1)
				.attribute(attr3)
				.create();
		Advice a4 = Advice.builder("id", Effect.PERMIT)
				.attribute(attr1)
				.attribute(attr2)
				.create();
		assertEquals(a1, a2);
		assertFalse(a1.equals(a3));
		assertFalse(a1.equals(a4));
	}
	
	@Test
	public void testCreateAttributeAssignmentsWithSameIds()
	{
		AttributeAssignment attr1 = new AttributeAssignment("testId1", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(0));
		AttributeAssignment attr2 = new AttributeAssignment("testId1", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(1));
		Advice a = Advice.builder("testId", Effect.DENY)
				.attribute(attr1)
				.attribute(attr2)
				.create();
		
		assertEquals("testId", a.getId());
		assertTrue(a.getAttribute("testId1").contains(new AttributeAssignment("testId1", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(0))));
		assertTrue(a.getAttribute("testId1").contains(new AttributeAssignment("testId1", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(1))));
	}
	
	@Test
	public void testImmutability1()
	{
		AttributeAssignment attr1 = new AttributeAssignment("testId1", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(0));
		Collection<AttributeAssignment> data = new LinkedList<AttributeAssignment>();
		data.add(attr1);
		Advice a = Advice.builder("testId", Effect.PERMIT)
				.attributes(data)
				.create();
		data.clear();
		assertTrue(a.getAttribute("testId1").contains(attr1));
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testImmutability2()
	{
		AttributeAssignment attr1 = new AttributeAssignment("testId1", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(0));
		Collection<AttributeAssignment> data = new LinkedList<AttributeAssignment>();
		data.add(attr1);
		Advice a = Advice.builder("testId", Effect.DENY)
				.attributes(data)
				.create();
		a.getAttribute("testId1").clear();
	}
}
