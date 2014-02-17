package org.xacml4j.v30.types;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.XacmlSyntaxException;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.common.reflect.TypeToken;

public abstract class Types
{
	/**
	 * Gets type by given type identifier
	 *
	 * @param typeId a type identifier
	 * @return {@link AttributeExpType} a data type instance
	 */
	public abstract AttributeExpType getType(String typeId);
	
	/**
	 * Adds given capability <T> to the given type
	 * 
	 * @param typeId a type identifier
	 * @param capability a type capability
	 */
	public abstract <T extends TypeCapability> void addCapability(String typeId, Class<T> capability, T capabilityImpl);
	
	/**
	 * Adds given capability <T> to the given type
	 * 
	 * @param type a type
	 * @param capability a type capability
	 */
	public abstract <T extends TypeCapability> void addCapability(AttributeExpType type, Class<T> capability, T capabilityImpl);
	
	/**
	 * Gets capability <T> for the given type
	 * 
	 * @param typeId a type identifier
	 * @return type capability or <code>null</code> if capability is not supported
	 */
	public abstract <T extends TypeCapability> T getCapability(String typeId, Class<T> capability);
	
	/**
	 * Gets capability <T> for the given type
	 * 
	 * @param typeId a type
	 * @return type capability or <code>null</code> if capability is not supported
	 */
	public abstract <T extends TypeCapability> T getCapability(AttributeExpType type, Class<T> capability);

	public static Builder builder(){
		return new Builder();
	}

	public final static class Builder
	{
		private final static Logger log = LoggerFactory.getLogger(Builder.class);
			
		private Map<String, AttributeExpType> types;
		
		private ConcurrentMap<String, ClassToInstanceMap<TypeCapability>> typeCapabilities;


		private Builder(){
			this.types = new ConcurrentHashMap<String, AttributeExpType>();
			this.typeCapabilities = new ConcurrentHashMap<String, ClassToInstanceMap<TypeCapability>>();
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
			addTypeCapability(AnyURIType.ANYURI, TypeToString.class, AnyURIType.ANYURI);
			addTypeCapability(AnyURIType.ANYURI, TypeToXacml30.class, AnyURIType.ANYURI);
			
			addType(Base64BinaryType.BASE64BINARY);
			addTypeCapability(Base64BinaryType.BASE64BINARY, TypeToString.class, Base64BinaryType.BASE64BINARY);
			addTypeCapability(Base64BinaryType.BASE64BINARY, TypeToXacml30.class, Base64BinaryType.BASE64BINARY);
			
			addType(BooleanType.BOOLEAN);
			addTypeCapability(BooleanType.BOOLEAN, TypeToString.class, BooleanType.BOOLEAN);
			addTypeCapability(BooleanType.BOOLEAN, TypeToXacml30.class, BooleanType.BOOLEAN);
			
			addType(DateTimeType.DATETIME);
			addTypeCapability(DateTimeType.DATETIME, TypeToString.class, DateTimeType.DATETIME);
			addTypeCapability(DateTimeType.DATETIME, TypeToXacml30.class, DateTimeType.DATETIME);
			
			addType(DateType.DATE);
			addTypeCapability(DateType.DATE, TypeToString.class, DateType.DATE);
			addTypeCapability(DateType.DATE, TypeToXacml30.class, DateType.DATE);
			
			addType(DayTimeDurationType.DAYTIMEDURATION);
			addTypeCapability(DayTimeDurationType.DAYTIMEDURATION, TypeToString.class, DayTimeDurationType.DAYTIMEDURATION);
			addTypeCapability(DayTimeDurationType.DAYTIMEDURATION, TypeToXacml30.class, DayTimeDurationType.DAYTIMEDURATION);
			
			addType(DNSNameType.DNSNAME);
			addTypeCapability(DNSNameType.DNSNAME, TypeToString.class, DNSNameType.DNSNAME);
			addTypeCapability(DNSNameType.DNSNAME, TypeToXacml30.class, DNSNameType.DNSNAME);
			
			addType(DoubleType.DOUBLE);
			addTypeCapability(DoubleType.DOUBLE, TypeToString.class, DoubleType.DOUBLE);
			addTypeCapability(DoubleType.DOUBLE, TypeToXacml30.class, DoubleType.DOUBLE);
			
			addType(HexBinaryType.HEXBINARY);
			addTypeCapability(HexBinaryType.HEXBINARY, TypeToString.class, HexBinaryType.HEXBINARY);
			addTypeCapability(HexBinaryType.HEXBINARY, TypeToXacml30.class, HexBinaryType.HEXBINARY);
			
			addType(IntegerType.INTEGER);
			addTypeCapability(IntegerType.INTEGER, TypeToString.class, IntegerType.INTEGER);
			addTypeCapability(IntegerType.INTEGER, TypeToXacml30.class, IntegerType.INTEGER);
			
			addType(IPAddressType.IPADDRESS);
			addTypeCapability(IPAddressType.IPADDRESS, TypeToString.class, IPAddressType.IPADDRESS);
			addTypeCapability(IPAddressType.IPADDRESS, TypeToXacml30.class, IPAddressType.IPADDRESS);
			
			addType(RFC822NameType.RFC822NAME);
			addTypeCapability(RFC822NameType.RFC822NAME, TypeToString.class, RFC822NameType.RFC822NAME);
			addTypeCapability(RFC822NameType.RFC822NAME, TypeToXacml30.class, RFC822NameType.RFC822NAME);
			
			addType(StringType.STRING);
			addTypeCapability(StringType.STRING, TypeToString.class, StringType.STRING);
			addTypeCapability(StringType.STRING, TypeToXacml30.class, StringType.STRING);
			
			addType(TimeType.TIME);
			addTypeCapability(TimeType.TIME, TypeToString.class, TimeType.TIME);
			addTypeCapability(TimeType.TIME, TypeToXacml30.class, TimeType.TIME);
			
			addType(X500NameType.X500NAME);
			addTypeCapability(X500NameType.X500NAME, TypeToString.class, X500NameType.X500NAME);
			addTypeCapability(X500NameType.X500NAME, TypeToXacml30.class, X500NameType.X500NAME);
			
			addType(XPathExpType.XPATHEXPRESSION);
			addTypeCapability(XPathExpType.XPATHEXPRESSION, TypeToXacml30.class, XPathExpType.XPATHEXPRESSION);
			
			addType(YearMonthDurationType.YEARMONTHDURATION);
			addTypeCapability(YearMonthDurationType.YEARMONTHDURATION, TypeToString.class, YearMonthDurationType.YEARMONTHDURATION);
			addTypeCapability(YearMonthDurationType.YEARMONTHDURATION, TypeToXacml30.class, YearMonthDurationType.YEARMONTHDURATION);
			
			addType(EntityType.ENTITY);
			addTypeCapability(EntityType.ENTITY, TypeToXacml30.class, EntityType.ENTITY);
			

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
			addType("entity", EntityType.ENTITY);

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
				public <T extends TypeCapability> void addCapability(String typeId,
						Class<T> capability, T capabilityImpl) {
					AttributeExpType type = getType(typeId);
					addTypeCapability(type, capability, capabilityImpl);
				}
				
				@Override
				public <T extends TypeCapability> void addCapability(AttributeExpType type,
						Class<T> capability, T capabilityImpl) {
					addTypeCapability(type, capability, capabilityImpl);
				}

				@Override
				public <T extends TypeCapability> T getCapability(
						String typeId, Class<T> capability) {
					AttributeExpType type = getType(typeId);
					ClassToInstanceMap<TypeCapability> map = typeCapabilities.get(type.getDataTypeId());
					return (map != null)?map.getInstance(capability):null;
				}
				
				@Override
				public <T extends TypeCapability> T getCapability(
						AttributeExpType type, Class<T> capability) {
					ClassToInstanceMap<TypeCapability> map = typeCapabilities.get(type.getDataTypeId());
					if(map != null){
						return map.getInstance(capability);
					}
					return null;
				}
			};
		}

