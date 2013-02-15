package org.xacml4j.v30.marshal.json;

import java.lang.reflect.Type;

import org.xacml4j.v30.AttributeExp;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

class AttributeExpSerializer implements JsonSerializer<AttributeExp>
{	
	@Override
	public JsonElement serialize(AttributeExp src, Type typeOfSrc,
			JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		o.addProperty("type", src.getType().getDataTypeId());
		o.addProperty("value", src.toXacmlString());
		return o;
	}
}
