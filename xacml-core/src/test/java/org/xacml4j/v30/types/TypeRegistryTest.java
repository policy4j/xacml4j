package org.xacml4j.v30.types;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.truth.Truth;
import org.xacml4j.v30.SyntaxException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class TypeRegistryTest
{
	public static Stream<Arguments> identifiers()
	{
		return Stream.of(
				Arguments.of("http://www.w3.org/2001/XMLSchema#anyURI", "http://www.w3.org/2001/XMLSchema#anyURI"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#anyURI", "anyURI"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#base64Binary", "http://www.w3.org/2001/XMLSchema#base64Binary"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#base64Binary", "base64Binary"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#boolean", "http://www.w3.org/2001/XMLSchema#boolean"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#boolean", "boolean"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#date", "http://www.w3.org/2001/XMLSchema#date"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#date", "date"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#dateTime", "http://www.w3.org/2001/XMLSchema#dateTime"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#dateTime", "dateTime"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#dayTimeDuration", "http://www.w3.org/2001/XMLSchema#dayTimeDuration"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#dayTimeDuration", "dayTimeDuration"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#dayTimeDuration", "http://www.w3.org/TR/2002/WD-xquery-operators-20020816#dayTimeDuration"),
				Arguments.of("urn:oasis:names:tc:xacml:2.0:data-type:dnsName", "urn:oasis:names:tc:xacml:2.0:data-type:dnsName"),
				Arguments.of("urn:oasis:names:tc:xacml:2.0:data-type:dnsName", "dnsName"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#double", "http://www.w3.org/2001/XMLSchema#double"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#double", "double"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#integer", "http://www.w3.org/2001/XMLSchema#integer"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#integer", "integer"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#hexBinary", "http://www.w3.org/2001/XMLSchema#hexBinary"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#hexBinary", "hexBinary"),
				Arguments.of("urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", "urn:oasis:names:tc:xacml:2.0:data-type:ipAddress"),
				Arguments.of("urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", "ipAddress"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#string", "http://www.w3.org/2001/XMLSchema#string"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#string", "string"),
				Arguments.of("urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", "urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name"),
				Arguments.of("urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", "rfc822Name"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#time", "http://www.w3.org/2001/XMLSchema#time"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#time", "time"),
				Arguments.of("urn:oasis:names:tc:xacml:1.0:data-type:x500Name", "urn:oasis:names:tc:xacml:1.0:data-type:x500Name"),
				Arguments.of("urn:oasis:names:tc:xacml:1.0:data-type:x500Name", "x500Name"),
				Arguments.of("urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression", "urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression"),
				Arguments.of("urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression", "xpathExpression"),
				Arguments.of("urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression", "urn:oasis:names:tc:xacml:2.0:data-type:xpathExpression"),
				Arguments.of("urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression", "urn:oasis:names:tc:xacml:2.0:data-type:xpath-expression"),
				Arguments.of("urn:oasis:names:tc:xacml:3.0:data-type:jsonPathExpression", "urn:oasis:names:tc:xacml:3.0:data-type:jsonPathExpression"),
				Arguments.of("urn:oasis:names:tc:xacml:3.0:data-type:jsonPathExpression", "jsonPathExpression"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#yearMonthDuration", "http://www.w3.org/2001/XMLSchema#yearMonthDuration"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#yearMonthDuration", "yearMonthDuration"),
				Arguments.of("http://www.w3.org/2001/XMLSchema#yearMonthDuration", "http://www.w3.org/TR/2002/WD-xquery-operators-20020816#yearMonthDuration"),
				Arguments.of("urn:oasis:names:tc:xacml:3.0:data-type:entity", "urn:oasis:names:tc:xacml:3.0:data-type:entity"),
				Arguments.of("urn:oasis:names:tc:xacml:3.0:data-type:entity", "entity"));
	}

	@ParameterizedTest(name = "{1} resolves to {0}")
	@MethodSource("identifiers")
	public void resolvesBaselineTypeIdentifierAndAlias(String canonicalId, String lookupId)
	{
		ValueType canonicalType = XacmlTypes.getType(canonicalId).orElseThrow();
		ValueType resolvedType = XacmlTypes.getType(lookupId).orElseThrow();

		Truth.assertThat(canonicalType.getTypeId()).isEqualTo(canonicalId);
		Truth.assertThat(resolvedType.getTypeId()).isEqualTo(canonicalId);
		Object sample = sampleValue(canonicalId);
		assertEquals((Object) canonicalType.ofAny(sample), (Object) resolvedType.ofAny(sample),
				"Alias must parse a representative value through the resolved type");
	}

	private static Object sampleValue(String canonicalId)
	{
		return switch (canonicalId) {
			case "http://www.w3.org/2001/XMLSchema#anyURI" -> "urn:example:test";
			case "http://www.w3.org/2001/XMLSchema#base64Binary" -> "YQ==";
			case "http://www.w3.org/2001/XMLSchema#boolean" -> "true";
			case "http://www.w3.org/2001/XMLSchema#date" -> "2002-03-22Z";
			case "http://www.w3.org/2001/XMLSchema#dateTime" -> "2002-03-22T08:23:48Z";
			case "http://www.w3.org/2001/XMLSchema#dayTimeDuration" -> "PT1.5S";
			case "urn:oasis:names:tc:xacml:2.0:data-type:dnsName" -> "example.com";
			case "http://www.w3.org/2001/XMLSchema#double" -> "12.5";
			case "http://www.w3.org/2001/XMLSchema#integer" -> "56";
			case "http://www.w3.org/2001/XMLSchema#hexBinary" -> "0A4F";
			case "urn:oasis:names:tc:xacml:2.0:data-type:ipAddress" -> "127.0.0.1";
			case "http://www.w3.org/2001/XMLSchema#string" -> "value";
			case "urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name" -> "alice@example.com";
			case "http://www.w3.org/2001/XMLSchema#time" -> "23:00:00Z";
			case "urn:oasis:names:tc:xacml:1.0:data-type:x500Name" -> "CN=Alice";
			case "urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression" -> "/root/item";
			case "urn:oasis:names:tc:xacml:3.0:data-type:jsonPathExpression" -> "$.root.item";
			case "http://www.w3.org/2001/XMLSchema#yearMonthDuration" -> "P1Y2M";
			case "urn:oasis:names:tc:xacml:3.0:data-type:entity" -> Entity.builder().build();
			default -> throw new IllegalArgumentException("No sample for " + canonicalId);
		};
	}

	public static Stream<Arguments> lexicalValues()
	{
		return Stream.of(
				Arguments.of(XacmlTypes.ANYURI, "urn:example:test", "urn:example:test", (Function<Value, String>) v -> ((AnyURI) v).toXacmlString()),
				Arguments.of(XacmlTypes.BASE64BINARY, "YQ==", "YQ==", (Function<Value, String>) v -> ((Base64Binary) v).toXacmlString()),
				Arguments.of(XacmlTypes.BOOLEAN, "true", "true", (Function<Value, String>) v -> ((BooleanVal) v).toXacmlString()),
				Arguments.of(XacmlTypes.DATE, "2002-03-22Z", "2002-03-22Z", (Function<Value, String>) v -> ((ISO8601Date) v).toXacmlString()),
				Arguments.of(XacmlTypes.DATETIME, "2002-03-22T08:23:48Z", "2002-03-22T08:23:48Z", (Function<Value, String>) v -> ((ISO8601DateTime) v).toXacmlString()),
				Arguments.of(XacmlTypes.DAYTIMEDURATION, "PT1.5S", "P0DT0H0M1.5S", (Function<Value, String>) v -> ((ISO8601DayTimeDuration) v).toXacmlString()),
				Arguments.of(XacmlTypes.DNSNAME, "example.com", "example.com", (Function<Value, String>) v -> ((DNSName) v).toXacmlString()),
				Arguments.of(XacmlTypes.DOUBLE, "12.5", "12.5", (Function<Value, String>) v -> ((DoubleVal) v).toXacmlString()),
				Arguments.of(XacmlTypes.INTEGER, "56", "56", (Function<Value, String>) v -> ((IntegerVal) v).toXacmlString()),
				Arguments.of(XacmlTypes.HEXBINARY, "0A4F", "0A4F", (Function<Value, String>) v -> ((HexBinary) v).get().toHexEncoded()),
				Arguments.of(XacmlTypes.IPADDRESS, "127.0.0.1", "127.0.0.1", (Function<Value, String>) v -> ((IPAddress) v).toXacmlString()),
				Arguments.of(XacmlTypes.STRING, "plain text", "plain text", (Function<Value, String>) v -> ((StringVal) v).get()),
				Arguments.of(XacmlTypes.RFC822NAME, "alice@example.com", "alice@example.com", (Function<Value, String>) v -> ((RFC822Name) v).toXacmlString()),
				Arguments.of(XacmlTypes.TIME, "23:00:00Z", "23:00:00Z", (Function<Value, String>) v -> ((ISO8601Time) v).toXacmlString()),
				Arguments.of(XacmlTypes.X500NAME, "CN=Alice, OU=Engineering, O=Example, C=US", "CN=Alice, OU=Engineering, O=Example, C=US", (Function<Value, String>) v -> ((X500Name) v).toXacmlString()),
				Arguments.of(XacmlTypes.YEARMONTHDURATION, "P1Y2M", "P1Y2M", (Function<Value, String>) v -> ((ISO8601YearMonthDuration) v).toXacmlString()));
	}

	public static Stream<Arguments> canonicalLexicalValues()
	{
		return Stream.of(
				Arguments.of(XacmlTypes.DOUBLE, "INF", "INF", (Function<Value, String>) v -> ((DoubleVal) v).toXacmlString()),
				Arguments.of(XacmlTypes.DOUBLE, "-INF", "-INF", (Function<Value, String>) v -> ((DoubleVal) v).toXacmlString()),
				Arguments.of(XacmlTypes.DOUBLE, "NaN", "NaN", (Function<Value, String>) v -> ((DoubleVal) v).toXacmlString()),
				Arguments.of(XacmlTypes.DATE, "2002-03-22Z", "2002-03-22Z", (Function<Value, String>) v -> ((ISO8601Date) v).toXacmlString()),
				Arguments.of(XacmlTypes.DATE, "2002-03-22-12:00", "2002-03-22-12:00", (Function<Value, String>) v -> ((ISO8601Date) v).toXacmlString()),
				Arguments.of(XacmlTypes.DATETIME, "2002-03-22T08:23:48.125+01:00", "2002-03-22T08:23:48.125+01:00", (Function<Value, String>) v -> ((ISO8601DateTime) v).toXacmlString()),
				Arguments.of(XacmlTypes.TIME, "23:00:00-05:00", "23:00:00-05:00", (Function<Value, String>) v -> ((ISO8601Time) v).toXacmlString()),
				Arguments.of(XacmlTypes.BASE64BINARY, "YQ==", "YQ==", (Function<Value, String>) v -> ((Base64Binary) v).toXacmlString()),
				Arguments.of(XacmlTypes.HEXBINARY, "0a4f", "0A4F", (Function<Value, String>) v -> ((HexBinary) v).get().toHexEncoded()),
				Arguments.of(XacmlTypes.STRING, "  spaced string  ", "  spaced string  ", (Function<Value, String>) v -> ((StringVal) v).get()));
	}

	@Test
	public void negativeDoubleZeroSurvivesSerialization()
	{
		// The engine prints valid xs:double lexical forms (e.g. 12.5), not the XML Schema
		// canonical mantissa/exponent form, so pin the value, not one spelling.
		DoubleVal parsed = (DoubleVal) XacmlTypes.DOUBLE.ofAny("-0");
		String lexical = parsed.toXacmlString();
		DoubleVal reparsed = (DoubleVal) XacmlTypes.DOUBLE.ofAny(lexical);
		assertEquals(Double.doubleToRawLongBits(-0.0d), Double.doubleToRawLongBits(reparsed.get()),
				"negative zero must survive serialization as " + lexical);
	}

	@Test
	public void base64BinaryRejectsUnpaddedLexicalFormAndAcceptsWhitespace()
	{
		SyntaxException error = assertThrows(SyntaxException.class,
				() -> XacmlTypes.BASE64BINARY.ofAny("YQ"));
		Truth.assertThat(error.getStatus().getStatusCode().getValue().getId())
				.isEqualTo("urn:oasis:names:tc:xacml:1.0:status:syntax-error");
		assertEquals("YQ==", ((Base64Binary) XacmlTypes.BASE64BINARY.ofAny(" \nYQ==\t"))
				.toXacmlString());
	}

	@ParameterizedTest(name = "{1} serializes to canonical {2}")
	@MethodSource("canonicalLexicalValues")
	public void lexicalValueUsesExpectedCanonicalForm(ValueType type, String lexical, String expectedCanonical,
			Function<Value, String> formatter)
	{
		Value parsed = (Value) type.ofAny(lexical);
		String serialized = formatter.apply(parsed);
		assertEquals(expectedCanonical, serialized);
		ValueType resolved = XacmlTypes.getType(type.getTypeId()).orElseThrow();
		assertEquals((Object) type.ofAny(expectedCanonical), (Object) resolved.ofAny(serialized));
	}

	@ParameterizedTest(name = "AnyURI preserves lexical form: {1}")
	@MethodSource("anyUriCanonicalLexicalValues")
	public void anyUriPreservesOriginalLexicalForm(String lexical, String expectedLexical)
	{
		AnyURI parsed = (AnyURI) XacmlTypes.ANYURI.ofAny(lexical);
		assertEquals(expectedLexical, parsed.toXacmlString());
		assertEquals((Object) XacmlTypes.ANYURI.ofAny(expectedLexical),
				(Object) XacmlTypes.ANYURI.ofAny(parsed.toXacmlString()));
	}

	public static Stream<Arguments> anyUriCanonicalLexicalValues()
	{
		return Stream.of(
				Arguments.of("http://example.com/a/../b", "http://example.com/a/../b"),
				Arguments.of("http://example.com/a%20b", "http://example.com/a%20b"));
	}

	@ParameterizedTest(name = "{0} lexical value round-trips")
	@MethodSource("lexicalValues")
	public void scalarLexicalValueRoundTrips(ValueType type, String lexical, String expectedLexical,
			Function<Value, String> formatter)
	{
		Value value = (Value) type.ofAny(lexical);
		String serialized = formatter.apply(value);

		assertEquals(expectedLexical, serialized);
		Truth.assertThat((Value) type.ofAny(serialized)).isEqualTo((Value) type.ofAny(expectedLexical));
	}

	public static Stream<Arguments> pathLexicalValues()
	{
		return Stream.of(
				Arguments.of(XacmlTypes.XPATH, "/root/item"),
				Arguments.of(XacmlTypes.JPATH, "$.root.item"));
	}

	@ParameterizedTest(name = "{0} path lexical value round-trips")
	@MethodSource("pathLexicalValues")
	public void pathLexicalValueRoundTrips(ValueType type, String lexical)
	{
		Path value = (Path) type.ofAny(lexical);
		Path reparsed = (Path) type.ofAny(value.getPath());

		Truth.assertThat(reparsed).isEqualTo(value);
	}

	@Test
	public void duplicateTypeIdentifierFailsRegistration()
	{
		ValueType first = mock(ValueType.class, "first-type-source");
		when(first.getTypeId()).thenReturn("urn:test:data-type:duplicate");
		when(first.getShortTypeId()).thenReturn("first-short-id");
		when(first.getTypeIdAliases()).thenReturn(java.util.Set.of());
		ValueType second = mock(ValueType.class, "second-type-source");
		when(second.getTypeId()).thenReturn("urn:test:data-type:duplicate");
		when(second.getShortTypeId()).thenReturn("second-short-id");
		when(second.getTypeIdAliases()).thenReturn(java.util.Set.of());

		IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
				() -> new TypeFactory.BaseTypeFactory(Arrays.asList(first, second)));
		assertTrue(failure.getMessage().contains("urn:test:data-type:duplicate"));
		assertTrue(failure.getMessage().contains("first-type-source"));
		assertTrue(failure.getMessage().contains("second-type-source"));
	}

	@Test
	public void duplicateFunctionIdentifierFailsRegistration()
	{
		IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
				() -> new org.xacml4j.v30.policy.function.impl.AggregatingFunctionProvider(Arrays.asList(
						new org.xacml4j.v30.policy.function.impl.XacmlDefaultFunctions(),
						new org.xacml4j.v30.policy.function.impl.XacmlDefaultFunctions())));
		assertTrue(failure.getMessage().contains("double-greater-than"), failure.getMessage());
	}

	@Test
	public void registeringSameTypeObjectTwiceIsIdempotent()
	{
		TypeFactory factory = new TypeFactory.BaseTypeFactory(Arrays.asList(XacmlTypes.STRING, XacmlTypes.STRING));
		assertSame(XacmlTypes.STRING, factory.forType(XacmlTypes.STRING.getTypeId()).orElseThrow());
	}
}
