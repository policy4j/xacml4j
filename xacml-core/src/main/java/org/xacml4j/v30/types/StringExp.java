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

public final class StringExp extends BaseAttributeExp<String>
{
	private static final long serialVersionUID = 657672949137533611L;

	private StringExp(String value) {
		super(XacmlTypes.STRING, value);
	}

	/**
	 * Creates {@link StringExp} from given string instance
	 *
	 * @param v a string value
	 * @return {@link StringExp}
	 * @exception IllegalArgumentException if given
	 * string value is null or empty
	 */
	public static StringExp valueOf(String v){
		Preconditions.checkArgument(!Strings.isNullOrEmpty(v));
		return new StringExp(v);
	}

	/**
	 * Delegates to {@link XacmlTypes#STRING#emptyBag()}
	 *
	 * @return {@link BagOfAttributeExp} empty bag
	 */
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.STRING.emptyBag();
	}

	/**
	 * Delegates to {@link XacmlTypes#STRING#bag()}
	 *
	 * @return {@link BagOfAttributeExp} empty bag
	 */
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.STRING.bag();
	}

	public boolean equalsIgnoreCase(StringExp v){
		return getValue().equalsIgnoreCase(v.getValue());
	}

	public StringExp concat(StringExp v){
		return StringExp.valueOf(getValue() + v);
	}

	public StringExp trim(){
		return StringExp.valueOf(getValue().trim());
	}

	public boolean startsWith(StringExp v){
		return  getValue().startsWith(v.getValue());
	}

	public boolean contains(StringExp v){
		return  getValue().contains(v.getValue());
	}

	public boolean endsWith(StringExp v){
		return  getValue().endsWith(v.getValue());
	}

	public StringExp toLowerCase(){
		return StringExp.valueOf(getValue().toLowerCase());
	}

	public StringExp toUpperCase(){
		return StringExp.valueOf(getValue().toUpperCase());
	}
}

