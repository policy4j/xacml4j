package com.xacml4j.xacml.proto;

/*
 * #%L
 * Xacml4J Google Protbuf 3 based request/response and policy format support
 * %%
 * Copyright (C) 2009 - 2023 Xacml4J.org
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
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.xacml4j.v30.Value;
import org.xacml4j.v30.types.XacmlTypes;


public class TypeToProtoTest
{

	@Test
	public void testHexBinary(){

		Value v = XacmlTypes.HEXBINARY.of("test".getBytes(StandardCharsets.UTF_8));
		PValue pv = TypeToProto.Types.HEXBINARY.toProto(v);
		Value v1 = TypeToProto.Types.HEXBINARY.fromProto(pv);
		assertEquals(v, v1);
	}

	@Test
	public void testString(){

		Value v = XacmlTypes.STRING.of("test");
		PValue pv = TypeToProto.Types.STRING.toProto(v);
		Value v1 = TypeToProto.Types.STRING.fromProto(pv);
		assertEquals(v, v1);
	}

	@Test
	public void testAnyURI(){

		Value v = XacmlTypes.ANYURI.of(URI.create("test"));
		PValue pv = TypeToProto.Types.ANYURI.toProto(v);
		Value v1 = TypeToProto.Types.ANYURI.fromProto(pv);
		assertEquals(v, v1);
	}

	@Test
	public void testBase64Binary(){

		Value v = XacmlTypes.BASE64BINARY.of("test".getBytes(StandardCharsets.UTF_8));
		PValue pv = TypeToProto.Types.BASE64BINARY.toProto(v);
		Value v1 = TypeToProto.Types.BASE64BINARY.fromProto(pv);
		assertEquals(v, v1);
	}
}
