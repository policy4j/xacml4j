package com.artagon.xacml.v3.types;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import com.google.common.base.Preconditions;

public final class DayTimeDurationValue extends BaseDurationValue
{
	DayTimeDurationValue(DayTimeDurationType type, 
			Duration value) {
		super(type, value);
		Preconditions.checkArgument(!value.isSet(DatatypeConstants.YEARS) && 
				!value.isSet(DatatypeConstants.MONTHS));
	}
}

