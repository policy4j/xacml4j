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

import org.junit.Test;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;


public class AnyURITypeTest
{

	@Test
	public void testEquals()
	{
		AttributeExp v0 = AnyURIExp.valueOf("http://www.test.org");
		AttributeExp v1 = AnyURIExp.valueOf("http://www.test.org");
		assertEquals(v0, v1);
		assertEquals(XacmlTypes.ANYURI, v0.getType());
		assertEquals(XacmlTypes.ANYURI, v1.getType());
	}
	
	
	@Test
	public void testBagOf(){
		AttributeExp v0 = AnyURIExp.valueOf("http://www.test.org");
		AttributeExp v1 = AnyURIExp.valueOf("http://www.test1.org");
		BagOfAttributeExp b0 = AnyURIExp.bag().attribute(v0, v1).build();
		BagOfAttributeExp b1 = AnyURIExp.bag().attributes(ImmutableList.of(v0, v1)).build();
		BagOfAttributeExp b2 = AnyURIExp.bag().value("http://www.test.org", "http://www.test1.org").build();
		BagOfAttributeExp b3 = AnyURIExp.bag().value(URI.create("http://www.test.org"), 
				URI.create("http://www.test1.org")).build();
		assertEquals(b0, b1);
		assertEquals(b1, b2);
		assertEquals(b2, b3);
		
	}
	
	@Test
	public void testTypeAlias(){
		Optional<AttributeExpType> t0 = XacmlTypes.getType("anyURI");
		assertEquals(XacmlTypes.ANYURI, t0.get());
	}
}
