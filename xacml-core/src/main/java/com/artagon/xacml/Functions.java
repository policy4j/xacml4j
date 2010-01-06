package com.artagon.xacml;

public enum Functions implements FunctionId
{
	INTEGER_EQUAL("urn:oasis:names:tc:xacml:1.0:function:integer-equal"),
	ANYURI_EQUAL("urn:oasis:names:tc:xacml:1.0:function:anyURI-equal"),
	BOOLEAN_EQUAL("urn:oasis:names:tc:xacml:1.0:function:boolean-equal"),
	DOUBLE_EQUAL("urn:oasis:names:tc:xacml:1.0:function:double-equal"),
	X500NAME_EQUAL("urn:oasis:names:tc:xacml:1.0:function:x500Name-equal"),
	STRING_IGNORECASE_EQUAL("urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case"),
	STRING_EQUAL("urn:oasis:names:tc:xacml:1.0:function:string-equal"),
	DATE_EQUAL("urn:oasis:names:tc:xacml:1.0:function:date-equal"),
	TIME_EQUAL("urn:oasis:names:tc:xacml:1.0:function:time-equal"),
	DATETIME_EQUAL("urn:oasis:names:tc:xacml:1.0:function:dateTime-equal"),
	DATETIMEDURATION_EQUAL("urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal"),
	YEARMONTHDURATION_EQUAL("urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal"),	 
	RFC833NAME_EQUAL("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal"),
	HEXBINARY_EQUAL("urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal"),
	BASE64BINARY_EQUAL("urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal"),
	
	INTEGER_ADD("urn:oasis:names:tc:xacml:1.0:function:integer-add"),
	DOUBLE_ADD("urn:oasis:names:tc:xacml:1.0:function:double-add"), 
	INTEGER_SUBSTRACT("urn:oasis:names:tc:xacml:1.0:function:integer-subtract"),
	DOUBLE_SUBTRACT("urn:oasis:names:tc:xacml:1.0:function:double-subtract"),
	INTEGER_MULTIPLY("urn:oasis:names:tc:xacml:1.0:function:integer-multiply"),
	DOUBLE_MULTIPLY("urn:oasis:names:tc:xacml:1.0:function:double-multiply"),
	INTEGER_DIVIDE("urn:oasis:names:tc:xacml:1.0:function:integer-divide"),
	DOUBLE_DIVIDE("urn:oasis:names:tc:xacml:1.0:function:double-divide"),
	INTEGER_MOD("urn:oasis:names:tc:xacml:1.0:function:integer-mod"),
	INTEGER_ABS("urn:oasis:names:tc:xacml:1.0:function:integer-abs"),
	DOUBLE_ABS("urn:oasis:names:tc:xacml:1.0:function:double-abs"),
	ROUND("urn:oasis:names:tc:xacml:1.0:function:round"),
	FLOOR("urn:oasis:names:tc:xacml:1.0:function:floor"),
	DOUBLE_TO_INTEGER("urn:oasis:names:tc:xacml:1.0:function:double-to-integer"),
	INTEGER_TO_DOUBLE("urn:oasis:names:tc:xacml:1.0:function:integer-to-double"),
	
	OR("urn:oasis:names:tc:xacml:1.0:function:or"), 
	AND("urn:oasis:names:tc:xacml:1.0:function:and"),
	N_OF("urn:oasis:names:tc:xacml:1.0:function:n-of"),
	NOT("urn:oasis:names:tc:xacml:1.0:function:not"),

	STRING_NORMALIZE_SPACE("urn:oasis:names:tc:xacml:1.0:function:string-normalize-space"),
	STRING_NORMALIZE_TO_LOWER_CASE("urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case"),
	 
	INTEGER_GREATER_THAN("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than"),
	INTEGER_GREATER_THAN_OR_EQUAL("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal"),
	
