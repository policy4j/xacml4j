package org.xacml4j.v30.marshal.json;

import java.lang.reflect.Type;
import java.util.Collection;

import org.xacml4j.v30.Attributes;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.RequestReference;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

final class RequestContextAdapter implements JsonDeserializer<RequestContext>, JsonSerializer<RequestContext> {

	private static final String RETURN_POLICY_ID_LIST_PROPERTY = "ReturnPolicyIdList";
	private static final String COMBINED_DECISION_PROPERTY = "CombinedDecision";
	private static final String ATTRIBUTES_PROPERTY = "Attributes";
	private static final String MULTI_REQUESTS_PROPERTY = "MultiRequests";

	@Override
	public RequestContext deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		boolean returnPolicyIdList = GsonUtil.getAsBoolean(o, RETURN_POLICY_ID_LIST_PROPERTY, false);
		boolean combinedDecision = GsonUtil.getAsBoolean(o, COMBINED_DECISION_PROPERTY, false);
		Collection<Attributes> attributes = context.deserialize(o.getAsJsonArray(ATTRIBUTES_PROPERTY),
				new TypeToken<Collection<Attributes>>() {
				}.getType());
		Collection<RequestReference> reqRef = context.deserialize(o.getAsJsonArray(MULTI_REQUESTS_PROPERTY),
				new TypeToken<Collection<RequestReference>>() {
				}.getType());
		return new RequestContext(returnPolicyIdList, combinedDecision, attributes, reqRef);
	}

	@Override
	public JsonElement serialize(RequestContext src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		o.addProperty(RETURN_POLICY_ID_LIST_PROPERTY, src.isReturnPolicyIdList());
		o.addProperty(COMBINED_DECISION_PROPERTY, src.isCombinedDecision());
		o.add(ATTRIBUTES_PROPERTY, context.serialize(src.getAttributes()));
		o.add(MULTI_REQUESTS_PROPERTY, context.serialize(src.getRequestReferences()));
		return o;
	}

}
