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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Implementation an XACML {@link Expression}
 * for {@link XacmlTypes#DOUBLE} type.
 */
public final class DoubleValue extends BaseValue<Double>
{
    private static final long serialVersionUID = -3689668541615314228L;

    private DoubleValue(Double value) {
        super(XacmlTypes.DOUBLE, value);
    }

    static DoubleValue of(Number value){
        Preconditions.checkNotNull(value);
        return new DoubleValue(value.doubleValue());
    }

    static DoubleValue of(String v){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(v));
         if (v.endsWith("INF")) {
                int infIndex = v.lastIndexOf("INF");
                v = v.substring(0, infIndex) + "Infinity";
         }
         return new DoubleValue(Double.parseDouble(v));
    }

    public StringValue toStringExp(){
        return StringValue.of(value().toString());
    }
    public IntegerValue toIntegerValue(){
        return IntegerValue.of(value().intValue());
    }

    public DoubleValue add(DoubleValue d){
        return  new DoubleValue(value() + d.value());
    }

    public DoubleValue subtract(DoubleValue d){
        return  new DoubleValue(value() - d.value());
    }

    public DoubleValue multiply(DoubleValue d){
        return  new DoubleValue(value() * d.value());
    }

    public DoubleValue divide(DoubleValue d){
        return  new DoubleValue(value() / d.value());
    }

}
