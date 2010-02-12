package com.artagon.xacml.v3.policy.type;

import javax.security.auth.x500.X500Principal;

import com.artagon.xacml.util.Preconditions;

final class X500NameTypeImpl extends BaseAttributeType<X500NameType.X500NameValue> implements X500NameType
{
	
	public X500NameTypeImpl(String typeId){
		super(typeId);
	}

	
	@Override
	public boolean isConvertableFrom(Object any) {
		return X500Principal.class.isInstance(any) || String.class.isInstance(any);
	}
	
	@Override
	public X500NameValue fromXacmlString(String v, Object ...params) {
		Preconditions.checkNotNull(v);
		return new X500NameValue(this, new X500Principal(v));
	}
	
	@Override
	public X500NameValue create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"x500Name\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		return new X500NameValue(this, (X500Principal)any);
	}
}


