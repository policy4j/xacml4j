package com.artagon.xacml.v3.policy.type;

import java.net.InetAddress;

import com.artagon.xacml.v3.policy.spi.type.BaseAttributeType;
import com.google.common.base.Preconditions;

final class IPAddressTypeImpl extends BaseAttributeType<IPAddressType.IPAddressValue> 
	implements IPAddressType
{
	public IPAddressTypeImpl(String typeId){
		super(typeId);
	}
	
	@Override
	public IPAddressValue create(InetAddress address, PortRange portRange) {
		IPAddress v = new IPAddress(address, portRange);
		return new IPAddressValue(this, v);
	}


	@Override
	public IPAddressValue create(InetAddress address, InetAddress mask) {
		Preconditions.checkNotNull(address, "IP address can't be null");
		Preconditions.checkNotNull(mask, "IP address mask can't be null");
		IPAddress v = new IPAddress(address, mask, PortRange.getAnyPort());
		return new IPAddressValue(this, v);
	}

	
	@Override
	public IPAddressValue create(InetAddress address, InetAddress mask,
			PortRange portRange) {
		Preconditions.checkNotNull(address, "IP address can't be null");
		Preconditions.checkNotNull(mask, "IP address mask can't be null");
		Preconditions.checkNotNull(portRange, "Port range can't be null");
		IPAddress v = new IPAddress(address, mask, portRange);
		return new IPAddressValue(this, v);
	}
	
	@Override
	public boolean isConvertableFrom(Object any) {
		return IPAddress.class.isInstance(any) || String.class.isInstance(any) 
		|| InetAddress.class.isInstance(any);
	}

	@Override
	public IPAddressValue create(Object any, Object ...params) {
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne " +
				"converted to XACML \"ipAddress\" type", 
				any, any.getClass()));
		if(any instanceof InetAddress){
			return new IPAddressValue(this, new IPAddress((InetAddress)any));
		}
		if(any instanceof String){
			return fromXacmlString((String)any);
		}
		return new IPAddressValue(this, (IPAddress)any);
	}

	@Override
	public IPAddressValue fromXacmlString(String v, Object ...params) {
		IPAddress addr = IPAddress.valueOf(v);
		return new IPAddressValue(this, addr);
	}
}
