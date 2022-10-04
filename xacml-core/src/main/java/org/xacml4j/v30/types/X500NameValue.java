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

import javax.security.auth.x500.X500Principal;

/**
 * Implementation an XACML {@link Expression}
 * for {@link XacmlTypes#X500NAME} type.
 */
public final class X500NameValue extends BaseValue<X500Principal>
{
    private static final long serialVersionUID = -609417077475809404L;

    private X500NameValue(X500Principal value) {
        super(XacmlTypes.X500NAME, value);
    }

    static X500NameValue of(String v){
        return new X500NameValue(new X500Principal(v));
    }

    static X500NameValue of(StringValue v){
        return of(v.value());
    }

    static X500NameValue of(X500Principal p){
        return new X500NameValue(p);
    }

    public StringValue toStringExp(){
        return StringValue.of(value().toString());
    }

}
