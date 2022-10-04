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

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.*;
import org.xacml4j.v30.types.Entity;

import static org.xacml4j.v30.types.XacmlTypes.STRING;
import static org.xacml4j.v30.types.XacmlTypes.ENTITY;

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
	
	@Test
	public void testEntityMarshall() throws Exception
	{
		Entity entity = Entity.builder()
		                      .xmlContent("<security>\n<through obscurity=\"true\"></through></security>")
		                      .attribute(Attribute
						.builder("testId3")
						.value(STRING.of("aaa"))
						.value(STRING.of("bbbb"))
						.value(STRING.of("cccc"))
						.build())
		                      .attribute(Attribute
						.builder("testId4")
						.value(STRING.of("zzzz"))
						.value(STRING.of("aaaa"))
						.value(STRING.of("cccc"))
						.build())
		                      .build();
		Category a = Category.builder()
		.category(CategoryId.SUBJECT_ACCESS)
		.entity(Entity.builder()
				.attribute(Attribute
						.builder("testId1")
						.value(ENTITY.of(entity))
						.build())
			    .build())
	    .build();
		JsonElement o = json.toJsonTree(a);
		System.out.println(o.toString());
		Category b = json.fromJson(o, Category.class);
		assertEquals(a,  b);
	}
}
