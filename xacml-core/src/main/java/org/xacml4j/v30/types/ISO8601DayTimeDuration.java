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

import org.xacml4j.util.DateTimeDurationUtils;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.SyntaxException;

import java.time.Duration;

/**
* Implementation an XACML {@link Expression}
* for {@link XacmlTypes#DAYTIMEDURATION} type.
*/
public final class ISO8601DayTimeDuration extends Value.OtherValue<Duration>
        implements Comparable<ISO8601DayTimeDuration>
{
    private static final long serialVersionUID = -3264977978603429807L;

    private ISO8601DayTimeDuration(java.time.Duration value) {
        super(XacmlTypes.DAYTIMEDURATION, value);
    }

    private transient String formatted;

    public static ISO8601DayTimeDuration ofAny(Object v, Object ...params){
        if(v instanceof String){
            return of((String)v);
        }
        if(v instanceof Duration){
            return of((Duration) v);
        }
        if(v instanceof javax.xml.datatype.Duration){
            return of((javax.xml.datatype.Duration) v);
        }
        throw SyntaxException
                .invalidAttributeValue(v, XacmlTypes.DAYTIMEDURATION);
    }
    public static ISO8601DayTimeDuration of(String v){
        return new ISO8601DayTimeDuration(DateTimeDurationUtils.parseISO8601DayTimeDuration(v));
    }

    public static ISO8601DayTimeDuration of(StringVal v){
        return new ISO8601DayTimeDuration(DateTimeDurationUtils.parseISO8601DayTimeDuration(v.get()));
    }

    public static ISO8601DayTimeDuration of(Duration v){
        return new ISO8601DayTimeDuration(v);
    }

    public static ISO8601DayTimeDuration of(long days){
        return new ISO8601DayTimeDuration(Duration
                .ofDays(days));
    }

    public static ISO8601DayTimeDuration of(long days, int hours){
        return new ISO8601DayTimeDuration(Duration
                .ofDays(days)
                .plusHours(hours));
    }

    public static ISO8601DayTimeDuration of(long days, int hours, int mins){
        return new ISO8601DayTimeDuration(Duration
                .ofDays(days)
                .plusHours(hours)
                .plusMinutes(mins));
    }

    public static ISO8601DayTimeDuration of(javax.xml.datatype.Duration duration){
        return ISO8601DayTimeDuration.of(
                DateTimeDurationUtils.toISO8601DayTimeDuration(duration));
    }

    public static ISO8601DayTimeDuration of(long days, int hours, int mins, int seconds){
        return new ISO8601DayTimeDuration(Duration.ofDays(days)
                .plusHours(hours)
                .plusMinutes(mins)
                .plusSeconds(seconds));
    }

    public static ISO8601DayTimeDuration of(int hours, int mins, int seconds){
        return new ISO8601DayTimeDuration(Duration.ofHours(hours)
                .plusMinutes(mins)
                .plusSeconds(seconds));
    }


    public long getDays(){
        return get().toDaysPart();
    }

    public int getHours(){
        return get().toHoursPart();
    }

    public int getMinutes(){
        return get().toMinutesPart();
    }

    public int getSeconds(){
        return get().toSecondsPart();
    }

    public boolean isPositive(){
        return !value.isNegative();
    }

    public boolean isZero(){
        return value.isZero();
    }

    public StringVal toStringVal(){
        return StringVal.of(toXacmlString());
    }

    public String toXacmlString(){
        if(formatted == null){
            formatted = DateTimeDurationUtils.toXmlDayTimeDuration(value).toString();
        }
        return formatted;
    }

    public ISO8601DayTimeDuration plus(ISO8601DayTimeDuration duration){
        return ISO8601DayTimeDuration.of(value.plus(duration.value));
    }

    public ISO8601DayTimeDuration minus(ISO8601DayTimeDuration duration){
        return ISO8601DayTimeDuration.of(value.minus(duration.value));
    }
    public ISO8601DayTimeDuration negated(){
        return ISO8601DayTimeDuration.of(value.negated());
    }

    @Override
    public int compareTo(ISO8601DayTimeDuration o) {
        return value.compareTo(o.value);
    }
}
