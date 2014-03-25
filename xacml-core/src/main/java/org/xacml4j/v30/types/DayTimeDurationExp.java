package org.xacml4j.v30.types;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.DayTimeDuration;

import com.google.common.base.Preconditions;

public final class DayTimeDurationExp extends BaseAttributeExp<DayTimeDuration>
	implements Comparable<DayTimeDurationExp>
{
	private static final long serialVersionUID = -3264977978603429807L;

	public DayTimeDurationExp(Duration value) {
		super(XacmlTypes.DAYTIMEDURATION, new DayTimeDuration(value));
		Preconditions.checkArgument(!value.isSet(DatatypeConstants.YEARS) &&
				!value.isSet(DatatypeConstants.MONTHS));
	}
	
	DayTimeDurationExp(DayTimeDuration value) {
		super(XacmlTypes.DAYTIMEDURATION, value);
	}
	
	public static DayTimeDurationExp valueOf(DayTimeDuration v){
		return new DayTimeDurationExp(v);
	}
	
	public static DayTimeDurationExp valueOf(Duration v){
		return new DayTimeDurationExp(v);
	}
	
	public static DayTimeDurationExp valueOf(String v){
		return new DayTimeDurationExp(DayTimeDuration.parse(v));
	}
	
	public static DayTimeDurationExp valueOf(StringExp v){
		return valueOf(v.getValue());
	}
	
	public StringExp toStringExp(){
		return StringExp.valueOf(getValue().toXacmlString());
	}

	@Override
	public int compareTo(DayTimeDurationExp o) {
		return getValue().compareTo(o.getValue());
	}
	
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.DAYTIMEDURATION.emptyBag();
	}
	
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.DAYTIMEDURATION.bag();
	}
}

