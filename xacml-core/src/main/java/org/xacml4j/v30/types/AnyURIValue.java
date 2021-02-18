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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.SyntaxException;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Implementation an XACML {@link Expression}
* for {@link XacmlTypes#ANYURI} type.
 */
public final class AnyURIValue extends BaseAttributeValue<URI>
{
    private static final long serialVersionUID = -1279561638068756670L;

    private AnyURIValue(URI value){
        super(XacmlTypes.ANYURI, value);
    }

    static AnyURIValue fromObjectWithParams(Object v,
                                            Object...params){
        if (v instanceof String) {
            return of((String) v);
        }
        if (v instanceof URI) {
            return of((URI) v);
        }
        if (v instanceof URL) {
            return of((URL) v);
        }
        if (v instanceof AnyURIValue) {
            return (AnyURIValue)v;
        }
        if (v instanceof StringValue) {
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
     * @return {@link AnyURIValue} defaultProvider
     */
    static AnyURIValue of(String v){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(v));
        return new AnyURIValue(URI.create(v).normalize());
    }

    static AnyURIValue of(StringValue v){
        return of(v.value());
    }

    static AnyURIValue of(URL v){
        try {
            return new AnyURIValue(v.toURI());
        } catch (URISyntaxException e) {
            throw SyntaxException.invalidAttributeValue(v,
                    XacmlTypes.ANYURI);
        }
    }

    static AnyURIValue of(URI v){
        Preconditions.checkNotNull(v);
        return new AnyURIValue(v);
    }

    public StringValue toStringExp(){
        return StringValue.of(value().toString());
    }

    public AnyURIValue normalize(){
        return new AnyURIValue(value().normalize());
    }
}
