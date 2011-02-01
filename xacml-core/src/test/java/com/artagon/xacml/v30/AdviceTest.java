package com.artagon.xacml.v30;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;

import com.artagon.xacml.v30.types.IntegerType;
import com.google.common.collect.ImmutableList;

public class AdviceTest 
{
	@Test
	public void testCreateAttributeAssignmentsWithDifferentIds()
	{
		AttributeAssignment attr1 = new AttributeAssignment("testId1", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(0));
		AttributeAssignment attr2 = new AttributeAssignment("testId2", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(0));
		Advice a = new Advice("testId", ImmutableList.of(attr1, attr2));
		assertEquals("testId", a.getId());
		assertTrue(a.getAttribute("testId1").contains(new AttributeAssignment("testId1", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(0))));
		assertFalse(a.getAttribute("testId1").contains(attr2));
		assertFalse(a.getAttribute("testId0").contains(attr1));

	}
	
	@Test
	public void testEquals()
	{
		AttributeAssignment attr1 = new AttributeAssignment("testId1", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(0));
		AttributeAssignment attr2 = new AttributeAssignment("testId2", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(0));
		AttributeAssignment attr3 = new AttributeAssignment("testId2", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(1));
		Advice a1 = new Advice("testId", ImmutableList.of(attr1, attr2));
		Advice a2 = new Advice("testId", ImmutableList.of(attr1, attr2));
		Advice a3 = new Advice("testId", ImmutableList.of(attr1, attr3));
		Advice a4 = new Advice("id", ImmutableList.of(attr1, attr2));
		assertEquals(a1, a2);
		assertFalse(a1.equals(a3));
		assertFalse(a1.equals(a4));
	}
	
	@Test
	public void testCreateAttributeAssignmentsWithSameIds()
	{
		AttributeAssignment attr1 = new AttributeAssignment("testId1", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(0));
		AttributeAssignment attr2 = new AttributeAssignment("testId1", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(1));
		Advice a = new Advice("testId", ImmutableList.of(attr1, attr2));
		
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
		Advice a = new Advice("testId", data);
		data.clear();
		assertTrue(a.getAttribute("testId1").contains(attr1));
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testImmutability2()
	{
		AttributeAssignment attr1 = new AttributeAssignment("testId1", AttributeCategories.SUBJECT_ACCESS, "testIssuer", IntegerType.INTEGER.create(0));
		Collection<AttributeAssignment> data = new LinkedList<AttributeAssignment>();
		data.add(attr1);
		Advice a = new Advice("testId", data);
		a.getAttribute("testId1").clear();
	}
}
