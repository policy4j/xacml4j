package org.xacml4j.v30.marshal.json;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Type;

import org.xacml4j.v30.StatusCode;
import org.xacml4j.v30.StatusCodeIds;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class StatusCodeAdapter implements JsonSerializer<StatusCode>, JsonDeserializer<StatusCode> {

	private static final String VALUE_PROPERTY = "Value";
	private static final String STATUS_CODE_PROPERTY = "StatusCode";

	@Override
	public StatusCode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		String value = GsonUtil.getAsString(o, VALUE_PROPERTY, null);
		checkNotNull(value);
		StatusCode.Builder builder = StatusCode.builder(StatusCodeIds.parse(value));

		StatusCode embededStatusCode = context.deserialize(o.getAsJsonObject(STATUS_CODE_PROPERTY), StatusCode.class);
		if (embededStatusCode != null) {
			builder.minorStatus(embededStatusCode);
		}

		return builder.build();
	}

	@Override
	public JsonElement serialize(StatusCode src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		o.addProperty(VALUE_PROPERTY, src.getValue().toString());
		if (src.getMinorStatus() != null) {
			o.add(STATUS_CODE_PROPERTY, context.serialize(src.getMinorStatus()));
		}

		return o;
	}

}
