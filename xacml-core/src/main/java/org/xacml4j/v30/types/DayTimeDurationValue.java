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

import com.google.common.base.Preconditions;

/**
* Implementation an XACML {@link Expression}
* for {@link XacmlTypes#DAYTIMEDURATION} type.
*/
public final class DayTimeDurationValue extends BaseValue<DayTimeDuration>
    implements Comparable<DayTimeDurationValue>
{
    private static final long serialVersionUID = -3264977978603429807L;

    public DayTimeDurationValue(Duration value) {
        super(XacmlTypes.DAYTIMEDURATION, new DayTimeDuration(value));
        Preconditions.checkArgument(!value.isSet(DatatypeConstants.YEARS) &&
                !value.isSet(DatatypeConstants.MONTHS));
    }



    DayTimeDurationValue(DayTimeDuration value) {
        super(XacmlTypes.DAYTIMEDURATION, value);
    }

    static DayTimeDurationValue of(DayTimeDuration v){
        return new DayTimeDurationValue(v);
    }

    static DayTimeDurationValue of(Duration v){
        return new DayTimeDurationValue(v);
    }

    static DayTimeDurationValue of(String v){
        return new DayTimeDurationValue(DayTimeDuration.parse(v));
    }

    static DayTimeDurationValue of(StringValue v){
        return of(v.value());
    }

    public StringValue toStringExp(){
        return StringValue.of(value().toXacmlString());
    }

    @Override
    public int compareTo(DayTimeDurationValue o) {
        return value().compareTo(o.value());
    }
}
