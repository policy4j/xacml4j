package com.artagon.xacml.v3.policy.type;

import java.net.URI;

import com.artagon.xacml.v3.policy.spi.type.BaseAttributeType;
import com.google.common.base.Preconditions;

final class AnyURITypeImpl extends BaseAttributeType<AnyURIType.AnyURIValue> implements AnyURIType
{
	public AnyURITypeImpl(String typeId) {
		super(typeId);
	}

	@Override
	public AnyURIValue fromXacmlString(String v, Object ...params) {
		Preconditions.checkNotNull(v);
		// FIXME: spec does not say anything
		// around URI normalization
		return new AnyURIValue(this, URI.create(v).normalize());
	}
	
	@Override
	public boolean isConvertableFrom(Object any) {
		return URI.class.isInstance(any) || String.class.isInstance(any);
	}

	@Override
	public AnyURIValue create(Object any, Object ...params){
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
