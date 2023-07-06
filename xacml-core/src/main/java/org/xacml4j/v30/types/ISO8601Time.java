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

import java.sql.Time;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.GregorianCalendar;

import javax.xml.datatype.XMLGregorianCalendar;

import org.xacml4j.util.DateTimeDurationUtils;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.SyntaxException;

/**
 * Implementation an XACML {@link Expression}
 * for {@link XacmlTypes#TIME} type.
 */
public final class ISO8601Time
        extends Value.OtherValue<OffsetTime>
        implements Comparable<ISO8601Time>
{
    private static final long serialVersionUID = -8244143626423796791L;

    private ISO8601Time(OffsetTime time) {
        super(XacmlTypes.TIME, time);
    }

    public static ISO8601Time ofAny(Object v, Object ...p)
    {
        if (v instanceof String) {
            return ISO8601Time.of((String) v);
        }
        if (v instanceof StringVal) {
            return ISO8601Time.of(((StringVal) v).get());
        }
        if (v instanceof XMLGregorianCalendar) {
            return ISO8601Time.of((XMLGregorianCalendar) v);
        }
        if (v instanceof GregorianCalendar) {
            return ISO8601Time.of((GregorianCalendar) v);
        }
        if (v instanceof Instant) {
            return ISO8601Time.of((Instant) v);
        }
        if (v instanceof LocalTime) {
            return ISO8601Time.of((LocalTime) v);
        }
        if (v instanceof Time) {
            return ISO8601Time.of(((Time)v).toLocalTime());
        }
        if (v instanceof ZonedDateTime) {
            return ISO8601Time.of(((ZonedDateTime)v).toOffsetDateTime().toOffsetTime());
        }
        throw SyntaxException
                .invalidAttributeValue(v, XacmlTypes.TIME);
    }



    public static ISO8601Time of(String v){
        return new ISO8601Time(DateTimeDurationUtils.parseISO860Time(v));
    }

    public static ISO8601Time of(OffsetTime v){
        return new ISO8601Time(v);
    }

    public static ISO8601Time of(XMLGregorianCalendar v){
        return new ISO8601Time(DateTimeDurationUtils.toISO860Time(v));
    }

    public static ISO8601Time of(GregorianCalendar v){
        return new ISO8601Time(DateTimeDurationUtils.toISO860Time(v));

    }

    public static ISO8601Time of(LocalTime v){
        return new ISO8601Time(v.atOffset(ZoneOffset.UTC));
    }
    public static ISO8601Time of(Instant v){
        return new ISO8601Time(
                v.atOffset(ZoneOffset.UTC)
                .toOffsetTime());
    }

    public StringVal toStringExp(){
        return StringVal.of(toXacmlString());
    }

    public String toXacmlString(){
        return value.format(DateTimeFormatter.ISO_TIME);
    }

    public StringVal toStringExp(DateTimeFormatter df){
        return StringVal.of(get()
                .format(df));
    }

    public int getHour(){
        return get().getHour();
    }

    public int getMinute(){
        return get().getMinute();
    }

    public int getSecond(){
        return get().getSecond();
    }

    public ZoneOffset getTimeZone(){
        return get().getOffset();
    }

    @Override
    public int compareTo(ISO8601Time v) {
        return get().compareTo(v.get());
    }
}
