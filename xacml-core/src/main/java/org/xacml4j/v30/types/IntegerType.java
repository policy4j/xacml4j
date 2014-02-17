package org.xacml4j.v30.types;

import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.BagOfAttributeExpType;

import com.google.common.base.Preconditions;

public enum IntegerType implements AttributeExpType, TypeToString, TypeToXacml30
{
	INTEGER("http://www.w3.org/2001/XMLSchema#integer");

	private final String typeId;
	private final BagOfAttributeExpType bagType;

	private IntegerType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}

	private boolean isConvertibleFrom(Object any) {
		return Long.class.isInstance(any) || Integer.class.isInstance(any) ||
		Short.class.isInstance(any) || Byte.class.isInstance(any) ||
		String.class.isInstance(any);
	}
	
	public IntegerExp create(Object any){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertibleFrom(any),
				"Value=\"%s\" of type=\"%s\" can't be converted to XACML \"%s\" type",
				any, any.getClass(), typeId);
		if(String.class.isInstance(any)){
			return fromString((String)any);
		}
		if(Byte.class.isInstance(any)){
			return new IntegerExp(this, ((Byte)any).longValue());
		}
		if(Short.class.isInstance(any)){
			return new IntegerExp(this, ((Short)any).longValue());
		}
		if(Integer.class.isInstance(any)){
			return new IntegerExp(this, ((Integer)any).longValue());
		}
		return new IntegerExp(this, (Long)any);
	}

	@Override
	public AttributeValueType toXacml30(AttributeExp v) {
		AttributeValueType xacml = new AttributeValueType();
		xacml.setDataType(v.getType().getDataTypeId());
		xacml.getContent().add(toString(v));
		return xacml;
	}

	@Override
	public IntegerExp fromXacml30(AttributeValueType v) {
		Preconditions.checkArgument(v.getDataType().equals(getDataTypeId()));
		return fromString((String)v.getContent().get(0));
	}

	@Override
	public String toString(AttributeExp exp) {
		IntegerExp v = (IntegerExp)exp.getValue();
		return v.getValue().toString();
	}

	@Override
	public IntegerExp fromString(String v) {
        Preconditions.checkNotNull(v);
		if ((v.length() >= 1) &&
        		(v.charAt(0) == '+')){
			v = v.substring(1);
		}
		return new IntegerExp(this, Long.valueOf(v));
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
	public BagOfAttributeExp bagOf(Iterable<AttributeExp> values) {
		return bagType.create(values);
	}

	@Override
	public BagOfAttributeExp.Builder bag(){
		return new BagOfAttributeExp.Builder(this);
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
