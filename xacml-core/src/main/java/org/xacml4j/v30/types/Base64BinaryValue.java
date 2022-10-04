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

/**
 * Implementation an XACML {@link Expression}
 * for {@link XacmlTypes#BASE64BINARY} type.

*/
public final class Base64BinaryValue extends BaseValue<Binary>
{
    private static final long serialVersionUID = 3298986540546649848L;

    static Base64BinaryValue fromObjectWithParams(Object v, Object ... p){
        if (v instanceof String) {
            return new Base64BinaryValue(Binary
                    .valueOfBase64Enc((String)v));
        }
        if (v instanceof byte[]) {
            return new Base64BinaryValue(
                    Binary.of(( byte[]) v));
        }
        if (v instanceof StringValue) {
            return new Base64BinaryValue(Binary
                    .valueOfBase64Enc(((StringValue) v).value()));
        }
        if(v instanceof Binary){
            return new Base64BinaryValue((Binary) v);
        }
        throw SyntaxException
                        .invalidAttributeValue(v,
                                XacmlTypes.BASE64BINARY);
    }

    private Base64BinaryValue(Binary value) {
        super(XacmlTypes.BASE64BINARY, value);
    }

    public StringValue toStringExp(){
        return StringValue.of(value()
                .toBase64Encoded());
    }

}
