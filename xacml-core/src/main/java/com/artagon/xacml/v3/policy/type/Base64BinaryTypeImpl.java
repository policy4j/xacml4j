package com.artagon.xacml.v3.policy.type;

import com.artagon.xacml.util.Base64;
import com.artagon.xacml.util.Base64DecoderException;
import com.artagon.xacml.util.Preconditions;

class Base64BinaryTypeImpl extends BaseAttributeType<Base64BinaryType.Base64BinaryValue> 
	implements Base64BinaryType
{
	public Base64BinaryTypeImpl(String typeId){
		super(typeId);
	}
	
	@Override
	public boolean isConvertableFrom(Object any) {
		return byte[].class.isInstance(any) || String.class.isInstance(any);
	}
	
	@Override
	public Base64BinaryValue create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"hexBinary\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		if(byte[].class.isInstance(any)){
			return new Base64BinaryValue(this, new BinaryValue((byte[])any));
		}
		return new Base64BinaryValue(this, (BinaryValue)any);
	}

	@Override
	public Base64BinaryValue fromXacmlString(String v, Object ...params) {
		Preconditions.checkNotNull(v);
		try{
			return create(Base64.decode(v));
		}catch(Base64DecoderException e){
			throw new IllegalArgumentException(e);
		}
	}
}

