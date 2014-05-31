package org.xacml4j.v30.marshal.json;

/*
 * #%L
 * Xacml4J Gson Integration
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */


import java.util.Collection;

import org.xacml4j.util.DOMUtil;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.types.EntityExp;
import org.xacml4j.v30.types.TypeCapability;
import org.xacml4j.v30.types.TypeToString;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.reflect.TypeToken;

public interface TypeToGSon extends TypeCapability
{
	AttributeExpType getType();
	JsonElement toJson(AttributeExp v, JsonSerializationContext ctx);
	AttributeExp fromJson(JsonElement v, JsonDeserializationContext ctx);


	public enum Types implements TypeToGSon
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
					o.addProperty(JsonProperties.CONTENT_PROPERTY, DOMUtil.nodeToString(entity.getContent()));
				}
				o.add(JsonProperties.ATTRIBUTE_PROPERTY, ctx.serialize(entity.getAttributes()));
				return o;
			}

			@Override
			public AttributeExp fromJson(JsonElement v,
					JsonDeserializationContext ctx) {
				Entity.Builder b = Entity.builder();
				JsonObject o  = v.getAsJsonObject();
				if(o.has(JsonProperties.CONTENT_PROPERTY)){
					String content = o.get(JsonProperties.CONTENT_PROPERTY).getAsString();
					b.content(DOMUtil.stringToNode(content));
				}
				if(o.has(JsonProperties.ATTRIBUTE_PROPERTY)){
					JsonArray array = o.get(JsonProperties.ATTRIBUTE_PROPERTY).getAsJsonArray();
					Collection<Attribute> attr = ctx.deserialize(array,
							new TypeToken<Collection<Attribute>>() {
							}.getType());
					b.attributes(attr);
				}
				return EntityExp.valueOf(b.build());
			}
		};

		private final static Index<TypeToGSon> INDEX = Index.<TypeToGSon>build(values());

		private AttributeExpType type;

		private Types(AttributeExpType type){
			this.type = type;
		}

		public AttributeExpType getType(){
			return type;
		}

		public static Index<TypeToGSon> getIndex(){
			return INDEX;
		}

	}
}
