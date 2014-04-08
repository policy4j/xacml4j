package org.xacml4j.v30.marshal.json;

import java.lang.reflect.Type;

import org.xacml4j.v30.CategoryReference;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

final class AttributesRefererenceAdapater implements JsonDeserializer<CategoryReference>,
		JsonSerializer<CategoryReference> {
	@Override
	public CategoryReference deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		String referenceId = json.getAsJsonPrimitive().getAsString();
		return CategoryReference.builder().id(referenceId).build();
	}

	@Override
	public JsonElement serialize(CategoryReference src, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonPrimitive(src.getReferenceId());
	}

}
