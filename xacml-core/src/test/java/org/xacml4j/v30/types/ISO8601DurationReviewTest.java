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

import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;

import org.junit.jupiter.api.Test;
import org.xacml4j.util.DateTimeDurationUtils;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;

@ExtendWith(MockitoExtension.class)
public class ISO8601DurationReviewTest
{
    @Test
    public void integralSecondsDiscardZeroFractionalScale()
    {
        for (String seconds : new String[]{"1", "10", "0"}) {
            for (String sign : new String[]{"", "-"}) {
                ISO8601DayTimeDuration whole = ISO8601DayTimeDuration.of(sign + "PT" + seconds + "S");
                ISO8601DayTimeDuration decimal = ISO8601DayTimeDuration.of(sign + "PT" + seconds + ".0S");
                assertEquals(whole.toXacmlString(), decimal.toXacmlString());
                assertEquals(0, ((BigDecimal)decimal.duration().getField(DatatypeConstants.SECONDS)).scale());
                assertEquals(whole, decimal);
                assertEquals(whole.hashCode(), decimal.hashCode());
                assertEquals(0, whole.compareTo(decimal));
            }
        }
        assertEquals("P0DT0H0M1S", ISO8601DayTimeDuration.of("PT1.0S").toXacmlString());
        assertEquals("-P0DT0H0M1S", ISO8601DayTimeDuration.of("-PT1.0S").toXacmlString());
    }

    @Test
    public void nonzeroFractionalLexicalFormStaysDecimal()
    {
        assertEquals("P0DT0H0M1.5S", ISO8601DayTimeDuration.of("PT1.5S").toXacmlString());
        assertEquals("-P0DT0H0M1.5S", ISO8601DayTimeDuration.of("-PT1.5S").toXacmlString());
    }

    @Test
    public void fractionalSecondsAreNotReportedAsZero()
    {
        for (String lexical : new String[]{"PT0.5S", "PT1.5S", "-PT0.5S", "PT0.001S"}) {
            ISO8601DayTimeDuration value = ISO8601DayTimeDuration.of(lexical);
            assertFalse(value.isZero(), lexical);
            BigDecimal seconds = new BigDecimal(lexical.startsWith("-")
                    ? lexical.substring(1, lexical.length() - 1).substring(2)
                    : lexical.substring(2, lexical.length() - 1));
            assertEquals(seconds, value.getSeconds(), lexical);
        }
        assertTrue(ISO8601DayTimeDuration.of("PT0S").isZero());
        assertTrue(ISO8601DayTimeDuration.of("PT1S").compareTo(
                ISO8601DayTimeDuration.of("PT1.0S")) == 0);
        assertEquals(ISO8601DayTimeDuration.of("PT1S"), ISO8601DayTimeDuration.of("PT1.0S"));
    }

    @Test
    public void javaDurationRoundTripKeepsFractionalSeconds()
    {
        assertEquals("P0DT0H0M0.5S",
                ISO8601DayTimeDuration.of(java.time.Duration.ofMillis(500)).toXacmlString());
        assertEquals("-P0DT0H0M0.5S",
                ISO8601DayTimeDuration.of(java.time.Duration.ofMillis(-500)).toXacmlString());
        for (String lexical : new String[]{"PT0.5S", "PT1.5S", "-PT0.5S", "PT0.001S"}) {
            ISO8601DayTimeDuration xacml = ISO8601DayTimeDuration.of(lexical);
            java.time.Duration javaDuration = xacml.toDuration();
            assertEquals(xacml, ISO8601DayTimeDuration.of(javaDuration), lexical);
            assertEquals(javaDuration, DateTimeDurationUtils.toISO8601DayTimeDuration(xacml.duration()), lexical);
        }
    }

