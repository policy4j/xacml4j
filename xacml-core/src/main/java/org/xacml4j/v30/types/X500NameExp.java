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

import javax.security.auth.x500.X500Principal;

import org.xacml4j.v30.BagOfAttributeExp;

public final class X500NameExp extends BaseAttributeExp<X500Principal>
{
	private static final long serialVersionUID = -609417077475809404L;

	X500NameExp(X500Principal value) {
		super(XacmlTypes.X500NAME, value);
	}
	
	public static X500NameExp valueOf(String v){
		return new X500NameExp(new X500Principal(v));
	}
	
	public static X500NameExp valueOf(StringExp v){
		return valueOf(v.getValue());
	}
	
	public static X500NameExp valueOf(X500Principal p){
		return new X500NameExp(p);
	}
	
	public StringExp toStringExp(){
		return StringExp.valueOf(getValue().toString());
	}
	
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.X500NAME.emptyBag();
	}
	
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.X500NAME.bag();
	}
}
