package org.xacml4j.v30.marshal.json;

import java.lang.reflect.Type;

import org.xacml4j.v30.AttributeExp;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

class AttributeExpSerializer implements JsonSerializer<AttributeExp> {
	@Override
	public JsonElement serialize(AttributeExp src, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonPrimitive(src.toXacmlString());
	}

}
