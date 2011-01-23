package com.artagon.xacml.v30.types;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

public final class TimeValue extends SimpleAttributeValue<XMLGregorianCalendar>
		implements Comparable<TimeValue> 
{
	private static final long serialVersionUID = -8244143626423796791L;

	TimeValue(TimeType type, XMLGregorianCalendar value) {
		super(type, value);
	}

	@Override
	public int compareTo(TimeValue v) {
		int r = getValue().compare(v.getValue());
		if (r == DatatypeConstants.INDETERMINATE) {
			throw new IllegalArgumentException(String.format(
					"Can't compare a=\"%s\" with b=\"%s\", "
							+ "result is INDETERMINATE", getValue(),
					v.getValue()));
		}
		return r == DatatypeConstants.EQUAL ? 0
				: ((r == DatatypeConstants.GREATER) ? 1 : -1);
	}
}
