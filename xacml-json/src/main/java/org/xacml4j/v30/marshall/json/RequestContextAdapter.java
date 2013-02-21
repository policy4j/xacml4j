package org.xacml4j.v30.marshall.json;

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

final class RequestContextAdapter implements JsonDeserializer<RequestContext>, JsonSerializer<RequestContext>
{

	@Override
	public RequestContext deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		boolean returnPolicyIdList = GsonUtil.getAsBoolean(o, "ReturnPolicyIdList", false);
		boolean combinedDecision = GsonUtil.getAsBoolean(o, "CombinedDecision", false);
		Collection<Attributes> attributes = context.deserialize(o.getAsJsonArray("Attributes"), new TypeToken<Collection<Attributes>>(){}.getType());
		Collection<RequestReference> reqRef = context.deserialize(o.getAsJsonArray("multipleRequests"), new TypeToken<Collection<RequestReference>>(){}.getType());
		return new RequestContext(returnPolicyIdList, combinedDecision, attributes, reqRef);
	}

	@Override
	public JsonElement serialize(RequestContext src, Type typeOfSrc,
			JsonSerializationContext context) {
		
		JsonObject o = new JsonObject();
		o.addProperty("ReturnPolicyIdList", src.isReturnPolicyIdList());
		o.addProperty("CombinedDecision", src.isCombinedDecision());
		o.add("Attributes", context.serialize(src.getAttributes()));
		o.add("MultipleRequests", context.serialize(src.getRequestReferences()));
		return o;
	}
	
	
}
