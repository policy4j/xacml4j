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

	@Override
	public Status deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		String statusMessage = GsonUtil.getAsString(o, "StatusMessage", null);
		// TODO: status detail
		StatusDetail statusDetail = null;
		StatusCode statusCode = context.deserialize(o.get("StatusCode"), StatusCode.class);
		return new Status(statusCode, statusMessage, statusDetail);
	}

	@Override
	public JsonElement serialize(Status src, Type typeOfSrc, JsonSerializationContext context) {
		// TODO Auto-generated method stub
		return null;
	}

}
