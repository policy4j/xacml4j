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
import org.xacml4j.v30.SyntaxException;

import java.math.BigInteger;

/**
* Implementation an XACML {@link Expression}
* for {@link XacmlTypes#INTEGER} type.
*/
public final class IntegerVal
        extends Value.OtherValue<Long>
{
    private static final long serialVersionUID = 6654857010399020496L;

    private IntegerVal(Long value) {
        super(XacmlTypes.INTEGER, value);
    }


    public static IntegerVal ofAny(Object v, Object... params) {
        if (v instanceof java.lang.String) {
            return IntegerVal.of((java.lang.String) v);
        }
        if (v instanceof StringVal) {
            return IntegerVal.of(((StringVal) v).get());
        }
        if (v instanceof DoubleVal) {
            return IntegerVal.of(((DoubleVal) v).get().intValue());
        }
        if(v instanceof Number){
            return IntegerVal.of((Number) v);
        }
        throw SyntaxException
                .invalidAttributeValue(v, XacmlTypes.INTEGER);
    }

    public static IntegerVal of(Number value){
        return new IntegerVal(value.longValue());
    }
    public static IntegerVal of(BigInteger value){
        return new IntegerVal(value.longValue());
    }

    static IntegerVal of(java.lang.String v){
        Preconditions.checkNotNull(v);
        if ((v.length() >= 1) &&
                (v.charAt(0) == '+')){
            v = v.substring(1);
        }
        return new IntegerVal(Long.parseLong(v));
    }

    public Integer toInt(){
        return value.intValue();
    }

    public Double toDouble(){
        return value.doubleValue();
    }

    public StringVal toStringValue(){
        return StringVal.of(toXacmlString());
    }
    public String toXacmlString(){
        return get().toString();
    }
    public DoubleVal toDoubleValue(){
        return DoubleVal.of(get());
    }

    public IntegerVal add(IntegerVal d){
        return  new IntegerVal(get() + d.get());
    }

    public IntegerVal subtract(IntegerVal d){
        return  new IntegerVal(get() - d.get());
    }

    public IntegerVal multiply(IntegerVal d){
        return  new IntegerVal(get() * d.get());
    }

    public DoubleVal divide(IntegerVal d){
        Preconditions.checkArgument(d.get() != null);
        return DoubleVal.of(get() / d.get());
    }

    public IntegerVal mod(IntegerVal d){
        Preconditions.checkArgument(d.get() != null);
        return IntegerVal.of(get() % d.get());
    }

    public IntegerVal abs(){
        return IntegerVal.of(Math.abs(get()));
    }
}
