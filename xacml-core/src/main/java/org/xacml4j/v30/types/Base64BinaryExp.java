package org.xacml4j.v30.types;

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

