package org.xacml4j.v30.xml;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.types.AnyURI;
import org.xacml4j.v30.types.XacmlTypes;
import org.xml.sax.InputSource;

public class AnyURIRequestLexicalTest
{
	// XACML 3.0 A.3.3 preserves the URI as originally represented in XML.
	@ParameterizedTest
	@ValueSource(strings = {"http://example.com/a/../b", "http://example.com/./a", "HTTP://Example.COM/a"})
	public void xmlRequestRoundTripKeepsUriSpelling(String lexical) throws Exception
	{
		String xml = "<Request xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\""
				+ " ReturnPolicyIdList=\"false\" CombinedDecision=\"false\">"
				+ "<Attributes Category=\"" + CategoryId.RESOURCE.getId() + "\">"
				+ "<Attribute AttributeId=\"urn:test:uri\" IncludeInResult=\"false\">"
				+ "<AttributeValue DataType=\"" + XacmlTypes.ANYURI.getTypeId() + "\">" + lexical
				+ "</AttributeValue></Attribute></Attributes></Request>";
		Xacml30RequestContextUnmarshaller reader = new Xacml30RequestContextUnmarshaller();
		RequestContext first = reader.unmarshal(new InputSource(new StringReader(xml)));
		assertEquals(lexical, lexicalValue(first));
		StringWriter encoded = new StringWriter();
		new Xacml30RequestContextMarshaller().marshal(first, encoded);
		assertTrue(encoded.toString().contains(lexical));
		RequestContext restored = reader.unmarshal(new InputSource(new StringReader(encoded.toString())));
		assertEquals(lexical, lexicalValue(restored));
		assertEquals(first, restored);
	}

	private String lexicalValue(RequestContext request)
	{
		return ((AnyURI) request.getAttributeValues(CategoryId.RESOURCE, "urn:test:uri", XacmlTypes.ANYURI, null)
				.iterator().next()).toXacmlString();
	}
}
