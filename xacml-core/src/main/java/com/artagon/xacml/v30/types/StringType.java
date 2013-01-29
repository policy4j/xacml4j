package com.artagon.xacml.v30.types;

import java.util.Collection;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.BagOfAttributeExp;
import com.artagon.xacml.v30.BagOfAttributeExpType;
import com.google.common.base.Preconditions;


public enum StringType implements AttributeExpType
{
	STRING("http://www.w3.org/2001/XMLSchema#string");

	private String typeId;
	private BagOfAttributeExpType bagType;

	private StringType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}

	public boolean isConvertableFrom(Object any) {
		return StringExp.class.isInstance(any) || String.class.isInstance(any);
	}

	@Override
	public StringExp fromXacmlString(String v, Object ...params) {
		return create(v);
	}

	@Override
	public StringExp create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"string\" type",
				any, any.getClass()));
		return new StringExp(this, (String)any);
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