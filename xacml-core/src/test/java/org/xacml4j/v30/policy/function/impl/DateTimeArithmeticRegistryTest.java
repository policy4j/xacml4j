package org.xacml4j.v30.policy.function.impl;

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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.ModelFixtures;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.ISO8601Date;
import org.xacml4j.v30.types.ISO8601DateTime;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * XACML 3.0 A.3.7: subtract a signed duration by adding its negation.
 * XML Schema Part 2 Appendix E.1 and XPath F&O 1.0 sections 10.8.4-10.8.10:
 * apply the combined year/month change before pinning the day, and retain
 * the input timezone, including its absence.
 * https://www.w3.org/TR/xmlschema-2/#adding-durations-to-dateTimes
 * https://www.w3.org/TR/2007/REC-xpath-functions-20070123/#func-add-yearMonthDuration-to-dateTime
 */
public class DateTimeArithmeticRegistryTest
{
	private static final FunctionProvider FUNCTIONS = FunctionProviderBuilder.builder()
			.withDefaultFunctions().build();

	static Stream<Arguments> cases()
	{
		String[][] rows = {
			{"dateTime-add-dayTimeDuration", "2002-03-22T08:00:00", "PT1H", "2002-03-22T09:00:00"},
			{"dateTime-add-dayTimeDuration", "2002-03-22T08:00:00", "-PT1H", "2002-03-22T07:00:00"},
			{"dateTime-add-dayTimeDuration", "2002-03-22T08:00:00", "PT0S", "2002-03-22T08:00:00"},
			{"dateTime-add-dayTimeDuration", "2002-03-22T08:00:00", "P1D", "2002-03-23T08:00:00"},
			{"dateTime-add-dayTimeDuration", "2002-03-22T08:00:00", "-P1D", "2002-03-21T08:00:00"},
			{"dateTime-add-dayTimeDuration", "2002-03-22T08:00:00", "PT1.5S", "2002-03-22T08:00:01.5"},
			{"dateTime-add-dayTimeDuration", "2002-03-22T08:00:00", "-PT1.5S", "2002-03-22T07:59:58.5"},
			{"dateTime-subtract-dayTimeDuration", "2002-03-22T08:00:00", "PT1H", "2002-03-22T07:00:00"},
			{"dateTime-subtract-dayTimeDuration", "2002-03-22T08:00:00", "-PT1H", "2002-03-22T09:00:00"},
			{"dateTime-subtract-dayTimeDuration", "2002-03-22T08:00:00", "PT0S", "2002-03-22T08:00:00"},
			{"dateTime-subtract-dayTimeDuration", "2002-03-22T08:00:00", "P1D", "2002-03-21T08:00:00"},
			{"dateTime-subtract-dayTimeDuration", "2002-03-22T08:00:00", "-P1D", "2002-03-23T08:00:00"},
			{"dateTime-subtract-dayTimeDuration", "2002-03-22T08:00:00", "PT1.5S", "2002-03-22T07:59:58.5"},
			{"dateTime-subtract-dayTimeDuration", "2002-03-22T08:00:00", "-PT1.5S", "2002-03-22T08:00:01.5"},
			{"dateTime-add-yearMonthDuration", "2002-03-22T08:00:00", "P1Y2M", "2003-05-22T08:00:00"},
			{"dateTime-add-yearMonthDuration", "2002-03-22T08:00:00", "-P1Y2M", "2001-01-22T08:00:00"},
			{"dateTime-add-yearMonthDuration", "2002-03-22T08:00:00", "P0M", "2002-03-22T08:00:00"},
			{"dateTime-add-yearMonthDuration", "2002-01-31T08:00:00", "P1M", "2002-02-28T08:00:00"},
			{"dateTime-add-yearMonthDuration", "2002-03-31T08:00:00", "-P1M", "2002-02-28T08:00:00"},
			{"dateTime-add-yearMonthDuration", "2004-02-29T08:00:00", "P1Y", "2005-02-28T08:00:00"},
			{"dateTime-add-yearMonthDuration", "2020-02-29T08:00:00", "P1Y1M", "2021-03-29T08:00:00"},
			{"dateTime-add-yearMonthDuration", "2020-02-29T08:00:00", "-P1Y1M", "2019-01-29T08:00:00"},
			{"dateTime-subtract-yearMonthDuration", "2002-03-22T08:00:00", "P1Y2M", "2001-01-22T08:00:00"},
			{"dateTime-subtract-yearMonthDuration", "2002-03-22T08:00:00", "-P1Y2M", "2003-05-22T08:00:00"},
			{"dateTime-subtract-yearMonthDuration", "2002-03-22T08:00:00", "P0M", "2002-03-22T08:00:00"},
			{"dateTime-subtract-yearMonthDuration", "2002-03-31T08:00:00", "P1M", "2002-02-28T08:00:00"},
			{"dateTime-subtract-yearMonthDuration", "2002-01-31T08:00:00", "-P1M", "2002-02-28T08:00:00"},
			{"dateTime-subtract-yearMonthDuration", "2004-02-29T08:00:00", "P1Y", "2003-02-28T08:00:00"},
			{"dateTime-subtract-yearMonthDuration", "2020-02-29T08:00:00", "P1Y1M", "2019-01-29T08:00:00"},
			{"dateTime-subtract-yearMonthDuration", "2020-02-29T08:00:00", "-P1Y1M", "2021-03-29T08:00:00"},
			{"date-add-yearMonthDuration", "2002-03-22", "P1Y2M", "2003-05-22"},
			{"date-add-yearMonthDuration", "2002-03-22", "-P1Y2M", "2001-01-22"},
			{"date-add-yearMonthDuration", "2002-03-22", "P0M", "2002-03-22"},
			{"date-add-yearMonthDuration", "2002-01-31", "P1M", "2002-02-28"},
			{"date-add-yearMonthDuration", "2002-03-31", "-P1M", "2002-02-28"},
			{"date-add-yearMonthDuration", "2004-02-29", "P1Y", "2005-02-28"},
			{"date-add-yearMonthDuration", "2020-02-29", "P1Y1M", "2021-03-29"},
			{"date-add-yearMonthDuration", "2020-02-29", "-P1Y1M", "2019-01-29"},
			{"date-subtract-yearMonthDuration", "2002-03-22", "P1Y2M", "2001-01-22"},
			{"date-subtract-yearMonthDuration", "2002-03-22", "-P1Y2M", "2003-05-22"},
			{"date-subtract-yearMonthDuration", "2002-03-22", "P0M", "2002-03-22"},
			{"date-subtract-yearMonthDuration", "2002-03-31", "P1M", "2002-02-28"},
			{"date-subtract-yearMonthDuration", "2002-01-31", "-P1M", "2002-02-28"},
			{"date-subtract-yearMonthDuration", "2004-02-29", "P1Y", "2003-02-28"},
			{"date-subtract-yearMonthDuration", "2020-02-29", "P1Y1M", "2019-01-29"},
			{"date-subtract-yearMonthDuration", "2020-02-29", "-P1Y1M", "2021-03-29"}
		};
		return Arrays.stream(rows).flatMap(row ->
				Stream.of("", "Z", "-05:00", "+05:30").flatMap(zone ->
						Stream.of("3.0", "1.0").flatMap(version ->
								Stream.of(false, true).map(validate -> Arguments.of(
										"urn:oasis:names:tc:xacml:" + version + ":function:" + row[0],
										row[1] + zone, row[2], row[3] + zone, validate)))));
	}

