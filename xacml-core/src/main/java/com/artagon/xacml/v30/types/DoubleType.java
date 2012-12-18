package com.artagon.xacml.v30.types;

import java.util.Collection;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.BagOfAttributeExp;
import com.artagon.xacml.v30.BagOfAttributeExpType;
import com.google.common.base.Preconditions;

public enum DoubleType implements AttributeExpType
{
	DOUBLE("http://www.w3.org/2001/XMLSchema#double");

	private String typeId;
	private BagOfAttributeExpType bagType;

	private DoubleType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}

	public boolean isConvertableFrom(Object any) {
		return Double.class.isInstance(any) || Integer.class.isInstance(any) ||
		Short.class.isInstance(any) || Byte.class.isInstance(any) ||
		Float.class.isInstance(any) || Long.class.isInstance(any)
		|| String.class.isInstance(any);
	}


	@Override
	public DoubleExp create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"double\" type",
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		if(Byte.class.isInstance(any)){
			return new DoubleExp(this, ((Byte)any).doubleValue());
		}
		if(Short.class.isInstance(any)){
			return new DoubleExp(this, ((Short)any).doubleValue());
		}
		if(Integer.class.isInstance(any)){
			return new DoubleExp(this, ((Integer)any).doubleValue());
		}
		if(Float.class.isInstance(any)){
			return new DoubleExp(this, ((Float)any).doubleValue());
		}
		if(Long.class.isInstance(any)){
			return new DoubleExp(this, ((Long)any).doubleValue());
		}
		return new DoubleExp(this, (Double)any);
	}

	@Override
	public DoubleExp fromXacmlString(String v, Object ...params) {

        if (v.endsWith("INF")) {
            int infIndex = v.lastIndexOf("INF");
            v = v.substring(0, infIndex) + "Infinity";
        }
        return new DoubleExp(this, Double.parseDouble(v));
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

	public BagOfAttributeExp.Builder bag(){
		return new BagOfAttributeExp.Builder(this);
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