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

import java.util.Calendar;

import javax.xml.datatype.XMLGregorianCalendar;

import org.xacml4j.v30.Expression;
import org.xacml4j.v30.Time;

/**
 * Implementation an XACML {@link Expression}
 * for {@link XacmlTypes#TIME} type.
 */
public final class TimeValue
        extends SingleValue<Time>
        implements Comparable<TimeValue>
{
    private static final long serialVersionUID = -8244143626423796791L;

    private TimeValue(Time time) {
        super(XacmlTypes.TIME, time);
    }

    static TimeValue of(String v){
        return new TimeValue(Time.valueOf(v));
    }

    static TimeValue of(XMLGregorianCalendar v){
        return new TimeValue(Time.valueOf(v));
    }

    static TimeValue of(Calendar v){
        return new TimeValue(Time.valueOf(v));
    }

    static TimeValue of(Time v){
        return new TimeValue(v);
    }

    public StringValue toStringExp(){
        return StringValue.of(get().toXacmlString());
    }


    @Override
    public int compareTo(TimeValue v) {
        return get().compareTo(v.get());
    }
}
