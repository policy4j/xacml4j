package org.xacml4j.v30.types;

import java.net.InetAddress;

import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.IPAddress;
import org.xacml4j.v30.PortRange;


public final class IPAddressExp extends BaseAttributeExp<IPAddress>
{
	private static final long serialVersionUID = 8391410414891430400L;

	/**
	 * Constructs IP address with a given address, mask and
	 * IP port range
	 *
	 * @param type IP address type
	 * @param address a IP address
	 */
	IPAddressExp(IPAddress address){
		super(XacmlTypes.IPADDRESS, address);
	}
	
	public static IPAddressExp valueOf(String v){
		return new IPAddressExp(IPAddress.valueOf(v));
	}
	
	public static IPAddressExp valueOf(IPAddress v){
		return new IPAddressExp(v);
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
	 * IP address mask or {@code null}
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
	
	public StringExp toStringExp(){
		return StringExp.valueOf(getValue().toXacmlString());
	}
	
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.IPADDRESS.emptyBag();
	}
	
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.IPADDRESS.bag();
	}
}
