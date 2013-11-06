package org.xacml4j.v30.types;

import java.util.Collection;

import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.BagOfAttributeExpType;
import org.xacml4j.v30.BinaryValue;

import com.google.common.base.Preconditions;

public enum HexBinaryType implements AttributeExpType
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

	@Override
	public HexBinaryExp create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertibleFrom(any),
				"Value=\"%s\" of type=\"%s\" can't be converted to XACML \"%s\" type",
				any, any.getClass(), typeId);
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		if(byte[].class.isInstance(any)){
			return new HexBinaryExp(this, BinaryValue.fromBytes((byte[])any));
		}
		return new HexBinaryExp(this, (BinaryValue)any);
	}

	@Override
	public HexBinaryExp fromXacmlString(String v, Object ...params) {
		return new HexBinaryExp(this, BinaryValue.fromHexEncoded(v));
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
