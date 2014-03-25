package org.xacml4j.v30.marshal.json;

import java.lang.reflect.Type;

import org.xacml4j.v30.AttributeExp;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

class AttributeExpDeserializer implements JsonDeserializer<AttributeExp>
{
	@Override
	public AttributeExp deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		String typeId = GsonUtil.getAsString(o, "type", null);
		Optional<TypeToGSon> toGson = TypeToGSon.JsonTypes.getIndex().get(typeId);
		Preconditions.checkState(toGson.isPresent());
		return toGson.get().fromJson(json);
	}
}
