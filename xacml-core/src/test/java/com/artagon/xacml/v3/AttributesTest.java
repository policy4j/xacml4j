package com.artagon.xacml.v3;

import static org.easymock.EasyMock.createStrictMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;

import com.artagon.xacml.v3.types.XacmlDataTypes;

public class AttributesTest 
{
	private Node content;
	
	private Collection<Attribute> attributes;
	
	@Before
	public void init(){
		this.content = createStrictMock(Node.class);
		this.attributes = new LinkedList<Attribute>();
		attributes.add(new Attribute("testId10", XacmlDataTypes.STRING.create("value0")));
		attributes.add(new Attribute("testId10", XacmlDataTypes.INTEGER.create(0), XacmlDataTypes.INTEGER.create(1)));
		attributes.add(new Attribute("testId11", XacmlDataTypes.STRING.create("value1")));
		attributes.add(new Attribute("testId11", "testIssuer", true, XacmlDataTypes.STRING.create("value1"), XacmlDataTypes.STRING.create("value2")));
		attributes.add(new Attribute("testId11", "testIssuer", true, XacmlDataTypes.INTEGER.create(10)));
	}
	
	@Test
	public void testCreate1()
	{
		
		Attributes test = new Attributes("id", AttributeCategoryId.RESOURCE,  content, attributes);
		assertTrue(attributes.containsAll(test.getAttributes()));
		assertTrue(test.getAttributes().containsAll(attributes));
		assertEquals("id", test.getId());
		assertSame(content, test.getContent());
		assertEquals(AttributeCategoryId.RESOURCE, test.getCategory());
	}
	
	@Test
	public void testCreate2()
	{
		
		Attributes test = new Attributes(AttributeCategoryId.RESOURCE,  content, attributes);
		assertTrue(attributes.containsAll(test.getAttributes()));
		assertTrue(test.getAttributes().containsAll(attributes));
		assertNull(test.getId());
		assertSame(content, test.getContent());
		assertEquals(AttributeCategoryId.RESOURCE, test.getCategory());
	}
	
	@Test
	public void testGetAttributesById()
	{
		Attributes test = new Attributes(AttributeCategoryId.RESOURCE,  content, attributes);
		assertEquals(2, test.getAttributes("testId10").size());
		assertEquals(3, test.getAttributes("testId11").size());
		assertEquals(5, test.getAttributes().size());
	}
	
	@Test
	public void testGetAttributesByIdAndIssuerAndType()
	{
		Attributes test = new Attributes(AttributeCategoryId.RESOURCE,  content, attributes);
		assertEquals(2, test.getAttributes("testId11", "testIssuer").size());
		assertEquals(0, test.getAttributes("testId10", "testIssuer").size());
	}
	
	@Test
	public void testGetIncludeInResultAttributes()
	{
		Attributes test = new Attributes(AttributeCategoryId.RESOURCE,  content, attributes);
		assertEquals(2, test.getIncludeInResultAttributes().size());
	}
	
	@Test
	public void testCreateWithTheSameAttributes()
	{
		Collection<Attribute> attributes = new LinkedList<Attribute>();
		attributes.add(new Attribute("testId10", XacmlDataTypes.STRING.create("value0")));
		attributes.add(new Attribute("testId10", XacmlDataTypes.STRING.create("value0")));
		assertEquals(2, attributes.size());
		Attributes test = new Attributes(AttributeCategoryId.RESOURCE,  content, attributes);
		assertEquals(2, test.getAttributeValues("testId10", XacmlDataTypes.STRING.getType()).size());
	}
	
	@Test
	public void testGetAttributeValuesByIdAndIssuerAndType()
	{
		Attributes test = new Attributes(AttributeCategoryId.RESOURCE,  content, attributes);
		assertEquals(2, test.getAttributeValues("testId10", null, XacmlDataTypes.INTEGER.getType()).size());
		assertEquals(1, test.getAttributeValues("testId10", null, XacmlDataTypes.STRING.getType()).size());
		assertEquals(2, test.getAttributeValues("testId11", "testIssuer", XacmlDataTypes.STRING.getType()).size());
		assertEquals(1, test.getAttributeValues("testId11", "testIssuer", XacmlDataTypes.INTEGER.getType()).size());
	}
}
