package com.artagon.xacml.v30;

import static com.artagon.xacml.v30.types.IntegerType.INTEGER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.Attribute.Builder;
import com.artagon.xacml.v30.types.StringType;
import com.google.common.collect.ImmutableSet;


public class AttributeTest
{
	private Collection<AttributeExp> values;

	@Before
	public void init()
	{
		this.values = new LinkedList<AttributeExp>();
		values.add(INTEGER.create(1));
		values.add(INTEGER.create(2));
		values.add(INTEGER.create(3));
		values.add(INTEGER.create(2));
	}

	@Test
	public void testCreateWithAllArguments()
	{
		Attribute attr = Attribute
				.builder("testId")
				.issuer("testIssuer")
				.includeInResult(true)
				.value(values)
				.build();
		assertEquals("testId", attr.getAttributeId());
		assertEquals("testIssuer", attr.getIssuer());
		assertTrue(attr.isIncludeInResult());
		assertEquals(values.size(), attr.getValues().size());
		assertTrue(attr.getValues().containsAll(values));
		assertTrue(values.containsAll(attr.getValues()));
	}

	@Test
	public void testCreateMethod()
	{
		Attribute attr = Attribute
				.builder("testId")
				.value(StringType.STRING, "value1", "value2")
				.build();
		assertEquals("testId", attr.getAttributeId());
		assertEquals(null, attr.getIssuer());
		assertFalse(attr.isIncludeInResult());
		assertEquals(2, attr.getValues().size());
		assertTrue(attr.getValues().contains(StringType.STRING.create("value1")));
		assertTrue(attr.getValues().contains(StringType.STRING.create("value2")));
	}

	@Test
	public void testCreateWithTheSameValues()
	{
		Collection<AttributeExp> values = new LinkedList<AttributeExp>();
		values.add(INTEGER.create(1));
		values.add(INTEGER.create(1));
		Builder b = Attribute.builder("testId").issuer("testIssuer").includeInResult(true).value(values);
		Attribute attr = b.build();
		Attribute attr1 = b.issuer(null).build();
		Attribute attr2 = b.includeInResult(false).build();
		Attribute attr3 = b.includeInResult(true).issuer("testIssuer").build();
		assertEquals("testId", attr.getAttributeId());
		assertEquals("testIssuer", attr.getIssuer());
		assertTrue(attr.isIncludeInResult());
		assertEquals(values.size(), attr.getValues().size());
		assertTrue(attr.getValues().containsAll(values));
		assertTrue(values.containsAll(attr.getValues()));
		assertFalse(attr.equals(attr1));
		assertFalse(attr.equals(attr2));
		assertTrue(attr.equals(attr3));
	}


	@Test
	public void testCreateWithIdAndValuesVarArg()
	{
		Attribute attr = Attribute.builder("testId")
				.value(INTEGER.create(1), INTEGER.create(2), INTEGER.create(3), INTEGER.create(2))
				.build();
		assertEquals("testId", attr.getAttributeId());
		assertNull(attr.getIssuer());
		assertFalse(attr.isIncludeInResult());
		assertEquals(values.size(), attr.getValues().size());
		assertTrue(attr.getValues().containsAll(values));
		assertTrue(values.containsAll(attr.getValues()));
	}

	@Test
	public void testBuilder()
	{
		Iterable<String> a = ImmutableSet.of("test1", "test2");
		Attribute.builder("testId")
		.value(StringType.STRING, a)
		.value(StringType.STRING, "test2", "test3")
		.build();
	}

}
