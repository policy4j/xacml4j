package org.xacml4j.v30.marshal.json;

import java.lang.reflect.Type;
import java.util.Collection;

import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

public class ResponseContextAdapter implements JsonDeserializer<ResponseContext>, JsonSerializer<ResponseContext> {
	private static final String RESULT_PROPERTY = "Result";

	private static final Type RESULTS_TYPE = new TypeToken<Collection<Result>>() {
	}.getType();

	@Override
	public ResponseContext deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		Collection<Result> r = context.deserialize(o.get(RESULT_PROPERTY), RESULTS_TYPE);
		return ResponseContext.builder().results(r).build();
	}

	@Override
	public JsonElement serialize(ResponseContext src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		System.out.println("Response marsahall");
		o.add(RESULT_PROPERTY, context.serialize(src.getResults()));
		return o;
	}

}
