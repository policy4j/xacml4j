package com.artagon.xacml.v30.types;

import java.util.Collection;

import com.artagon.xacml.v30.AttributeValue;
import com.artagon.xacml.v30.AttributeValueType;
import com.artagon.xacml.v30.BagOfAttributeValues;
import com.artagon.xacml.v30.BagOfAttributeValuesType;
import com.google.common.base.Preconditions;

public enum DoubleType implements AttributeValueType
{
	DOUBLE("http://www.w3.org/2001/XMLSchema#double");
	
	private String typeId;
	private BagOfAttributeValuesType bagType;
	
	private DoubleType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeValuesType(this);
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