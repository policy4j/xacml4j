package com.artagon.xacml.v3.types;


import java.util.Collection;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.BagOfAttributeValuesType;
import com.google.common.base.Preconditions;

public enum DateType implements AttributeValueType
{
	DATE("http://www.w3.org/2001/XMLSchema#date");
	
	private DatatypeFactory xmlDataTypesFactory;

	private String typeId;
	private BagOfAttributeValuesType bagType;

	private DateType(String typeId) 
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
	public DateValue fromXacmlString(String v, Object ...params) {
		Preconditions.checkNotNull(v);
		XMLGregorianCalendar dateTime = xmlDataTypesFactory.newXMLGregorianCalendar(v);
		// XACML default time zone is UTC
		if(dateTime.getTimezone() == 
			DatatypeConstants.FIELD_UNDEFINED){
			dateTime.setTimezone(0);
		}
		return new DateValue(this, validateXmlDate(dateTime));
	}
	
	@Override
	public DateValue create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" " +
				"can't ne converted to XACML \"date\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		if(GregorianCalendar.class.isInstance(any)){
			XMLGregorianCalendar date = xmlDataTypesFactory.newXMLGregorianCalendar((GregorianCalendar)any);
			return new DateValue(this, xmlDataTypesFactory.newXMLGregorianCalendarDate(
					date.getYear(), date.getMonth(), date.getDay(), date.getTimezone()));
		}
		XMLGregorianCalendar date = validateXmlDate((XMLGregorianCalendar)any);
		// XACML default time zone is UTC
		if(date.getTimezone() == 
			DatatypeConstants.FIELD_UNDEFINED){
			date.setTimezone(0);
		}
		return new DateValue(this, date);
	}
	
	private XMLGregorianCalendar validateXmlDate(XMLGregorianCalendar dateTime){
		if(!dateTime.getXMLSchemaType().equals(DatatypeConstants.DATE)){
			throw new IllegalArgumentException(String.format("Given value=\"%s\" does " +
					"not represent type=\"%s\"", dateTime.toXMLFormat(), DatatypeConstants.DATETIME));
		}
		return dateTime;
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
}
