package org.xacml4j.v30.types;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import org.xacml4j.v30.DayTimeDuration;

import com.google.common.base.Preconditions;

public final class DayTimeDurationExp extends BaseAttributeExp<DayTimeDuration>
	implements Comparable<DayTimeDurationExp>
{
	private static final long serialVersionUID = -3264977978603429807L;

	public DayTimeDurationExp(Duration value) {
		super(DayTimeDurationType.DAYTIMEDURATION, new DayTimeDuration(value));
		Preconditions.checkArgument(!value.isSet(DatatypeConstants.YEARS) &&
				!value.isSet(DatatypeConstants.MONTHS));
	}
	
	DayTimeDurationExp(DayTimeDurationType type,
			DayTimeDuration value) {
		super(type, value);
	}

	@Override
	public int compareTo(DayTimeDurationExp o) {
		return getValue().compareTo(o.getValue());
	}
}

