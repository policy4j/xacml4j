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

import org.xacml4j.v30.BagOfAttributeExp;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public final class DoubleExp extends BaseAttributeExp<Double>
{
	private static final long serialVersionUID = -3689668541615314228L;

	private DoubleExp(Double value) {
		super(XacmlTypes.DOUBLE, value);
	}

	public static DoubleExp valueOf(Number value){
		Preconditions.checkNotNull(value);
		return new DoubleExp(value.doubleValue());
	}

	public static DoubleExp valueOf(String v){
		Preconditions.checkArgument(!Strings.isNullOrEmpty(v));
		 if (v.endsWith("INF")) {
	            int infIndex = v.lastIndexOf("INF");
	            v = v.substring(0, infIndex) + "Infinity";
	     }
		 return new DoubleExp(Double.parseDouble(v));
	}

	public StringExp toStringExp(){
		return StringExp.valueOf(getValue().toString());
	}

	public DoubleExp add(DoubleExp d){
		return  new DoubleExp(getValue() + d.getValue());
	}

	public DoubleExp subtract(DoubleExp d){
		return  new DoubleExp(getValue() - d.getValue());
	}

	public DoubleExp multiply(DoubleExp d){
		return  new DoubleExp(getValue() * d.getValue());
	}

	public DoubleExp divide(DoubleExp d){
		return  new DoubleExp(getValue() / d.getValue());
	}

	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.DOUBLE.emptyBag();
	}

	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.DOUBLE.bag();
	}
}

