package com.artagon.xacml.v30.marshall.json;

import java.lang.reflect.Type;

import com.artagon.xacml.v30.Status;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class StatusAdapter implements JsonSerializer<Status>, JsonDeserializer<Status>
{

	@Override
	public Status deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		return null;
	}

	@Override
	public JsonElement serialize(Status src, Type typeOfSrc,
			JsonSerializationContext context) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
