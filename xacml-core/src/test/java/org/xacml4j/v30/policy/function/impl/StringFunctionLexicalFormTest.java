package org.xacml4j.v30.policy.function.impl;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.XacmlTypes;

public class StringFunctionLexicalFormTest
{
	private static final FunctionProvider FUNCTIONS =
			FunctionProviderBuilder.builder().withDefaultFunctions().build();

	public static Stream<Arguments> conversions()
	{
		return Stream.of(
				Arguments.of("urn:oasis:names:tc:xacml:3.0:function:string-from-double", "INF",
						XacmlTypes.DOUBLE.ofAny(Double.POSITIVE_INFINITY)),
				Arguments.of("urn:oasis:names:tc:xacml:3.0:function:string-from-double", "-INF",
						XacmlTypes.DOUBLE.ofAny(Double.NEGATIVE_INFINITY)),
				Arguments.of("urn:oasis:names:tc:xacml:3.0:function:string-from-double", "NaN",
						XacmlTypes.DOUBLE.ofAny(Double.NaN)),
				Arguments.of("urn:oasis:names:tc:xacml:3.0:function:string-from-integer", "56",
						XacmlTypes.INTEGER.ofAny(56)),
				Arguments.of("urn:oasis:names:tc:xacml:3.0:function:string-from-boolean", "true",
						XacmlTypes.BOOLEAN.ofAny(true)),
				Arguments.of("urn:oasis:names:tc:xacml:3.0:function:string-from-date", "2002-03-22",
						XacmlTypes.DATE.ofAny("2002-03-22")),
				Arguments.of("urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime", "2002-03-22T08:23:48Z",
						XacmlTypes.DATETIME.ofAny("2002-03-22T08:23:48Z")),
				Arguments.of("urn:oasis:names:tc:xacml:3.0:function:string-from-time", "23:00:00Z",
						XacmlTypes.TIME.ofAny("23:00:00Z")),
				Arguments.of("urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration", "P0DT0H0M1.5S",
						XacmlTypes.DAYTIMEDURATION.ofAny("PT1.5S")),
				Arguments.of("urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration", "P1Y2M",
						XacmlTypes.YEARMONTHDURATION.ofAny("P1Y2M")),
				Arguments.of("urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI", "urn:example:test",
						XacmlTypes.ANYURI.ofAny("urn:example:test")),
				Arguments.of("urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name", "CN=Alice",
						XacmlTypes.X500NAME.ofAny("CN=Alice")),
				Arguments.of("urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name", "alice@example.com",
						XacmlTypes.RFC822NAME.ofAny("alice@example.com")),
				Arguments.of("urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress", "127.0.0.1",
						XacmlTypes.IPADDRESS.ofAny("127.0.0.1")),
				Arguments.of("urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName", "example.com",
						XacmlTypes.DNSNAME.ofAny("example.com")));
	}

	@ParameterizedTest(name = "{0} emits {1}")
	@MethodSource("conversions")
	public void stringFromFunctionsUseXacmlLexicalForms(String functionId, String expected, Value value)
			throws Exception
	{
		FunctionSpec function = FUNCTIONS.getFunction(functionId).orElseThrow();
		Expression expression = Apply.builder(function).param(value).build();
		ValueExp result = expression.evaluate(mock(EvaluationContext.class));

		assertEquals(expected, ((StringVal) result).get());
	}
}
