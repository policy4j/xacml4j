package com.artagon.xacml.policy.type;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.artagon.xacml.DataTypes;
import com.artagon.xacml.policy.BaseAttributeDataType;
import com.artagon.xacml.policy.type.DateTimeType.DateTimeValue;
import com.artagon.xacml.util.Preconditions;

final class DateTimeTypeImpl extends BaseAttributeDataType<DateTimeValue> implements DateTimeType
{
	private DateTimeFormatter formatter;
	
	public DateTimeTypeImpl(){
		super(DataTypes.DATETIME, DateTime.class);
		this.formatter = ISODateTimeFormat.basicDateTime();
	}

	@Override
	public DateTimeValue fromXacmlString(String v) {
		Preconditions.checkNotNull(v);
		return new DateTimeValue(this, formatter.parseDateTime(v));
	}
	
	@Override
	public DateTimeValue create(Object any){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"dateTime\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		return new DateTimeValue(this, (DateTime)any);
	}
}
