package com.artagon.xacml.v30.marshall.json;

import java.lang.reflect.Type;
import java.util.Collection;

import com.artagon.xacml.v30.AttributesReference;
import com.artagon.xacml.v30.RequestReference;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

final class RequestReferenceAdapter implements JsonDeserializer<RequestReference>, JsonSerializer<RequestReference>
{
	@Override
	public RequestReference deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		Collection<AttributesReference> refs = context.deserialize(json.getAsJsonArray(), 
				new TypeToken<Collection<AttributesReference>>(){}.getType());
		RequestReference ref = new RequestReference(refs);
		return ref;
	}
	
	@Override
	public JsonElement serialize(RequestReference src, Type typeOfSrc,
			JsonSerializationContext context) {
		JsonArray refs = new JsonArray();
		for(AttributesReference ref : src.getReferencedAttributes()){
			refs.add(context.serialize(ref));
		}
		return refs;
	}
}