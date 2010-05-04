package com.artagon.xacml.v3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.impl.DefaultAttribute;
import com.artagon.xacml.v3.policy.AttributeValue;
import com.artagon.xacml.v3.policy.type.DataTypes;

public class AttributeTest 
{
	private Collection<AttributeValue> values;
	
	@Before
	public void init()
	{
		this.values = new LinkedList<AttributeValue>();
		values.add(DataTypes.INTEGER.create(1));
		values.add(DataTypes.INTEGER.create(2));
		values.add(DataTypes.INTEGER.create(3));
		values.add(DataTypes.INTEGER.create(2));
	}
	
	@Test
	public void testCreateWithAllArguments()
	{
		Attribute attr = new DefaultAttribute("testId", "testIssuer", true, values);
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
		Attribute attr = new DefaultAttribute("testId", values);
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
		Attribute attr = new DefaultAttribute("testId", 
				DataTypes.INTEGER.create(1), 
				DataTypes.INTEGER.create(2),
				DataTypes.INTEGER.create(3),
				DataTypes.INTEGER.create(2));
		assertEquals("testId", attr.getAttributeId());
		assertNull(attr.getIssuer());
		assertFalse(attr.isIncludeInResult());
		assertEquals(values.size(), attr.getValues().size());
		assertTrue(attr.getValues().containsAll(values));
		assertTrue(values.containsAll(attr.getValues()));
	}
	
}
