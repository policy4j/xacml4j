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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.XacmlTypes;

public class BooleanRequestLexicalTest
{
	// XML Schema Part 2 section 3.2.2, with whiteSpace=collapse (section 4.3.6).
	@ParameterizedTest
	@ValueSource(ints = {20, 30})
	public void requestParsesOneAndZeroAsDistinctBooleanValues(int version) throws Exception
	{
		RequestContext request = unmarshal(version, "1", "0");
		assertEquals(List.of(BooleanVal.of(true), BooleanVal.of(false)),
				List.copyOf(request.getAttributeValues(CategoryId.RESOURCE, "urn:test:boolean", XacmlTypes.BOOLEAN, null)));
		if (version == 30) {
			assertTrue(request.isReturnPolicyIdList());
			assertFalse(request.isCombinedDecision());
		}
	}

	@ParameterizedTest
	@ValueSource(ints = {20, 30})
	public void requestCollapsesXmlWhitespaceAroundBooleanValues(int version) throws Exception
	{
		RequestContext request = unmarshal(version, " \t1\r\n ", " \t0\r\n ");
		assertEquals(List.of(BooleanVal.of(true), BooleanVal.of(false)),
				List.copyOf(request.getAttributeValues(CategoryId.RESOURCE, "urn:test:boolean", XacmlTypes.BOOLEAN, null)));
	}

	@ParameterizedTest
	@CsvSource({"20,yes", "20,TRUE", "30,yes", "30,TRUE"})
	public void requestRejectsInvalidBooleanWithSyntaxError(int version, String lexical)
	{
		SyntaxException error = assertThrows(SyntaxException.class, () -> unmarshal(version, lexical, "0"));
		assertEquals(StatusCodeId.SYNTAX_ERROR, error.getStatus().getStatusCode().getValue());
	}

	private RequestContext unmarshal(int version, String first, String second) throws Exception
	{
		String values = "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#boolean\">" + first
				+ "</AttributeValue><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#boolean\">" + second
				+ "</AttributeValue>";
		String request = version == 30
				? "<Request xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" ReturnPolicyIdList=\"1\" CombinedDecision=\"0\">"
						+ "<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\">"
						+ "<Attribute AttributeId=\"urn:test:boolean\" IncludeInResult=\"false\">" + values
						+ "</Attribute></Attributes></Request>"
				: "<Request xmlns=\"urn:oasis:names:tc:xacml:2.0:context:schema:os\"><Subject/>"
						+ "<Resource><Attribute AttributeId=\"urn:test:boolean\" DataType=\"http://www.w3.org/2001/XMLSchema#boolean\">"
						+ values + "</Attribute></Resource><Action/><Environment/></Request>";
		ByteArrayInputStream input = new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
		return version == 30 ? new Xacml30RequestContextUnmarshaller().unmarshal(input)
				: new Xacml20RequestContextUnmarshaller().unmarshal(input);
	}
}
