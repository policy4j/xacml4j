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

public final class HexBinaryExp extends
	BaseAttributeExp<BinaryValue>
{
	private static final long serialVersionUID = 8087916652227967791L;

	HexBinaryExp(BinaryValue value) {
		super(XacmlTypes.HEXBINARY, value);
	}
	
	public static HexBinaryExp valueOf(String v){
		return new HexBinaryExp(BinaryValue.valueOfHexEnc(v));
	}
	
	public static HexBinaryExp valueOf(byte[] v){
		return new HexBinaryExp(BinaryValue.valueOf(v));
	}
	
	public static HexBinaryExp valueOf(BinaryValue v){
		return new HexBinaryExp(v);
	}
	
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.HEXBINARY.emptyBag();
	}
	
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.HEXBINARY.bag();
	}
}

