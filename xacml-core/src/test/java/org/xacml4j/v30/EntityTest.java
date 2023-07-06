package org.xacml4j.v30;

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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.XacmlTypes;

public class   EntityTest
{
	
	private String testXml = "<md:record xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
			"xmlns:md=\"urn:example:med:schemas:record\">" +
			"<md:patient>" +
			"<md:patientDoB>1992-03-21</md:patientDoB>" +
			"<md:patient-number>555555</md:patient-number>" +
			"</md:patient>" +
			"</md:record>";
	
	private Entity entity;
	
	@Before
	public void init() throws Exception{
		this.entity = Entity
				.builder()
				.content(testXml)
				.build();
	}
	@Test
	public void testBuildEntity(){
		Entity e0 = Entity
				.builder()
				.attribute(
						Attribute.
						builder("testId1")
						.value(XacmlTypes.STRING.ofAny("a"), XacmlTypes.STRING.ofAny("bb"))
						.build(),
						Attribute
						.builder("testId2")
						.value(XacmlTypes.STRING.ofAny("aa"), XacmlTypes.STRING.ofAny("bbb"))
						.build(),
						Attribute
						.builder("testId3")
						.value(XacmlTypes.INTEGER.ofAny(10), XacmlTypes.DOUBLE.ofAny(0.1))
						.build())
				.build();
		Entity e1 = Entity.builder().copyOf(e0).build();
		assertEquals(e0,  e1);
		assertTrue(e1.findValues("testId1", XacmlTypes.STRING).contains(XacmlTypes.STRING.ofAny("a")));
		assertTrue(e1.findValues("testId1", XacmlTypes.STRING).contains(XacmlTypes.STRING.ofAny("bb")));
		Entity e2 = Entity
				.builder()
				.copyOf(e0,
						(a)->a.getAttributeId()
								.equalsIgnoreCase("testId2")
				).build();
		assertFalse(e2.findValues("testId1", XacmlTypes.STRING).contains(XacmlTypes.STRING.ofAny("bb")));
		assertTrue(e2.findValues("testId2", XacmlTypes.STRING).contains(XacmlTypes.STRING.ofAny("aa")));
		assertTrue(e2.findValues("testId2", XacmlTypes.STRING).contains(XacmlTypes.STRING.ofAny("bbb")));
	}

	@Test
	public void testEntityDesignatorResolve()
	{
		Entity entity = Entity
				.builder()
				.attribute(
						Attribute.
								builder("testId1")
								.value(XacmlTypes.STRING.ofAny("a"), XacmlTypes.STRING.ofAny("bb"))
								.build(),
						Attribute
								.builder("testId2")
								.value(XacmlTypes.STRING.ofAny("aa"), XacmlTypes.STRING.ofAny("bbb"))
								.build(),
						Attribute
								.builder("testId3")
								.issuer("test")
								.value(XacmlTypes.INTEGER.ofAny(10), XacmlTypes.DOUBLE.ofAny(0.1))
								.build())
				.build();

		assertEquals(XacmlTypes.STRING.bagBuilder().value(XacmlTypes.STRING.ofAny("a"), XacmlTypes.STRING.ofAny("bb")).build(),
		             entity.resolve(AttributeDesignatorKey.builder()
		                                                  .attributeId("testId1")
		                                                  .dataType(XacmlTypes.STRING)
		                                                  .category(CategoryId.SUBJECT_ACCESS)
		                                                  .build()).get());
		assertEquals(XacmlTypes.INTEGER.bagBuilder().value(10).build(),
		             entity.resolve(AttributeDesignatorKey.builder()
		                                                  .attributeId("testId3")
		                                                  .dataType(XacmlTypes.INTEGER)
		                                                  .category(CategoryId.SUBJECT_ACCESS)
		                                                  .build()).get());

		assertEquals(XacmlTypes.DOUBLE.bagBuilder().value(0.1).build(),
		             entity.resolve(AttributeDesignatorKey.builder()
		                                                  .attributeId("testId3")
		                                                  .dataType(XacmlTypes.DOUBLE)
		                                                  .category(CategoryId.SUBJECT_ACCESS)
		                                                  .build()).get());

	}



	@Test
	public void testEntityType(){
		assertNotNull(XacmlTypes.getType(XacmlTypes.ENTITY.getTypeId()).orElse(null));
	}
	@Test
	public void testEntityEquals(){
		Entity e0 = Entity
				.builder()
				.attribute(Attribute.builder("testId1").value(XacmlTypes.STRING.ofAny("aa"), XacmlTypes.STRING.ofAny("bb")).build())
				.attribute(Attribute.builder("testId2").value(XacmlTypes.STRING.ofAny("cc"), XacmlTypes.STRING.ofAny("dd")).build())
				.build();
		
		Entity e1 = Entity
				.builder()
				.attribute(Attribute.builder("testId2").value(XacmlTypes.STRING.ofAny("dd"), XacmlTypes.STRING.ofAny("cc")).build())
				.attribute(Attribute.builder("testId1").value(XacmlTypes.STRING.ofAny("bb"), XacmlTypes.STRING.ofAny("aa")).build())
				.build();

		Entity e2 = Entity
				.builder()
				.content(testXml)
				.attribute(Attribute.builder("testId1").value(XacmlTypes.STRING.ofAny("aa"), XacmlTypes.STRING.ofAny("bb")).build())
				.attribute(Attribute.builder("testId2").value(XacmlTypes.STRING.ofAny("cc"), XacmlTypes.STRING.ofAny("dd")).build())
				.build();
		Entity e3 = Entity
				.builder()
				.content(testXml)
				.attribute(Attribute.builder("testId1").value(XacmlTypes.STRING.ofAny("aa"), XacmlTypes.STRING.ofAny("bb")).build())
				.attribute(Attribute.builder("testId2").value(XacmlTypes.STRING.ofAny("cc"), XacmlTypes.STRING.ofAny("dd")).build())
				.build();
		assertEquals(e0, e1);
		assertEquals(e2, e3);
	}

	@Test
	public void testEntityEquals2(){
		System.out.println(StringVal.of("aaaaa"));
		Entity embeddedEntity = Entity.builder()
		                      .xmlContent("<security>\n<through obscurity=\"true\"></through></security>")
		                      .attribute(Attribute.builder("testId1").value(XacmlTypes.STRING.ofAny("aa"), XacmlTypes.STRING.ofAny("bb")).build())
		                      .attribute(Attribute.builder("testId2").value(XacmlTypes.STRING.ofAny("cc"), XacmlTypes.STRING.ofAny("dd")).build())
		                      .build();
		System.out.println(embeddedEntity);
		Entity a = Entity.builder()
				              .attribute(Attribute.builder("testId")
				                                  .value(embeddedEntity)
				                                  .build())
				              .build();
		System.out.println(a);
		Entity b = Entity.builder()
		                 .attribute(Attribute.builder("testId")
		                                     .value(embeddedEntity)
		                                     .build())
		                 .build();
		assertEquals(a, b);

	}
}


