package org.xacml4j.v30.types;

import java.net.URI;

import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.BagOfAttributeExpType;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public enum AnyURIType implements AttributeExpType, 
TypeToString, TypeToXacml30
{
	ANYURI("http://www.w3.org/2001/XMLSchema#anyURI");

	private final String typeId;
	private final BagOfAttributeExpType bagType;

	private AnyURIType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}

	public AnyURIExp fromAny(Object any) {
		Preconditions.checkArgument(isConvertibleFrom(any),
				"Value=\"%s\" of type=\"%s\" can't be converted to XACML \"%s\" type",
				any, any.getClass(), typeId);
		if(any instanceof String){
			return new AnyURIExp(URI.create((String)any));
		}
		return new AnyURIExp((URI)any);
	}
	
	@Override
	public AnyURIExp fromString(String v) {
		Preconditions.checkNotNull(v);
		URI u = URI.create(v).normalize();
		return new AnyURIExp(u);
	}
	
	@Override
	public String toString(AttributeExp v) {
		AnyURIExp anyUri = (AnyURIExp)v;
		return anyUri.getValue().toString();
	}
	
	@Override
	public AttributeValueType toXacml30(AttributeExp v) {
		AttributeValueType xacml = new AttributeValueType();
		xacml.setDataType(v.getType().getDataTypeId());
		xacml.getContent().add(toString(v));
		return xacml;
	}

	@Override
	public AnyURIExp fromXacml30(AttributeValueType v) {
		Preconditions.checkArgument(v.getDataType().equals(getDataTypeId()));
		return new AnyURIExp(URI.create((String)v.getContent().get(0)));
	}

	public boolean isConvertibleFrom(Object any) {
		return URI.class.isInstance(any) || String.class.isInstance(any);
	}

	@Override
	public String getDataTypeId() {
		return typeId;
	}

	@Override
	public BagOfAttributeExp.Builder bag(){
		return new BagOfAttributeExp.Builder(this);
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
	public BagOfAttributeExp bagOf(Iterable<AttributeExp> values) {
		return bagType.create(values);
	}
	
	public BagOfAttributeExp bagOf(String ...values) {
		ImmutableList.Builder<AttributeExp> b = ImmutableList.builder();
		for(String v : values){
			b.add(fromAny(v));
		}
		return bagOf(b.build());
	}
	
	public BagOfAttributeExp bagOf(URI ...values) {
		ImmutableList.Builder<AttributeExp> b = ImmutableList.builder();
		for(URI v : values){
			b.add(fromAny(v));
		}
		return bagOf(b.build());
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
