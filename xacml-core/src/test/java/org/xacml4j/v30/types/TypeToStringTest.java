package org.xacml4j.v30.types;

/*
 * #%L
 * Xacml4J Core Engine Implementation
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Map;
import java.util.LinkedHashMap;

import org.junit.jupiter.api.Test;

public class TypeToStringTest
{
	@Test
	public void testBasicTypes(){
		Optional<TypeToString> c = TypeToString.forType(XacmlTypes.INTEGER);
		assertTrue(c.isPresent());
		c = TypeToString.forType(XacmlTypes.ENTITY);
		assertFalse(c.isPresent());
	}

	@Test
	public void testXmlSchemaLexicalFormsRoundTrip()
	{
		Map<ValueType, String> lexicalForms = new LinkedHashMap<>();
		lexicalForms.put(XacmlTypes.INTEGER, "56");
		lexicalForms.put(XacmlTypes.DOUBLE, "INF");
		lexicalForms.put(XacmlTypes.BOOLEAN, "true");
		lexicalForms.put(XacmlTypes.STRING, "plain text");
		lexicalForms.put(XacmlTypes.ANYURI, "urn:example:test");
		lexicalForms.put(XacmlTypes.BASE64BINARY, "YQ==");
		lexicalForms.put(XacmlTypes.HEXBINARY, "0A4F");
		lexicalForms.put(XacmlTypes.DATE, "2002-03-22Z");
		lexicalForms.put(XacmlTypes.DATETIME, "2002-03-22T08:23:48Z");
		lexicalForms.put(XacmlTypes.DAYTIMEDURATION, "PT1.5S");
		lexicalForms.put(XacmlTypes.YEARMONTHDURATION, "P1Y2M");
		lexicalForms.put(XacmlTypes.X500NAME, "CN=Alice, OU=Engineering, O=Example, C=US");
		lexicalForms.put(XacmlTypes.RFC822NAME, "alice@example.com");
		lexicalForms.put(XacmlTypes.IPADDRESS, "127.0.0.1");
		lexicalForms.put(XacmlTypes.DNSNAME, "example.com");

		for (Map.Entry<ValueType, String> testCase : lexicalForms.entrySet()) {
			TypeToString formatter = TypeToString.forType(testCase.getKey()).orElseThrow();
			Value parsed = formatter.fromString(testCase.getValue());
			String serialized = formatter.toString(parsed);
			assertEquals(parsed, formatter.fromString(serialized), testCase.getKey().toString());
		}
		assertEquals("56", TypeToString.Types.INTEGER.toString(IntegerVal.of("56")));
		TypeToString doubleFormatter = TypeToString.forType(XacmlTypes.DOUBLE).orElseThrow();
		assertEquals("INF", doubleFormatter.toString(doubleFormatter.fromString("INF")));
		assertEquals("-INF", doubleFormatter.toString(doubleFormatter.fromString("-INF")));
		assertEquals("NaN", doubleFormatter.toString(doubleFormatter.fromString("NaN")));
		assertEquals(-0.0d, ((DoubleVal) doubleFormatter.fromString(
					doubleFormatter.toString(doubleFormatter.fromString("-0")))).get());
	}
}
