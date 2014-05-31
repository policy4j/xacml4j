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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.xacml4j.v30.BagOfAttributeExp;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Represents an XACML expression of {@link XacmlTypes#ANYURI} type.
 * 
 * @author Giedrius Trumpickas
 *
 */
public final class AnyURIExp extends BaseAttributeExp<URI>
{
	private static final long serialVersionUID = -1279561638068756670L;
	
	private AnyURIExp(URI value){
		super(XacmlTypes.ANYURI, value);
	}
	
	/**
	 * Creates an XACML expression of {@link XacmlTypes#ANYURI} 
	 * from a given string
	 * 
	 * @param v a string value
	 * @return {@link AnyURIExp} instance
	 */
	public static AnyURIExp valueOf(String v){
		Preconditions.checkArgument(!Strings.isNullOrEmpty(v));
		return new AnyURIExp(URI.create(v).normalize());
	}
	
	public static AnyURIExp valueOf(StringExp v){
		return valueOf(v.getValue());
	}
	
	public static AnyURIExp valueOf(URL v){
		try {
			return new AnyURIExp(v.toURI());
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public static AnyURIExp valueOf(URI v){
		Preconditions.checkNotNull(v);
		return new AnyURIExp(v);
	}
	
	public StringExp toStringExp(){
		return StringExp.valueOf(getValue().toString());
	}
	
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.ANYURI.emptyBag();
	}
	
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.ANYURI.bag();
	}
	
	public AnyURIExp normalize(){
		return new AnyURIExp(getValue().normalize());
	}
}

