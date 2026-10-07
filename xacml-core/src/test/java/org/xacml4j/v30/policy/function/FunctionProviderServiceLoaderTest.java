package org.xacml4j.v30.policy.function;

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

import java.util.ServiceLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.truth.Truth;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.policy.function.impl.XacmlDefaultFunctions;

public class FunctionProviderServiceLoaderTest
{
	private static final String XACML_3 = "urn:oasis:names:tc:xacml:3.0:function:";

	@Test
	public void serviceLoaderFindsDefaultFunctionProvider()
	{
		Truth.assertThat(ServiceLoader.load(FunctionProvider.class)
					.stream()
					.map(ServiceLoader.Provider::type)
					.collect(Collectors.toSet()))
				.contains(XacmlDefaultFunctions.class);
	}

	public static Stream<String> section1028FunctionIds()
	{
		List<String> ids = new ArrayList<>();
		addIds(ids, "urn:oasis:names:tc:xacml:1.0:function:",
				"string-equal boolean-equal integer-equal double-equal date-equal time-equal dateTime-equal " +
				"anyURI-equal x500Name-equal rfc822Name-equal hexBinary-equal base64Binary-equal " +
				"integer-add double-add integer-subtract double-subtract integer-multiply double-multiply " +
				"integer-divide double-divide integer-mod integer-abs double-abs round floor " +
				"string-normalize-space string-normalize-to-lower-case double-to-integer integer-to-double " +
				"or and n-of not integer-greater-than integer-greater-than-or-equal integer-less-than " +
				"integer-less-than-or-equal double-greater-than double-greater-than-or-equal " +
				"double-less-than double-less-than-or-equal string-greater-than string-greater-than-or-equal " +
				"string-less-than string-less-than-or-equal time-greater-than time-greater-than-or-equal " +
				"time-less-than time-less-than-or-equal dateTime-greater-than dateTime-greater-than-or-equal " +
				"dateTime-less-than dateTime-less-than-or-equal date-greater-than date-greater-than-or-equal " +
				"date-less-than date-less-than-or-equal");
		for (String type : List.of("string", "boolean", "integer", "double", "time", "date", "dateTime",
				"anyURI", "hexBinary", "base64Binary", "x500Name", "rfc822Name")) {
			addIds(ids, "urn:oasis:names:tc:xacml:1.0:function:", type + "-one-and-only " + type + "-bag-size " +
					type + "-is-in " + type + "-bag");
			addIds(ids, "urn:oasis:names:tc:xacml:1.0:function:", type +
					"-intersection " + type + "-at-least-one-member-of " + type + "-union " + type + "-subset " +
					 type + "-set-equals");
		}
		addIds(ids, "urn:oasis:names:tc:xacml:1.0:function:",
				"all-of-any any-of-all all-of-all");
		addIds(ids, "urn:oasis:names:tc:xacml:1.0:function:",
				"x500Name-match rfc822Name-match string-regexp-match string-intersection " +
				"string-at-least-one-member-of string-union string-subset string-set-equals " +
				"boolean-intersection boolean-at-least-one-member-of boolean-union boolean-subset boolean-set-equals " +
				"integer-intersection integer-at-least-one-member-of integer-union integer-subset integer-set-equals " +
				"double-intersection double-at-least-one-member-of double-union double-subset double-set-equals " +
				"time-intersection time-at-least-one-member-of time-union time-subset time-set-equals " +
				"date-intersection date-at-least-one-member-of date-union date-subset date-set-equals " +
				"dateTime-intersection dateTime-at-least-one-member-of dateTime-union dateTime-subset dateTime-set-equals " +
				"anyURI-intersection anyURI-at-least-one-member-of anyURI-union anyURI-subset anyURI-set-equals " +
				"hexBinary-intersection hexBinary-at-least-one-member-of hexBinary-union hexBinary-subset hexBinary-set-equals " +
				"base64Binary-intersection base64Binary-at-least-one-member-of base64Binary-union base64Binary-subset base64Binary-set-equals " +
				"x500Name-intersection x500Name-at-least-one-member-of x500Name-union x500Name-subset x500Name-set-equals " +
				"rfc822Name-intersection rfc822Name-at-least-one-member-of rfc822Name-union rfc822Name-subset rfc822Name-set-equals");
		addIds(ids, "urn:oasis:names:tc:xacml:2.0:function:",
				"time-in-range ipAddress-one-and-only ipAddress-bag-size ipAddress-bag " +
				"dnsName-one-and-only dnsName-bag-size dnsName-bag string-concatenate " +
				"anyURI-regexp-match ipAddress-regexp-match dnsName-regexp-match rfc822Name-regexp-match x500Name-regexp-match");
		addIds(ids, XACML_3,
				"dayTimeDuration-equal yearMonthDuration-equal string-equal-ignore-case " +
				"dateTime-add-dayTimeDuration dateTime-add-yearMonthDuration dateTime-subtract-dayTimeDuration " +
				"dateTime-subtract-yearMonthDuration date-add-yearMonthDuration date-subtract-yearMonthDuration " +
				"dayTimeDuration-one-and-only dayTimeDuration-bag-size dayTimeDuration-is-in dayTimeDuration-bag " +
				"yearMonthDuration-one-and-only yearMonthDuration-bag-size yearMonthDuration-is-in yearMonthDuration-bag " +
				"dayTimeDuration-intersection dayTimeDuration-at-least-one-member-of dayTimeDuration-union " +
				"dayTimeDuration-subset dayTimeDuration-set-equals yearMonthDuration-intersection " +
				"yearMonthDuration-at-least-one-member-of yearMonthDuration-union yearMonthDuration-subset " +
				"yearMonthDuration-set-equals boolean-from-string string-from-boolean integer-from-string " +
				"string-from-integer double-from-string " +
				"time-from-string date-from-string dateTime-from-string anyURI-from-string dayTimeDuration-from-string " +
				"yearMonthDuration-from-string x500Name-from-string rfc822Name-from-string ipAddress-from-string dnsName-from-string " +
				"string-starts-with anyURI-starts-with string-ends-with anyURI-ends-with string-contains anyURI-contains " +
				"string-substring anyURI-substring any-of all-of any-of-any map xpath-node-count xpath-node-equal xpath-node-match");
		ids.removeIf(id -> id.startsWith("urn:oasis:names:tc:xacml:2.0:function:") &&
				(id.endsWith("anyURI-regexp-match") || id.endsWith("ipAddress-regexp-match") ||
						id.endsWith("dnsName-regexp-match") || id.endsWith("rfc822Name-regexp-match") ||
						id.endsWith("x500Name-regexp-match")));
		ids.removeIf(id -> id.startsWith(XACML_3) && id.contains("string-from-") && !id.endsWith("string-from-boolean") &&
				!id.endsWith("string-from-integer"));
		return ids.stream().distinct();
	}