	INTEGER_LESS_THAN("urn:oasis:names:tc:xacml:1.0:function:integer-less-than"),
	INTEGER_LESS_THAN_OR_EQUALS("urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal"),
	DOUBLE_GREATER_THAN("urn:oasis:names:tc:xacml:1.0:function:double-greater-than"),
	DOUBLE_GREATER_THAN_OR_EQUAL("urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal"),
	DOUBLE_LESS_THAN("urn:oasis:names:tc:xacml:1.0:function:double-less-than"),
	DOUBLE_LESS_THAN_OR_EQUALS("urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal"),
	/*
	urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration 
	urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration 
	urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration 
	urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract- yearMonthDuration 
	urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration 
	urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration 
	urn:oasis:names:tc:xacml:1.0:function:string-greater-than 
	urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal 
	urn:oasis:names:tc:xacml:1.0:function:string-less-than 
	urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal 
	urn:oasis:names:tc:xacml:1.0:function:time-greater-than 
	urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal 
	urn:oasis:names:tc:xacml:1.0:function:time-less-than 
	urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal 
	urn:oasis:names:tc:xacml:2.0:function:time-in-range 
	urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than 
	urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal 
	urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than 
	urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal 
	urn:oasis:names:tc:xacml:1.0:function:date-greater-than 
	urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal 
	urn:oasis:names:tc:xacml:1.0:function:date-less-than 
	urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal 
	urn:oasis:names:tc:xacml:1.0:function:string-one-and-only 
	urn:oasis:names:tc:xacml:1.0:function:string-bag-size 
	urn:oasis:names:tc:xacml:1.0:function:string-is-in 
	urn:oasis:names:tc:xacml:1.0:function:string-bag 
	urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only 
	urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size 
	urn:oasis:names:tc:xacml:1.0:function:boolean-is-in 
	urn:oasis:names:tc:xacml:1.0:function:boolean-bag 
	urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only
	urn:oasis:names:tc:xacml:1.0:function:integer-bag-size 
	urn:oasis:names:tc:xacml:1.0:function:integer-is-in 
	urn:oasis:names:tc:xacml:1.0:function:integer-bag 
	urn:oasis:names:tc:xacml:1.0:function:double-one-and-only 
	urn:oasis:names:tc:xacml:1.0:function:double-bag-size 
	urn:oasis:names:tc:xacml:1.0:function:double-is-in 
	urn:oasis:names:tc:xacml:1.0:function:double-bag 
	urn:oasis:names:tc:xacml:1.0:function:time-one-and-only 
	urn:oasis:names:tc:xacml:1.0:function:time-bag-size 
	urn:oasis:names:tc:xacml:1.0:function:time-is-in 
	urn:oasis:names:tc:xacml:1.0:function:time-bag 
	urn:oasis:names:tc:xacml:1.0:function:date-one-and-only 
	urn:oasis:names:tc:xacml:1.0:function:date-bag-size 
	urn:oasis:names:tc:xacml:1.0:function:date-is-in 
	urn:oasis:names:tc:xacml:1.0:function:date-bag 
	urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only 
	urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size 
	urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in 
	urn:oasis:names:tc:xacml:1.0:function:dateTime-bag 
	urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only 
	urn:oasis:names:tc:xacml:1.0:function:anyURI-bag-size 
	urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in 
	urn:oasis:names:tc:xacml:1.0:function:anyURI-bag 
	urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only 
	urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size 
	urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in 
	urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag 
	urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only 
	urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size 
	urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in 
	urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag 
	urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-one-and-only 
	urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag-size 
	urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-is-in 
	urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag 
	urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-one-and-only 
	urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag-size 
	urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-is-in 
	urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag 
	urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only 
	urn:oasis:names:tc:xacml:1.0:function:x500Name-bag-size 
	urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in 
	urn:oasis:names:tc:xacml:1.0:function:x500Name-bag 
	urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only 
	urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size 
	urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in 
	urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag 
	urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only 
	urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size 
	urn:oasis:names:tc:xacml:2.0:function:ipAddress-is-in 
	urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag 
	urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only 
	urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size 
	urn:oasis:names:tc:xacml:2.0:function:dnsName-is-in 
	urn:oasis:names:tc:xacml:2.0:function:dnsName-bag 
	urn:oasis:names:tc:xacml:2.0:function:string-concatenate
	*/ 
	BOOLEAN_FROM_STRING("urn:oasis:names:tc:xacml:3.0:function:boolean-from-string"),
	STRING_FROM_BOOLEAN("urn:oasis:names:tc:xacml:3.0:function:string-from-boolean"),
	INTEGER_FROM_STRING("urn:oasis:names:tc:xacml:3.0:function:integer-from-string"), 
	STRING_FROM_INTEGER("urn:oasis:names:tc:xacml:3.0:function:string-from-integer"), 
	DOUBLE_FROM_STRING("urn:oasis:names:tc:xacml:3.0:function:double-from-string"), 
	STRING_FROM_DOUBLE("urn:oasis:names:tc:xacml:3.0:function:string-from-double"), 
	TIME_FROM_STRING("urn:oasis:names:tc:xacml:3.0:function:time-from-string"), 
	STRING_FROM_TIME("urn:oasis:names:tc:xacml:3.0:function:string-from-time"), 
	DATE_FROM_STRING("urn:oasis:names:tc:xacml:3.0:function:date-from-string"), 
	STRING_FROM_DATE("urn:oasis:names:tc:xacml:3.0:function:string-from-date"),
	DATETIME_FROM_STRING("urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string"), 
	STRING_FROM_DATETIME("urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime"), 
	ANYURI_FROM_STRING("urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string"), 
	STRING_FROM_ANYURI("urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI"), 
	DAYTIMEDURATION_FROM_STRING("urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string"), 
	STRING_FROM_DAYTIMEDURATION("urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration"), 
	YEARMONTHDURATION_FROM_STRING("urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string"), 
	STRING_FROM_YEARMONTHDURATION("urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration"), 
	X500NAME_FROM_STRING("urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string"), 
	STRING_FROM_X500NAME("urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name"),
	RFC822NAME_FROM_STRING("urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string"), 
	STRING_FROM_RFC822NAME("urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name"), 
	IPADDRESS_FROM_STRING("urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string"), 
	STRING_FROM_IPADDRESS("urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress"), 
	DNSNAME_FROM_STRING("urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string"), 
	STRING_FROM_DNSNAME("urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName");
	
