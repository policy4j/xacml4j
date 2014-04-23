package org.xacml4j.v30.marshal.json;

import java.lang.reflect.Type;

import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCode;
import org.xacml4j.v30.StatusDetail;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class StatusAdapter implements JsonSerializer<Status>, JsonDeserializer<Status> {

	private static final String STATUS_MESSAGE_PROPERTY = "StatusMessage";
	private static final String STATUS_CODE_PROPERTY = "StatusCode";

	@Override
	public Status deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		StatusDetail detail = null;
		String statusMessage = GsonUtil.getAsString(o, STATUS_MESSAGE_PROPERTY, null);
		StatusCode code = context.deserialize(o.get(STATUS_CODE_PROPERTY), StatusCode.class);
		return Status
				.builder(code)
				.message(statusMessage)
				.detail(detail)
				.build();
	}

	@Override
	public JsonElement serialize(Status src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		o.addProperty(STATUS_MESSAGE_PROPERTY, src.getMessage());
		// TODO: serialize status detail
		o.add(STATUS_CODE_PROPERTY, context.serialize(src.getStatusCode()));
		return o;
	}

}
