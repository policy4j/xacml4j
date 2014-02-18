package org.xacml4j.v30.types;

import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.BagOfAttributeExpType;
import org.xacml4j.v30.XacmlSyntaxException;

import com.google.common.base.Preconditions;

public enum BooleanType implements AttributeExpType, TypeToString, TypeToXacml30
{
	BOOLEAN("http://www.w3.org/2001/XMLSchema#boolean");

	private final String typeId;
	private final BagOfAttributeExpType bagType;

	private BooleanType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}
	
	public boolean isConvertibleFrom(Object any) {
		return Boolean.class.isInstance(any) || String.class.isInstance(any);
	}

	public BooleanExp create(Object any){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertibleFrom(any),
				"Value=\"%s\" of type=\"%s\" can't be converted to XACML \"%s\" type",
				any, any.getClass(), typeId);
		if(String.class.isInstance(any)){
			return fromString((String)any);
		}
		return ((Boolean)any)?BooleanExp.TRUE:BooleanExp.FALSE;
	}

	@Override
	public BooleanExp fromString(String v) {
		Preconditions.checkNotNull(v);
		return Boolean.parseBoolean(v)?BooleanExp.TRUE:BooleanExp.FALSE;
	}
	
	@Override
	public String toString(AttributeExp v) {
		Preconditions.checkNotNull(v);
		BooleanExp boolVal = (BooleanExp)v;
		return boolVal.getValue().toString();
	}
	
	@Override
	public AttributeValueType toXacml30(Types types, AttributeExp v) {
		Preconditions.checkArgument(v.getType().equals(this));
		AttributeValueType xacml = new AttributeValueType();
		xacml.setDataType(v.getType().getDataTypeId());
		xacml.getContent().add(toString(v));
		return xacml;
	}

	@Override
	public AttributeExp fromXacml30(Types types, AttributeValueType v) {
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