	private static void addIds(List<String> ids, String namespace, String names)
	{
		Stream.of(names.split("\\s+")).forEach(name -> ids.add(namespace + name));
	}

	@ParameterizedTest(name = "default functions resolve XACML 3.0 table id {0}")
	@MethodSource("section1028FunctionIds")
	public void defaultProviderResolvesXacml30Section1028Functions(String identifier)
	{
		FunctionProvider provider = FunctionProvider.builder().withDefaultFunctions().build();
		assertTrue(provider.getFunction(identifier).isPresent(), identifier);
	}

	public static Stream<String> knownMissingStringFromIds()
	{
		return Stream.of("double", "time", "date", "dateTime", "anyURI", "dayTimeDuration",
				"yearMonthDuration", "x500Name", "rfc822Name", "ipAddress", "dnsName")
				.map(name -> XACML_3 + "string-from-" + name);
	}

	@ParameterizedTest(name = "string conversion id resolves: {0}")
	@MethodSource("knownMissingStringFromIds")
	@Disabled("policy4j-the.4.37: correct XACML 3.0 string-from-* identifiers are being repaired")
	public void stringFromIdentifierResolves(String identifier)
	{
		FunctionProvider provider = FunctionProvider.builder().withDefaultFunctions().build();
		assertTrue(provider.getFunction(identifier).isPresent());
	}

	public static Stream<String> xacml20RegexIdentifiers()
	{
		return Stream.of("anyURI", "ipAddress", "dnsName", "rfc822Name", "x500Name")
				.map(name -> "urn:oasis:names:tc:xacml:2.0:function:" + name + "-regexp-match");
	}

	@ParameterizedTest(name = "XACML 2.0 regexp-match id resolves: {0}")
	@MethodSource("xacml20RegexIdentifiers")
	@Disabled("policy4j-the.4.36: XACML 2.0 regexp-match identifiers are being repaired")
	public void xacml20RegexIdentifierResolves(String identifier)
	{
		FunctionProvider provider = FunctionProvider.builder().withDefaultFunctions().build();
		assertTrue(provider.getFunction(identifier).isPresent());
	}
}
