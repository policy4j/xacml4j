package org.xacml4j.v30.types;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.AttributeCategory;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.XacmlSyntaxException;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public abstract class Types
{
	/**
	 * Gets type by given type identifier
	 *
	 * @param typeId a type identifier
	 * @return {@link AttributeExpType} a data type instance
	 */
	public abstract AttributeExpType getType(String typeId);
	public abstract  AttributeExp valueOf(String typeId, Object value);

	public abstract AttributeExp valueOf(String typeId, Object value, Map<QName, String> attrs);
	
	public static Builder builder(){
		return new Builder();
	}

	public final static class Builder
	{
		private final static Logger log = LoggerFactory.getLogger(Builder.class);

		private final static String XPATH_CATEGORY_ATTR_NAME = "XPathCategory";

		private Map<String, AttributeExpType> types;


		private Builder(){
			this.types = new ConcurrentHashMap<String, AttributeExpType>();
		}
		
		/**
		 * Adds default XACML 2.0/3.0 types to this registry
		 * 
		 * @return {@link Builder}
		 */
		public Builder defaultTypes()
		{
			// default types
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
			addType(XPathExpType.XPATHEXPRESSION);
			addType(YearMonthDurationType.YEARMONTHDURATION);
			
			// short type aliases
			addType("anyURI", AnyURIType.ANYURI);
			addType("base64Binary", Base64BinaryType.BASE64BINARY);
			addType("boolean", BooleanType.BOOLEAN);
			addType("dateTime", DateTimeType.DATETIME);
			addType("date", DateType.DATE);
			addType("dayTimeDuration", DayTimeDurationType.DAYTIMEDURATION);
			addType("dnsName", DNSNameType.DNSNAME);
			addType("double", DoubleType.DOUBLE);
			addType("hexBinary", HexBinaryType.HEXBINARY);
			addType("integer", IntegerType.INTEGER);
			addType("ipAddress", IPAddressType.IPADDRESS);
			addType("rfc822Name", RFC822NameType.RFC822NAME);
			addType("string", StringType.STRING);
			addType("time", TimeType.TIME);
			addType("x500Name", X500NameType.X500NAME);
			addType("xpathExpression", XPathExpType.XPATHEXPRESSION);
			addType("yearMonthDuration", YearMonthDurationType.YEARMONTHDURATION);
			
			// Legacy XACML 2.0 type mappings/aliases
			addType("urn:oasis:names:tc:xacml:2.0:data-type:xpathExpression",  XPathExpType.XPATHEXPRESSION);
			addType("urn:oasis:names:tc:xacml:2.0:data-type:xpath-expression", XPathExpType.XPATHEXPRESSION);
			addType("http://www.w3.org/TR/2002/WD-xquery-operators-20020816#dayTimeDuration", DayTimeDurationType.DAYTIMEDURATION);
			addType("http://www.w3.org/TR/2002/WD-xquery-operators-20020816#yearMonthDuration", YearMonthDurationType.YEARMONTHDURATION);
			return this;
		}

		/**
		 * Adds given type to this registry
		 * 
		 * @param type a XACML type definition
		 * @return {@link Builder}
		 */
		public Builder type(AttributeExpType type){
			addType(type);
			return this;
		}
		
		public Types create(){
			return new Types() {
				@Override
				public AttributeExpType getType(String typeId) {
					Preconditions.checkArgument(
							!Strings.isNullOrEmpty(typeId));
					AttributeExpType type = types.get(typeId);
					if(type == null){
						throw new XacmlSyntaxException(
								"Unknown XACML type=\"%s\"", typeId);
					}
					return type;
				}

				@Override
				public AttributeExp valueOf(String typeId, Object value)
						throws XacmlSyntaxException {
					AttributeExpType type = getType(typeId);
					return type.create(value);
				}

				@Override
				public AttributeExp valueOf(String typeId,
						Object value, Map<QName, String> values) throws XacmlSyntaxException
				{
					AttributeExpType type = getType(typeId);
					try {
						return type.create(value, getXPathCategory(values));
					} catch (Exception e) {
						throw new XacmlSyntaxException(e);
					}
				}
			};
		}

		private void addType(AttributeExpType type){
			addType(type.getDataTypeId(), type);
		}

		private void addType(String typeId, AttributeExpType type){
			if(log.isDebugEnabled()){
				log.debug("Adding typeId=\"{}\"", typeId);
			}
			Preconditions.checkArgument(!types.containsKey(typeId), 
					"Type with identifier=\"%s\" already exist", typeId);
			this.types.put(typeId, type);
		}

		private static AttributeCategory getXPathCategory(Map<QName, String> attr)
				throws XacmlSyntaxException
			{
				for (QName n : attr.keySet()) {
					if (n.getLocalPart().equals(XPATH_CATEGORY_ATTR_NAME)) {
						return AttributeCategories.parse(attr.get(n));
					}
				}
				return null;
			}
	}
}
