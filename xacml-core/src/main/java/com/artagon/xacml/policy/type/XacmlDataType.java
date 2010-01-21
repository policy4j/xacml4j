package com.artagon.xacml.policy.type;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.artagon.xacml.policy.Attribute;
import com.artagon.xacml.policy.AttributeDataType;


public enum XacmlDataType 
{
	/** XACML DataType:  <b>http://www.w3.org/2001/XMLSchema#anyURI</b> */
	ANYURI(new AnyURITypeImpl("http://www.w3.org/2001/XMLSchema#anyURI")),
	/** XACML DataType:  <b>http://www.w3.org/2001/XMLSchema#boolean</b> */
	BOOLEAN(new BooleanTypeImpl("http://www.w3.org/2001/XMLSchema#boolean")),
	
	/** XACML DataType:  <b>http://www.w3.org/2001/XMLSchema#base64Binary</b> */
	BASE64BINARY(new Base64BinaryTypeImpl("http://www.w3.org/2001/XMLSchema#base64Binary")),

	/** XACML DataType:  <b>http://www.w3.org/2001/XMLSchema#date</b> */
	DATE(new DateTypeImpl("http://www.w3.org/2001/XMLSchema#date")),

	/** XACML DataType:  <b>http://www.w3.org/2001/XMLSchema#dateTime</b> */
	DATETIME(new DateTimeTypeImpl("http://www.w3.org/2001/XMLSchema#dateTime")),

	/** XACML DataType:  <b>urn:oasis:names:tc:xacml:2.0:data-type:dayTimeDuration</b> */
	DAYTIMEDURATION(new DayTimeDurationTypeImpl("urn:oasis:names:tc:xacml:2.0:data-type:dayTimeDuration")),
	 
	DNSNAME(new DNSNameTypeImpl("urn:oasis:names:tc:xacml:2.0:data-type:dnsName")),

	/** XACML DataType:  <b>http://www.w3.org/2001/XMLSchema#double</b> */
	DOUBLE(new DoubleTypeImpl("http://www.w3.org/2001/XMLSchema#double")),

	/** XACML DataType:  <b>http://www.w3.org/2001/XMLSchema#hexBinary</b> */
	HEXBINARY(new HexBinaryTypeImpl("http://www.w3.org/2001/XMLSchema#hexBinary")),

	/** XACML DataType:  <b>http://www.w3.org/2001/XMLSchema#integer</b> */
	INTEGER(new IntegerTypeImpl("http://www.w3.org/2001/XMLSchema#integer")),
	 
	IPADDRESS(new IPAddressTypeImpl("urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")),

	/** XACML DataType:  <b>http://www.w3.org/2001/XMLSchema#string</b> */ 
	RFC822NAME(new RFC822NameTypeImpl("urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")),

	/** XACML DataType:  <b>http://www.w3.org/2001/XMLSchema#string</b> */
	STRING(new StringTypeImpl("http://www.w3.org/2001/XMLSchema#string")),

	/** XACML DataType:  <b>http://www.w3.org/2001/XMLSchema#time</b> */
	TIME(new TimeTypeImpl("http://www.w3.org/2001/XMLSchema#time")),

	X500NAME(new X500NameTypeImpl("urn:oasis:names:tc:xacml:1.0:data-type:x500Name")),

	/** XACML DataType:  <b>urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression</b> */
	//XPATHEXPRESSION("urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression"),

	/** XACML DataType:  <b>urn:oasis:names:tc:xacml:2.0:data-type:yearMonthDuration</b> */
	YEARMONTHDURATION(new YearMonthDurationTypeImpl("urn:oasis:names:tc:xacml:2.0:data-type:yearMonthDuration"));
	
	private static final Map<String, AttributeDataType> BY_TYPE_ID = new ConcurrentHashMap<String, AttributeDataType>();

	static {
		for(XacmlDataType t : EnumSet.allOf(XacmlDataType.class)){
			BY_TYPE_ID.put(t.getTypeId(), t.getType());
		}
	}

	private AttributeDataType type;
	
	private XacmlDataType(AttributeDataType type){
		this.type = type;
	}
	
	public String getTypeId(){
		return type.getDataTypeId();
	}
	
	@SuppressWarnings("unchecked")
	public <V extends Attribute> V create(Object o){
		return ((V)type.create(o));
	}
	
	@SuppressWarnings("unchecked")
	public <V extends Attribute> V fromXacmlString(String v){
		return ((V)type.fromXacmlString(v));
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AttributeDataType> T getType(){
		return (T)type;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends AttributeDataType> T getByTypeId(String typeId){
		AttributeDataType type = BY_TYPE_ID.get(typeId);
		return (T)type;
	} 
}
