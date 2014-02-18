package org.xacml4j.v30.types;

import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.BagOfAttributeExpType;
import org.xacml4j.v30.BinaryValue;
import org.xacml4j.v30.XacmlSyntaxException;

import com.google.common.base.Preconditions;

public enum HexBinaryType implements AttributeExpType, 
TypeToString, TypeToXacml30
{
	HEXBINARY("http://www.w3.org/2001/XMLSchema#hexBinary");

	private final String typeId;
	private final BagOfAttributeExpType bagType;

	private HexBinaryType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}

	private boolean isConvertibleFrom(Object any) {
		return byte[].class.isInstance(any) || String.class.isInstance(any)
				|| BinaryValue.class.isInstance(any);
	}

	public HexBinaryExp create(Object any){
		Preconditions.checkNotNull(any);
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertibleFrom(any),
				"Value=\"%s\" of type=\"%s\" can't be converted to XACML \"%s\" type",
				any, any.getClass(), typeId);
		if(String.class.isInstance(any)){
			return new HexBinaryExp(BinaryValue.fromHexEncoded((String)any));
		}
		if(byte[].class.isInstance(any)){
			return new HexBinaryExp(BinaryValue.fromBytes((byte[])any));
		}
		return new HexBinaryExp((BinaryValue)any);
	}
	
	@Override
	public HexBinaryExp fromString(String v) {
		Preconditions.checkNotNull(v);
		return create(v);
	}
	
	@Override
	public String toString(AttributeExp v) {
		Preconditions.checkNotNull(v);
		HexBinaryExp base64Value = (HexBinaryExp)v;
		return base64Value.getValue().toHexEncoded();
	}
	
	@Override
	public AttributeValueType toXacml30(Types types, AttributeExp v) {
		AttributeValueType xacml = new AttributeValueType();
		xacml.setDataType(v.getType().getDataTypeId());
		xacml.getContent().add(toString(v));
		return xacml;
	}

	@Override
	public HexBinaryExp fromXacml30(Types types, AttributeValueType v) {
		if(v.getContent().size() > 0){
			return create((String)v.getContent().get(0));
		}
		throw new XacmlSyntaxException(
				"No content found for the attribute value");
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
