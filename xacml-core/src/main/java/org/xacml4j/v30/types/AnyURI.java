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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.xacml4j.v30.Expression;
import org.xacml4j.v30.SyntaxException;

import com.google.common.base.Strings;

/**
 * Implementation an XACML {@link Expression}
* for {@link XacmlTypes#ANYURI} type.
 */
public final class AnyURI extends Value.OtherValue<URI>
{
    private static final long serialVersionUID = -1279561638068756670L;

    private AnyURI(URI value){
        super(XacmlTypes.ANYURI, value);
    }

    static AnyURI ofAny(Object v,
                        Object...params){
        if (v instanceof AnyURI) {
            return (AnyURI)v;
        }
        if (v instanceof java.lang.String) {
            return of((java.lang.String) v);
        }
        if (v instanceof URI) {
            return of((URI) v);
        }
        if (v instanceof URL) {
            return of((URL) v);
        }
        if (v instanceof String) {
            return of(v.toString());
        }
        throw SyntaxException
                .invalidAttributeValue(v,
                        XacmlTypes.ANYURI);
    }

    /**
     * Creates an XACML expression of {@link XacmlTypes#ANYURI}
     * from a given string
     *
     * @param v a string value
     * @return {@link AnyURI} defaultProvider
     */
    static AnyURI of(java.lang.String v){
        if(Strings.isNullOrEmpty(v)){
            throw SyntaxException
                    .invalidAttributeValue(v,
                            XacmlTypes.ANYURI);
        }
        return new AnyURI(URI.create(v).normalize());
    }

    static AnyURI of(StringVal v){
        if(v == null){
            throw SyntaxException.invalidAttributeValue(null,
                    XacmlTypes.ANYURI);
        }
        return of(v.get());
    }

    static AnyURI of(URL v){
        if(v == null){
            throw SyntaxException.invalidAttributeValue(null,
                    XacmlTypes.ANYURI);
        }
        try {
            return new AnyURI(v.toURI());
        } catch (URISyntaxException e) {
            throw SyntaxException.invalidAttributeValue(v,
                    XacmlTypes.ANYURI);
        }
    }

    static AnyURI of(URI v){
        if(v == null){
            throw SyntaxException.invalidAttributeValue(null,
                    XacmlTypes.ANYURI);
        }
        return new AnyURI(v);
    }

    public StringVal toStringExp(){
        return StringVal.of(get().toString());
    }
    public String toXacmlString(){
        return get().toString();
    }
    public StringVal concat(StringVal v){
        return StringVal.of(get().toString().concat(v.get()));
    }

    public BooleanVal endsWith(StringVal v){
        return BooleanVal.of(get().toString().endsWith(v.get()));
    }

    public BooleanVal contains(StringVal v){
        return BooleanVal.of(get().toString().contains(v.get()));
    }

    public AnyURI normalize(){
        return new AnyURI(get().normalize());
    }
}
