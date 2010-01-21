package com.artagon.xacml.policy.type;

import com.artagon.xacml.policy.BaseAttributeDataType;
import com.artagon.xacml.util.Preconditions;

final class BooleanTypeImpl extends BaseAttributeDataType<BooleanType.BooleanValue> implements BooleanType
{
	public BooleanTypeImpl(String typeId){
		super(typeId, Boolean.class);
	}
	
	@Override
	public BooleanValue create(Object any){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any),String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"boolean\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		return new BooleanValue(this, (Boolean)any);
	}

	@Override
	public BooleanValue fromXacmlString(String v) {
		Preconditions.checkNotNull(v);
		return create(Boolean.parseBoolean(v));
	}
}