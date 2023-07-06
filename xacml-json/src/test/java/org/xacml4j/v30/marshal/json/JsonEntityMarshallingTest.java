package org.xacml4j.v30.marshal.json;

/*
 * #%L
 * Xacml4J Gson Integration
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
import static org.xacml4j.v30.types.XacmlTypes.STRING;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.types.Entity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class JsonEntityMarshallingTest 
{
	private Gson json;
	
	@Before
	public void init(){
		this.json = new GsonBuilder()
				.registerTypeAdapter(Category.class, new CategoryAdapter())
				.registerTypeAdapter(Attribute.class, new AttributeSerializer())
				.registerTypeAdapter(Attribute.class, new AttributeDeserializer())
				.create();
	}

	/**
	 * FIXME: https://travis-ci.org/github/xacml4j-opensource/xacml4j/jobs/771024841
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testEntityMarshall() throws Exception
	{
		Entity entity = Entity.builder()
		                      .xmlContent("<security>\n<through obscurity=\"true\"></through></security>")
		                      .attribute(Attribute.builder("testId3")
		                                          .value(STRING.ofAny("aaa"))
		                                          .value(STRING.ofAny("bbbb"))
		                                          .value(STRING.ofAny("cccc"))
		                                          .build())
		                      .attribute(Attribute
				                                 .builder("testId4")
				                                 .value(STRING.ofAny("zzzz"))
				                                 .value(STRING.ofAny("aaaa"))
				                                 .value(STRING.ofAny("cccc"))
				                                 .build())
		                      .build();
		Category a = Category
				.builder()
		.category(CategoryId.SUBJECT_ACCESS)
		.entity(entity)
	    .build();
		JsonElement o = json.toJsonTree(a);
		Category b = json.fromJson(o, Category.class);
		assertEquals(a,  b);
	}

	@Test
	public void testEntityAsAttributeValueMarshall() throws Exception
	{
		Entity entity1 = Entity.builder()
		                      .xmlContent("<security>\n<through obscurity=\"true\"></through></security>")
		                      .attribute(Attribute
				                                 .builder("testId4")
				                                 .value(STRING.ofAny("zzzz"))
				                                 .value(STRING.ofAny("aaaa"))
				                                 .value(STRING.ofAny("cccc"))
				                                 .build())
		                      .build();
		Entity entity2 =Entity.builder()
		      .xmlContent("<security>\n<through obscurity=\"true\"></through></security>")
		      .attribute(Attribute
				                 .builder("testId4")
				                 .value(STRING.ofAny("zzzz"))
				                 .value(STRING.ofAny("aaaa"))
				                 .value(STRING.ofAny("cccc"))
				                 .build())
		      .build();
		assertEquals(entity1, entity2);

		Category a1 = Category
				.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.entity(Entity.builder()
				              .attribute(Attribute.builder("testId")
				                                  .entity(entity1)
				                                  .build())
				              .build())
				.build();
		Category a2 = Category
				.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.entity(Entity.builder()
				              .attribute(Attribute.builder("testId")
				                                  .entity(entity2)
				                                  .build())
				              .build())
				.build();
		JsonElement o1 = json.toJsonTree(a1);
		Category b1 = json.fromJson(o1, Category.class);
		JsonElement o2 = json.toJsonTree(a2);
		Category b2 = json.fromJson(o2, Category.class);
		assertEquals(a1,  b1);
		assertEquals(a2,  b2);
		assertEquals(a2,  b1);
		assertEquals(a1,  b2);
	}
}
