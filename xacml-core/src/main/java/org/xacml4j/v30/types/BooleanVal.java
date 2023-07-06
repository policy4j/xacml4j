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
* for {@link XacmlTypes#BOOLEAN} type.
*/
public final class BooleanVal extends Value.OtherValue<Boolean>
{
    private static final long serialVersionUID = -421397689674188254L;

    final static BooleanVal FALSE = new BooleanVal(java.lang.Boolean.FALSE);
    final static BooleanVal TRUE = new BooleanVal(java.lang.Boolean.TRUE);

    private BooleanVal(java.lang.Boolean value) {
        super(XacmlTypes.BOOLEAN, value);
    }

    public static BooleanVal of(java.lang.Boolean value) {
        return value?TRUE:FALSE;
    }

    public static BooleanVal ofAny(Object v, Object ...params){
        if (v instanceof java.lang.String) {
            return java.lang.Boolean.parseBoolean((java.lang.String)v)?
                    BooleanVal.TRUE: BooleanVal.FALSE;
        }
        if (v instanceof StringVal) {
            return java.lang.Boolean.parseBoolean(((StringVal)v).get()) ?
                    BooleanVal.TRUE : BooleanVal.FALSE;
        }
        if (v instanceof java.lang.Boolean) {
            return (java.lang.Boolean)v? BooleanVal.TRUE: BooleanVal.FALSE;
        }
        throw SyntaxException.invalidAttributeValue(
                v, XacmlTypes.BOOLEAN);
    }

    public BooleanVal and(BooleanVal v){
        return BooleanVal.of(get() && v.get());
    }

    public BooleanVal or(BooleanVal v){
        return BooleanVal.of(get() || v.get());
    }
    public BooleanVal not(){
        return BooleanVal.of(!get());
    }
    public BooleanVal xor(BooleanVal v){
        return BooleanVal.of(get() ^ v.get());
    }

    public StringVal toStringExp(){
        return StringVal.of(java.lang.Boolean.toString(get()));
    }
    public String toXacmlString(){
        return java.lang.Boolean.toString(get());
    }
}
