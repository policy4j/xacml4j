package org.xacml4j.v30.marshal.json;

import java.lang.reflect.Type;
import java.util.Collection;

import org.xacml4j.v30.Advice;
import org.xacml4j.v30.Attributes;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

public class ResultAdapter implements JsonDeserializer<Result> {

	@Override
	public Result deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		Decision decision = Decision.parse(GsonUtil.getAsString(o, "Decision", null));
		Status status = context.deserialize(o.get("Status"), Status.class);

		Result.Builder builder = Result.builder(decision, status);

		Collection<Obligation> obligations = context.deserialize(o.get("Obligations"),
				new TypeToken<Collection<Obligation>>() {
				}.getType());
		if (obligations != null) {
			builder.obligation(obligations);
		}

		Collection<Advice> advice = context.deserialize(o.get("AssociatedAdvice"), new TypeToken<Collection<Advice>>() {
		}.getType());
		if (advice != null) {
			builder.advice(advice);
		}

		Collection<Attributes> attributes = context.deserialize(o.get("attributes"),
				new TypeToken<Collection<Obligation>>() {
				}.getType());
		return builder.includeInResultAttr(attributes).build();
	}

}
