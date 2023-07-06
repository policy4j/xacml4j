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

import javax.security.auth.x500.X500Principal;

import org.xacml4j.v30.Expression;
import org.xacml4j.v30.SyntaxException;

/**
 * Implementation an XACML {@link Expression}
 * for {@link XacmlTypes#X500NAME} type.
 */
public final class X500Name extends Value.OtherValue<X500Principal>
{
    private static final long serialVersionUID = -609417077475809404L;

    private X500Name(X500Principal value) {
        super(XacmlTypes.X500NAME, value);
    }

    public static X500Name ofAny(Object v, Object... params) {
        if (v instanceof String) {
            return X500Name.of((String) v);
        }
        if (v instanceof X500Name) {
            return (X500Name)v;
        }
        if (v instanceof StringVal) {
            return X500Name.of(((StringVal) v));
        }
        if (v instanceof X500Principal) {
            return X500Name.of((X500Principal) v);
        }
        throw SyntaxException
                .invalidAttributeValue(v, XacmlTypes.X500NAME);
    }

    public static X500Name of(java.lang.String v){
        return new X500Name(new X500Principal(v));
    }

    public static X500Name of(StringVal v){
        return of(v.get());
    }

    public static X500Name of(X500Principal p){
        return new X500Name(p);
    }

    public StringVal toStringExp(){
        return StringVal.of(get().toString());
    }
    public String toXacmlString(){
        return value.toString();
    }

}
