package com.artagon.xacml.v3;

import static com.artagon.xacml.v3.types.IntegerType.INTEGER;
import static com.artagon.xacml.v3.types.StringType.STRING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.artagon.xacml.v3.context.Attribute;
import com.artagon.xacml.v3.context.Attributes;
import com.artagon.xacml.v3.types.DataTypes;

public class AttributesTest 
{
	private Collection<Attribute> attributes;
	
	private String testXml1 = "<md:record xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
	"xmlns:md=\"urn:example:med:schemas:record\">" +
	"<md:patient>" +
	"<md:patientDoB>1992-03-21</md:patientDoB>" +
	"<md:patient-number>555555</md:patient-number>" +
	"</md:patient>" +
	"</md:record>";
	
	private String testXml2 = "<md:record xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
	"xmlns:md=\"urn:example:med:schemas:record\">" +
	"<md:patient>" +
	"<md:patientDoB>1991-03-21</md:patientDoB>" +
	"<md:patient-number>555556</md:patient-number>" +
	"</md:patient>" +
	"</md:record>";
	
	private Node content1;
	private Node content2;
	private Node content3;
	
	@Before
	public void init() throws Exception
	{
		this.attributes = new LinkedList<Attribute>();
		attributes.add(new Attribute("testId10", STRING.create("value0")));
		attributes.add(new Attribute("testId10", INTEGER.create(0), INTEGER.create(1)));
		attributes.add(new Attribute("testId11", STRING.create("value1")));
		attributes.add(new Attribute("testId11", "testIssuer", true, STRING.create("value1"), 
				STRING.create("value2")));
		attributes.add(new Attribute("testId11", "testIssuer", true, INTEGER.create(10)));
	
		
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setNamespaceAware(true);
		DocumentBuilder builder = f.newDocumentBuilder();
		this.content1 = builder.parse(new InputSource(new StringReader(testXml1)));
		this.content2 = builder.parse(new InputSource(new StringReader(testXml2)));
		this.content3 = builder.parse(new InputSource(new StringReader(testXml1)));
	}
	
	@Test
	public void testCreate1()
	{
		
		Attributes test = new Attributes("id", AttributeCategories.RESOURCE,  content1, attributes);
		assertTrue(attributes.containsAll(test.getAttributes()));
		assertTrue(test.getAttributes().containsAll(attributes));
		assertEquals("id", test.getId());
		assertTrue(content1.isSameNode(test.getContent()));
		assertTrue(content1.isEqualNode(test.getContent()));
		System.out.println(test);
		assertEquals(AttributeCategories.RESOURCE, test.getCategory());
	}
	
	@Test
	public void testCreateWithContentNull()
	{
		
		Attributes test = new Attributes("id", AttributeCategories.RESOURCE,  null, attributes);
		assertTrue(attributes.containsAll(test.getAttributes()));
		assertTrue(test.getAttributes().containsAll(attributes));
		assertEquals("id", test.getId());
		assertNull(test.getContent());
		assertEquals(AttributeCategories.RESOURCE, test.getCategory());
	}
	
	@Test
	public void testEquals()
	{
		
		Attributes a1 = new Attributes("id", AttributeCategories.RESOURCE,  content1, attributes);
		Attributes a2 = new Attributes("id", AttributeCategories.RESOURCE,  content2, attributes);
		Attributes a3 = new Attributes("id", AttributeCategories.RESOURCE,  content3, attributes);
		assertTrue(a1.equals(a1));
		assertTrue(content1.isEqualNode(content3));
		assertFalse(a1.equals(a2));
		assertTrue(a1.equals(a3));
	
	}
	
	@Test
	public void testCreate2()
	{
		
		Attributes test = new Attributes(AttributeCategories.RESOURCE,  content1, attributes);
		assertTrue(attributes.containsAll(test.getAttributes()));
		assertTrue(test.getAttributes().containsAll(attributes));
		assertNull(test.getId());
		assertTrue(content1.isEqualNode(test.getContent()));
		assertEquals(AttributeCategories.RESOURCE, test.getCategory());
	}
	
	@Test
	public void testGetAttributesById()
	{
		Attributes test = new Attributes(AttributeCategories.RESOURCE,  content1, attributes);
		assertEquals(2, test.getAttributes("testId10").size());
		assertEquals(3, test.getAttributes("testId11").size());
		assertEquals(5, test.getAttributes().size());
	}
	
	@Test
	public void testGetAttributesByIdAndIssuerAndType()
	{
		Attributes test = new Attributes(AttributeCategories.RESOURCE,  content1, attributes);
		assertEquals(2, test.getAttributes("testId11", "testIssuer").size());
		assertEquals(0, test.getAttributes("testId10", "testIssuer").size());
	}
	
	@Test
	public void testGetIncludeInResultAttributes()
	{
		Attributes test = new Attributes(AttributeCategories.RESOURCE,  content1, attributes);
		assertEquals(2, test.getIncludeInResultAttributes().size());
	}
	
	@Test
	public void testCreateWithTheSameAttributes()
	{
		Collection<Attribute> attributes = new LinkedList<Attribute>();
		attributes.add(new Attribute("testId10", STRING.create("value0")));
		attributes.add(new Attribute("testId10", STRING.create("value0")));
		assertEquals(2, attributes.size());
		Attributes test = new Attributes(AttributeCategories.RESOURCE,  content1, attributes);
		assertEquals(2, test.getAttributeValues("testId10", STRING).size());
	}
	
	@Test
	public void testGetAttributeValuesByIdAndIssuerAndType()
	{
		Attributes test = new Attributes(AttributeCategories.RESOURCE,  content1, attributes);
		assertEquals(2, test.getAttributeValues("testId10", null, INTEGER).size());
		assertEquals(1, test.getAttributeValues("testId10", null, STRING).size());
		assertEquals(2, test.getAttributeValues("testId11", "testIssuer", DataTypes.STRING.getDataType()).size());
		assertEquals(1, test.getAttributeValues("testId11", "testIssuer", DataTypes.INTEGER.getDataType()).size());
	}
}
