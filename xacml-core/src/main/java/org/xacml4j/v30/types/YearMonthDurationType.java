package org.xacml4j.v30.types;

import javax.xml.datatype.Duration;

import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.BagOfAttributeExpType;
import org.xacml4j.v30.YearMonthDuration;

import com.google.common.base.Preconditions;


public enum YearMonthDurationType implements AttributeExpType, 
TypeToString, TypeToXacml30
{
	YEARMONTHDURATION("http://www.w3.org/2001/XMLSchema#yearMonthDuration");

	private final String typeId;
	private final BagOfAttributeExpType bagType;

	private YearMonthDurationType(String typeId)
	{
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}

	public boolean isConvertibleFrom(Object any) {
		return any instanceof Duration || any instanceof String || any instanceof YearMonthDuration;
	}

	public YearMonthDurationExp fromString(String v)
	{
		Preconditions.checkNotNull(v);
		return new YearMonthDurationExp(this, YearMonthDuration.create(v));
	}

	public YearMonthDurationExp create(Object any){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertibleFrom(any),
				"Value=\"%s\" of type=\"%s\" can't be converted to XACML \"%s\" type",
				any, any.getClass(), typeId);
		return new YearMonthDurationExp(this, YearMonthDuration.create(any));
	}
	
	@Override
	public AttributeValueType toXacml30(AttributeExp v) {
		AttributeValueType xacml = new AttributeValueType();
		xacml.setDataType(v.getType().getDataTypeId());
		xacml.getContent().add(toString(v));
		return xacml;
	}

	@Override
	public YearMonthDurationExp fromXacml30(AttributeValueType v) {
		Preconditions.checkArgument(v.getDataType().equals(getDataTypeId()));
		return create((String)v.getContent().get(0));
	}
	
	@Override
	public String toString(AttributeExp exp) {
		YearMonthDurationExp v = (YearMonthDurationExp)exp;
		return v.getValue().toString();
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



