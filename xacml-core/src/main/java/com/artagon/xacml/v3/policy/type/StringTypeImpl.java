package com.artagon.xacml.v3.policy.type;


import com.artagon.xacml.v3.policy.spi.type.BaseAttributeType;
import com.google.common.base.Preconditions;

final class StringTypeImpl extends BaseAttributeType<StringType.StringValue> implements StringType
{
	public StringTypeImpl(String typeId){
		super(typeId);
	}
	
	@Override
	public boolean isConvertableFrom(Object any) {
		return StringValue.class.isInstance(any) || String.class.isInstance(any);
	}

	@Override
	public StringValue fromXacmlString(String v, Object ...params) {
		return create(v);
	}
	
	@Override
	public StringValue create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"string\" type", 
				any, any.getClass()));
		return new StringValue(this, (String)any);
	}
}
