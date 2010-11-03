package com.artagon.xacml.v3.types;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import com.google.common.base.Preconditions;

public final class IPAddressValue extends BaseAttributeValue
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
	public IPAddressValue(IPAddressType type, 
			InetAddress address, 
			InetAddress mask, PortRange range)
	{
		super(type);
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
	public IPAddressValue(IPAddressType type, InetAddress address, InetAddress mask){
		this(type, address, mask, PortRange.getAnyPort());
	}
	
	/**
	 * Constructs IP address with a given address
	 * 
	 * @param address an TCP/IP address
	 */
	public IPAddressValue(IPAddressType type, InetAddress address){
		this(type, address, null, PortRange.getAnyPort());
	}
	
	/**
	 * Constructs IP address with a given address 
	 * and specified port range
	 * 
	 * @param address an TCP/IP address
	 * @param range an address port range
	 */
	public IPAddressValue(IPAddressType type, InetAddress address, PortRange range){
		this(type, address, null, range);
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
		
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toXacmlString() 
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
