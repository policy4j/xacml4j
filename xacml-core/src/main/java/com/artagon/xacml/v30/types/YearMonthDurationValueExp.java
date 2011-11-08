package com.artagon.xacml.v30.types;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.core.YearMonthDuration;
import com.google.common.base.Preconditions;

public final class YearMonthDurationValueExp 
	extends BaseAttributeExpression<YearMonthDuration> 
	implements Comparable<YearMonthDurationValueExp>
{
	private static final long serialVersionUID = 6510264772808336009L;

	YearMonthDurationValueExp(
			AttributeExpType type, 
			Duration value) {
		this(type, new YearMonthDuration(value));
		Preconditions.checkArgument(
				value.getXMLSchemaType() == DatatypeConstants.DURATION_YEARMONTH);
	}
	
	YearMonthDurationValueExp(
			AttributeExpType type, 
			YearMonthDuration value) {
		super(type, value);
		Preconditions.checkArgument(type.getDataTypeId().equals(
				YearMonthDurationType.YEARMONTHDURATION.getDataTypeId()));
	}

	@Override
	public int compareTo(YearMonthDurationValueExp o) {
		return getValue().compareTo(o.getValue());
	}
	
	public YearMonthDurationValueExp add(YearMonthDurationValueExp d){
		return new YearMonthDurationValueExp(
				getType(),
				getValue().add(d.getValue()));
	}
	
	public YearMonthDurationValueExp substract(YearMonthDurationValueExp d){
		return new YearMonthDurationValueExp(
				getType(),
				getValue().substract(d.getValue()));
	}
}
