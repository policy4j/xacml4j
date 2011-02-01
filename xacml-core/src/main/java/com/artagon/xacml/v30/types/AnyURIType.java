package com.artagon.xacml.v30.types;

import java.net.URI;
import java.util.Collection;

import com.artagon.xacml.v30.AttributeValue;
import com.artagon.xacml.v30.AttributeValueType;
import com.artagon.xacml.v30.BagOfAttributeValues;
import com.artagon.xacml.v30.BagOfAttributeValuesType;
import com.google.common.base.Preconditions;

public enum AnyURIType implements AttributeValueType
{	
	ANYURI("http://www.w3.org/2001/XMLSchema#anyURI");
	
	private String typeId;
	private BagOfAttributeValuesType bagType;
	
	private AnyURIType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeValuesType(this);
	}
	
	@Override
	public AnyURIValue fromXacmlString(String v, Object ...params) {
		Preconditions.checkNotNull(v);
		return new AnyURIValue(URI.create(v).normalize());
	}
	
	@Override
	public boolean isConvertableFrom(Object any) {
		return URI.class.isInstance(any) || String.class.isInstance(any);
	}

	@Override
	public AnyURIValue create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"anyURI\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		return new AnyURIValue((URI)any);
	}

	@Override
	public String getDataTypeId() {
		return typeId;
	}

	@Override
	public BagOfAttributeValuesType bagType() {
		return bagType;
	}

	@Override
	public BagOfAttributeValues bagOf(AttributeValue... values) {
		return bagType.create(values);
	}
	
	@Override
	public BagOfAttributeValues bagOf(Collection<AttributeValue> values) {
		return bagType.create(values);
	}
	
	@Override
	public BagOfAttributeValues bagOf(Object... values) {
		return bagType.bagOf(values);
	}

	@Override
	public BagOfAttributeValues emptyBag() {
		return bagType.createEmpty();
	}
	
	@Override
	public String toString(){
		return typeId;
	}
}