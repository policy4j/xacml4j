package com.artagon.xacml.v3.policy.type;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.type.DateType.DateValue;

final class DateTypeImpl extends BaseAttributeType<DateValue> implements DateType
{
	private DatatypeFactory xmlDataTypesFactory;
	
	public DateTypeImpl(String typeId)
	{
		super(typeId);
		try{
			this.xmlDataTypesFactory = DatatypeFactory.newInstance();
		}catch(DatatypeConfigurationException e){
			throw new IllegalStateException(e);
		}
	}
	
	@Override
	public boolean isConvertableFrom(Object any) {
		return XMLGregorianCalendar.class.isInstance(any) || String.class.isInstance(any);
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
}

