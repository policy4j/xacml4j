package org.xacml4j.v30.marshal.json;

import java.lang.reflect.Type;

import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.types.Types;

import com.google.common.base.Preconditions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

class AttributeExpDeserializer implements JsonDeserializer<AttributeExp>
{
	private Types typesRegistry;

	public AttributeExpDeserializer(Types registry){
		Preconditions.checkNotNull(registry);
		this.typesRegistry = registry;
	}

	@Override
	public AttributeExp deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		AttributeExpType type = typesRegistry.getType(GsonUtil.getAsString(o, "type", null));
		Preconditions.checkState(type != null);
		AttributeExp v = type.fromXacmlString(o.get("value").getAsString());
		return v;
	}
}
