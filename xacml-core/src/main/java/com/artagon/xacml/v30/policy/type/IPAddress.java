package com.artagon.xacml.v30.policy.type;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import com.artagon.xacml.util.Objects;
import com.artagon.xacml.util.Preconditions;


public class IPAddress 
{
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
	public IPAddress(InetAddress address, 
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
	
	/**
	 * Gets XACML IP address port range
	 * 
	 * @return {@link PortRange} instance
	 */
	public PortRange getRange(){
		return range;
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
		return getAddress().equals(a.getAddress()) &&
		getRange().equals(a.getRange()) && Objects.equal(mask, a.getMask());
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(getAddress(), getMask(), getRange());
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() 
	{
		StringBuilder b = new StringBuilder(64);
		if(getAddress() instanceof Inet6Address){
			b.append("[").append(getAddress().getHostAddress()).append("]");
		}else{
			b.append(getAddress().getHostAddress());
		}
        if (getMask() != null){
        	if(getMask() instanceof Inet6Address){
        		b.append("/[").append(getMask().getHostAddress()).append("]");
        	}else{
        		b.append("/").append(getMask().getHostAddress());
        	}
        }
        if (!getRange().isUnbound()){
        	b.append(":").append(getRange());
        }
        return b.toString();
    }
}
