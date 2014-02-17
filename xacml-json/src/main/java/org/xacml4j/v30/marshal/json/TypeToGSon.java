package org.xacml4j.v30.marshal.json;


import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.types.AnyURIType;
import org.xacml4j.v30.types.Base64BinaryType;
import org.xacml4j.v30.types.BooleanType;
import org.xacml4j.v30.types.DNSNameType;
import org.xacml4j.v30.types.DateTimeType;
import org.xacml4j.v30.types.DateType;
import org.xacml4j.v30.types.DayTimeDurationType;
import org.xacml4j.v30.types.EntityType;
import org.xacml4j.v30.types.HexBinaryType;
import org.xacml4j.v30.types.IPAddressType;
import org.xacml4j.v30.types.IntegerType;
import org.xacml4j.v30.types.RFC822NameType;
import org.xacml4j.v30.types.StringType;
import org.xacml4j.v30.types.TimeType;
import org.xacml4j.v30.types.TypeCapability;
import org.xacml4j.v30.types.Types;
import org.xacml4j.v30.types.X500NameType;
import org.xacml4j.v30.types.YearMonthDurationType;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public interface TypeToGSon extends TypeCapability
{
	AttributeExpType getType();
	JsonElement toJson(Types types, AttributeExp v);
	AttributeExp fromJson(Types types, JsonElement v);
	
	
	public enum JsonTypes implements TypeToGSon
	{
		ANYURI(AnyURIType.ANYURI){
			@Override
			public JsonElement toJson(Types types, AttributeExp v) {
				return new JsonPrimitive(AnyURIType.ANYURI.toString(v));
			}

			@Override
			public AttributeExp fromJson(Types types, JsonElement v) {
				return AnyURIType.ANYURI.fromString(v.getAsString());
			}
		},
		BASE64BINARY(Base64BinaryType.BASE64BINARY){
			@Override
			public JsonElement toJson(Types types, AttributeExp v) {
				return new JsonPrimitive(Base64BinaryType.BASE64BINARY.toString(v));
			}

			@Override
			public AttributeExp fromJson(Types types, JsonElement v) {
				return Base64BinaryType.BASE64BINARY.fromString(v.getAsString());
			}
		},
		BOOLEAN(BooleanType.BOOLEAN){
			@Override
			public JsonElement toJson(Types types, AttributeExp v) {
				return new JsonPrimitive(BooleanType.BOOLEAN.toString(v));
			}

			@Override
			public AttributeExp fromJson(Types types, JsonElement v) {
				return BooleanType.BOOLEAN.fromString(v.getAsString());
			}
		},
		DATE(DateType.DATE){
			@Override
			public JsonElement toJson(Types types, AttributeExp v) {
				return new JsonPrimitive(DateType.DATE.toString(v));
			}

			@Override
			public AttributeExp fromJson(Types types, JsonElement v) {
				return DateType.DATE.fromString(v.getAsString());
			}
		},
		DATETIME(DateTimeType.DATETIME){
			@Override
			public JsonElement toJson(Types types, AttributeExp v) {
				return new JsonPrimitive(DateTimeType.DATETIME.toString(v));
			}

			@Override
			public AttributeExp fromJson(Types types, JsonElement v) {
				return DateTimeType.DATETIME.fromString(v.getAsString());
			}
		},
		DAYTIMEDUARATION(DayTimeDurationType.DAYTIMEDURATION){
			@Override
			public JsonElement toJson(Types types, AttributeExp v) {
				return new JsonPrimitive(DayTimeDurationType.DAYTIMEDURATION.toString(v));
			}

			@Override
			public AttributeExp fromJson(Types types, JsonElement v) {
				return DayTimeDurationType.DAYTIMEDURATION.fromString(v.getAsString());
			}
		},
		DNSNAME(DNSNameType.DNSNAME){
			@Override
			public JsonElement toJson(Types types, AttributeExp v) {
				return new JsonPrimitive(DNSNameType.DNSNAME.toString(v));
			}

			@Override
			public AttributeExp fromJson(Types types, JsonElement v) {
				return DNSNameType.DNSNAME.fromString(v.getAsString());
			}
		},
		HEXBINARY(HexBinaryType.HEXBINARY){
			@Override
			public JsonElement toJson(Types types, AttributeExp v) {
				return new JsonPrimitive(HexBinaryType.HEXBINARY.toString(v));
			}

			@Override
			public AttributeExp fromJson(Types types, JsonElement v) {
				return HexBinaryType.HEXBINARY.fromString(v.getAsString());
			}
		},
		INTEGER(IntegerType.INTEGER){
			@Override
			public JsonElement toJson(Types types, AttributeExp v) {
				return new JsonPrimitive(IntegerType.INTEGER.toString(v));
			}

			@Override
			public AttributeExp fromJson(Types types, JsonElement v) {
				return IntegerType.INTEGER.fromString(v.getAsString());
			}
		},
		IPADDRESS(IPAddressType.IPADDRESS){
			@Override
			public JsonElement toJson(Types types, AttributeExp v) {
				return new JsonPrimitive(IPAddressType.IPADDRESS.toString(v));
			}

			@Override
			public AttributeExp fromJson(Types types, JsonElement v) {
				return IPAddressType.IPADDRESS.fromString(v.getAsString());
			}
		},
		RFC822NAME(RFC822NameType.RFC822NAME){
			@Override
			public JsonElement toJson(Types types, AttributeExp v) {
				return new JsonPrimitive(RFC822NameType.RFC822NAME.toString(v));
			}

			@Override
			public AttributeExp fromJson(Types types, JsonElement v) {
				return RFC822NameType.RFC822NAME.fromString(v.getAsString());
			}
		},
		STRING(StringType.STRING){
			@Override
			public JsonElement toJson(Types types, AttributeExp v) {
				return new JsonPrimitive(StringType.STRING.toString(v));
			}

			@Override
			public AttributeExp fromJson(Types types, JsonElement v) {
				return StringType.STRING.fromString(v.getAsString());
			}
		},
		TIME(TimeType.TIME){
			@Override
			public JsonElement toJson(Types types, AttributeExp v) {
				return new JsonPrimitive(TimeType.TIME.toString(v));
			}

			@Override
			public AttributeExp fromJson(Types types, JsonElement v) {
				return TimeType.TIME.fromString(v.getAsString());
			}
		},
		X500NAME(X500NameType.X500NAME){
			@Override
			public JsonElement toJson(Types types, AttributeExp v) {
				return new JsonPrimitive(X500NameType.X500NAME.toString(v));
			}

			@Override
			public AttributeExp fromJson(Types types, JsonElement v) {
				return X500NameType.X500NAME.fromString(v.getAsString());
			}
		},
		YEARMONTHDURATION(YearMonthDurationType.YEARMONTHDURATION){
			@Override
			public JsonElement toJson(Types types, AttributeExp v) {
				return new JsonPrimitive(YearMonthDurationType.YEARMONTHDURATION.toString(v));
			}

			@Override
			public AttributeExp fromJson(Types types, JsonElement v) {
				return YearMonthDurationType.YEARMONTHDURATION.fromString(v.getAsString());
			}
		},
		ENTITY(EntityType.ENTITY){
			@Override
			public JsonElement toJson(Types types, AttributeExp v) {
				
			}

			@Override
			public AttributeExp fromJson(Types types, JsonElement v) {
				
			}
		};
		
		private AttributeExpType type;
		
		private JsonTypes(AttributeExpType type){
			this.type = type;
		}
		
		public AttributeExpType getType(){
			return type;
		}
	}
}
