package com.artagon.xacml.policy.type;


import com.artagon.xacml.util.Preconditions;

final class StringTypeImpl extends BaseAttributeType<StringType.StringValue> implements StringType
{
	public StringTypeImpl(String typeId){
		super(typeId, String.class);
	}

	@Override
	public StringValue fromXacmlString(String v) {
		return create(v);
	}
	
	@Override
	public StringValue create(Object any){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"string\" type", 
				any, any.getClass()));
		return new StringValue(this, (String)any);
	}
}
