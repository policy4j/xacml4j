package com.artagon.xacml.v30.types;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v30.AttributeValueType;
import com.google.common.base.Preconditions;

public final class DefaultDataTypeRegistry 
	implements DataTypeRegistry
{
	private final static Logger log = LoggerFactory.getLogger(DefaultDataTypeRegistry.class);
	
	private Map<String, AttributeValueType> types;
	
	public DefaultDataTypeRegistry()
	{
		this.types = new ConcurrentHashMap<String, AttributeValueType>(32);
		
		// Default XACML 3.0 types
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
	}
	
	@Override
	public AttributeValueType getType(String typeId) {
		return types.get(typeId);
	}

	@Override
	public void setTypes(Collection<AttributeValueType> types){
		for(AttributeValueType type : types){
			addType(type);
		}
	}
	
	private void addType(AttributeValueType type){
		addType(type.getDataTypeId(), type);
	}
	
	private void addType(String typeId, AttributeValueType type){
		if(log.isDebugEnabled()){
			log.debug("Adding type=\"{}\"", 
					typeId);
		}
		Preconditions.checkArgument(
				this.types.put(typeId, type) == null);
	}
}