		private void addType(AttributeExpType type){
			addType(type.getDataTypeId(), type);
		}
		
		/**
		 * Adds type with new type identifier alias
		 * 
		 * @param typeId a type identifier alias
		 * @param type
		 */
		private void addType(String typeId, AttributeExpType type){
			if(log.isDebugEnabled()){
				log.debug("Adding typeId=\"{}\"", typeId);
			}
			Preconditions.checkArgument(!types.containsKey(typeId),
					"Type with identifier=\"%s\" already exist", typeId);
			this.types.put(typeId, type);
		}
		
		private <T extends TypeCapability> void addTypeCapability(AttributeExpType type, 
				Class<T> capability, T capabilityImpl) {
			if(!types.containsKey(type.getDataTypeId())){
				throw new XacmlSyntaxException(
						String.format("Type=\"%s\" does not exist", type));
			}
			TypeToken<?> capabilityTypeTok = TypeToken.of(capability);
			TypeToken<?> capabilityImplTypeTok = TypeToken.of(capabilityImpl.getClass());
			Preconditions.checkArgument(capability.isInterface());
			Preconditions.checkArgument(capabilityTypeTok.isAssignableFrom(capabilityImplTypeTok));
			ClassToInstanceMap<TypeCapability> map = typeCapabilities.get(type.getDataTypeId());
			if(map == null){
				map = MutableClassToInstanceMap.<TypeCapability>create(
						new ConcurrentHashMap<Class<? extends TypeCapability>, TypeCapability>());
				ClassToInstanceMap<TypeCapability> prev = typeCapabilities.putIfAbsent(type.getDataTypeId(), map);
				if(prev != null){
					map = prev;
				}
			}
			if(!map.containsKey(capability)){
				map.putInstance(capability, capabilityImpl);
			}
		}
	}
}
