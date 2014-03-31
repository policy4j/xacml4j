package org.xacml4j.v30.pdp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.types.IntegerExp;


public class AttributeAssignmentTest
{
	@Test
	public void testCreateAndEquals()
	{
		AttributeAssignment a0 =  AttributeAssignment.builder()
				.id("testId")
				.category(Categories.ACTION)
				.value(IntegerExp.valueOf(10))
				.build();
		assertEquals("testId", a0.getAttributeId());
		assertEquals(Categories.ACTION, a0.getCategory());
		assertEquals(IntegerExp.valueOf(10), a0.getAttribute());
		AttributeAssignment a1 =  AttributeAssignment.builder()
				.id("testId")
				.category(Categories.ACTION)
				.value(IntegerExp.valueOf(10))
				.build();
		assertEquals(a0, a1);
	}
}
