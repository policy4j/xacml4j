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

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import java.time.Duration;
import java.time.OffsetDateTime;

/**
* Implementation an XACML {@link Expression}
* for {@link XacmlTypes#DAYTIMEDURATION} type.
*/
public final class ISO8601DayTimeDuration extends ISO8601Duration<ISO8601DayTimeDuration>
{
    private static final long serialVersionUID = -3264977978603429807L;
    private transient int hashCode;

    private ISO8601DayTimeDuration(javax.xml.datatype.Duration d) {
        super(XacmlTypes.DAYTIMEDURATION, d);
    }
    private ISO8601DayTimeDuration(Duration d) {
        super(XacmlTypes.DAYTIMEDURATION, DateTimeDurationUtils.toXmlDayTimeDuration(d));
    }

    public static ISO8601DayTimeDuration ofAny(Object v, Object ...params){
        if(v instanceof String){
            return of((String)v);
        }
        if(v instanceof StringVal){
            return of((StringVal) v);
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



    public static ISO8601DayTimeDuration of(javax.xml.datatype.Duration duration){
        return new ISO8601DayTimeDuration(duration);
    }

    public static ISO8601DayTimeDuration of(boolean positive, int days, int hours, int mins, int seconds){
        return new ISO8601DayTimeDuration(
                DateTimeDurationUtils.of(positive, days, hours, mins, seconds));
    }

    public static ISO8601DayTimeDuration of(boolean positive, int hours, int mins, int seconds){
        return new ISO8601DayTimeDuration(
                DateTimeDurationUtils.of(positive, DatatypeConstants.FIELD_UNDEFINED, hours, mins, seconds));
    }

    @Override
    public int hashCode() {
        int hash = hashCode;
        if (hash == 0) {
            hash = super.hashCode();
            hashCode = hash;
        }
        return hash;
    }

    @Override
    protected ISO8601DayTimeDuration build(javax.xml.datatype.Duration d) {
        return new ISO8601DayTimeDuration(d);
    }
}
