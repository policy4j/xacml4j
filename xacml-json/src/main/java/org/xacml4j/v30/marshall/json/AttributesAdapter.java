package org.xacml4j.v30.marshall.json;

import java.lang.reflect.Type;
import java.util.Collection;

import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.Attributes;
import org.xacml4j.v30.XacmlSyntaxException;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

class AttributesAdapter implements JsonDeserializer<Attributes>, JsonSerializer<Attributes>
{
	@Override
	public Attributes deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		try{
			JsonObject o = json.getAsJsonObject();
			Collection<Attribute> attr = context.deserialize(o.getAsJsonArray("attributes"), new TypeToken<Collection<Attribute>>(){}.getType());
			return Attributes
					.builder( AttributeCategories.parse(GsonUtil.getAsString(o, "category", null)))
					.id(GsonUtil.getAsString(o, "id", null))
					.attributes(attr)
					.build();
		}catch(XacmlSyntaxException e){
			throw new JsonParseException(e);
		}
	}

	@Override
	public JsonElement serialize(Attributes src, Type typeOfSrc,
			JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		if(src.getId() != null){
			o.addProperty("id", src.getId());
		}
		o.addProperty("category", src.getCategory().getId());
		o.add("attributes", context.serialize(src.getAttributes()));
		return o;
	}
}
