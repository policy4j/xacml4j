package com.artagon.xacml.v3.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;

import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.policy.type.DataTypes;

public class DefaultAttributesTest 
{
	@Test
	public void testCreate()
	{
		Collection<Attribute> attr = new LinkedList<Attribute>();
		attr.add(new DefaultAttribute("testId10", DataTypes.STRING.create("value0")));
		attr.add(new DefaultAttribute("testId11", DataTypes.STRING.create("value1")));
		attr.add(new DefaultAttribute("testId11", "testIssuer", true, DataTypes.STRING.create("value1")));
		Attributes test = new DefaultAttributes(AttributeCategoryId.RESOURCE, attr);
		assertTrue(attr.containsAll(test.getAttributes()));
		assertTrue(test.getAttributes().containsAll(attr));
		assertNull(test.getContent());
		assertEquals(AttributeCategoryId.RESOURCE, test.getCategoryId());
	}
}
