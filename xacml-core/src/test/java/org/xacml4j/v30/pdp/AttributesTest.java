package org.xacml4j.v30.pdp;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.Assert.assertEquals;
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
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.types.IntegerExp;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.XacmlTypes;
import org.xml.sax.InputSource;


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
		attributes.add(Attribute.builder("testId10").value(StringExp.valueOf("value0")).build());
		attributes.add(Attribute.builder("testId10").value(IntegerExp.valueOf(1), IntegerExp.valueOf(2)).build());
		attributes.add(Attribute.builder("testId11").value(StringExp.valueOf("value1")).build());
		attributes.add(Attribute.builder("testId11").issuer("testIssuer").includeInResult(true)
				.value(StringExp.valueOf("value1"), StringExp.valueOf("value2")).build());
		attributes.add(Attribute.builder("testId11").issuer("testIssuer").includeInResult(true).value(IntegerExp.valueOf(10)).build());


		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setNamespaceAware(true);
		DocumentBuilder builder = f.newDocumentBuilder();
		this.content1 = builder.parse(new InputSource(new StringReader(testXml1)));
		this.content2 = builder.parse(new InputSource(new StringReader(testXml2)));
		this.content3 = builder.parse(new InputSource(new StringReader(testXml1)));
	}

	@Test
	public void testBuilderFrom()
	{
		Category test = Category.builder(Categories.RESOURCE)
				.entity(Entity.builder().attributes(attributes).content(content1).build())
				.build();
		Category test1 = Category.builder().copyOf(test).build();
		assertEquals(test, test1);
	}

	@Test
	public void testCreate2()
	{
		Category test = Category.builder(Categories.RESOURCE)
				.entity(Entity.builder().attributes(attributes).content(content1).build())
				.build();
		Entity e = test.getEntity();
		assertTrue(e.getAttributes().containsAll(attributes));
		assertNull(test.getId());
		assertTrue(content1.isEqualNode(e.getContent()));
		assertEquals(Categories.RESOURCE, test.getCategoryId());
	}

	@Test
	public void testGetAttributesById()
	{
		Category test = Category.builder(Categories.RESOURCE)
				.entity(Entity.builder().attributes(attributes).content(content1).build())
				.build();
		Entity e = test.getEntity();
		assertEquals(2, e.getAttributes("testId10").size());
		assertEquals(3, e.getAttributes("testId11").size());
		assertEquals(5, e.getAttributes().size());
	}

	@Test
	public void testGetAttributesByIdAndIssuerAndType()
	{
		Category test = Category.builder(Categories.RESOURCE)
				.entity(Entity.builder().attributes(attributes).content(content1).build())
				.build();
		Entity e = test.getEntity();
		assertEquals(2, e.getAttributes("testId11", "testIssuer").size());
		assertEquals(0, e.getAttributes("testId10", "testIssuer").size());
	}

	@Test
	public void testGetIncludeInResultAttributes()
	{
		Category test = Category.builder(Categories.RESOURCE)
				.entity(Entity.builder().attributes(attributes).content(content1).build())
				.build();
		Entity e = test.getEntity();
		assertEquals(2, e.getIncludeInResultAttributes().size());
	}

	@Test
	public void testCreateWithTheSameAttributes()
	{
		Collection<Attribute> attributes = new LinkedList<Attribute>();
		attributes.add(Attribute.builder("testId10").value(StringExp.valueOf("value0")).build());
		attributes.add(Attribute.builder("testId10").value(StringExp.valueOf("value0")).build());
		assertEquals(2, attributes.size());
		Category test = Category.builder(Categories.RESOURCE)
				.entity(Entity.builder().attributes(attributes).content(content1).build())
				.build();
		Entity e = test.getEntity();
		assertEquals(2, e.getAttributeValues("testId10", XacmlTypes.STRING).size());
	}

	@Test
	public void testGetAttributeValuesByIdAndIssuerAndType()
	{
		Category test = Category.builder(Categories.RESOURCE)
				.entity(Entity.builder().attributes(attributes).content(content1).build())
				.build();
		Entity e = test.getEntity();
		assertEquals(2, e.getAttributeValues("testId10", null, XacmlTypes.INTEGER).size());
		assertEquals(1, e.getAttributeValues("testId10", null, XacmlTypes.STRING).size());
		assertEquals(2, e.getAttributeValues("testId11", "testIssuer", XacmlTypes.STRING).size());
		assertEquals(1, e.getAttributeValues("testId11", "testIssuer", XacmlTypes.INTEGER).size());
	}
}
