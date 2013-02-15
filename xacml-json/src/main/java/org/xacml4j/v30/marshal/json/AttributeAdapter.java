package org.xacml4j.v30.marshal.json;

import java.lang.reflect.Type;
import java.util.Collection;

import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeExp;

import com.google.common.base.Preconditions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

class AttributeAdapter implements JsonDeserializer<Attribute>, JsonSerializer<Attribute>
{

	@Override
	public Attribute deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		String attrId = GsonUtil.getAsString(o, "attrId", null);
		String issuer = GsonUtil.getAsString(o, "issuer", null);
		boolean inclInRes = GsonUtil.getAsBoolean(o, "inclInRes", false);
		Collection<AttributeExp> values = context.deserialize(o.get("values"),  new TypeToken<Collection<AttributeExp>>(){}.getType());
		Preconditions.checkState(values != null && !values.isEmpty());
		return Attribute
				.builder(attrId)
				.issuer(issuer)
				.includeInResult(inclInRes)
				.values(values)
				.build();
	}

	@Override
	public JsonElement serialize(Attribute src, Type typeOfSrc,
			JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		o.addProperty("attrId", src.getAttributeId());
		if(src.getIssuer() != null){
			o.addProperty("issuer", src.getIssuer());
		}
		o.addProperty("inclInRes", src.isIncludeInResult());
		o.add("values", context.serialize(src.getValues(), new TypeToken<Collection<AttributeExp>>(){}.getType()));
		return o;
	}
}
