package com.artagon.xacml.v30.core;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import com.google.common.base.Preconditions;

public final class YearMonthDuration extends BaseDuration<YearMonthDuration>
{
	private static final long serialVersionUID = -1840184626138996986L;

	public YearMonthDuration(Duration v) {
		super(v);
		Preconditions.checkArgument(!(v.isSet(DatatypeConstants.DAYS) 
				|| v.isSet(DatatypeConstants.HOURS) 
				|| v.isSet(DatatypeConstants.MINUTES) 
				|| v.isSet(DatatypeConstants.SECONDS)), 
				"Given XML duration value=\"%s\" does " +
				"not represents YearMonth duration", v.toString());
	}
	
	public YearMonthDuration(boolean positive, int years, int  months) {
		this(df.newDurationYearMonth(positive, years, months));
	}
	
	public int getYears(){
		return getDuration().getYears();
	}
	
	public final int getMonths(){
		return getDuration().getMonths();
	}
	
	public static YearMonthDuration create(Object any){
		if(any instanceof YearMonthDuration){
			return (YearMonthDuration)any;
		}
		Duration d = parseDuration(any);
		return new YearMonthDuration(d);
	}
	
	protected YearMonthDuration makeDuration(Duration d){
		return new YearMonthDuration(d);
	}
}
