package com.artagon.xacml.v30.pdp;

import static com.artagon.xacml.v30.types.IntegerType.INTEGER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.pdp.Attribute;
import com.artagon.xacml.v30.pdp.AttributeExp;
import com.artagon.xacml.v30.types.StringType;


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
		Attribute attr = new Attribute("testId", "testIssuer", true, values);
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
		Attribute attr = Attribute.create("testId", StringType.STRING, "value1", "value2");
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
		Attribute attr = new Attribute("testId", "testIssuer", true, values);
		Attribute attr1 = new Attribute("testId", null, true, values);
		Attribute attr2 = new Attribute("testId", "testIssuer", false, values);
		Attribute attr3 = new Attribute("testId", "testIssuer", true, values);
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
	public void testCreateWithIdAndValuesCollection()
	{
		Attribute attr = new Attribute("testId", values);
		assertEquals("testId", attr.getAttributeId());
		assertNull(attr.getIssuer());
		assertFalse(attr.isIncludeInResult());
		assertEquals(values.size(), attr.getValues().size());
		assertTrue(attr.getValues().containsAll(values));
		assertTrue(values.containsAll(attr.getValues()));
	}
	
	@Test
	public void testCreateWithIdAndValuesVarArg()
	{
		Attribute attr = new Attribute("testId", 
				INTEGER.create(1), 
				INTEGER.create(2),
				INTEGER.create(3),
				INTEGER.create(2));
		assertEquals("testId", attr.getAttributeId());
		assertNull(attr.getIssuer());
		assertFalse(attr.isIncludeInResult());
		assertEquals(values.size(), attr.getValues().size());
		assertTrue(attr.getValues().containsAll(values));
		assertTrue(values.containsAll(attr.getValues()));
	}
	
}
