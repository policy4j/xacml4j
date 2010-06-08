package com.artagon.xacml.v3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.types.XacmlDataTypes;

public class AttributeTest 
{
	private Collection<AttributeValue> values;
	
	@Before
	public void init()
	{
		this.values = new LinkedList<AttributeValue>();
		values.add(XacmlDataTypes.INTEGER.create(1));
		values.add(XacmlDataTypes.INTEGER.create(2));
		values.add(XacmlDataTypes.INTEGER.create(3));
		values.add(XacmlDataTypes.INTEGER.create(2));
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
				XacmlDataTypes.INTEGER.create(1), 
				XacmlDataTypes.INTEGER.create(2),
				XacmlDataTypes.INTEGER.create(3),
				XacmlDataTypes.INTEGER.create(2));
		assertEquals("testId", attr.getAttributeId());
		assertNull(attr.getIssuer());
		assertFalse(attr.isIncludeInResult());
		assertEquals(values.size(), attr.getValues().size());
		assertTrue(attr.getValues().containsAll(values));
		assertTrue(values.containsAll(attr.getValues()));
	}
	
}
