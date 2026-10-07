package org.xacml4j.v30.types;

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


import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.policy.function.impl.DateTimeArithmeticFunctions;

/**
 * XACML 3.0 Appendix A.3.7 delegates to XML Schema Part 2 Appendix E.1;
 * the seconds addition includes the fractional parts of both operands.
 * https://www.w3.org/TR/xmlschema-2/#adding-durations-to-dateTimes
 */
public class ISO8601FractionalDateTimeArithmeticTest
{
    public static Collection<Object[]> cases()
    {
        return Arrays.asList(new Object[][] {
            {"2002-03-22T08:23:47-05:00", "PT1.5S", "2002-03-22T08:23:48.5-05:00", "2002-03-22T08:23:45.5-05:00"},
            {"2002-03-22T08:23:47-05:00", "PT0.5S", "2002-03-22T08:23:47.5-05:00", "2002-03-22T08:23:46.5-05:00"},
            {"2002-03-22T08:23:47-05:00", "PT0.001S", "2002-03-22T08:23:47.001-05:00", "2002-03-22T08:23:46.999-05:00"},
            {"2002-03-22T08:23:47-05:00", "-PT1.5S", "2002-03-22T08:23:45.5-05:00", "2002-03-22T08:23:48.5-05:00"},
            {"2002-03-22T08:23:47-05:00", "-PT0.5S", "2002-03-22T08:23:46.5-05:00", "2002-03-22T08:23:47.5-05:00"},
            {"2002-03-22T08:23:47-05:00", "-PT0.001S", "2002-03-22T08:23:46.999-05:00", "2002-03-22T08:23:47.001-05:00"},
            {"2002-03-22T08:23:47.250-05:00", "PT1.5S", "2002-03-22T08:23:48.75-05:00", "2002-03-22T08:23:45.75-05:00"},
            {"2002-03-22T08:23:47.250-05:00", "PT0.5S", "2002-03-22T08:23:47.75-05:00", "2002-03-22T08:23:46.75-05:00"},
            {"2002-03-22T08:23:47.250-05:00", "PT0.001S", "2002-03-22T08:23:47.251-05:00", "2002-03-22T08:23:47.249-05:00"},
            {"2002-03-22T08:23:47.250-05:00", "-PT1.5S", "2002-03-22T08:23:45.75-05:00", "2002-03-22T08:23:48.75-05:00"},
            {"2002-03-22T08:23:47.250-05:00", "-PT0.5S", "2002-03-22T08:23:46.75-05:00", "2002-03-22T08:23:47.75-05:00"},
            {"2002-03-22T08:23:47.250-05:00", "-PT0.001S", "2002-03-22T08:23:47.249-05:00", "2002-03-22T08:23:47.251-05:00"},
            {"2020-01-31T23:59:59.75-05:00", "PT0.5S", "2020-02-01T00:00:00.25-05:00", "2020-01-31T23:59:59.25-05:00"},
            {"2002-03-22T08:23:47-05:00", "P1DT2H3M1.5S", "2002-03-23T10:26:48.5-05:00", "2002-03-21T06:20:45.5-05:00"},
            {"2002-03-22T08:23:47-05:00", "PT0.000000001S", "2002-03-22T08:23:47.000000001-05:00", "2002-03-22T08:23:46.999999999-05:00"},
            // Conductor decision (packet 1.3i): discard sub-nanosecond magnitude toward zero.
            {"2002-03-22T08:23:47-05:00", "PT0.0000000001S", "2002-03-22T08:23:47-05:00", "2002-03-22T08:23:47-05:00"},
            {"2002-03-22T08:23:47-05:00", "-PT0.0000000001S", "2002-03-22T08:23:47-05:00", "2002-03-22T08:23:47-05:00"},
            {"2002-03-22T08:23:47-05:00", "PT1.0000000019S", "2002-03-22T08:23:48.000000001-05:00", "2002-03-22T08:23:45.999999999-05:00"},
            {"2002-03-22T08:23:47-05:00", "-PT1.0000000019S", "2002-03-22T08:23:45.999999999-05:00", "2002-03-22T08:23:48.000000001-05:00"},
            {"2002-03-22T08:23:47-05:00", "PT0.9999999999S", "2002-03-22T08:23:47.999999999-05:00", "2002-03-22T08:23:46.000000001-05:00"},
            {"2002-03-22T08:23:47-05:00", "-PT0.9999999999S", "2002-03-22T08:23:46.000000001-05:00", "2002-03-22T08:23:47.999999999-05:00"},
            {"2002-03-22T08:23:47.250-05:00", "PT1.0000000019S", "2002-03-22T08:23:48.250000001-05:00", "2002-03-22T08:23:46.249999999-05:00"},
            {"2002-03-22T08:23:47.250-05:00", "-PT1.0000000019S", "2002-03-22T08:23:46.249999999-05:00", "2002-03-22T08:23:48.250000001-05:00"}
        });
    }

	private String start;
	private String duration;
	private String expectedAdd;
	private String expectedSubtract;

	public void initISO8601FractionalDateTimeArithmeticTest(String start, String duration,
                                                 String expectedAdd, String expectedSubtract)
    {
        this.start = start;
        this.duration = duration;
        this.expectedAdd = expectedAdd;
        this.expectedSubtract = expectedSubtract;
    }

	@MethodSource("cases")
	@ParameterizedTest(name = "{0}, {1}")
	public void addPreservesFractionalSeconds(String start, String duration, String expectedAdd, String expectedSubtract)
    {
		initISO8601FractionalDateTimeArithmeticTest(start, duration, expectedAdd, expectedSubtract);
        assertEquals(expectedAdd, DateTimeArithmeticFunctions.add(ISO8601DateTime.of(start),
                ISO8601DayTimeDuration.of(duration)).toXacmlString());
    }

	@MethodSource("cases")
	@ParameterizedTest(name = "{0}, {1}")
	public void subtractPreservesFractionalSeconds(String start, String duration, String expectedAdd, String expectedSubtract)
    {
		initISO8601FractionalDateTimeArithmeticTest(start, duration, expectedAdd, expectedSubtract);
        assertEquals(expectedSubtract, DateTimeArithmeticFunctions.subtract(ISO8601DateTime.of(start),
                ISO8601DayTimeDuration.of(duration)).toXacmlString());
    }
}
