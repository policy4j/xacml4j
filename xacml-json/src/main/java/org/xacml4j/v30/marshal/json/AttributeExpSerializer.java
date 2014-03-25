package org.xacml4j.v30.marshal.json;

import java.lang.reflect.Type;

import org.xacml4j.v30.AttributeExp;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

class AttributeExpSerializer implements JsonSerializer<AttributeExp> 
{
	
	@Override
	public JsonElement serialize(AttributeExp src, Type typeOfSrc, JsonSerializationContext context) {
		Optional<TypeToGSon> toGson = TypeToGSon.JsonTypes.getIndex().get(src.getType());
		Preconditions.checkState(toGson.isPresent());
		return toGson.get().toJson(src);
	}

}
