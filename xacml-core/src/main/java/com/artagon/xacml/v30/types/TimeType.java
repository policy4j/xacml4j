package com.artagon.xacml.v30.types;

import java.util.Collection;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.v30.AttributeValue;
import com.artagon.xacml.v30.AttributeValueType;
import com.artagon.xacml.v30.BagOfAttributeValues;
import com.artagon.xacml.v30.BagOfAttributeValuesType;
import com.google.common.base.Preconditions;

public enum TimeType implements AttributeValueType
{	
	TIME("http://www.w3.org/2001/XMLSchema#time");
	
	private DatatypeFactory xmlDataTypesFactory;

	private String typeId;
	private BagOfAttributeValuesType bagType;

	private TimeType(String typeId) 
	{
		this.typeId = typeId;
		this.bagType = new BagOfAttributeValuesType(this);
		try {
			this.xmlDataTypesFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace(System.err);
		}
	}
	
	@Override
	public boolean isConvertableFrom(Object any) {
		return XMLGregorianCalendar.class.isInstance(any) || String.class.isInstance(any) ||
		GregorianCalendar.class.isInstance(any);
	}
	
	@Override
	public TimeValue fromXacmlString(String v, Object ...params) {
		Preconditions.checkNotNull(v);
		XMLGregorianCalendar dateTime = xmlDataTypesFactory.newXMLGregorianCalendar(v);
		return new TimeValue(this, validateXmlTime(dateTime));
	}
	
	@Override
	public TimeValue create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" " +
				"can't ne converted to XACML \"dateTime\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		if(GregorianCalendar.class.isInstance(any)){
			XMLGregorianCalendar dt = xmlDataTypesFactory.newXMLGregorianCalendar((GregorianCalendar)any);
			return new TimeValue(this, xmlDataTypesFactory.newXMLGregorianCalendarTime(
					dt.getHour(), dt.getMinute(), 
					dt.getSecond(), dt.getMillisecond(), 
					dt.getTimezone()));
		}
		return new TimeValue(this, validateXmlTime(((XMLGregorianCalendar)any)));
	}
	
	private XMLGregorianCalendar validateXmlTime(XMLGregorianCalendar time){
		if(!time.getXMLSchemaType().equals(DatatypeConstants.TIME)){
			throw new IllegalArgumentException(String.format("Given value=\"%s\" does " +
					"not represent type=\"%s\"", time.toXMLFormat(), DatatypeConstants.TIME));
		}
		return time;
	}
	
	@Override
	public String getDataTypeId() {
		return typeId;
	}

	@Override
	public BagOfAttributeValuesType bagType() {
		return bagType;
	}

	@Override
	public BagOfAttributeValues bagOf(AttributeValue... values) {
		return bagType.create(values);
	}

	@Override
	public BagOfAttributeValues bagOf(Collection<AttributeValue> values) {
		return bagType.create(values);
	}

	@Override
	public BagOfAttributeValues emptyBag() {
		return bagType.createEmpty();
	}
	
	@Override
	public String toString(){
		return typeId;
	}
}