	@ParameterizedTest(name = "[{index}] {0}({1}, {2}) = {3}; validate={4}")
	@MethodSource("cases")
	public void arithmeticThroughCanonicalAndLegacyUrns(String urn, String input,
			String duration, String expected, boolean validate)
	{
		Expression value = input.contains("T")
				? XacmlTypes.DATETIME.ofAny(input) : XacmlTypes.DATE.ofAny(input);
		Expression interval = urn.endsWith("dayTimeDuration")
				? XacmlTypes.DAYTIMEDURATION.ofAny(duration) : XacmlTypes.YEARMONTHDURATION.ofAny(duration);
		Apply apply = Apply.builder(urn, FUNCTIONS::getFunction).param(value, interval).build();
		assertEquals(value.getEvaluatesTo(), apply.getEvaluatesTo(), "Arithmetic result type");
		ValueExp result = apply.evaluate(ModelFixtures.context(validate));
		assertEquals(value.getEvaluatesTo(), result.getEvaluatesTo(), "Evaluated result type");
		String lexical = value instanceof ISO8601DateTime
				? assertInstanceOf(ISO8601DateTime.class, result).toXacmlString()
				: assertInstanceOf(ISO8601Date.class, result).toXacmlString();
		assertEquals(expected, lexical, "Signed arithmetic, month-end and timezone preservation");
	}
}
