package com.artagon.xacml.v3.policy.type;


import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.spi.type.BaseAttributeType;

final class DoubleTypeImpl extends BaseAttributeType<DoubleType.DoubleValue> implements DoubleType
{
	public DoubleTypeImpl(String typeId){
		super(typeId);
	}
	
	@Override
	public boolean isConvertableFrom(Object any) {
		return Double.class.isInstance(any) || Integer.class.isInstance(any) ||
		Short.class.isInstance(any) || Byte.class.isInstance(any) ||
		Float.class.isInstance(any) || Long.class.isInstance(any) 
		|| String.class.isInstance(any);
	}
	
	
	@Override
	public DoubleValue create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"double\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		if(Byte.class.isInstance(any)){
			return new DoubleValue(this, ((Byte)any).doubleValue());
		}
		if(Short.class.isInstance(any)){
			return new DoubleValue(this, ((Short)any).doubleValue());
		}
		if(Integer.class.isInstance(any)){
			return new DoubleValue(this, ((Integer)any).doubleValue());
		}
		if(Float.class.isInstance(any)){
			return new DoubleValue(this, ((Float)any).doubleValue());
		}
		if(Long.class.isInstance(any)){
			return new DoubleValue(this, ((Long)any).doubleValue());
		}
		return new DoubleValue(this, (Double)any);
	}
	
	@Override
	public DoubleValue fromXacmlString(String v, Object ...params) {

        if (v.endsWith("INF")) {
            int infIndex = v.lastIndexOf("INF");
            v = v.substring(0, infIndex) + "Infinity";
        }
        return new DoubleValue(this, Double.parseDouble(v));
	}
}
