package org.xacml4j.v30.types;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import org.xacml4j.v30.YearMonthDuration;

import com.google.common.base.Preconditions;

public final class YearMonthDurationExp
	extends BaseAttributeExp<YearMonthDuration>
	implements Comparable<YearMonthDurationExp>
{
	private static final long serialVersionUID = 6510264772808336009L;

	public YearMonthDurationExp(
			Duration value) {
		this(new YearMonthDuration(value));
		Preconditions.checkArgument(
				value.getXMLSchemaType() == DatatypeConstants.DURATION_YEARMONTH);
	}

	public YearMonthDurationExp(
			YearMonthDuration value) {
		super(YearMonthDurationType.YEARMONTHDURATION, value);
	}

	@Override
	public int compareTo(YearMonthDurationExp o) {
		return getValue().compareTo(o.getValue());
	}

	public YearMonthDurationExp add(YearMonthDurationExp d){
		return new YearMonthDurationExp(getValue().add(d.getValue()));
	}

	public YearMonthDurationExp substract(YearMonthDurationExp d){
		return new YearMonthDurationExp(getValue().substract(d.getValue()));
	}
}
