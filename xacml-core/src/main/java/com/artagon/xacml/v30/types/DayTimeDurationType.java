package com.artagon.xacml.v30.types;

import java.util.Collection;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.BagOfAttributeExp;
import com.artagon.xacml.v30.BagOfAttributeExpType;
import com.google.common.base.Preconditions;


public enum DayTimeDurationType
	implements AttributeExpType
{
	DAYTIMEDURATION("http://www.w3.org/2001/XMLSchema#dayTimeDuration");

	private DatatypeFactory xmlDataTypesFactory;

	private String typeId;
	private BagOfAttributeExpType bagType;

	private DayTimeDurationType(String typeId)
	{
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
		try {
			this.xmlDataTypesFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace(System.err);
		}
	}

	public boolean isConvertableFrom(Object any) {
		return Duration.class.isInstance(any) || String.class.isInstance(any);
	}

	@Override
	public DayTimeDurationExp fromXacmlString(String v, Object ...params) {
		Preconditions.checkNotNull(v);
		Duration dayTimeDuration = xmlDataTypesFactory.newDurationDayTime(v);
		return new DayTimeDurationExp(this, validate(dayTimeDuration));
	}

	@Override
	public DayTimeDurationExp create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" " +
				"can't ne converted to XACML \"date\" type",
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		return new DayTimeDurationExp(this, validate((Duration)any));
	}

	private Duration validate(Duration duration){
		if(!(duration.isSet(DatatypeConstants.YEARS)
				&& duration.isSet(DatatypeConstants.MONTHS))){
			return duration;
		}
		throw new IllegalArgumentException();
	}

	@Override
	public String getDataTypeId() {
		return typeId;
	}

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

