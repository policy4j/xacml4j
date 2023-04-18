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

import java.net.InetAddress;

import org.xacml4j.v30.Expression;
import org.xacml4j.v30.IPAddress;
import org.xacml4j.v30.PortRange;

/**
* Implementation an XACML {@link Expression}
* for {@link XacmlTypes#IPADDRESS} type.
*/
public final class IPAddressValue extends SingleValue<IPAddress>
{
    private static final long serialVersionUID = 8391410414891430400L;

    /**
     * Constructs IP address with a given address, mask and
     * IP port range
     *
     * @param address an IP address
     */
    IPAddressValue(IPAddress address){
        super(XacmlTypes.IPADDRESS, address);
    }

    static IPAddressValue of(String v){
        return new IPAddressValue(IPAddress.valueOf(v));
    }

    static IPAddressValue of(IPAddress v){
        return new IPAddressValue(v);
    }

    /**
     * Gets IP address
     *
     * @return {@link InetAddress}
     */
    public InetAddress getAddress(){
        return value().getAddress();
    }

    /**
     * Gets IP address mask
     *
     * @return {@link InetAddress} representing
     * IP address mask or {@code null}
     * if mask is not specified
     */
    public InetAddress getMask(){
        return value().getMask();
    }

    /**
     * Gets XACML IP address port range
     *
     * @return {@link PortRange} defaultProvider
     */
    public PortRange getRange(){
        return value().getRange();
    }

    public StringValue toStringExp(){
        return StringValue.of(value().toXacmlString());
    }
}
