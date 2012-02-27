package com.artagon.xacml.v30.marshall.json;

import java.lang.reflect.Type;

import com.artagon.xacml.v30.pdp.AttributesReference;
import com.google.common.base.Preconditions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

final class AttributesRefererenceAdapater implements JsonDeserializer<AttributesReference>, JsonSerializer<AttributesReference>
{
	@Override
	public AttributesReference deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject ref = json.getAsJsonObject();
		JsonElement element = ref.get("$ref");
		Preconditions.checkNotNull(element);
		return new AttributesReference(element.getAsString());
	}

	@Override
	public JsonElement serialize(AttributesReference src, Type typeOfSrc,
			JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		o.addProperty("$ref", src.getReferenceId());
		return o;
	}	
}

