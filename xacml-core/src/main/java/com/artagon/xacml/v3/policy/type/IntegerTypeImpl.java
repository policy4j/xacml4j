package com.artagon.xacml.v3.policy.type;


import com.artagon.xacml.util.Preconditions;

final class IntegerTypeImpl extends BaseAttributeType<IntegerType.IntegerValue> implements IntegerType
{
	public IntegerTypeImpl(String typeId) {
		super(typeId);
	}
	
	@Override
	public boolean isConvertableFrom(Object any) {
		return Long.class.isInstance(any) || Integer.class.isInstance(any) ||
		Short.class.isInstance(any) || Byte.class.isInstance(any) ||
		String.class.isInstance(any);
	}


	@Override
	public IntegerValue create(Object any){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"integer\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		if(Byte.class.isInstance(any)){
			return new IntegerValue(this, ((Byte)any).longValue());
		}
		if(Short.class.isInstance(any)){
			return new IntegerValue(this, ((Short)any).longValue());
		}
		if(Integer.class.isInstance(any)){
			return new IntegerValue(this, ((Integer)any).longValue());
		}
		return new IntegerValue(this, (Long)any);
	}

	@Override
	public IntegerValue fromXacmlString(String v) {
        Preconditions.checkNotNull(v);
		if ((v.length() >= 1) && 
        		(v.charAt(0) == '+')){
			v = v.substring(1);
		}
        return new IntegerValue(this, Long.valueOf(v));
	}
}
