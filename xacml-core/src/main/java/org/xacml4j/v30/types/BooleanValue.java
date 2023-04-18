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

import com.google.common.base.Preconditions;

/**
* Implementation an XACML {@link Expression}
* for {@link XacmlTypes#BOOLEAN} type.
*/
public final class BooleanValue extends SingleValue<Boolean>
{
    private static final long serialVersionUID = -421397689674188254L;

    final static BooleanValue FALSE = new BooleanValue(Boolean.FALSE);
    final static BooleanValue TRUE = new BooleanValue(Boolean.TRUE);

    private BooleanValue(Boolean value) {
        super(XacmlTypes.BOOLEAN, value);
    }

    static BooleanValue fromObjectWithParams(Object v, Object ...params){
        if (v instanceof String) {
            return Boolean.parseBoolean((String)v)?
                    BooleanValue.TRUE: BooleanValue.FALSE;
        }
        if (v instanceof StringValue) {
            return Boolean.parseBoolean(((StringValue)v).value())?
                    BooleanValue.TRUE: BooleanValue.FALSE;
        }
        if (v instanceof Boolean) {
            return (Boolean)v? BooleanValue.TRUE: BooleanValue.FALSE;
        }
        throw SyntaxException.invalidAttributeValue(
                v, XacmlTypes.BOOLEAN);
    }

    static BooleanValue of(String v){
        return Boolean.parseBoolean(v)? BooleanValue.TRUE: BooleanValue.FALSE;
    }

    static BooleanValue of(boolean v){
        Preconditions.checkNotNull(v);
        return v? BooleanValue.TRUE: BooleanValue.FALSE;
    }

    public StringValue toStringExp(){
        return StringValue.of(Boolean.toString(value()));
    }
}
