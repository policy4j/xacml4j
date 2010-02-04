package com.artagon.xacml.v3.policy.type;

import com.artagon.xacml.util.Preconditions;

final class DNSNameTypeImpl extends BaseAttributeType<DNSNameType.DNSNameValue> implements DNSNameType
{
	public DNSNameTypeImpl(String typeId) {
		super(typeId);
	}
	
	@Override
	public boolean isConvertableFrom(Object any) {
		return DNSName.class.isInstance(any) || String.class.isInstance(any);
	}
	
	@Override
	public DNSNameValue create(Object o) {
		Preconditions.checkNotNull(o);
		Preconditions.checkArgument(isConvertableFrom(o), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"DNSName\" type", 
				o, o.getClass()));
		if(String.class.isInstance(o)){
			return fromXacmlString((String)o);
		}
		return new DNSNameValue(this, (DNSName)o);
	}

	@Override
	public DNSNameValue fromXacmlString(String v) {
		Preconditions.checkNotNull(v);
		int pos = v.indexOf(':');
		if(pos == -1){
			return new DNSNameValue(this, new DNSName(v, PortRange.getAnyPort()));
		}
		String name = v.substring(0, pos);
		return new DNSNameValue(this, 
				new DNSName(name, PortRange.valueOf(pos + 1, v)));
	}
}
