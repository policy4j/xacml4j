package com.artagon.xacml.v30.marshall.json;

import java.lang.reflect.Type;
import java.util.Collection;

import com.artagon.xacml.v30.Advice;
import com.artagon.xacml.v30.Attributes;
import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.Obligation;
import com.artagon.xacml.v30.pdp.Result;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

public class ResultAdapter implements JsonDeserializer<Result>
{

	@Override
	public Result deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject o = json.getAsJsonObject(); 
		Collection<Advice> advice = context.deserialize(o.get("assocAdvice"), new TypeToken<Collection<Advice>>(){}.getType());
		Collection<Obligation> obligations = context.deserialize(o.get("obligations"), new TypeToken<Collection<Obligation>>(){}.getType());
		Collection<Attributes> attributes = context.deserialize(o.get("attributes"), new TypeToken<Collection<Obligation>>(){}.getType());
		Decision decision = Decision.parse(o.getAsJsonPrimitive("decision").toString());
		return null;
	}
	
}
