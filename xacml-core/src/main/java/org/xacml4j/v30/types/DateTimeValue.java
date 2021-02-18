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

import org.xacml4j.v30.DateTime;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.SyntaxException;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;

/**
* Implementation an XACML {@link Expression}
* for {@link XacmlTypes#DATETIME} type.
*/
public final class DateTimeValue
    extends BaseAttributeValue<DateTime>
    implements Comparable<DateTimeValue>
{
    private static final long serialVersionUID = 1085808268199675887L;

    private DateTimeValue(DateTime value) {
        super(XacmlTypes.DATETIME, value);
    }

    static DateTimeValue fromObjectWithParams(
            Object v, Object...params)
    {
        if (v instanceof String) {
            return new DateTimeValue(DateTime.of(v));
        }
        if (v instanceof StringValue) {
            return new DateTimeValue(DateTime.of(((StringValue) v).value()));
        }
        if (v instanceof Calendar ||
                v instanceof XMLGregorianCalendar) {
            return new DateTimeValue(DateTime.of(v));
        }
        throw SyntaxException
                .invalidAttributeValue(v,
                        XacmlTypes.ANYURI);
    }

    static DateTimeValue of(String v){
        return new DateTimeValue(DateTime.of(v));
    }

    static DateTimeValue of(DateTime v){
        return new DateTimeValue(v);
    }

    static DateTimeValue of(XMLGregorianCalendar v){
        return new DateTimeValue(DateTime.of(v));
    }

    static DateTimeValue of(Calendar v){
        return new DateTimeValue(DateTime.of(v));
    }

    static DateTimeValue of(StringValue v){
        return of(v.value());
    }

    public StringValue toStringExp(){
        return StringValue.of(value().toXacmlString());
    }

    @Override
    public int compareTo(DateTimeValue o) {
        return value().compareTo(o.value());
    }

    public DateTimeValue add(DayTimeDurationValue v){
        return new DateTimeValue(
                value().add(v.value()));
    }

    public DateTimeValue subtract(DayTimeDurationValue v){
        return new DateTimeValue(value().subtract(v.value()));
    }

    public DateTimeValue subtract(YearMonthDurationValue v){
        return new DateTimeValue(value().subtract(v.value()));
    }

    public DateTimeValue add(YearMonthDurationValue v){
        return new DateTimeValue(value().add(v.value()));
    }

}
