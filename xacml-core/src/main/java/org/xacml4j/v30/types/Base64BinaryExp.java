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
import org.xacml4j.v30.BinaryValue;

public final class Base64BinaryExp extends BaseAttributeExp<BinaryValue>
{
	private static final long serialVersionUID = 3298986540546649848L;

	private Base64BinaryExp(BinaryValue value) {
		super(XacmlTypes.BASE64BINARY, value);
	}
	
	public static Base64BinaryExp valueOf(byte[] v){
		return new Base64BinaryExp(BinaryValue.valueOf(v));
	}
	
	public static Base64BinaryExp valueOf(String v){
		return new Base64BinaryExp(BinaryValue.valueOfBase64Enc(v));
	}
	
	public static Base64BinaryExp valueOf(BinaryValue v){
		return new Base64BinaryExp(v);
	}
	
	public StringExp toStringExp(){
		return StringExp.valueOf(getValue().toBase64Encoded());
	}
	
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.BASE64BINARY.emptyBag();
	}
	
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.BASE64BINARY.bag();
	}
}

