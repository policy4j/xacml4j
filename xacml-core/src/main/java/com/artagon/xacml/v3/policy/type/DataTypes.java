package com.artagon.xacml.v3.policy.type;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;


public enum DataTypes
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

	/** XACML DataType:  <b>http://www.w3.org/2001/XMLSchema#dayTimeDuration</b> */
	DAYTIMEDURATION(new DayTimeDurationTypeImpl("http://www.w3.org/2001/XMLSchema#dayTimeDuration")),
	
	@Deprecated
	/** XACML DataType:  <b>http://www.w3.org/TR/2002/WD-xquery-operators-20020816#dayTimeDuration</b> */
	DAYTIMEDURATION_XACML2(new DayTimeDurationTypeImpl("http://www.w3.org/TR/2002/WD-xquery-operators-20020816#dayTimeDuration")),

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
	XPATHEXPRESSION(new XPathExpressionTypeImpl("urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression")),

	/** XACML DataType:  <b>http://www.w3.org/2001/XMLSchema#yearMonthDuration</b> */
	YEARMONTHDURATION(new YearMonthDurationTypeImpl("http://www.w3.org/2001/XMLSchema#yearMonthDuration")),
	
	@Deprecated
	/** XACML DataType:  <b>http://www.w3.org/TR/2002/WD-xquery-operators- 20020816#yearMonthDuration</b> */
	YEARMONTHDURATION_XACML2(new YearMonthDurationTypeImpl("http://www.w3.org/TR/2002/WD-xquery-operators- 20020816#yearMonthDuration"));
	
	private static final Map<String, AttributeValueType> BY_TYPE_ID = new HashMap<String, AttributeValueType>();

	static {
		for(DataTypes t : EnumSet.allOf(DataTypes.class)){
			BY_TYPE_ID.put(t.getTypeId(), t.getType());
		}
	}

	private AttributeValueType type;
	
	private DataTypes(AttributeValueType type){
		this.type = type;
	}
	
	/**
	 * Gets type XACML identifier
	 * 
	 * @return XACML identifier for a type
	 */
	public String getTypeId(){
		return type.getDataTypeId();
	}
	
	/**
	 * Creates type instance value from
	 * a given {@link Object}
	 * 
	 * @param <V> an attribute value type
	 * @param o an attribute value
	 * @return an XACML data type value instance
	 */
	@SuppressWarnings("unchecked")
	public <V extends AttributeValue> V create(Object o, Object ...params)
	{
		return ((V)type.create(o,params));
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AttributeValue> BagOfAttributeValues<T> bag(AttributeValue ...attributes)
	{
		return (BagOfAttributeValues<T>)type.bagOf().create(attributes);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AttributeValue> BagOfAttributeValues<T> bag(Collection<AttributeValue> attributes)
	{
		return (BagOfAttributeValues<T>)type.bagOf().create(attributes);
	}
	
	
	@SuppressWarnings("unchecked")
	public <T extends AttributeValue> BagOfAttributeValues<T> emptyBag()
	{
		return (BagOfAttributeValues<T>)type.bagOf().createEmpty();
	}
	
	@SuppressWarnings("unchecked")
	public <V extends AttributeValue> V fromXacmlString(String v, Object ...params) 
	{
		return ((V)type.fromXacmlString(v, params));
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AttributeValueType> T getType(){
		return (T)type;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends AttributeValueType> T getByTypeId(String typeId){
		AttributeValueType type = BY_TYPE_ID.get(typeId);
		return (T)type;
	} 
	
	@Override
	public String toString(){
		return type.toString();
	}
}
