package org.xacml4j.v30.types;

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

import java.net.URI;
import java.util.Optional;

import org.junit.Test;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.Value;
import org.xacml4j.v30.ValueType;

import com.google.common.collect.ImmutableList;


public class AnyURITypeTest
{

	@Test
	public void testEquals()
	{
		Value v0 = XacmlTypes.ANYURI.of("http://www.test.org");
		Value v1 = XacmlTypes.ANYURI.of("http://www.test.org");
		assertEquals(v0, v1);
		assertEquals(XacmlTypes.ANYURI, v0.getType());
		assertEquals(XacmlTypes.ANYURI, v1.getType());
	}
	
	
	@Test
	public void testBagOf(){
		Value v0 = XacmlTypes.ANYURI.of("http://www.test.org");
		Value v1 = XacmlTypes.ANYURI.of("http://www.test1.org");
		BagOfValues b0 = XacmlTypes.ANYURI.bag().attribute(v0, v1).build();
		BagOfValues b1 = XacmlTypes.ANYURI.bag().attributes(ImmutableList.of(v0, v1)).build();
		BagOfValues b2 = XacmlTypes.ANYURI.bag().value("http://www.test.org", "http://www.test1.org").build();
		BagOfValues b3 = XacmlTypes.ANYURI.bag().value(URI.create("http://www.test.org"),
		                                               URI.create("http://www.test1.org")).build();
		BagOfValues b4 = XacmlTypes.ANYURI.bag().value(URI.create("http://www.test.org"),
		                                               "http://www.test1.org").build();
		assertEquals(b0, b1);
		assertEquals(b1, b2);
		assertEquals(b2, b3);
		assertEquals(b3, b4);
		
	}
	
	@Test
	public void testTypeAlias(){
		Optional<ValueType> t0 = XacmlTypes.getType("anyURI");
		assertEquals(XacmlTypes.ANYURI, t0.get());
	}
}
