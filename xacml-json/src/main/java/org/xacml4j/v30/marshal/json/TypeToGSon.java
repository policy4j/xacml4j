package org.xacml4j.v30.marshal.json;


import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.types.EntityExp;
import org.xacml4j.v30.types.TypeCapability;
import org.xacml4j.v30.types.TypeToString;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

public interface TypeToGSon extends TypeCapability
{
	AttributeExpType getType();
	JsonElement toJson(AttributeExp v, JsonSerializationContext ctx);
	AttributeExp fromJson(JsonElement v, JsonDeserializationContext ctx);
	
	
	public enum JsonTypes implements TypeToGSon
	{
		ANYURI(XacmlTypes.ANYURI){
			@Override
			public JsonElement toJson(AttributeExp v, JsonSerializationContext ctx) {
				return new JsonPrimitive(TypeToString.Types.ANYURI.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v, JsonDeserializationContext ctx) {
				return TypeToString.Types.ANYURI.fromString(v.getAsString());
			}
		},
		BASE64BINARY(XacmlTypes.BASE64BINARY){
			@Override
			public JsonElement toJson(AttributeExp v, JsonSerializationContext ctx) {
				return new JsonPrimitive(TypeToString.Types.BASE64BINARY.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v, JsonDeserializationContext ctx) {
				return TypeToString.Types.BASE64BINARY.fromString(v.getAsString());
			}
		},
		BOOLEAN(XacmlTypes.BOOLEAN){
			@Override
			public JsonElement toJson(AttributeExp v, JsonSerializationContext ctx) {
				return new JsonPrimitive(TypeToString.Types.BOOLEAN.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v, JsonDeserializationContext ctx) {
				return TypeToString.Types.BOOLEAN.fromString(v.getAsString());
			}
		},
		DATE(XacmlTypes.DATE){
			@Override
			public JsonElement toJson(AttributeExp v, JsonSerializationContext ctx) {
				return new JsonPrimitive(TypeToString.Types.DATE.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v, JsonDeserializationContext ctx) {
				return TypeToString.Types.DATE.fromString(v.getAsString());
			}
		},
		DATETIME(XacmlTypes.DATETIME){
			@Override
			public JsonElement toJson(AttributeExp v, JsonSerializationContext ctx) {
				return new JsonPrimitive(TypeToString.Types.DATETIME.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v, JsonDeserializationContext ctx) {
				return TypeToString.Types.DATETIME.fromString(v.getAsString());
			}
		},
		DAYTIMEDUARATION(XacmlTypes.DAYTIMEDURATION){
			@Override
			public JsonElement toJson(AttributeExp v, JsonSerializationContext ctx) {
				return new JsonPrimitive(TypeToString.Types.DAYTIMEDURATION.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v, JsonDeserializationContext ctx) {
				return TypeToString.Types.DAYTIMEDURATION.fromString(v.getAsString());
			}
		},
		DNSNAME(XacmlTypes.DNSNAME){
			@Override
			public JsonElement toJson(AttributeExp v, JsonSerializationContext ctx) {
				return new JsonPrimitive(TypeToString.Types.DNSNAME.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v, JsonDeserializationContext ctx) {
				return TypeToString.Types.DNSNAME.fromString(v.getAsString());
			}
		},
		HEXBINARY(XacmlTypes.HEXBINARY){
			@Override
			public JsonElement toJson(AttributeExp v, JsonSerializationContext ctx) {
				return new JsonPrimitive(TypeToString.Types.HEXBINARY.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v, JsonDeserializationContext ctx) {
				return TypeToString.Types.HEXBINARY.fromString(v.getAsString());
			}
		},
		INTEGER(XacmlTypes.INTEGER){
			@Override
			public JsonElement toJson(AttributeExp v, JsonSerializationContext ctx) {
				return new JsonPrimitive(TypeToString.Types.INTEGER.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v, JsonDeserializationContext ctx) {
				return TypeToString.Types.INTEGER.fromString(v.getAsString());
			}
		},
		IPADDRESS(XacmlTypes.IPADDRESS){
			@Override
			public JsonElement toJson(AttributeExp v, JsonSerializationContext ctx) {
				return new JsonPrimitive(TypeToString.Types.IPADDRESS.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v, JsonDeserializationContext ctx) {
				return TypeToString.Types.IPADDRESS.fromString(v.getAsString());
			}
		},
		RFC822NAME(XacmlTypes.RFC822NAME){
			@Override
			public JsonElement toJson(AttributeExp v, JsonSerializationContext ctx) {
				return new JsonPrimitive(TypeToString.Types.RFC822NAME.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v, JsonDeserializationContext ctx) {
				return TypeToString.Types.RFC822NAME.fromString(v.getAsString());
			}
		},
		STRING(XacmlTypes.STRING){
			@Override
			public JsonElement toJson(AttributeExp v, JsonSerializationContext ctx) {
				return new JsonPrimitive(TypeToString.Types.STRING.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v, JsonDeserializationContext ctx) {
				return TypeToString.Types.STRING.fromString(v.getAsString());
			}
		},
		TIME(XacmlTypes.TIME){
			@Override
			public JsonElement toJson(AttributeExp v, JsonSerializationContext ctx) {
				return new JsonPrimitive(TypeToString.Types.TIME.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v, JsonDeserializationContext ctx) {
				return TypeToString.Types.TIME.fromString(v.getAsString());
			}
		},
		X500NAME(XacmlTypes.X500NAME){
			@Override
			public JsonElement toJson(AttributeExp v, JsonSerializationContext ctx) {
				return new JsonPrimitive(TypeToString.Types.X500NAME.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v, JsonDeserializationContext ctx) {
				return TypeToString.Types.X500NAME.fromString(v.getAsString());
			}
		},
		YEARMONTHDURATION(XacmlTypes.YEARMONTHDURATION){
			@Override
			public JsonElement toJson(AttributeExp v, JsonSerializationContext ctx) {
				return new JsonPrimitive(TypeToString.Types.YEARMONTHDURATION.toString(v));
			}

			@Override
			public AttributeExp fromJson(JsonElement v, JsonDeserializationContext ctx) {
				return TypeToString.Types.YEARMONTHDURATION.fromString(v.getAsString());
			}
		},
		ENTITY(XacmlTypes.ENTITY){
			@Override
			public JsonElement toJson(AttributeExp v, JsonSerializationContext ctx) {
				Entity entity = ((EntityExp)v).getValue();
				JsonObject o = new JsonObject();
				if(entity.hasContent()){
					o.addProperty(JsonProperties.CONTENT_PROPERTY, nodeToString(entity.getContent()));
				}
				o.add(JsonProperties.ATTRIBUTE_PROPERTY, ctx.serialize(entity.getAttributes()));
				return o;
			}

			@Override
			public AttributeExp fromJson(JsonElement v, JsonDeserializationContext ctx) {
				throw new UnsupportedOperationException();
			}
		};
	
		private final static TransformerFactory transformerFactory = TransformerFactory.newInstance();
		private final static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		
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
		
		
		private static String nodeToString(Node node) {
			if (node == null) {
				return null;
			}
			final Transformer transformer;
			try {
				transformer = transformerFactory.newTransformer();
			} catch (TransformerConfigurationException e) {
				throw new IllegalStateException(String.format("Failed to build %s", Transformer.class.getName()), e);
			}

			DOMSource source = new DOMSource(node);
			StreamResult result = new StreamResult(new StringWriter());
			try {
				transformer.transform(source, result);
				return result.getWriter().toString();
			} catch (TransformerException e) {
				// TODO: should the content serialization be fatal?
				throw new IllegalArgumentException("Failed to serialize Node to String.", e);
			}
	}
	}
}
