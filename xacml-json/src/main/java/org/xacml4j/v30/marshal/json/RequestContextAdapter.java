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

final class RequestContextAdapter implements JsonDeserializer<RequestContext>, JsonSerializer<RequestContext>
{

	@Override
	public RequestContext deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		boolean returnPolicyIdList = GsonUtil.getAsBoolean(o, "returnPolicyIdList", false);
		boolean combinedDecision = GsonUtil.getAsBoolean(o, "combinedDecision", false);
		Collection<Attributes> attributes = context.deserialize(o.getAsJsonArray("attributes"), new TypeToken<Collection<Attributes>>(){}.getType());
		Collection<RequestReference> reqRef = context.deserialize(o.getAsJsonArray("multipleRequests"), new TypeToken<Collection<RequestReference>>(){}.getType());
		return new RequestContext(returnPolicyIdList, combinedDecision, attributes, reqRef);
	}

	@Override
	public JsonElement serialize(RequestContext src, Type typeOfSrc,
			JsonSerializationContext context) {
		
		JsonObject o = new JsonObject();
		o.addProperty("returnPolicyIdList", src.isReturnPolicyIdList());
		o.addProperty("combinedDecision", src.isCombinedDecision());
		o.add("attributes", context.serialize(src.getAttributes()));
		o.add("multipleRequests", context.serialize(src.getRequestReferences()));
		return o;
	}
	
	
}
