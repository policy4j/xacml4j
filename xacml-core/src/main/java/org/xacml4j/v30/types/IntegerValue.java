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

import org.xacml4j.v30.Expression;

import com.google.common.base.Preconditions;

/**
* Implementation an XACML {@link Expression}
* for {@link XacmlTypes#INTEGER} type.
*/
public final class IntegerValue
        extends SingleValue<Long>
{
    private static final long serialVersionUID = 6654857010399020496L;

    private IntegerValue(Long value) {
        super(XacmlTypes.INTEGER, value);
    }

    static IntegerValue of(Number value){
        return new IntegerValue(value.longValue());
    }

    static IntegerValue of(String v){
        Preconditions.checkNotNull(v);
        if ((v.length() >= 1) &&
                (v.charAt(0) == '+')){
            v = v.substring(1);
        }
        return new IntegerValue(Long.parseLong(v));
    }

    public StringValue toStringValue(){
        return StringValue.of(value().toString());
    }
    public DoubleValue toDoubleValue(){
        return DoubleValue.of(value());
    }

    public IntegerValue add(IntegerValue d){
        return  new IntegerValue(value() + d.value());
    }

    public IntegerValue subtract(IntegerValue d){
        return  new IntegerValue(value() - d.value());
    }

    public IntegerValue multiply(IntegerValue d){
        return  new IntegerValue(value() * d.value());
    }

    public DoubleValue divide(IntegerValue d){
        Preconditions.checkArgument(d.value() != null);
        return DoubleValue.of(value() / d.value());
    }

    public IntegerValue mod(IntegerValue d){
        Preconditions.checkArgument(d.value() != null);
        return IntegerValue.of(value() % d.value());
    }
}
