package org.xacml4j.v30.pdp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.types.IntegerExp;


public class AdviceTest
{
	@Test
	public void testCreateAttributeAssignmentsWithDifferentIds()
	{
		AttributeAssignment.Builder attr1Builder = AttributeAssignment.builder()
				.id("testId1")
				.category(Categories.SUBJECT_ACCESS)
				.issuer("testIssuer")
				.value(IntegerExp.valueOf(0));
		AttributeAssignment.Builder attr2Builder = AttributeAssignment.builder()
				.id("testId2")
				.category(Categories.SUBJECT_ACCESS)
				.issuer("testIssuer")
				.value(IntegerExp.valueOf(0));
		Advice a = Advice.builder("testId", Effect.DENY)
				.attribute(attr1Builder.build())
				.attribute(attr2Builder.build())
				.build();
		assertEquals("testId", a.getId());
		assertTrue(a.getAttribute("testId1").contains(attr1Builder.build()));
		assertFalse(a.getAttribute("testId1").contains(attr2Builder.build()));
		assertFalse(a.getAttribute("testId0").contains(attr1Builder.build()));
	}

	@Test
	public void testAdviceMergeDifferentAttributeIds()
	{
		AttributeAssignment.Builder attrBuilder = AttributeAssignment.builder()
				.id("testId1")
				.category(Categories.SUBJECT_ACCESS)
				.issuer("testIssuer")
				.value(IntegerExp.valueOf(0));

		Advice a1 = Advice.builder("testId", Effect.DENY)
				.attribute(attrBuilder.build())
				.attribute(attrBuilder.id("testId2").build())
				.build();

		Advice a2 = Advice.builder("testId", Effect.DENY)
				.attribute(attrBuilder.id("testId3").build())
				.attribute(attrBuilder.id("testId4").build())
				.build();
		Advice a = a1.merge(a2);
		assertEquals(a1.getId(), a.getId());

		assertTrue(a.getAttribute("testId1").contains(attrBuilder.id("testId1").build()));
		assertTrue(a.getAttribute("testId2").contains(attrBuilder.id("testId2").build()));
		assertTrue(a.getAttribute("testId3").contains(attrBuilder.id("testId3").build()));
		assertTrue(a.getAttribute("testId4").contains(attrBuilder.id("testId4").build()));
	}

	@Test
	public void testAdviceMergeHasSameAttributeIds()
	{
		AttributeAssignment.Builder attrBuilder = AttributeAssignment.builder()
				.id("testId1")
				.category(Categories.SUBJECT_ACCESS)
				.issuer("testIssuer")
				.value(IntegerExp.valueOf(0));

		Advice a1 = Advice.builder("testId", Effect.DENY)
				.attribute(attrBuilder.build())
				.attribute(attrBuilder.id("testId2").build())
				.build();


		Advice a2 = Advice.builder("testId", Effect.DENY)
				.attribute(attrBuilder.id("testId3").build())
				.attribute(attrBuilder.id("testId4").build())
				.build();

		Advice a = a1.merge(a2);
		assertEquals(a1.getId(), a.getId());
		assertTrue(a.getAttribute("testId1").contains(attrBuilder.id("testId1").build()));
		assertTrue(a.getAttribute("testId2").contains(attrBuilder.id("testId2").build()));
		assertTrue(a.getAttribute("testId4").contains(attrBuilder.id("testId4").build()));
	}

	@Test
	public void testEquals()
	{
		AttributeAssignment.Builder attrBuilder = AttributeAssignment.builder()
				.id("testId1")
				.category(Categories.SUBJECT_ACCESS)
				.issuer("testIssuer")
				.value(IntegerExp.valueOf(0));

		Advice a1 = Advice.builder("testId", Effect.DENY)
				.attribute(attrBuilder.id("testId1").build())
				.attribute(attrBuilder.id("testId2").build())
				.build();
		Advice a2 = Advice.builder("testId", Effect.PERMIT)
				.attribute(attrBuilder.id("testId1").build())
				.attribute(attrBuilder.id("testId2").build())
				.build();
		Advice a3 = Advice.builder("testId", Effect.PERMIT)
				.attribute(attrBuilder.id("testId1").build())
				.attribute(attrBuilder.id("testId2").value(IntegerExp.valueOf(1)).build())
				.build();
		Advice a4 = Advice.builder("id", Effect.PERMIT)
				.attribute(attrBuilder.id("testId1").build())
				.attribute(attrBuilder.id("testId2").build())
				.build();
		assertEquals(a1, a1);
		assertEquals(a1, a2);
		assertFalse(a1.equals(a3));
		assertFalse(a1.equals(a4));
		assertFalse(a1.equals(null));
	}

	@Test
	public void testCreateAttributeAssignmentsWithSameIds()
	{
		AttributeAssignment.Builder attrBuilder = AttributeAssignment.builder()
				.id("testId1")
				.category(Categories.SUBJECT_ACCESS)
				.issuer("testIssuer")
				.value(IntegerExp.valueOf(0));

		Advice a = Advice.builder("testId", Effect.DENY)
				.attribute(attrBuilder.build())
				.attribute(attrBuilder.value(IntegerExp.valueOf(1)).build())
				.build();

		assertEquals("testId", a.getId());
		assertTrue(a.getAttribute("testId1").contains(attrBuilder.value(IntegerExp.valueOf(0)).build()));
		assertTrue(a.getAttribute("testId1").contains(attrBuilder.value(IntegerExp.valueOf(1)).build()));
	}

	@Test
	public void testImmutability1()
	{
		AttributeAssignment.Builder attrBuilder = AttributeAssignment.builder()
				.id("testId1")
				.category(Categories.SUBJECT_ACCESS)
				.issuer("testIssuer")
				.value(IntegerExp.valueOf(0));

		Advice a = Advice.builder("testId", Effect.PERMIT)
				.attribute(attrBuilder.build())
				.build();

		assertTrue(a.getAttribute("testId1").contains(attrBuilder.build()));
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testImmutability2()
	{
		AttributeAssignment.Builder attrBuilder = AttributeAssignment.builder()
				.id("testId1")
				.category(Categories.SUBJECT_ACCESS)
				.issuer("testIssuer")
				.value(IntegerExp.valueOf(0));

		Advice a = Advice.builder("testId", Effect.DENY)
				.attribute(attrBuilder.build())
				.build();
		a.getAttribute("testId1").clear();
	}
}
