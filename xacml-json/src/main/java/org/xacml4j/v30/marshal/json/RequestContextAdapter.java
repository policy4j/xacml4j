package org.xacml4j.v30.marshal.json;

import java.lang.reflect.Type;
import java.util.Collection;

import org.xacml4j.v30.Category;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.RequestReference;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

final class RequestContextAdapter implements JsonDeserializer<RequestContext>, JsonSerializer<RequestContext> {

	private static final String REQUEST_REFERENCE_PROPERTY = "RequestReference";
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
		Collection<Category> attributes = context.deserialize(o.getAsJsonArray(ATTRIBUTES_PROPERTY),
				new TypeToken<Collection<Category>>() {
				}.getType());

		Collection<RequestReference> reqRefs = ImmutableList.of();
		JsonObject multiRequests = o.getAsJsonObject(MULTI_REQUESTS_PROPERTY);
		if (multiRequests != null) {
			reqRefs = context.deserialize(multiRequests.getAsJsonArray(REQUEST_REFERENCE_PROPERTY),
					new TypeToken<Collection<RequestReference>>() {
					}.getType());
		}

		return new RequestContext(returnPolicyIdList, combinedDecision, attributes, reqRefs);
	}

	@Override
	public JsonElement serialize(RequestContext src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		o.addProperty(RETURN_POLICY_ID_LIST_PROPERTY, src.isReturnPolicyIdList());
		o.addProperty(COMBINED_DECISION_PROPERTY, src.isCombinedDecision());
		// TODO: add support for predefined Attributes objects: Subject, Action, Resource, Environment
		o.add(ATTRIBUTES_PROPERTY, context.serialize(src.getAttributes()));
		// SPEC: There must be at least one RequestReference object inside the MultiRequests object
		Collection<RequestReference> requestReferences = src.getRequestReferences();
		if (requestReferences != null && !requestReferences.isEmpty()) {
			JsonObject multiRequests = new JsonObject();
			multiRequests.add(REQUEST_REFERENCE_PROPERTY, context.serialize(requestReferences));
			o.add(MULTI_REQUESTS_PROPERTY, multiRequests);
		}
		return o;
	}

}
