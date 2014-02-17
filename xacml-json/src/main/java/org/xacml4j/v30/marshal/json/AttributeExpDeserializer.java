package org.xacml4j.v30.marshal.json;

import java.lang.reflect.Type;

import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.types.Types;

import com.google.common.base.Preconditions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

class AttributeExpDeserializer extends Support implements JsonDeserializer<AttributeExp>
{
	public AttributeExpDeserializer(Types registry){
		super(registry);
	}

	@Override
	public AttributeExp deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		String typeId = GsonUtil.getAsString(o, "type", null);
		TypeToGSon gson = types.getCapability(typeId, TypeToGSon.class);
		Preconditions.checkState(gson != null);
		return gson.fromJson(types, json);
	}
}
