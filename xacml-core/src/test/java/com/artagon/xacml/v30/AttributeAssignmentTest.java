package com.artagon.xacml.v30;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.artagon.xacml.v30.core.AttributeCategories;
import com.artagon.xacml.v30.types.IntegerType;

public class AttributeAssignmentTest 
{
	@Test
	public void testCreateAndEquals()
	{
		AttributeAssignment a0 = new AttributeAssignment("testId", AttributeCategories.ACTION, null, IntegerType.INTEGER.create(10));
		assertEquals("testId", a0.getAttributeId());
		assertEquals(AttributeCategories.ACTION, a0.getCategory());
		assertEquals(IntegerType.INTEGER.create(10), a0.getAttribute());
		AttributeAssignment a1 = new AttributeAssignment("testId", AttributeCategories.ACTION, null, IntegerType.INTEGER.create(10));
		assertEquals(a0, a1);
	}
}
