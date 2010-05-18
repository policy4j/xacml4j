package com.artagon.xacml.v3.policy.type;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.v3.policy.spi.type.BaseAttributeType;
import com.artagon.xacml.v3.policy.type.DateTimeType.DateTimeValue;
import com.google.common.base.Preconditions;

final class DateTimeTypeImpl extends BaseAttributeType<DateTimeValue> implements DateTimeType
{
	private DatatypeFactory xmlDataTypesFactory;
	
	public DateTimeTypeImpl(String typeId)
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
	public DateTimeValue fromXacmlString(String v, Object ...params) {
		Preconditions.checkNotNull(v);
		XMLGregorianCalendar dateTime = xmlDataTypesFactory.newXMLGregorianCalendar(v);
		// XACML default time zone is UTC
		if(dateTime.getTimezone() == 
			DatatypeConstants.FIELD_UNDEFINED){
			dateTime.setTimezone(0);
		}
		return new DateTimeValue(this, validateXmlDateTime(dateTime));
	}
	
	@Override
	public DateTimeValue create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" " +
				"can't ne converted to XACML \"dateTime\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		XMLGregorianCalendar dateTime = validateXmlDateTime((XMLGregorianCalendar)any);
		// XACML default time zone is UTC
		if(dateTime.getTimezone() == 
			DatatypeConstants.FIELD_UNDEFINED){
			dateTime.setTimezone(0);
		}
		return new DateTimeValue(this, dateTime);
	}
	
	private XMLGregorianCalendar validateXmlDateTime(XMLGregorianCalendar dateTime){
		if(!dateTime.getXMLSchemaType().equals(DatatypeConstants.DATETIME)){
			throw new IllegalArgumentException(String.format("Given value=\"%s\" does " +
					"not represent type=\"%s\"", dateTime.toXMLFormat(), DatatypeConstants.DATETIME));
		}
		return dateTime;
	}
}
