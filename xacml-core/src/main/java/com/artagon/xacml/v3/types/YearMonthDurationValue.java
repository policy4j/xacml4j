package com.artagon.xacml.v3.types;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import com.google.common.base.Preconditions;

public final class YearMonthDurationValue extends BaseDurationValue
{
	YearMonthDurationValue(YearMonthDurationType type, 
			Duration value) {
		super(type, value);
		Preconditions.checkArgument(
				value.getXMLSchemaType() == DatatypeConstants.DURATION_YEARMONTH);
	}
}