	/*
	urn:oasis:names:tc:xacml:3.0:function:string-starts-with 
	urn:oasis:names:tc:xacml:3.0:function:uri-starts-with 
	urn:oasis:names:tc:xacml:3.0:function:string-ends-with 
	urn:oasis:names:tc:xacml:3.0:function:uri-ends-with 
	urn:oasis:names:tc:xacml:3.0:function:string-contains 
	urn:oasis:names:tc:xacml:3.0:function:uri-contains 
	urn:oasis:names:tc:xacml:3.0:function:string-substring 
	urn:oasis:names:tc:xacml:3.0:function:uri-substring 
	urn:oasis:names:tc:xacml:1.0:function:any-of 
	urn:oasis:names:tc:xacml:1.0:function:all-of 
	urn:oasis:names:tc:xacml:1.0:function:any-of-any 
	urn:oasis:names:tc:xacml:1.0:function:all-of-any 
	urn:oasis:names:tc:xacml:1.0:function:any-of-all 
	urn:oasis:names:tc:xacml:1.0:function:all-of-all 
	urn:oasis:names:tc:xacml:1.0:function:map 
	urn:oasis:names:tc:xacml:1.0:function:x500Name-match 
	urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match 
	urn:oasis:names:tc:xacml:1.0:function:string-regexp-match 
	urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match 
	urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match 
	urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match 
	urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match 
	urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match 
	urn:oasis:names:tc:xacml:3.0:function:xpath-node-count 
	urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal 
	urn:oasis:names:tc:xacml:3.0:function:xpath-node-match 
	urn:oasis:names:tc:xacml:1.0:function:string-intersection 
	urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of 
	urn:oasis:names:tc:xacml:1.0:function:string-union 
	urn:oasis:names:tc:xacml:1.0:function:string-subset 
	urn:oasis:names:tc:xacml:1.0:function:string-set-equals 
	urn:oasis:names:tc:xacml:1.0:function:boolean-intersection 
	urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of 
	urn:oasis:names:tc:xacml:1.0:function:boolean-union*/
	
	private String id;
	
	private Functions(String functionId){
		this.id = functionId;
	}
	
	public static FunctionId parse(final String v)
	{
		for(Functions typeId : values()){
			if(typeId.id.equals(v)){
				return typeId;
			}
		}
		return new CustomFunctionId(v);
	}
	
	public String toString(){
		return id;
	}

}
