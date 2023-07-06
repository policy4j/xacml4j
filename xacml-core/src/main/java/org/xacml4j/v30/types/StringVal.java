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

import org.xacml4j.v30.SyntaxException;

import java.util.Objects;

/**
 * Implementation an XACML {@link Expression}
 * for {@link XacmlTypes#STRING} type.
 */
public final class StringVal extends Value.OtherValue<String>
{
    private static final long serialVersionUID = 657672949137533611L;

    private StringVal(java.lang.String value) {
        super(XacmlTypes.STRING, value);
    }

    /**
     * Creates {@link String} from given string defaultProvider
     *
     * @param v a string value
     * @return {@link String}
     * @exception IllegalArgumentException if given
     * string value is null or empty
     */
    public static StringVal of(String v){
        return new StringVal(Objects.requireNonNull(v));
    }

    public static StringVal ofAny(Object v, Object ...params){
        Objects.requireNonNull(v);
        if(v instanceof String){
            return of((String) v);
        }
        if(v instanceof StringVal){
            return (StringVal) v;
        }
        throw SyntaxException
                .invalidAttributeValue(v, XacmlTypes.STRING);
    }


    public BooleanVal equalsIgnoreCase(StringVal v){
        return BooleanVal.of(get().equalsIgnoreCase(v.get()));
    }
    public BooleanVal equalsIgnoreCase(String v){
        return BooleanVal.of(get().equalsIgnoreCase(v));
    }

    public StringVal concat(StringVal v){
        return StringVal.of(get() + v.value);
    }
    public StringVal concat(String v){
        return StringVal.of(get() + v);
    }

    public StringVal trim(){
        return StringVal.of(get().trim());
    }

    public BooleanVal startsWith(StringVal v){
        return  BooleanVal.of(get().startsWith(v.get()));
    }

    public BooleanVal contains(StringVal v){
        return BooleanVal.of(get().contains(v.get()));
    }

    public BooleanVal endsWith(StringVal v){
        return  BooleanVal.of(get().endsWith(v.get()));
    }

    public StringVal toLowerCase(){
        return StringVal.of(get().toLowerCase());
    }

    public StringVal toUpperCase(){
        return StringVal.of(get().toUpperCase());
    }
}
