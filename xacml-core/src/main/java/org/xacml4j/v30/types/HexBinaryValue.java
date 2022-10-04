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

/**
* Implementation an XACML {@link Expression}
* for {@link XacmlTypes#HEXBINARY} type.
*/
public final class HexBinaryValue extends
                                  BaseValue<Binary>
{
    private static final long serialVersionUID = 8087916652227967791L;

    HexBinaryValue(Binary value) {
        super(XacmlTypes.HEXBINARY, value);
    }

    static HexBinaryValue of(String v){
        return new HexBinaryValue(Binary.valueOfHexEnc(v));
    }

    static HexBinaryValue of(byte[] v){
        return new HexBinaryValue(Binary.of(v));
    }

    static HexBinaryValue of(Binary v){
        return new HexBinaryValue(v);
    }

}
