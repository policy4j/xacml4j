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

	public YearMonthDurationExp subtract(YearMonthDurationExp d){
		return new YearMonthDurationExp(getValue().subtract(d.getValue()));
	}

	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.YEARMONTHDURATION.emptyBag();
	}

	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.YEARMONTHDURATION.bag();
	}
}
