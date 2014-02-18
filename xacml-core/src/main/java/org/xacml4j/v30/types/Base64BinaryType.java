package org.xacml4j.v30.types;

import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.BagOfAttributeExpType;
import org.xacml4j.v30.BinaryValue;

import com.google.common.base.Preconditions;

public enum Base64BinaryType implements AttributeExpType, TypeToString, TypeToXacml30
{
	BASE64BINARY("http://www.w3.org/2001/XMLSchema#base64Binary");

	private final String typeId;
	private final BagOfAttributeExpType bagType;

	private Base64BinaryType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}
	
	private boolean isConvertibleFrom(Object any) {
		return byte[].class.isInstance(any) || String.class.isInstance(any) ||
				BinaryValue.class.isInstance(any);
	}

	public Base64BinaryExp create(Object any){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertibleFrom(any),
				"Value=\"%s\" of type=\"%s\" can't be converted to XACML \"%s\" type",
				any, any.getClass(), typeId);
		if(String.class.isInstance(any)){
			return fromString((String)any);
		}
		if(byte[].class.isInstance(any)){
			return new Base64BinaryExp(BinaryValue.fromBytes((byte[])any));
		}
		return new Base64BinaryExp((BinaryValue)any);
	}

	@Override
	public Base64BinaryExp fromString(String v) {
		Preconditions.checkNotNull(v);
		return new Base64BinaryExp(BinaryValue.fromBase64Encoded(v));
	}
	
	@Override
	public String toString(AttributeExp v) {
		Preconditions.checkNotNull(v);
		Base64BinaryExp base64Value = (Base64BinaryExp)v;
		return base64Value.getValue().toBase64Encoded();
	}
	
	@Override
	public AttributeValueType toXacml30(Types types, AttributeExp v) {
		AttributeValueType xacml = new AttributeValueType();
		xacml.setDataType(v.getType().getDataTypeId());
		xacml.getContent().add(toString(v));
		return xacml;
	}

	@Override
	public AttributeExp fromXacml30(Types types, AttributeValueType v) {
		return create((String)v.getContent().get(0));
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
	public BagOfAttributeExp.Builder bag(){
		return new BagOfAttributeExp.Builder(this);
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
	public BagOfAttributeExp emptyBag() {
		return bagType.createEmpty();
	}

	@Override
	public String toString(){
		return typeId;
	}
}
