package com.artagon.xacml.v30.policy.type;


import com.artagon.xacml.util.Preconditions;

final class DoubleTypeImpl extends BaseAttributeType<DoubleType.DoubleValue> implements DoubleType
{
	public DoubleTypeImpl(String typeId){
		super(typeId, Double.class);
	}
	
	@Override
	public DoubleValue create(Object any){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"double\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		return new DoubleValue(this, (Double)any);
	}
	
	@Override
	public DoubleValue fromXacmlString(String v) {

        if (v.endsWith("INF")) {
            int infIndex = v.lastIndexOf("INF");
            v = v.substring(0, infIndex) + "Infinity";
        }
        return new DoubleValue(this, Double.parseDouble(v));
	}
}
