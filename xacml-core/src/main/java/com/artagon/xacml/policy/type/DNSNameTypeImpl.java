package com.artagon.xacml.policy.type;

import com.artagon.xacml.util.Preconditions;

final class DNSNameTypeImpl extends BaseAttributeType<DNSNameType.DNSNameValue> implements DNSNameType
{
	public DNSNameTypeImpl(String typeId) {
		super(typeId, DNSName.class);
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
		if(getValueClass().isInstance(o)){
			return new DNSNameValue(this, (DNSName)o);
		}
		throw new IllegalArgumentException(o.getClass().getName());
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
