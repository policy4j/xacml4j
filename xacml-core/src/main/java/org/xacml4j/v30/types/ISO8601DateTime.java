package org.xacml4j.v30.types;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2019 Xacml4J.org
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

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import com.google.common.collect.Range;
import org.xacml4j.util.DateTimeDurationUtils;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.SyntaxException;

/**
* Implementation an XACML {@link Expression}
* for {@link XacmlTypes#DATETIME} type.
*/
public final class ISO8601DateTime
        extends Value.OtherValue<OffsetDateTime>
    implements Comparable<ISO8601DateTime>
{
    private static final long serialVersionUID = 1085808268199675887L;

    // Keep lexical timezone absence while retaining the existing UTC comparison default.
    private final boolean timeZoneAbsent;
    private transient int hashCode;

    private ISO8601DateTime(OffsetDateTime value) {
        this(value, false);
    }

    private ISO8601DateTime(OffsetDateTime value, boolean timeZoneAbsent) {
        super(XacmlTypes.DATETIME, value);
        this.timeZoneAbsent = timeZoneAbsent;
    }

    /**
     * Supported factory types
     *
     * {@link java.lang.String}
     * {@link String}
     * {@link OffsetDateTime}
     * {@link ZonedDateTime}
     * {@link GregorianCalendar}
     * {@link XMLGregorianCalendar}
     * {@link Instant}
     * {@link Date}
     *
     * @param v a value
     * @param p a value parameters
     * @return {@link ISO8601DateTime}
     * @exception SyntaxException if value can not be converted to
     * {@link XacmlTypes#DATETIME}
     *
     */
    static ISO8601DateTime ofAny(Object v, Object ...p) throws SyntaxException
    {
        if (v instanceof java.lang.String) {
            return of((java.lang.String)v);
        }
        if (v instanceof StringVal) {
            return of(((StringVal)v));
        }
        if (v instanceof ZonedDateTime) {
            return of((ZonedDateTime)v);
        }
        if (v instanceof OffsetDateTime) {
            return of((OffsetDateTime) v);
        }
        if (v instanceof GregorianCalendar) {
            return of((GregorianCalendar) v);
        }
        if (v instanceof Instant) {
            return of((Instant) v);
        }
        if (v instanceof Date) {
            return of(((Date)v).toInstant());
        }
        if (v instanceof XMLGregorianCalendar) {
            return of((XMLGregorianCalendar) v);
        }
        throw SyntaxException
                .invalidAttributeValue(v,
                                       XacmlTypes.DATETIME);
    }

    public static ISO8601DateTime of(Instant v){
        return new ISO8601DateTime(DateTimeDurationUtils.toISO860DateTime(v));
    }

    public static ISO8601DateTime of(String v){
        OffsetDateTime value = DateTimeDurationUtils.parseISO860DateTime(v);
        int timeStart = v.indexOf('T');
        boolean timeZoneAbsent = v.indexOf('Z', timeStart) < 0
                && v.indexOf('+', timeStart) < 0
                && v.indexOf('-', timeStart) < 0;
        return new ISO8601DateTime(value, timeZoneAbsent);
    }

    public static ISO8601DateTime of(StringVal v){
        return of(v.get());
    }

    public static ISO8601DateTime of(XMLGregorianCalendar v){
        return new ISO8601DateTime(DateTimeDurationUtils.toISO860DateTime(v),
                v.getTimezone() == DatatypeConstants.FIELD_UNDEFINED);
    }

    public static ISO8601DateTime of(GregorianCalendar v){
        return new ISO8601DateTime(DateTimeDurationUtils.toISO860DateTime(v));
    }

    public static ISO8601DateTime of(ZonedDateTime v){
        return new ISO8601DateTime(v.toOffsetDateTime());
    }

    public static ISO8601DateTime of(OffsetDateTime v){
        return new ISO8601DateTime(v);
    }

    public StringVal toStringExp(){
        return toStringExp(DateTimeFormatter.ISO_DATE_TIME);
    }
    public java.lang.String toXacmlString(){
        return timeZoneAbsent ? value.toLocalDateTime().format(DateTimeFormatter.ISO_DATE_TIME)
                : value.format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public StringVal toStringExp(DateTimeFormatter df){
        return StringVal.of(timeZoneAbsent ? value.toLocalDateTime().format(df) : value.format(df));
    }

    @Override
    protected java.lang.String formatVal(OffsetDateTime v){
        return toXacmlString();
    }

    public ZoneOffset getTimeZoneOffset(){
        return value.getOffset();
    }
    @Override
    public int compareTo(ISO8601DateTime o) {
        int comparison = Long.compare(value.toEpochSecond(), o.value.toEpochSecond());
        return comparison != 0 ? comparison : value.getNano() - o.value.getNano();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ISO8601DateTime
                && value.toEpochSecond() == ((ISO8601DateTime)other).value.toEpochSecond()
                && value.getNano() == ((ISO8601DateTime)other).value.getNano();
    }

    @Override
    public int hashCode() {
        int hash = hashCode;
        if (hash == 0) {
            int instantHash = Long.hashCode(value.toEpochSecond()) + 51 * value.getNano();
            hash = 31 * (31 + instantHash) + getEvaluatesTo().hashCode();
            hashCode = hash;
        }
        return hash;
    }

    public ISO8601DateTime plus(Duration v){
        return new ISO8601DateTime(get().plus(v), timeZoneAbsent);
    }
    public ISO8601DateTime plus(ISO8601YearMonthDuration v){
        return new ISO8601DateTime(value.plus(v.toPeriod()), timeZoneAbsent);
    }

    public ISO8601DateTime plus(ISO8601DayTimeDuration v){
        return new ISO8601DateTime(value.plus(v.toDuration()), timeZoneAbsent);
    }
    public ISO8601DateTime minus(ISO8601YearMonthDuration v){
        return new ISO8601DateTime(value.minus(v.toPeriod()), timeZoneAbsent);
    }
    public ISO8601DateTime minus(ISO8601DayTimeDuration v){
        return new ISO8601DateTime(value.minus(v.toDuration()), timeZoneAbsent);
    }

    public ISO8601DateTime minus(Duration v){
        return new ISO8601DateTime(get().minus(v), timeZoneAbsent);
    }

    public boolean isInRange(Range<ISO8601DateTime> range){
        return range.contains(this);
    }

}
