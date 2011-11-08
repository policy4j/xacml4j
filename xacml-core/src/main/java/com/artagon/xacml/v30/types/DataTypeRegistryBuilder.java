package com.artagon.xacml.v30.types;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.artagon.xacml.v30.AttributeExpType;
import com.google.common.base.Preconditions;

public final class DataTypeRegistryBuilder 
{
	private Map<String, AttributeExpType> types;
	
	private DataTypeRegistryBuilder(){
		this.types = new ConcurrentHashMap<String, AttributeExpType>();
	}
	
	public DataTypeRegistryBuilder withDefaultTypes()
	{
		addType(AnyURIType.ANYURI);
		addType(Base64BinaryType.BASE64BINARY);
		addType(BooleanType.BOOLEAN);
		addType(DateTimeType.DATETIME);
		addType(DateType.DATE);
		addType(DayTimeDurationType.DAYTIMEDURATION);
		addType(DNSNameType.DNSNAME);
		addType(DoubleType.DOUBLE);
		addType(HexBinaryType.HEXBINARY);
		addType(IntegerType.INTEGER);
		addType(IPAddressType.IPADDRESS);
		addType(RFC822NameType.RFC822NAME);
		addType(StringType.STRING);
		addType(TimeType.TIME);
		addType(X500NameType.X500NAME);
		addType(XPathExpressionType.XPATHEXPRESSION);
		addType(YearMonthDurationType.YEARMONTHDURATION);
		
		// Legacy XACML 2.0 type mappings
		addType("urn:oasis:names:tc:xacml:2.0:data-type:xpathExpression",  XPathExpressionType.XPATHEXPRESSION);
		addType("urn:oasis:names:tc:xacml:2.0:data-type:xpath-expression", XPathExpressionType.XPATHEXPRESSION);
		addType("http://www.w3.org/TR/2002/WD-xquery-operators-20020816#dayTimeDuration", DayTimeDurationType.DAYTIMEDURATION);
		addType("http://www.w3.org/TR/2002/WD-xquery-operators-20020816#yearMonthDuration", YearMonthDurationType.YEARMONTHDURATION);
		return this;
	}
	
	public DataTypeRegistryBuilder withType(AttributeExpType type){
		addType(type);
		return this;
	}
	
	public DataTypeRegistry build(){
		return new DataTypeRegistry() {
			@Override
			public AttributeExpType getType(String typeId) {
				return types.get(typeId);
			}
		};
	}
	
	private void addType(AttributeExpType type){
		addType(type.getDataTypeId(), type);
	}
	
	private void addType(String typeId, AttributeExpType type){
		Preconditions.checkArgument(!types.containsKey(type.getDataTypeId()));
		this.types.put(typeId, type);
	}
}
