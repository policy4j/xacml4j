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

import org.xacml4j.v30.DNSName;
import org.xacml4j.v30.Expression;

/**
* Implementation an XACML {@link Expression}
* for {@link XacmlTypes#DNSNAME} type.
*/
public final class DNSNameValue extends SingleValue<DNSName>
{
    private static final long serialVersionUID = -1729624624549215684L;

    DNSNameValue(DNSName value){
        super(XacmlTypes.DNSNAME, value);
    }

    static DNSNameValue of(String v){
        return new DNSNameValue(DNSName.of(v));
    }

    static DNSNameValue of(StringValue v){
        return DNSNameValue.of(v.get());
    }

    static DNSNameValue of(DNSName v){
        return new DNSNameValue(v);
    }

    public StringValue toStringExp(){
        return StringValue.of(get().toXacmlString());
    }
}
