package org.xacml4j.v30.marshal.json;


import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.types.TypeCapability;
import org.xacml4j.v30.types.TypeToString;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public interface TypeToGSon extends TypeCapability
{
	AttributeExpType getType();
	JsonElement toJson(AttributeExp v);
	AttributeExp fromJson(JsonElement v);
	
	
	public enum JsonTypes implements TypeToGSon
	{
		ANYURI(XacmlTypes.ANYURI){
			@Override
			public JsonElement toJson(AttributeExp v) {
				return new JsonPrimitive(TypeToString.Types.ANYURI.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v) {
				return TypeToString.Types.ANYURI.fromString(v.getAsString());
			}
		},
		BASE64BINARY(XacmlTypes.BASE64BINARY){
			@Override
			public JsonElement toJson(AttributeExp v) {
				return new JsonPrimitive(TypeToString.Types.BASE64BINARY.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v) {
				return TypeToString.Types.BASE64BINARY.fromString(v.getAsString());
			}
		},
		BOOLEAN(XacmlTypes.BOOLEAN){
			@Override
			public JsonElement toJson(AttributeExp v) {
				return new JsonPrimitive(TypeToString.Types.BOOLEAN.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v) {
				return TypeToString.Types.BOOLEAN.fromString(v.getAsString());
			}
		},
		DATE(XacmlTypes.DATE){
			@Override
			public JsonElement toJson(AttributeExp v) {
				return new JsonPrimitive(TypeToString.Types.DATE.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v) {
				return TypeToString.Types.DATE.fromString(v.getAsString());
			}
		},
		DATETIME(XacmlTypes.DATETIME){
			@Override
			public JsonElement toJson(AttributeExp v) {
				return new JsonPrimitive(TypeToString.Types.DATETIME.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v) {
				return TypeToString.Types.DATETIME.fromString(v.getAsString());
			}
		},
		DAYTIMEDUARATION(XacmlTypes.DAYTIMEDURATION){
			@Override
			public JsonElement toJson(AttributeExp v) {
				return new JsonPrimitive(TypeToString.Types.DAYTIMEDURATION.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v) {
				return TypeToString.Types.DAYTIMEDURATION.fromString(v.getAsString());
			}
		},
		DNSNAME(XacmlTypes.DNSNAME){
			@Override
			public JsonElement toJson(AttributeExp v) {
				return new JsonPrimitive(TypeToString.Types.DNSNAME.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v) {
				return TypeToString.Types.DNSNAME.fromString(v.getAsString());
			}
		},
		HEXBINARY(XacmlTypes.HEXBINARY){
			@Override
			public JsonElement toJson(AttributeExp v) {
				return new JsonPrimitive(TypeToString.Types.HEXBINARY.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v) {
				return TypeToString.Types.HEXBINARY.fromString(v.getAsString());
			}
		},
		INTEGER(XacmlTypes.INTEGER){
			@Override
			public JsonElement toJson(AttributeExp v) {
				return new JsonPrimitive(TypeToString.Types.INTEGER.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v) {
				return TypeToString.Types.INTEGER.fromString(v.getAsString());
			}
		},
		IPADDRESS(XacmlTypes.IPADDRESS){
			@Override
			public JsonElement toJson(AttributeExp v) {
				return new JsonPrimitive(TypeToString.Types.IPADDRESS.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v) {
				return TypeToString.Types.IPADDRESS.fromString(v.getAsString());
			}
		},
		RFC822NAME(XacmlTypes.RFC822NAME){
			@Override
			public JsonElement toJson(AttributeExp v) {
				return new JsonPrimitive(TypeToString.Types.RFC822NAME.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v) {
				return TypeToString.Types.RFC822NAME.fromString(v.getAsString());
			}
		},
		STRING(XacmlTypes.STRING){
			@Override
			public JsonElement toJson(AttributeExp v) {
				return new JsonPrimitive(TypeToString.Types.STRING.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v) {
				return TypeToString.Types.STRING.fromString(v.getAsString());
			}
		},
		TIME(XacmlTypes.TIME){
			@Override
			public JsonElement toJson(AttributeExp v) {
				return new JsonPrimitive(TypeToString.Types.TIME.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v) {
				return TypeToString.Types.TIME.fromString(v.getAsString());
			}
		},
		X500NAME(XacmlTypes.X500NAME){
			@Override
			public JsonElement toJson(AttributeExp v) {
				return new JsonPrimitive(TypeToString.Types.X500NAME.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v) {
				return TypeToString.Types.X500NAME.fromString(v.getAsString());
			}
		},
		YEARMONTHDURATION(XacmlTypes.YEARMONTHDURATION){
			@Override
			public JsonElement toJson(AttributeExp v) {
				return new JsonPrimitive(TypeToString.Types.YEARMONTHDURATION.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v) {
				return TypeToString.Types.YEARMONTHDURATION.fromString(v.getAsString());
			}
		},
		ENTITY(XacmlTypes.ENTITY){
			@Override
			public JsonElement toJson(AttributeExp v) {
				throw new UnsupportedOperationException();
			}

			@Override
			public AttributeExp fromJson(JsonElement v) {
				throw new UnsupportedOperationException();
			}
		};
	
		private final static Index<TypeToGSon> INDEX = Index.<TypeToGSon>build(values());
		
		private AttributeExpType type;
		
		private JsonTypes(AttributeExpType type){
			this.type = type;
		}
		
		public AttributeExpType getType(){
			return type;
		}
		
		public final static Index<TypeToGSon> getIndex(){
			return INDEX;
		}
	}
}
