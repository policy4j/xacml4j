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

public final class BooleanExp extends
	BaseAttributeExp<Boolean>
{
	private static final long serialVersionUID = -421397689674188254L;

	public final static BooleanExp FALSE = new BooleanExp(Boolean.FALSE);
	public final static BooleanExp TRUE = new BooleanExp(Boolean.TRUE);
	
	private BooleanExp(Boolean value) {
		super(XacmlTypes.BOOLEAN, value);
	}
	
	public static BooleanExp create(boolean val){
		return val?TRUE:FALSE;
	}
	
	public static BooleanExp valueOf(String v){
		return Boolean.parseBoolean(v)?BooleanExp.TRUE:BooleanExp.FALSE;
	}
	
	public static BooleanExp valueOf(Boolean v){
		Preconditions.checkNotNull(v);
		return v?BooleanExp.TRUE:BooleanExp.FALSE;
	}
	
	public StringExp toStringExp(){
		return StringExp.valueOf(Boolean.toString(getValue()));
	}
	
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.BOOLEAN.emptyBag();
	}
	
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.BOOLEAN.bag();
	}
}
