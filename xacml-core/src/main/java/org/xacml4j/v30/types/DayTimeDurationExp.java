package org.xacml4j.v30.types;

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

