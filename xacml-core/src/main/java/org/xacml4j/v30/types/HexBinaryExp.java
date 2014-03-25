package org.xacml4j.v30.types;

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

