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

import java.nio.ByteBuffer;

/**
* Implementation an XACML {@link Expression}
* for {@link XacmlTypes#HEXBINARY} type.
*/
public final class HexBinary extends
        Value.OtherValue<Binary>
{
    private static final long serialVersionUID = 8087916652227967791L;

    public static HexBinary ofAny(Object v, Object ... p) throws SyntaxException{
        if (v instanceof java.lang.String) {
            return new HexBinary(Binary.ofHex(((java.lang.String) v)));
        }
        if (v instanceof StringVal) {
            return new HexBinary(Binary.ofHex((StringVal)v));
        }
        return Binary.ofAnyBinary(v, p)
                .map(HexBinary::new)
                .orElseThrow(()->SyntaxException
                        .invalidAttributeValue(v,
                                XacmlTypes.HEXBINARY));
    }

    private HexBinary(Binary value) {
        super(XacmlTypes.HEXBINARY, value);
    }

    public static HexBinary of(String v){
        return new HexBinary(Binary.hexDecode(v));
    }

    public static HexBinary of(byte[] v){
        return new HexBinary(Binary.of(v));
    }

    public static HexBinary of(Binary v){
        return new HexBinary(v);
    }
    public static HexBinary of(ByteBuffer v){
        return new HexBinary(Binary.of(v));
    }

}
