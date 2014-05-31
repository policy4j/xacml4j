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


public final class IntegerExp
	extends BaseAttributeExp<Long>
{
	private static final long serialVersionUID = 6654857010399020496L;

	private IntegerExp(Long value) {
		super(XacmlTypes.INTEGER, value);
	}

	public static IntegerExp valueOf(Number value){
		return new IntegerExp(value.longValue());
	}

	public static IntegerExp valueOf(String v){
		Preconditions.checkNotNull(v);
		if ((v.length() >= 1) &&
        		(v.charAt(0) == '+')){
			v = v.substring(1);
		}
		return new IntegerExp(Long.parseLong(v));
	}

	public StringExp toStringExp(){
		return StringExp.valueOf(getValue().toString());
	}

	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.INTEGER.emptyBag();
	}

	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.INTEGER.bag();
	}

	public IntegerExp add(IntegerExp d){
		return  new IntegerExp(getValue() + d.getValue());
	}

	public IntegerExp subtract(IntegerExp d){
		return  new IntegerExp(getValue() - d.getValue());
	}

	public IntegerExp multiply(IntegerExp d){
		return  new IntegerExp(getValue() * d.getValue());
	}

	public DoubleExp divide(IntegerExp d){
		Preconditions.checkArgument(d.getValue() != null);
		return DoubleExp.valueOf(getValue() / d.getValue());
	}

	public IntegerExp mod(IntegerExp d){
		Preconditions.checkArgument(d.getValue() != null);
		return IntegerExp.valueOf(getValue() % d.getValue());
	}
}

