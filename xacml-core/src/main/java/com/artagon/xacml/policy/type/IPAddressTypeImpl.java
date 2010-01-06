package com.artagon.xacml.policy.type;

import java.net.InetAddress;

import com.artagon.xacml.DataTypes;
import com.artagon.xacml.policy.BaseAttributeDataType;
import com.artagon.xacml.util.Preconditions;

final class IPAddressTypeImpl extends BaseAttributeDataType<IPAddressType.IPAddressValue> implements IPAddressType
{
	public IPAddressTypeImpl(){
		super(DataTypes.IPADDRESS, IPAddress.class);
	}
	
	@Override
	public IPAddressValue create(InetAddress address, InetAddress mask,
			PortRange portRange) {
		IPAddress v = new IPAddress(address, mask, portRange);
		return new IPAddressValue(this, v);
	}
	
	@Override
	public IPAddressValue create(Object any) {
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"ipAddress\" type", 
				any, any.getClass()));
		return new IPAddressValue(this, (IPAddress)any);
	}

	@Override
	public IPAddressValue create(InetAddress address, PortRange portRange) {
		IPAddress v = new IPAddress(address, portRange);
		return new IPAddressValue(this, v);
	}


	@Override
	public IPAddressValue create(InetAddress address, InetAddress mask) {
		IPAddress v = new IPAddress(address, PortRange.getAnyPort());
		return new IPAddressValue(this, v);
	}


	@Override
	public IPAddressValue fromXacmlString(String v) {
		return null;
	}
}
