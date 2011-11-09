package com.artagon.xacml.v30;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import com.artagon.xacml.util.IPAddressUtils;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class IPAddress implements Serializable
{
	private static final long serialVersionUID = 8391410414891430400L;
	
	private InetAddress address;
	private InetAddress mask;
	private PortRange range;
	
	/**
	 * Constructs IP address with a given address, mask and
	 * IP port range
	 * 
	 * @param address an TCP/IP address
	 * @param mask an address mask
	 * @param range an address port range
	 */
	public IPAddress( InetAddress address, 
			InetAddress mask, PortRange range)
	{
		Preconditions.checkNotNull(address);
		Preconditions.checkNotNull(range);
		Preconditions.checkArgument(
				((address instanceof Inet6Address) && (mask == null || mask instanceof Inet6Address)) ||
				((address instanceof Inet4Address) && (mask == null || mask instanceof Inet4Address)),
				String.format("Address=\"%s\" and mask=\"%s\" " +
						"should be either IPV4 or IPV6", address, mask));
		this.address = address;
		this.mask = mask;
		this.range = range;
	}
	
	public static IPAddress parse(Object any)
	{
		Preconditions.checkNotNull(any);
		if(String.class.isInstance(any)){
			String v = (String)any;
			v = v.trim();
			if(v.indexOf('[') == 0){
				return getV6Instance(v);
			}
			return getV4Instance(v);
		}
		if(any instanceof IPAddress){
			return (IPAddress)any;
		}
		InetAddress address = (InetAddress)any;
		return new IPAddress(address);
	}
	
	private static IPAddress getV6Instance(String value)
    {
		InetAddress address = null;
		InetAddress mask = null;
		PortRange range = null;
		int len = value.length();
		int endIndex = value.indexOf(']');
		Preconditions.checkArgument(endIndex > 0);
		String addrr = value.substring(1, endIndex);
		Preconditions.checkArgument(IPAddressUtils.isIPv6LiteralAddress(addrr), 
         		  "Expected IPV6 address, but found=\"%s\"", addrr);
		address = IPAddressUtils.parseAddress(addrr);
      
      // see if there's anything left in the string
      if (endIndex != (len - 1)) {
          // if there's a mask, it's also an IPv6 address
          if (value.charAt(endIndex + 1) == '/') {
              int startIndex = endIndex + 3;
              endIndex = value.indexOf(']', startIndex);
              addrr = value.substring(startIndex, endIndex);
              Preconditions.checkArgument(IPAddressUtils.isIPv6LiteralAddress(addrr), 
            		  "Expected IPV6 mask, but found=\"%s\"", addrr);
              mask = IPAddressUtils.parseAddress(addrr);
          }
          if ((endIndex != (len - 1)) && (value.charAt(endIndex + 1) == ':'))
              range = PortRange.valueOf(value.substring(endIndex + 2, len));
      }
      return new IPAddress(address, mask, (range != null)?range:PortRange.getAnyPort());
    }
	
    private static IPAddress getV4Instance(String value)
    {
        InetAddress address = null;
        InetAddress mask = null;
        PortRange range = null;
        int maskPos = value.indexOf("/");
        int rangePos = value.indexOf(":");
        if (maskPos == rangePos) {
            // the sting is just an address
        	 Preconditions.checkArgument(IPAddressUtils.isIPv4LiteralAddress(value), 
            		  "Expected IPV4 address, but found=\"%s\"", value);
            address = IPAddressUtils.parseAddress(value);
        } else if (maskPos != -1) {
            // there is also a mask (and maybe a range)
        	String addrr = value.substring(0, maskPos);
        	 Preconditions.checkArgument(IPAddressUtils.isIPv4LiteralAddress(addrr), 
           		  "Expected IPV4 address, but found=\"%s\"", addrr);
            address = IPAddressUtils.parseAddress(value.substring(0, maskPos));
            if (rangePos != -1) {
                // there's a range too, so get it and the mask
                mask = IPAddressUtils.parseAddress(value.substring(maskPos + 1,
                                                          rangePos));
                range =
                    PortRange.valueOf(value.substring(rangePos + 1,
                                                          value.length()));
            } else {
                // there's no range, so just get the mask
                mask = IPAddressUtils.parseAddress(value.substring(maskPos + 1,
                                                             value.length()));
            }
        } else {
            // there is a range, but no mask
            address = IPAddressUtils.parseAddress(value.substring(0, rangePos));
            range = PortRange.valueOf(value.substring(rangePos + 1,
                                                          value.length()));
        }
        return new IPAddress(address, mask, range != null?range:PortRange.getAnyPort());
    }
    
	/**
	 * Constructs IP address with a given address, mask
	 * and any port
	 * 
	 * @param address an TCP/IP address
	 * @param mask an address mask
	 */
	public IPAddress(InetAddress address, InetAddress mask){
		this(address, mask, PortRange.getAnyPort());
	}
	
	/**
	 * Constructs IP address with a given address
	 * 
	 * @param address an TCP/IP address
	 */
	public IPAddress(InetAddress address){
		this(address, null, PortRange.getAnyPort());
	}
	
	/**
	 * Constructs IP address with a given address 
	 * and specified port range
	 * 
	 * @param address an TCP/IP address
	 * @param range an address port range
	 */
	public IPAddress(InetAddress address, PortRange range){
		this(address, null, range);
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
	 * IP address mask or <code>null</code>
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
		if(o == null){
			return false;
		}
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
}
