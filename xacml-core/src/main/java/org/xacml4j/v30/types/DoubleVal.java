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
import com.google.common.base.Strings;
import org.xacml4j.v30.SyntaxException;

/**
 * Implementation an XACML {@link Expression}
 * for {@link XacmlTypes#DOUBLE} type.
 */
public final class DoubleVal extends Value.OtherValue<Double>
{
    private static final long serialVersionUID = -3689668541615314228L;

    private DoubleVal(java.lang.Double value) {
        super(XacmlTypes.DOUBLE, value);
    }

    public static DoubleVal ofAny(Object v, Object... params) {
        if (v instanceof java.lang.String) {
            return of((java.lang.String) v);
        }
        if (v instanceof StringVal) {
            return of(((StringVal) v).get());
        }
        if(v instanceof Number) {
            return of((Number) v);
        }
        throw SyntaxException
                .invalidAttributeValue(v, XacmlTypes.DOUBLE);
    }

    public static DoubleVal of(Number value){
        Preconditions.checkNotNull(value);
        return new DoubleVal(value.doubleValue());
    }

    public static DoubleVal of(java.lang.String v){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(v));
         if (v.endsWith("INF")) {
                int infIndex = v.lastIndexOf("INF");
                v = v.substring(0, infIndex) + "Infinity";
         }
         return new DoubleVal(java.lang.Double.parseDouble(v));
    }

    public StringVal toStringExp(){
        return StringVal.of(get().toString());
    }
    public String toXacmlString(){
        return get().toString();
    }
    public IntegerVal toIntegerValue(){
        return IntegerVal.of(get().intValue());
    }

    public DoubleVal add(DoubleVal d){
        return  new DoubleVal(get() + d.get());
    }

    public DoubleVal subtract(DoubleVal d){
        return  new DoubleVal(get() - d.get());
    }

    public DoubleVal multiply(DoubleVal d){
        return  new DoubleVal(get() * d.get());
    }

    public DoubleVal divide(DoubleVal d){
        return  new DoubleVal(get() / d.get());
    }

    public DoubleVal round(){
        return DoubleVal.of(Math.round(get()));
    }

    public DoubleVal floor() {
        return DoubleVal.of(Math.floor(get()));
    }
}