    @Test
    public void javaDurationWholeSecondsUseSafeDecimalScaleAndKeepFractions()
    {
        java.time.Duration[] durations = {
                java.time.Duration.ofSeconds(10),
                java.time.Duration.ofSeconds(20),
                java.time.Duration.ofSeconds(59),
                java.time.Duration.ofSeconds(60),
                java.time.Duration.ofMinutes(1),
                java.time.Duration.ofMinutes(1).plusSeconds(30),
                java.time.Duration.ofSeconds(-10),
                java.time.Duration.ofSeconds(-20),
                java.time.Duration.ofSeconds(-59),
                java.time.Duration.ofSeconds(-60),
                java.time.Duration.ofMinutes(-1),
                java.time.Duration.ofMinutes(-1).minusSeconds(30),
                java.time.Duration.ofMillis(10_500),
                java.time.Duration.ofMillis(-10_500)
        };
        for (java.time.Duration duration : durations) {
            ISO8601DayTimeDuration value = ISO8601DayTimeDuration.of(duration);
            assertEquals(duration, value.toDuration(), duration.toString());
            assertEquals(value, ISO8601DayTimeDuration.of(value.toXacmlString()), duration.toString());
            assertEquals(duration.isNegative(), value.isNegative(), duration.toString());
        }
        assertEquals("P0DT0H0M10S",
                ISO8601DayTimeDuration.of(java.time.Duration.ofSeconds(10)).toXacmlString());
        assertEquals("P0DT0H0M10.5S",
                ISO8601DayTimeDuration.of(java.time.Duration.ofMillis(10_500)).toXacmlString());
        assertEquals("-P0DT0H0M10.5S",
                ISO8601DayTimeDuration.of(java.time.Duration.ofMillis(-10_500)).toXacmlString());
    }

    @Test
    public void subNanosecondConversionTruncatesTowardZero() throws Exception
    {
        // Packet 1.3i: preserve XML decimal precision; truncate only at the java.time boundary.
        String[] seconds = {"0.0000000001", "1.0000000019", "0.9999999999"};
        long[] nanos = {0L, 1_000_000_001L, 999_999_999L};
        DatatypeFactory factory = DatatypeFactory.newInstance();
        for (int i = 0; i < seconds.length; i++) {
            for (boolean negative : new boolean[]{false, true}) {
                String lexical = (negative ? "-" : "") + "PT" + seconds[i] + "S";
                ISO8601DayTimeDuration value = ISO8601DayTimeDuration.of(lexical);
                assertEquals(0, new BigDecimal(seconds[i]).compareTo(
                        (BigDecimal)value.duration().getField(DatatypeConstants.SECONDS)));
                java.time.Duration expected = java.time.Duration.ofNanos(negative ? -nanos[i] : nanos[i]);
                assertEquals(expected, value.toDuration());
                assertEquals(expected, DateTimeDurationUtils.toISO8601DayTimeDuration(factory.newDuration(lexical)));
            }
        }
    }

    @Test
    public void subNanosecondArithmeticEvaluatesThroughApply() throws Exception
    {
        FunctionProvider functions = FunctionProviderBuilder.builder().withDefaultFunctions().build();
        String start = "2002-03-22T08:23:47-05:00";
        String[][] cases = {
                {"PT0.0000000001S", start, start},
                {"-PT0.0000000001S", start, start},
                {"PT1.0000000019S", "2002-03-22T08:23:48.000000001-05:00", "2002-03-22T08:23:45.999999999-05:00"},
                {"-PT1.0000000019S", "2002-03-22T08:23:45.999999999-05:00", "2002-03-22T08:23:48.000000001-05:00"}
        };
        for (boolean validate : new boolean[]{false, true}) {
            EvaluationContext context = mock(EvaluationContext.class);
            when(context.isValidateFuncParamsAtRuntime()).thenReturn(validate);
            for (String[] values : cases) {
                for (String version : new String[]{"3.0", "1.0"}) {
                    Apply add = Apply.builder(functions.getFunction("urn:oasis:names:tc:xacml:"
                            + version + ":function:dateTime-add-dayTimeDuration").get())
                            .param(ISO8601DateTime.of(start), ISO8601DayTimeDuration.of(values[0])).build();
                    Apply subtract = Apply.builder(functions.getFunction("urn:oasis:names:tc:xacml:"
                            + version + ":function:dateTime-subtract-dayTimeDuration").get())
                            .param(ISO8601DateTime.of(start), ISO8601DayTimeDuration.of(values[0])).build();
                    ISO8601DateTime added = (ISO8601DateTime)add.evaluate(context);
                    ISO8601DateTime subtracted = (ISO8601DateTime)subtract.evaluate(context);
                    assertEquals(values[1], added.toXacmlString());
                    assertEquals(values[2], subtracted.toXacmlString());
                }
            }
            verify(context, times(16)).isValidateFuncParamsAtRuntime();
            verifyNoMoreInteractions(context);
        }
    }

