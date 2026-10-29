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

import javax.xml.datatype.Duration;
import java.time.Period;

/**
 * Implementation an XACML {@link Expression}
 * for {@link XacmlTypes#YEARMONTHDURATION} type.
 */
public final class ISO8601YearMonthDuration
        extends ISO8601Duration<ISO8601YearMonthDuration>
{
    private static final long serialVersionUID = 6510264772808336009L;
    private transient int hashCode;
    private ISO8601YearMonthDuration(Duration value) {
       super(XacmlTypes.YEARMONTHDURATION, value);
    }

    private ISO8601YearMonthDuration(Period period) {
        super(XacmlTypes.YEARMONTHDURATION,
                DateTimeDurationUtils.toXmlYearsMonthsDuration(period));
    }

    public static ISO8601YearMonthDuration ofAny(Object v, Object ...params){
        if(v instanceof String){
            return of((String)v);
        }
        if(v instanceof StringVal){
            return of((StringVal)v);
        }
        if(v instanceof Duration){
            return of((Duration) v);
        }
        if(v instanceof Period){
            return of((Period) v);
        }
        throw SyntaxException
                .invalidAttributeValue(v, XacmlTypes.YEARMONTHDURATION);
    }

    public static ISO8601YearMonthDuration of(String v){
        return new ISO8601YearMonthDuration(DateTimeDurationUtils.parseISO8601YearMonthDuration(v));
    }

    public static ISO8601YearMonthDuration of(Duration v){
        return new ISO8601YearMonthDuration(v);
    }

    public static ISO8601YearMonthDuration of(StringVal v){
        return new ISO8601YearMonthDuration(DateTimeDurationUtils.parseISO8601YearMonthDuration(v.get()));
    }
    public static ISO8601YearMonthDuration of(Period v){
        return new ISO8601YearMonthDuration(v);
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
    protected ISO8601YearMonthDuration build(Duration d) {
        return new ISO8601YearMonthDuration(d);
    }
}
