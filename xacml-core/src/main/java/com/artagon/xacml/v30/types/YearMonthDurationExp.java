package com.artagon.xacml.v30.types;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import com.artagon.xacml.v30.YearMonthDuration;
import com.artagon.xacml.v30.pdp.AttributeExpType;
import com.google.common.base.Preconditions;

public final class YearMonthDurationExp 
	extends BaseAttributeExp<YearMonthDuration> 
	implements Comparable<YearMonthDurationExp>
{
	private static final long serialVersionUID = 6510264772808336009L;

	YearMonthDurationExp(
			AttributeExpType type, 
			Duration value) {
		this(type, new YearMonthDuration(value));
		Preconditions.checkArgument(
				value.getXMLSchemaType() == DatatypeConstants.DURATION_YEARMONTH);
	}
	
	YearMonthDurationExp(
			AttributeExpType type, 
			YearMonthDuration value) {
		super(type, value);
		Preconditions.checkArgument(type.getDataTypeId().equals(
				YearMonthDurationType.YEARMONTHDURATION.getDataTypeId()));
	}

	@Override
	public int compareTo(YearMonthDurationExp o) {
		return getValue().compareTo(o.getValue());
	}
	
	public YearMonthDurationExp add(YearMonthDurationExp d){
		return new YearMonthDurationExp(
				getType(),
				getValue().add(d.getValue()));
	}
	
	public YearMonthDurationExp substract(YearMonthDurationExp d){
		return new YearMonthDurationExp(
				getType(),
				getValue().substract(d.getValue()));
	}
}