    @Test
    public void fractionalLexicalRoundTripKeepsDecimalSeconds()
    {
        for (String seconds : new String[]{"1.5", "0.5", "0.001"}) {
            for (String sign : new String[]{"", "-"}) {
                ISO8601DayTimeDuration value = ISO8601DayTimeDuration.of(sign + "PT" + seconds + "S");
                // XML Schema duration seconds are decimal, including the lexical fractional part.
                assertEquals(0, new BigDecimal(seconds).compareTo(
                        (BigDecimal)value.duration().getField(DatatypeConstants.SECONDS)));
                assertEquals(value, ISO8601DayTimeDuration.of(value.toXacmlString()));
                assertEquals(0, value.compareTo(ISO8601DayTimeDuration.of(value.toXacmlString())));
                assertEquals(sign.equals("-"), value.isNegative());
            }
        }
    }

    @Test
    public void fractionalEqualityAndComparisonKeepSubsecondDifferences()
    {
        ISO8601DayTimeDuration half = ISO8601DayTimeDuration.of("PT0.5S");
        ISO8601DayTimeDuration equivalent = ISO8601DayTimeDuration.of("PT0.500S");
        assertEquals(half, equivalent);
        assertEquals(half.hashCode(), equivalent.hashCode());
        assertEquals(0, half.compareTo(equivalent));
        assertFalse(half.equals(ISO8601DayTimeDuration.of("PT0.001S")));
        assertTrue(half.compareTo(ISO8601DayTimeDuration.of("PT0.001S")) > 0);
        assertTrue(half.compareTo(ISO8601DayTimeDuration.of("PT1.5S")) < 0);
        assertTrue(ISO8601DayTimeDuration.of("-PT1.5S").compareTo(
                ISO8601DayTimeDuration.of("-PT0.5S")) < 0);
    }

    @Test
    public void javaDurationConversionKeepsFractionAndSign()
    {
        for (long milliseconds : new long[]{1500, 500, 1, -1500, -500, -1}) {
            String lexical = (milliseconds < 0 ? "-" : "") + "PT"
                    + BigDecimal.valueOf(Math.abs(milliseconds), 3).toPlainString() + "S";
            assertEquals(java.time.Duration.ofMillis(milliseconds),
                    ISO8601DayTimeDuration.of(lexical).toDuration());
        }
    }

    @Test
    public void fractionalXmlDurationReachesDateTimeArithmetic() throws Exception
    {
        ISO8601DayTimeDuration duration = ISO8601DayTimeDuration.of(
                DatatypeFactory.newInstance().newDuration("PT1.5S"));
        ISO8601DateTime start = ISO8601DateTime.of("2002-03-22T08:23:47-05:00");
        assertEquals("2002-03-22T08:23:48.5-05:00", start.plus(duration).toXacmlString());
        assertEquals("2002-03-22T08:23:45.5-05:00", start.minus(duration).toXacmlString());
    }

    @Test
    public void programmaticFourteenMonthsAreCountedOnce() throws Exception
    {
        // General XML Duration keeps raw P14M; normalized year/month duration represents P1Y2M.
        javax.xml.datatype.Duration raw = DatatypeFactory.newInstance().newDuration(true,
                BigInteger.ZERO, BigInteger.valueOf(14), null, null, null, null);
        assertEquals(14, raw.getMonths());
        ISO8601YearMonthDuration duration = ISO8601YearMonthDuration.of(raw);
        assertEquals(14L, duration.toPeriod().toTotalMonths());
        assertEquals(-14L, ISO8601YearMonthDuration.of(raw.negate()).toPeriod().toTotalMonths());
        ISO8601DateTime start = ISO8601DateTime.of("2002-03-22T08:23:47-05:00");
        assertEquals("2003-05-22T08:23:47-05:00", start.plus(duration).toXacmlString());
        assertEquals("2001-01-22T08:23:47-05:00", start.minus(duration).toXacmlString());
    }
}
