package org.xacml4j.util;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.types.ISO8601DayTimeDuration;
import org.xacml4j.v30.types.XacmlTypes;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Optional;

public class DateTimeDurationUtils {
    private final static Logger LOG = LoggerFactory.getLogger(DateTimeDurationUtils.class);

    static DatatypeFactory DF_FACTORY = null;

    static {
        try {
            DF_FACTORY = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public static Duration of(boolean positive, int days, int hours, int mins, int seconds){
        assert DF_FACTORY != null;
        return DF_FACTORY.newDurationDayTime(positive, days, hours, mins, seconds);
    }

    public static Duration of(boolean positive, int hours, int mins, int seconds){
        assert DF_FACTORY != null;
        return DF_FACTORY.newDurationDayTime(positive, DatatypeConstants.FIELD_UNDEFINED, hours, mins, seconds);
    }

    public static Duration of(boolean positive, int mins, int seconds){
        assert DF_FACTORY != null;
        return DF_FACTORY.newDurationDayTime(positive, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, mins, seconds);
    }

    public static boolean isZero(Duration d){
        assert DF_FACTORY != null;
        BigDecimal seconds = (BigDecimal)d.getField(DatatypeConstants.SECONDS);
        return isUndefinedOrZero(d.getYears()) &&
                isUndefinedOrZero(d.getMonths()) &&
                isUndefinedOrZero(d.getDays()) &&
                isUndefinedOrZero(d.getHours()) &&
                isUndefinedOrZero(d.getMinutes()) &&
                (seconds == null || seconds.signum() == 0);

    }

    private static  boolean isUndefinedOrZero(int v){
        return v == 0 || v == DatatypeConstants.FIELD_UNDEFINED;
    }

    public static OffsetDateTime toISO860DateTime(XMLGregorianCalendar c) {
        if (c == null) {
            throw SyntaxException
                    .invalidAttributeValue(null, XacmlTypes.DATETIME);
        }
        if (c.getXMLSchemaType() != DatatypeConstants.DATETIME) {
            throw SyntaxException
                    .invalidAttributeValue(c, XacmlTypes.DATETIME);
        }
        ZoneOffset offset = c.getTimezone() != DatatypeConstants.FIELD_UNDEFINED ?
                ZoneOffset.ofTotalSeconds(c.getTimezone() * 60) : ZoneOffset.UTC;
        BigDecimal fractionalSecond = c.getFractionalSecond();
        int nanos = fractionalSecond != null
                ? fractionalSecond.multiply(BigDecimal.valueOf(1_000_000_000)).intValueExact()
                : 0;
        return OffsetDateTime.of(c.getYear(), c.getMonth(), c.getDay(),
                c.getHour(), c.getMinute(), c.getSecond(), nanos, offset);
    }

    public static LocalDate toISO860Date(XMLGregorianCalendar v) {
        if (v == null) {
            throw SyntaxException
                    .invalidAttributeValue(null, XacmlTypes.DATE);
        }
        if (!(v.getXMLSchemaType() == DatatypeConstants.DATE ||
                v.getXMLSchemaType() == DatatypeConstants.DATETIME)) {
            throw SyntaxException
                    .invalidAttributeValue(v, XacmlTypes.DATE);
        }
        return LocalDate.of(v.getYear(),
                v.getMonth(),
                v.getDay());
    }

    public static OffsetTime toISO860Time(XMLGregorianCalendar c) {
        if (c == null) {
            throw SyntaxException
                    .invalidAttributeValue(null, XacmlTypes.TIME);
        }
        if (!(c.getXMLSchemaType().equals(DatatypeConstants.DATETIME) ||
                c.getXMLSchemaType().equals(DatatypeConstants.TIME))) {
            throw SyntaxException
                    .invalidAttributeValue(c, XacmlTypes.TIME);
        }
        ZoneOffset offset = c.getTimezone() != DatatypeConstants.FIELD_UNDEFINED ?
                ZoneOffset.ofTotalSeconds(c.getTimezone() * 60) : ZoneOffset.UTC;
        BigDecimal fractionalSecond = c.getFractionalSecond();
        int nanos = fractionalSecond != null
                ? fractionalSecond.multiply(BigDecimal.valueOf(1_000_000_000)).intValueExact()
                : 0;
        return OffsetTime.of(c.getHour(), c.getMinute(), c.getSecond(), nanos, offset);
    }


    public static LocalDate toISO860Date(GregorianCalendar v) {
        if (v == null) {
            throw SyntaxException
                    .invalidAttributeValue(null, XacmlTypes.DATE);
        }
        return LocalDate.of(v.get(Calendar.YEAR),
                v.get(Calendar.MONTH) + 1,
                v.get(Calendar.DAY_OF_MONTH));
    }

    public static OffsetDateTime toISO860DateTime(Instant v) {
        if (v == null) {
            throw SyntaxException
                    .invalidAttributeValue(null, XacmlTypes.DATETIME);
        }
        return v.atOffset(ZoneOffset.UTC);
    }

    public static OffsetTime toISO860Time(Instant v) {
        if (v == null) {
            throw SyntaxException
                    .invalidAttributeValue(null, XacmlTypes.TIME);
        }
        return v.atOffset(ZoneOffset.UTC).toOffsetTime();
    }

    public static OffsetTime toISO860Time(GregorianCalendar calendar) {
        if (calendar == null) {
            throw SyntaxException
                    .invalidAttributeValue(null, XacmlTypes.TIME);
        }
        return calendar.toZonedDateTime()
                .toOffsetDateTime()
                .toOffsetTime();
    }

    public static OffsetDateTime toISO860DateTime(GregorianCalendar calendar) {
        if (calendar == null) {
            throw SyntaxException
                    .invalidAttributeValue(null, XacmlTypes.DATETIME);
        }
        return calendar.toZonedDateTime()
                .toOffsetDateTime();
    }


    public static OffsetTime parseISO860Time(String v) throws SyntaxException {
        if (StringUtils.isNullOrEmpty(v)) {
            throw SyntaxException
                    .invalidAttributeValue(v, XacmlTypes.TIME);
        }
        XMLGregorianCalendar c = DF_FACTORY.newXMLGregorianCalendar(v);
        if (c.getXMLSchemaType() != DatatypeConstants.TIME) {
            throw SyntaxException
                    .invalidAttributeValue(v, XacmlTypes.DATE);
        }
        return toISO860Time(c);
    }

    public static OffsetDateTime parseISO860DateTime(String v) throws SyntaxException {
        if (StringUtils.isNullOrEmpty(v)) {
            throw SyntaxException
                    .invalidAttributeValue(v, XacmlTypes.DATETIME);
        }
        return toISO860DateTime(DF_FACTORY.newXMLGregorianCalendar(v));
    }

    public static java.time.Duration toISO8601DayTimeDuration(Duration v) {
        Duration d = v;
        java.time.Duration duration = null;
        if (d.getDays() != DatatypeConstants.FIELD_UNDEFINED) {
            duration = java.time.Duration.ofDays(d.getDays());
        }
        if (d.getHours() != DatatypeConstants.FIELD_UNDEFINED) {
            duration = duration != null ? duration.plusHours(d.getHours()) :
                    java.time.Duration.ofHours(d.getHours());
        }
        if (d.getMinutes() != DatatypeConstants.FIELD_UNDEFINED) {
            duration = duration != null ? duration.plusMinutes(d.getMinutes()) :
                    java.time.Duration.ofMinutes(d.getMinutes());
        }
        BigDecimal seconds = (BigDecimal)d.getField(DatatypeConstants.SECONDS);
        if (seconds != null) {
            long wholeSeconds = seconds.toBigInteger().longValueExact();
            int nanos = seconds.remainder(BigDecimal.ONE).movePointRight(9)
                    .setScale(0, RoundingMode.DOWN).intValueExact();
            duration = duration != null ? duration.plusSeconds(wholeSeconds).plusNanos(nanos) :
                    java.time.Duration.ofSeconds(wholeSeconds, nanos);
        }
        if (duration == null) {
            throw SyntaxException
                    .invalidAttributeValue(v, XacmlTypes.DAYTIMEDURATION);
        }
        return d.getSign() > 0 ? duration : duration.negated();
    }

    public static Period toISO8601YearsMonthsPeriod(Duration v) {
        Duration d = DF_FACTORY.newDurationYearMonth(v.getSign() >= 0,
                v.getYears(), v.getMonths());
        Period period = Period.ZERO;
        if (d.getYears() != DatatypeConstants.FIELD_UNDEFINED) {
            period = Period.ofYears(d.getYears());
        }
        if (d.getMonths() != DatatypeConstants.FIELD_UNDEFINED) {
            period = period != null ? period.plusMonths(d.getMonths()) : period;
        }
        return d.getSign() > 0 ? period : period.negated();
    }

    public static Duration parseISO8601DayTimeDuration(String v) {
        assert DF_FACTORY != null;
        Duration duration = null;
        try{
            duration = DF_FACTORY.newDuration(v);
        }catch (IllegalArgumentException e){
            LOG.debug(e.getMessage(), e);
            duration = parseAsJDKDuration(v);
        }
        BigDecimal seconds = (BigDecimal)duration.getField(DatatypeConstants.SECONDS);
        if (seconds != null && seconds.remainder(BigDecimal.ONE).signum() == 0) {
            seconds = new BigDecimal(seconds.toBigInteger());
        }
        Duration dayTime = DF_FACTORY.newDuration(duration.getSign() >= 0, null, null,
                BigInteger.valueOf(duration.getDays()), BigInteger.valueOf(duration.getHours()),
                BigInteger.valueOf(duration.getMinutes()), seconds != null ? seconds : BigDecimal.ZERO);
        return DF_FACTORY.newDurationDayTime(dayTime.toString());
    }

    private static Duration parseAsJDKDuration(String v){
        try {
            return toXmlDayTimeDuration(java.time.Duration.parse(v));
        }catch (DateTimeParseException e){
            LOG.debug(e.getMessage(), e);
            throw new SyntaxException(v, XacmlTypes.DAYTIMEDURATION);
        }
    }

    private static Duration parseAsJDKPeriod(String v){
        try {
            return toXmlYearsMonthsDuration(java.time.Period.parse(v));
        }catch (DateTimeParseException e){
            throw new SyntaxException(v, XacmlTypes.YEARMONTHDURATION);
        }
    }

    public static Duration parseISO8601YearMonthDuration(String v) {
        assert DF_FACTORY != null;
        if (!v.matches("[+-]?P(?:\\d+Y)?(?:\\d+M)?")
                || !v.matches(".*\\d[YM]$")) {
            throw new SyntaxException(v, XacmlTypes.YEARMONTHDURATION);
        }
        Duration duration;
        try{
            duration = DF_FACTORY.newDuration(v);
        }catch (IllegalArgumentException e){
            duration = parseAsJDKPeriod(v);
        }
        return DF_FACTORY.newDurationYearMonth(duration.getSign() >= 0,
                duration.getYears(), duration.getMonths());
    }

    public static Duration toXmlDayTimeDuration(java.time.Duration d) {
        java.time.Duration magnitude = d.abs();
        BigDecimal seconds = BigDecimal.valueOf(magnitude.toSecondsPart())
                .add(BigDecimal.valueOf(magnitude.getNano(), 9));
        if (magnitude.getNano() == 0) {
            seconds = new BigDecimal(seconds.toBigInteger());
        } else {
            int fractionalNanos = magnitude.getNano();
            int scale = 9;
            while (fractionalNanos % 10 == 0) {
                fractionalNanos /= 10;
                scale--;
            }
            seconds = BigDecimal.valueOf(magnitude.toSecondsPart())
                    .add(new BigDecimal(BigInteger.valueOf(fractionalNanos), scale));
        }
        return DF_FACTORY.newDuration(!d.isNegative(), null, null,
                BigInteger.valueOf(magnitude.toDaysPart()),
                BigInteger.valueOf(magnitude.toHoursPart()),
                BigInteger.valueOf(magnitude.toMinutesPart()), seconds);
    }

    public static Duration toXmlYearsMonthsDuration(Period p) {
        return DF_FACTORY.newDurationYearMonth(!p.isNegative(), p.getYears(), p.getMonths());
    }

}
