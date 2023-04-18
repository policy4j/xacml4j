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

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import org.xacml4j.v30.Expression;
import org.xacml4j.v30.YearMonthDuration;

import com.google.common.base.Preconditions;

/**
 * Implementation an XACML {@link Expression}
 * for {@link XacmlTypes#YEARMONTHDURATION} type.
 */
public final class YearMonthDurationValue
        extends SingleValue<YearMonthDuration>
    implements Comparable<YearMonthDurationValue>
{
    private static final long serialVersionUID = 6510264772808336009L;

    YearMonthDurationValue(Duration value) {
        this(new YearMonthDuration(value));
        Preconditions.checkArgument(
                value.getXMLSchemaType() == DatatypeConstants.DURATION_YEARMONTH);
    }

    YearMonthDurationValue(
            YearMonthDuration value) {
        super(XacmlTypes.YEARMONTHDURATION, value);
    }

    public static YearMonthDurationValue of(String v){
        return new YearMonthDurationValue(YearMonthDuration.parse(v));
    }

    public static YearMonthDurationValue of(Duration v){
        return new YearMonthDurationValue(YearMonthDuration.parse(v));
    }

    public static YearMonthDurationValue of(YearMonthDuration v){
        return new YearMonthDurationValue(v);
    }

    public static YearMonthDurationValue of(StringValue v){
        return of(v.get());
    }

    public StringValue toStringExp(){
        return StringValue.of(get().toXacmlString());
    }

    @Override
    public int compareTo(YearMonthDurationValue o) {
        return get().compareTo(o.get());
    }

    public YearMonthDurationValue add(YearMonthDurationValue d){
        return new YearMonthDurationValue(get().add(d.get()));
    }

    public YearMonthDurationValue subtract(YearMonthDurationValue d){
        return new YearMonthDurationValue(get().subtract(d.get()));
    }
}
