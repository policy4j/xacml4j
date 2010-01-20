package com.artagon.xacml.policy.type;

import java.net.URI;

import com.artagon.xacml.policy.BaseAttributeDataType;
import com.artagon.xacml.util.Preconditions;

final class AnyURITypeImpl extends BaseAttributeDataType<AnyURIType.AnyURIValue> implements AnyURIType
{
	public AnyURITypeImpl() {
		super(AnyURITypeId.ANYURI, URI.class);
	}

	@Override
	public AnyURIValue fromXacmlString(String v) {
		Preconditions.checkNotNull(v);

		return new AnyURIValue(this, URI.create(v));
	}
	
	@Override
	public AnyURIValue create(Object any){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"AnyURI\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		return new AnyURIValue(this, (URI)any);
	}
}
