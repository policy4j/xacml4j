package com.artagon.xacml.v30.types;

import java.net.URI;
import java.util.Collection;

import com.artagon.xacml.v30.pdp.AttributeExp;
import com.artagon.xacml.v30.pdp.AttributeExpType;
import com.artagon.xacml.v30.pdp.BagOfAttributeExp;
import com.artagon.xacml.v30.pdp.BagOfAttributeExpType;
import com.google.common.base.Preconditions;

public enum AnyURIType implements AttributeExpType
{	
	ANYURI("http://www.w3.org/2001/XMLSchema#anyURI");
	
	private String typeId;
	private BagOfAttributeExpType bagType;
	
	private AnyURIType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}
	
	@Override
	public AnyURIExp fromXacmlString(String v, Object ...params) {
		Preconditions.checkNotNull(v);
		URI u = URI.create(v).normalize();
		return new AnyURIExp(u);
	}
	
	public boolean isConvertableFrom(Object any) {
		return URI.class.isInstance(any) || String.class.isInstance(any);
	}

	@Override
	public AnyURIExp create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"anyURI\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		return new AnyURIExp((URI)any);
	}

	@Override
	public String getDataTypeId() {
		return typeId;
	}

	@Override
	public BagOfAttributeExpType bagType() {
		return bagType;
	}

	@Override
	public BagOfAttributeExp bagOf(AttributeExp... values) {
		return bagType.create(values);
	}
	
	@Override
	public BagOfAttributeExp bagOf(Collection<AttributeExp> values) {
		return bagType.create(values);
	}
	
	@Override
	public BagOfAttributeExp bagOf(Object... values) {
		return bagType.bagOf(values);
	}

	@Override
	public BagOfAttributeExp emptyBag() {
		return bagType.createEmpty();
	}
	
	@Override
	public String toString(){
		return typeId;
	}
}