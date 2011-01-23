package com.artagon.xacml.v30.types;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

public final class DateValue extends SimpleAttributeValue<XMLGregorianCalendar>
		implements Comparable<DateValue> 
{

	private static final long serialVersionUID = -79539790774966290L;

	DateValue(DateType type, XMLGregorianCalendar value) {
		super(type, value);
	}

	public DateValue add(YearMonthDurationValue duration) {
		return add(duration.getValue());
	}

	public DateValue subtract(YearMonthDurationValue duration) {
		return subtract(duration.getValue());
	}

	private DateValue add(Duration duration) {
		XMLGregorianCalendar dateTime = getValue();
		XMLGregorianCalendar copy = (XMLGregorianCalendar) dateTime.clone();
		copy.add(duration);
		return new DateValue((DateType) getType(), copy);
	}

	private DateValue subtract(Duration duration) {
		return add(duration.negate());
	}

	@Override
	public int compareTo(DateValue v) {
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
