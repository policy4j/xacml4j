package com.artagon.xacml.v30.types;

import java.net.InetAddress;

import com.artagon.xacml.v30.core.IPAddress;
import com.artagon.xacml.v30.core.PortRange;

public final class IPAddressValueExp extends BaseAttributeExpression<IPAddress>
{
	private static final long serialVersionUID = 8391410414891430400L;
	
	/**
	 * Constructs IP address with a given address, mask and
	 * IP port range
	 * 
	 * @param address an TCP/IP address
	 * @param mask an address mask
	 * @param range an address port range
	 */
	IPAddressValueExp(IPAddressType type, 
			IPAddress address)
	{
		super(type, address);
		
	}
	
	/**
	 * Gets IP address
	 * 
	 * @return {@link InetAddress}
	 */
	public InetAddress getAddress(){
		return getValue().getAddress();
	}
	
	/**
	 * Gets IP address mask
	 * 
	 * @return {@link InetAddress} representing
	 * IP address mask or <code>null</code>
	 * if mask is not specified
	 */
	public InetAddress getMask(){
		return getValue().getMask();
	}
	
	/**
	 * Gets XACML IP address port range
	 * 
	 * @return {@link PortRange} instance
	 */
	public PortRange getRange(){
		return getValue().getRange();
	}
		
	@Override
	public String toXacmlString(){
		return getValue().toXacmlString();
	}
}
