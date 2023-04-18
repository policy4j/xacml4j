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
 * for {@link XacmlTypes#STRING} type.
 */
public final class StringValue extends SingleValue<String>
{
    private static final long serialVersionUID = 657672949137533611L;

    private StringValue(String value) {
        super(XacmlTypes.STRING, value);
    }

    /**
     * Creates {@link StringValue} from given string defaultProvider
     *
     * @param v a string value
     * @return {@link StringValue}
     * @exception IllegalArgumentException if given
     * string value is null or empty
     */
    static StringValue of(String v){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(v));
        return new StringValue(v);
    }

       public boolean equalsIgnoreCase(StringValue v){
        return get().equalsIgnoreCase(v.get());
    }

    public StringValue concat(StringValue v){
        return StringValue.of(get() + v);
    }

    public StringValue trim(){
        return StringValue.of(get().trim());
    }

    public boolean startsWith(StringValue v){
        return  get().startsWith(v.get());
    }

    public boolean contains(StringValue v){
        return  get().contains(v.get());
    }

    public boolean endsWith(StringValue v){
        return  get().endsWith(v.get());
    }

    public StringValue toLowerCase(){
        return StringValue.of(get().toLowerCase());
    }

    public StringValue toUpperCase(){
        return StringValue.of(get().toUpperCase());
    }
}
