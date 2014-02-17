package org.xacml4j.v30.types;

import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.BagOfAttributeExpType;

import com.google.common.base.Preconditions;

public enum DoubleType implements AttributeExpType, TypeToString, TypeToXacml30
{
	DOUBLE("http://www.w3.org/2001/XMLSchema#double");

	private final String typeId;
	private final BagOfAttributeExpType bagType;

	private DoubleType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}

	public boolean isConvertibleFrom(Object any) {
		return Double.class.isInstance(any) || Integer.class.isInstance(any) ||
		Short.class.isInstance(any) || Byte.class.isInstance(any) ||
		Float.class.isInstance(any) || Long.class.isInstance(any)
		|| String.class.isInstance(any);
	}
	
	public DoubleExp create(Object any){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertibleFrom(any),
				"Value=\"%s\" of type=\"%s\" can't be converted to XACML \"%s\" type",
				any, any.getClass(), typeId);
		if(String.class.isInstance(any)){
			return fromString((String)any);
		}
		if(Byte.class.isInstance(any)){
			return new DoubleExp(((Byte)any).doubleValue());
		}
		if(Short.class.isInstance(any)){
			return new DoubleExp(((Short)any).doubleValue());
		}
		if(Integer.class.isInstance(any)){
			return new DoubleExp(((Integer)any).doubleValue());
		}
		if(Float.class.isInstance(any)){
			return new DoubleExp(((Float)any).doubleValue());
		}
		if(Long.class.isInstance(any)){
			return new DoubleExp(((Long)any).doubleValue());
		}
		return new DoubleExp((Double)any);
	}
	
	@Override
	public AttributeValueType toXacml30(Types types, AttributeExp v) {
		AttributeValueType xacml = new AttributeValueType();
		xacml.setDataType(v.getType().getDataTypeId());
		xacml.getContent().add(toString(v));
		return xacml;
	}

	@Override
	public DoubleExp fromXacml30(Types types, AttributeValueType v) {
		Preconditions.checkArgument(v.getDataType().equals(getDataTypeId()));
		return create((String)v.getContent().get(0));
	}

	@Override
	public String toString(AttributeExp exp) {
		DoubleExp v = (DoubleExp)exp.getValue();
		return v.getValue().toString();
	}

	@Override
	public DoubleExp fromString(String v) {
		Preconditions.checkNotNull(v);
		 if (v.endsWith("INF")) {
	            int infIndex = v.lastIndexOf("INF");
	            v = v.substring(0, infIndex) + "Infinity";
	     }
		 return new DoubleExp(Double.parseDouble(v));
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
	public BagOfAttributeExp.Builder bag(){
		return new BagOfAttributeExp.Builder(this);
	}

	@Override
	public BagOfAttributeExp bagOf(Iterable<AttributeExp> values) {
		return bagType.create(values);
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
