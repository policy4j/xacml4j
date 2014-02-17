package org.xacml4j.v30.types;

import java.util.GregorianCalendar;

import javax.xml.datatype.XMLGregorianCalendar;

import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.BagOfAttributeExpType;
import org.xacml4j.v30.Time;

import com.google.common.base.Preconditions;


public enum TimeType implements 
	AttributeExpType, TypeToString, TypeToXacml30
{
	TIME("http://www.w3.org/2001/XMLSchema#time");

	private final String typeId;
	private final BagOfAttributeExpType bagType;

	private TimeType(String typeId)
	{
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}

	public boolean isConvertibleFrom(Object any) {
		return XMLGregorianCalendar.class.isInstance(any)
				|| String.class.isInstance(any)
				|| GregorianCalendar.class.isInstance(any);
	}

	@Override
	public TimeExp fromString(String v) {
		return new TimeExp(this, Time.create(v));
	}
	
	@Override
	public AttributeValueType toXacml30(Types types, AttributeExp v) {
		AttributeValueType xacml = new AttributeValueType();
		xacml.setDataType(v.getType().getDataTypeId());
		xacml.getContent().add(toString(v));
		return xacml;
	}

	@Override
	public TimeExp fromXacml30(Types types, AttributeValueType v) {
		Preconditions.checkArgument(v.getDataType().equals(getDataTypeId()));
		return create((String)v.getContent().get(0));
	}
	
	@Override
	public String toString(AttributeExp exp) {
		TimeExp v = (TimeExp)exp;
		return v.getValue().toXacmlString();
	}

	public TimeExp create(Object any){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertibleFrom(any),
				"Value=\"%s\" of type=\"%s\" can't be converted to XACML \"%s\" type",
				any, any.getClass(), typeId);
		return new TimeExp(this, Time.create(any));
	}

	@Override
	public BagOfAttributeExp bagOf(Iterable<AttributeExp> values) {
		return new BagOfAttributeExp
				.Builder(this)
				.attributes(values)
				.build();
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
	public BagOfAttributeExp emptyBag() {
		return bagType.createEmpty();
	}

	@Override
	public String toString(){
		return typeId;
	}
}


