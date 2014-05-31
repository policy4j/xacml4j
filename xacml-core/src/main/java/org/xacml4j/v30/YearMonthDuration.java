package org.xacml4j.v30;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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

	@Override
	protected YearMonthDuration makeDuration(Duration d){
		return new YearMonthDuration(d);
	}
}
