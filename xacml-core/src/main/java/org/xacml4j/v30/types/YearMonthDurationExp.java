package org.xacml4j.v30.types;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.YearMonthDuration;

import com.google.common.base.Preconditions;

public final class YearMonthDurationExp
	extends BaseAttributeExp<YearMonthDuration>
	implements Comparable<YearMonthDurationExp>
{
	private static final long serialVersionUID = 6510264772808336009L;

	YearMonthDurationExp(
			Duration value) {
		this(new YearMonthDuration(value));
		Preconditions.checkArgument(
				value.getXMLSchemaType() == DatatypeConstants.DURATION_YEARMONTH);
	}

	YearMonthDurationExp(
			YearMonthDuration value) {
		super(XacmlTypes.YEARMONTHDURATION, value);
	}
	
	public static YearMonthDurationExp valueOf(String v){
		return new YearMonthDurationExp(YearMonthDuration.create(v));
	}
	
	public static YearMonthDurationExp valueOf(Duration v){
		return new YearMonthDurationExp(YearMonthDuration.create(v));
	}
	
	public static YearMonthDurationExp valueOf(YearMonthDuration v){
		return new YearMonthDurationExp(v);
	}
	
	public static YearMonthDurationExp valueOf(StringExp v){
		return valueOf(v.getValue());
	}
	
	public StringExp toStringExp(){
		return StringExp.valueOf(getValue().toXacmlString());
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
	
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.YEARMONTHDURATION.emptyBag();
	}
	
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.YEARMONTHDURATION.bag();
	}
}
