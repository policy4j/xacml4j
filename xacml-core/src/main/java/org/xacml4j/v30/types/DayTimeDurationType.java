package org.xacml4j.v30.types;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.BagOfAttributeExpType;
import org.xacml4j.v30.DayTimeDuration;
import org.xacml4j.v30.XacmlSyntaxException;

import com.google.common.base.Preconditions;


public enum DayTimeDurationType implements 
AttributeExpType, TypeToString, TypeToXacml30
{
	DAYTIMEDURATION("http://www.w3.org/2001/XMLSchema#dayTimeDuration");

	private final String typeId;
	private final BagOfAttributeExpType bagType;

	private DayTimeDurationType(String typeId)
	{
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}

	public boolean isConvertibleFrom(Object any) {
		return Duration.class.isInstance(any) || String.class.isInstance(any);
	}

	public DayTimeDurationExp create(Object any){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertibleFrom(any),
				"Value=\"%s\" of type=\"%s\" can't be converted to XACML \"%s\" type",
				any, any.getClass(), typeId);
		if(String.class.isInstance(any)){
			return fromString((String)any);
		}
		return new DayTimeDurationExp(validate((Duration)any));
	}

	private Duration validate(Duration duration){
		if(!(duration.isSet(DatatypeConstants.YEARS)
				&& duration.isSet(DatatypeConstants.MONTHS))){
			return duration;
		}
		throw new IllegalArgumentException("Invalid duration");
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
	public DayTimeDurationExp fromXacml30(Types types, AttributeValueType v) {
		if(v.getContent().size() > 0){
			return create((String)v.getContent().get(0));
		}
		throw new XacmlSyntaxException(
				"No content found for the attribute value");
	}

	@Override
	public String toString(AttributeExp exp) {
		DayTimeDurationExp v = (DayTimeDurationExp)exp;
		return v.getValue().toString();
	}

	@Override
	public DayTimeDurationExp fromString(String v) {
		return new DayTimeDurationExp(this, DayTimeDuration.parse(v));
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

