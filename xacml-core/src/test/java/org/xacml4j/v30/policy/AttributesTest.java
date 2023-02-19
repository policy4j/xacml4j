package org.xacml4j.v30.policy;

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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.content.XmlContent;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.XacmlTypes;


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

	private XmlContent content1;
	private XmlContent content2;
	private XmlContent content3;

	@Before
	public void init() throws Exception
	{
		this.attributes = new LinkedList<Attribute>();
		attributes.add(Attribute.builder("testId10").value(XacmlTypes.STRING.of("value0")).build());
		attributes.add(Attribute.builder("testId10").value(XacmlTypes.INTEGER.of(1), XacmlTypes.INTEGER.of(2)).build());
		attributes.add(Attribute.builder("testId11").value(XacmlTypes.STRING.of("value1")).build());
		attributes.add(Attribute.builder("testId11").issuer("testIssuer").includeInResult(true)
				.value(XacmlTypes.STRING.of("value1"), XacmlTypes.STRING.of("value2")).build());
		attributes.add(Attribute.builder("testId11").issuer("testIssuer").includeInResult(true).value(XacmlTypes.INTEGER.of(10)).build());



		this.content1 = XmlContent.of(XmlContent.fromString(testXml1));
		this.content1 = XmlContent.of(XmlContent.fromString(testXml2));
		this.content3 = XmlContent.of(XmlContent.fromString(testXml1));
	}

	@Test
	public void testBuilderFrom()
	{
		Category test = Category.builder(CategoryId.RESOURCE)
				.entity(Entity.builder().attributes(attributes).content(content1).build())
				.build();
		Category test1 = Category.builder().copyOf(test).build();
		assertEquals(test, test1);
	}

	@Test
	public void testCreate2()
	{
		Category test = Category.builder(CategoryId.RESOURCE)
				.entity(Entity.builder().attributes(attributes).content(content1).build())
				.build();
		Entity e = test.getEntity();
		assertTrue(e.getAttributes().containsAll(attributes));
		assertFalse(test.getReferenceId().isPresent());
		assertTrue(content1.equals(e.getContent().get()));
		assertEquals(CategoryId.RESOURCE, test.getCategoryId());
	}

	@Test
	public void testGetAttributesById()
	{
		Category test = Category.builder(CategoryId.RESOURCE)
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
		Category test = Category.builder(CategoryId.RESOURCE)
				.entity(Entity.builder().attributes(attributes).content(content1).build())
				.build();
		Entity e = test.getEntity();
		assertEquals(2, e.getAttributes("testId11", "testIssuer").size());
		assertEquals(0, e.getAttributes("testId10", "testIssuer").size());
	}

	@Test
	public void testGetIncludeInResultAttributes()
	{
		Category test = Category.builder(CategoryId.RESOURCE)
				.entity(Entity.builder().attributes(attributes).content(content1).build())
				.build();
		Entity e = test.getEntity();
		assertEquals(2, e.getIncludeInResultAttributes().size());
	}

	@Test
	public void testCreateWithTheSameAttributes()
	{
		Collection<Attribute> attributes = new LinkedList<Attribute>();
		attributes.add(Attribute.builder("testId10").value(XacmlTypes.STRING.of("value0")).build());
		attributes.add(Attribute.builder("testId10").value(XacmlTypes.STRING.of("value0")).build());
		assertEquals(2, attributes.size());
		Category test = Category.builder(CategoryId.RESOURCE)
				.entity(Entity.builder().attributes(attributes).content(content1).build())
				.build();
		Entity e = test.getEntity();
		assertEquals(2, e.getAttributeValues("testId10", XacmlTypes.STRING).size());
	}

	@Test
	public void testGetAttributeValuesByIdAndIssuerAndType()
	{
		Category test = Category.builder(CategoryId.RESOURCE)
				.entity(Entity.builder().attributes(attributes).content(content1).build())
				.build();
		Entity e = test.getEntity();
		assertEquals(2, e.getAttributeValues("testId10", null, XacmlTypes.INTEGER).size());
		assertEquals(1, e.getAttributeValues("testId10", null, XacmlTypes.STRING).size());
		assertEquals(2, e.getAttributeValues("testId11", "testIssuer", XacmlTypes.STRING).size());
		assertEquals(1, e.getAttributeValues("testId11", "testIssuer", XacmlTypes.INTEGER).size());
	}
}
