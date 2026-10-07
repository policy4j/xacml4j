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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.types.AnyURI;
import org.xacml4j.v30.types.XacmlTypes;

public class AnyURIRequestLexicalTest
{
	@ParameterizedTest
	@ValueSource(strings = {"http://example.com/a/../b", "http://example.com/./a", "HTTP://Example.COM/a"})
	public void jsonRequestRoundTripKeepsUriSpelling(String lexical) throws Exception
	{
		String json = "{\"Category\":[{\"CategoryId\":\"" + CategoryId.RESOURCE.getId()
				+ "\",\"Attribute\":[{\"AttributeId\":\"urn:test:uri\",\"DataType\":\""
				+ XacmlTypes.ANYURI.getTypeId() + "\",\"Value\":\"" + lexical + "\"}]}]}";
		JsonRequestContextUnmarshaller reader = new JsonRequestContextUnmarshaller();
		RequestContext first = reader.unmarshal(new StringReader(json));
		assertEquals(lexical, lexicalValue(first));
		String encoded = new JsonRequestContextMarshaller().marshal(first).toString();
		assertTrue(encoded.contains(lexical));
		RequestContext restored = reader.unmarshal(new StringReader(encoded));
		assertEquals(lexical, lexicalValue(restored));
		assertEquals(first, restored);
	}

	private String lexicalValue(RequestContext request)
	{
		return ((AnyURI) request.getAttributeValues(CategoryId.RESOURCE, "urn:test:uri", XacmlTypes.ANYURI, null)
				.iterator().next()).toXacmlString();
	}
}
