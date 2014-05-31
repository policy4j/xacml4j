package org.xacml4j.v30;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
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

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import org.xacml4j.util.IPAddressUtils;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class IPAddress implements Serializable
{
	private static final long serialVersionUID = 8391410414891430400L;

	private final InetAddress address;
	private final InetAddress mask;
	private final PortRange range;

	/**
	 * Constructs IP address with a given address, mask and
	 * IP port range
	 *
	 * @param b IPAddress builder
	 */
	private IPAddress(Builder b)
	{
		Preconditions.checkNotNull(b.addr);
		Preconditions.checkArgument(
				((b.addr instanceof Inet6Address) && (b.mask == null || b.mask instanceof Inet6Address)) ||
				((b.addr instanceof Inet4Address) && (b.mask == null || b.mask instanceof Inet4Address)),
				String.format("Address=\"%s\" and mask=\"%s\" " +
						"should be either IPV4 or IPV6", b.addr, b.mask));
		this.address = b.addr;
		this.mask = b.mask;
		this.range = b.range;
	}

	public static Builder builder(){
		return new Builder();
	}

	public static IPAddress valueOf(String v)
	{
		v = v.trim();
		return (v.indexOf('[') == 0)?getV6Instance(v):getV4Instance(v);
	}

	private static IPAddress getV6Instance(String value)
    {
		int len = value.length();
		int endIndex = value.indexOf(']');
		Preconditions.checkArgument(endIndex > 0);
		String addr = value.substring(1, endIndex);
		Preconditions.checkArgument(IPAddressUtils.isIPv6LiteralAddress(addr),
         		  "Expected IPV6 address, but found=\"%s\"", addr);

		Builder builder = IPAddress.builder();
		builder.address(addr);
		if (endIndex != (len - 1)) {
          // if there's a mask, it's also an IPv6 address
          if (value.charAt(endIndex + 1) == '/') {
              int startIndex = endIndex + 3;
              endIndex = value.indexOf(']', startIndex);
              addr = value.substring(startIndex, endIndex);
              Preconditions.checkArgument(IPAddressUtils.isIPv6LiteralAddress(addr),
            		  "Expected IPV6 mask, but found=\"%s\"", addr);
              builder.mask(addr);
          }
          if ((endIndex != (len - 1)) && (value.charAt(endIndex + 1) == ':'))
              builder.portRange(PortRange.valueOf(value.substring(endIndex + 2, len)));
        }
		return builder.build();
    }

    private static IPAddress getV4Instance(String value)
    {
        int maskPos = value.indexOf("/");
        int rangePos = value.indexOf(":");
        Builder builder = IPAddress.builder();
        if (maskPos == rangePos) {
            // the sting is just an address
        	 Preconditions.checkArgument(IPAddressUtils.isIPv4LiteralAddress(value),
            		  "Expected IPV4 address, but found=\"%s\"", value);
            builder.address(value);
        } else if (maskPos != -1) {
            // there is also a mask (and maybe a range)
        	String addrr = value.substring(0, maskPos);
        	 Preconditions.checkArgument(IPAddressUtils.isIPv4LiteralAddress(addrr),
           		  "Expected IPV4 address, but found=\"%s\"", addrr);
            builder.address(value.substring(0, maskPos));
            if (rangePos != -1) {
                // there's a range too, so get it and the mask
                builder.mask(value.substring(maskPos + 1, rangePos));
                builder.portRange(PortRange.valueOf(value.substring(rangePos + 1,
                                                          value.length())));
            } else {
                // there's no range, so just get the mask
                builder.mask(value.substring(maskPos + 1, value.length()));
            }
        } else {
            // there is a range, but no mask
            builder.address(value.substring(0, rangePos));
            builder.portRange(PortRange.valueOf(value.substring(rangePos + 1,
                                                          value.length())));
        }
        return builder.build();
    }

	/**
	 * Gets IP address
	 *
	 * @return {@link InetAddress}
	 */
	public InetAddress getAddress(){
		return address;
	}

	/**
	 * Gets IP address mask
	 *
	 * @return {@link InetAddress} representing
	 * IP address mask or {@code null}
	 * if mask is not specified
	 */
	public InetAddress getMask(){
		return mask;
	}

	public boolean isV6Address(){
		return (address instanceof Inet6Address);
	}

	public boolean isV4Address(){
		return (address instanceof Inet4Address);
	}

	/**
	 * Gets XACML IP address port range
	 *
	 * @return {@link PortRange} instance
	 */
	public PortRange getRange(){
		return range;
	}

	@Override
	public String toString(){
		return toXacmlString();
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(address, mask, range);
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof IPAddress)){
			return false;
		}
		IPAddress a = (IPAddress)o;
		return address.equals(a.address)
				&& Objects.equal(mask, a.mask)
				&& range.equals(a.range);
	}

	public String toXacmlString()
	{
		StringBuilder b = new StringBuilder(64);

		if(getAddress() instanceof Inet6Address){
			b.append("[").append(IPAddressUtils.toStringWithNoHostname(getAddress())).append("]");
		}else{
			b.append(getAddress().getHostAddress());
		}
        if (getMask() != null){
        	if(getMask() instanceof Inet6Address){
        		b.append("/[")
        		.append(IPAddressUtils.toStringWithNoHostname(getMask()))
        		.append("]");
        	}else{
        		b.append("/")
        		.append(IPAddressUtils.toStringWithNoHostname(getMask()));
        	}
        }
        if (!getRange().isUnbound()){
        	b.append(":").append(getRange());
        }
        return b.toString();
	}

	public static class Builder
	{
		private InetAddress addr;
		private InetAddress mask;
		private PortRange range = PortRange.getAnyPort();

		Builder address(InetAddress address){
			this.addr = address;
			return this;
		}

		public Builder address(String address){
			this.addr = IPAddressUtils.parseAddress(address);
			return this;
		}

		public Builder mask(InetAddress mask){
			this.mask = mask;
			return this;
		}

		public Builder mask(String mask){
			this.mask = IPAddressUtils.parseAddress(mask);
			return this;
		}

		public Builder portRange(PortRange range){
			this.range = range;
			return this;
		}

		public Builder portRange(String range){
			this.range = PortRange.valueOf(range);
			return this;
		}

		public IPAddress build(){
			return new IPAddress(this);
		}
	}
}
