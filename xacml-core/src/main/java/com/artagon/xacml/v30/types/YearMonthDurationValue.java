package com.artagon.xacml.v30.types;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import com.google.common.base.Preconditions;

public final class YearMonthDurationValue extends BaseDurationValue
{
	private static final long serialVersionUID = 6510264772808336009L;

	YearMonthDurationValue(YearMonthDurationType type, 
			Duration value) {
		super(type, value);
		Preconditions.checkArgument(
				value.getXMLSchemaType() == DatatypeConstants.DURATION_YEARMONTH);
	}
}
