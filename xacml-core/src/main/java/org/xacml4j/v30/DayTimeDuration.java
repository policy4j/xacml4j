package org.xacml4j.v30;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import com.google.common.base.Preconditions;

public final class DayTimeDuration
	extends BaseDuration<DayTimeDuration>
{
	private static final long serialVersionUID = -7792873011027722382L;

	public DayTimeDuration(Duration value) {
		super(value);
		Preconditions.checkArgument(!value.isSet(DatatypeConstants.YEARS) &&
				!value.isSet(DatatypeConstants.MONTHS));
	}

	public DayTimeDuration(boolean positive,
			int days,
			int hours,
			int minutes,
			int seconds) {
		this(df.newDurationDayTime(positive, days, hours, minutes, seconds));
	}

	public final int getDays(){
		return getDuration().getDays();
	}

	public final int getHours(){
		return getDuration().getHours();
	}

	public final int getMinutes(){
		return getDuration().getMinutes();
	}

	public final int getSeconds(){
		return getDuration().getSeconds();
	}

	/**
	 * Parses a given object to {@link DayTimeDuration} instance
	 * @param any an object to be parsed
	 * @return new instance
	 */
	public static DayTimeDuration parse(Object any){
		if(any instanceof DayTimeDuration){
			new DayTimeDuration(((DayTimeDuration)any).getDuration());
		}
		Duration d = parseDuration(any);
		return new DayTimeDuration(d);
	}

	@Override
	protected DayTimeDuration makeDuration(Duration d){
		return new DayTimeDuration(d);
	}
}
