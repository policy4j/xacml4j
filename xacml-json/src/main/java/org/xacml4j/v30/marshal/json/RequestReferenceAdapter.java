package org.xacml4j.v30.marshal.json;

import java.lang.reflect.Type;
import java.util.Collection;

import org.xacml4j.v30.AttributesReference;
import org.xacml4j.v30.RequestReference;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

final class RequestReferenceAdapter implements JsonDeserializer<RequestReference>, JsonSerializer<RequestReference> {
	private static final String REFERENCE_ID_PROPERTY = "ReferenceId";

	@Override
	public RequestReference deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		Collection<AttributesReference> refs = context.deserialize(o.getAsJsonArray(REFERENCE_ID_PROPERTY),
				new TypeToken<Collection<AttributesReference>>() {
				}.getType());
		return RequestReference.builder().reference(refs).build();
	}

	@Override
	public JsonElement serialize(RequestReference src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		o.add(REFERENCE_ID_PROPERTY, context.serialize(src.getReferencedAttributes()));
		return o;
	}

}
