package org.xacml4j.v30.marshal.json;

import java.lang.reflect.Type;

import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.types.Types;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

class AttributeExpSerializer extends Support implements JsonSerializer<AttributeExp> {
	
	public AttributeExpSerializer(Types types) {
		super(types);
	}
	
	@Override
	public JsonElement serialize(AttributeExp src, Type typeOfSrc, JsonSerializationContext context) {
		TypeToGSon toGson = types.getCapability(src.getType(), TypeToGSon.class);
		Preconditions.checkState(toGson != null);
		return toGson.toJson(types, src);
	}

}
