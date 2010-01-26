package com.artagon.xacml.v3.policy.type;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.type.DateTimeType.DateTimeValue;

final class DateTimeTypeImpl extends BaseAttributeType<DateTimeValue> implements DateTimeType
{
	private DatatypeFactory xmlDataTypesFactory;
	
	public DateTimeTypeImpl(String typeId)
	{
		super(typeId, XMLGregorianCalendar.class);
		try{
			this.xmlDataTypesFactory = DatatypeFactory.newInstance();
		}catch(DatatypeConfigurationException e){
			throw new IllegalStateException(e);
		}
	}

	@Override
	public DateTimeValue fromXacmlString(String v) {
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
	public DateTimeValue create(Object any){
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
