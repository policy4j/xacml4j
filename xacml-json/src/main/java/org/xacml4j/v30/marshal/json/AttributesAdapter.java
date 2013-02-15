package org.xacml4j.v30.marshal.json;

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

class AttributesAdapter implements JsonDeserializer<Attributes>, JsonSerializer<Attributes> {
	private static final String CATEGORY_PROPERTY = "Category";
	private static final String ID_PROPERTY = "Id";
	private static final String CONTENT_PROPERTY = "Content";
	private static final String ATTRIBUTE_PROPERTY = "Attribute";

	@Override
	public Attributes deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		try {
			JsonObject o = json.getAsJsonObject();
			Collection<Attribute> attr = context.deserialize(o.getAsJsonArray(ATTRIBUTE_PROPERTY),
					new TypeToken<Collection<Attribute>>() {
					}.getType());
			return Attributes.builder(AttributeCategories.parse(GsonUtil.getAsString(o, CATEGORY_PROPERTY, null)))
					.id(GsonUtil.getAsString(o, ID_PROPERTY, null)).attributes(attr).build();
		} catch (XacmlSyntaxException e) {
			throw new JsonParseException(e);
		}
	}

	@Override
	public JsonElement serialize(Attributes src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		if (src.getId() != null) {
			o.addProperty(ID_PROPERTY, src.getId());
		}
		o.addProperty(CATEGORY_PROPERTY, src.getCategory().getId());
		o.add(ATTRIBUTE_PROPERTY, context.serialize(src.getAttributes()));
		return o;
	}

}
