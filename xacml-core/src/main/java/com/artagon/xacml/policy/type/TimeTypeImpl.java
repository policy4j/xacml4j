
package com.artagon.xacml.policy.type;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.policy.BaseAttributeDataType;
import com.artagon.xacml.policy.type.TimeType.TimeValue;
import com.artagon.xacml.util.Preconditions;

final class TimeTypeImpl extends BaseAttributeDataType<TimeValue> implements TimeType
{
	private DatatypeFactory xmlDataTypesFactory;
	
	public TimeTypeImpl(String typeId)
	{
		super(typeId, XMLGregorianCalendar.class);
		try{
			this.xmlDataTypesFactory = DatatypeFactory.newInstance();
		}catch(DatatypeConfigurationException e){
			throw new IllegalStateException(e);
		}
	}

	@Override
	public TimeValue fromXacmlString(String v) {
		Preconditions.checkNotNull(v);
		XMLGregorianCalendar dateTime = xmlDataTypesFactory.newXMLGregorianCalendar(v);
		return new TimeValue(this, validateXmlTime(dateTime));
	}
	
	@Override
	public TimeValue create(Object any){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" " +
				"can't ne converted to XACML \"dateTime\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		return new TimeValue(this, validateXmlTime((XMLGregorianCalendar)any));
	}
	
	private XMLGregorianCalendar validateXmlTime(XMLGregorianCalendar time){
		if(!time.getXMLSchemaType().equals(DatatypeConstants.TIME)){
			throw new IllegalArgumentException(String.format("Given value=\"%s\" does " +
					"not represent type=\"%s\"", time.toXMLFormat(), DatatypeConstants.DATETIME));
		}
		return time;
	}
}

