
package com.artagon.xacml.v3.types;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.v3.types.TimeType.TimeValue;
import com.google.common.base.Preconditions;

final class TimeTypeImpl extends BaseAttributeType<TimeValue> implements TimeType
{
	private DatatypeFactory xmlDataTypesFactory;
	
	public TimeTypeImpl(String typeId)
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
}

