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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class BooleanRequestLexicalTest
{
	// Declared boolean values use XML Schema Part 2 section 3.2.2 and whiteSpace=collapse.
	@Test
	public void requestParsesOneAndZeroAsDistinctBooleanValues() throws Exception
	{
		assertBooleanValues(unmarshal(request("1", "0")));
	}

	@Test
	public void requestCollapsesXmlWhitespaceInTextValues() throws Exception
	{
		assertBooleanValues(unmarshal(request(" \t1\r\n ", " \t0\r\n ")));
	}

	@ParameterizedTest
	@ValueSource(strings = {"yes", "TRUE"})
	public void invalidAttributeLexicalFormsAreSyntaxErrors(String lexical)
	{
		SyntaxException error = assertThrows(SyntaxException.class, () -> unmarshal(request(lexical, "0")));
		assertEquals(StatusCodeId.SYNTAX_ERROR, error.getStatus().getStatusCode().getValue());
	}

	@Test
	public void textFlagsUseTheSameBooleanParser() throws Exception
	{
		JsonObject source = request("true", "false");
		source.addProperty("ReturnPolicyIdList", " \t1\r\n ");
		source.addProperty("CombinedDecision", "0");
		attribute(source).addProperty("IncludeInResult", "1");
		RequestContext actual = unmarshal(source);
		assertTrue(actual.isReturnPolicyIdList());
		assertFalse(actual.isCombinedDecision());
		assertEquals(1, actual.getIncludeInResultAttributes().size());
		assertBooleanValues(actual);
	}

	@ParameterizedTest
	@CsvSource({"ReturnPolicyIdList,yes", "ReturnPolicyIdList,TRUE", "CombinedDecision,yes",
			"CombinedDecision,TRUE", "IncludeInResult,yes", "IncludeInResult,TRUE"})
	public void invalidTextFlagsAreSyntaxErrors(String property, String lexical)
	{
		JsonObject source = request("true", "false");
		(property.equals("IncludeInResult") ? attribute(source) : source).addProperty(property, lexical);
		SyntaxException error = assertThrows(SyntaxException.class, () -> unmarshal(source));
		assertEquals(StatusCodeId.SYNTAX_ERROR, error.getStatus().getStatusCode().getValue());
	}

	@Test
	public void nativeJsonBooleansAndAbsentFlagDefaultsArePreserved() throws Exception
	{
		JsonObject source = request("true", "false");
		JsonArray values = new JsonArray();
		values.add(true);
		values.add(false);
		attribute(source).add("Value", values);
		RequestContext defaults = unmarshal(source);
		assertFalse(defaults.isReturnPolicyIdList());
		assertFalse(defaults.isCombinedDecision());
		assertTrue(defaults.getIncludeInResultAttributes().isEmpty());
		assertBooleanValues(defaults);
		source.addProperty("ReturnPolicyIdList", true);
		source.addProperty("CombinedDecision", false);
		attribute(source).addProperty("IncludeInResult", true);
		RequestContext explicit = unmarshal(source);
		assertTrue(explicit.isReturnPolicyIdList());
		assertFalse(explicit.isCombinedDecision());
		assertEquals(1, explicit.getIncludeInResultAttributes().size());
		assertBooleanValues(explicit);
	}

	private void assertBooleanValues(RequestContext actual)
	{
		assertEquals(List.of(BooleanVal.of(true), BooleanVal.of(false)), List.copyOf(
				actual.getAttributeValues(CategoryId.RESOURCE, "urn:test:boolean", XacmlTypes.BOOLEAN, null)));
	}

	private RequestContext unmarshal(JsonObject source) throws Exception
	{
		return new JsonRequestContextUnmarshaller().unmarshal(new StringReader(source.toString()));
	}

	private JsonObject request(String first, String second)
	{
		JsonArray values = new JsonArray();
		values.add(first);
		values.add(second);
		JsonObject attribute = new JsonObject();
		attribute.addProperty("AttributeId", "urn:test:boolean");
		attribute.addProperty("DataType", XacmlTypes.BOOLEAN.getTypeId());
		attribute.add("Value", values);
		JsonArray attributes = new JsonArray();
		attributes.add(attribute);
		JsonObject category = new JsonObject();
		category.addProperty("CategoryId", CategoryId.RESOURCE.getId());
		category.add("Attribute", attributes);
		JsonArray categories = new JsonArray();
		categories.add(category);
		JsonObject request = new JsonObject();
		request.add("Category", categories);
		return request;
	}

	private JsonObject attribute(JsonObject request)
	{
		return request.getAsJsonArray("Category").get(0).getAsJsonObject()
				.getAsJsonArray("Attribute").get(0).getAsJsonObject();
	}
}
