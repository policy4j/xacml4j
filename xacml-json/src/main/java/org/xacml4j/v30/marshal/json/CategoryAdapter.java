package org.xacml4j.v30.marshal.json;

import java.lang.reflect.Type;
import java.util.Collection;

import org.w3c.dom.Node;
import org.xacml4j.util.DOMUtil;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.XacmlSyntaxException;

import com.google.common.collect.ImmutableBiMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

class CategoryAdapter implements JsonDeserializer<Category>, JsonSerializer<Category> 
{	
	private final static ImmutableBiMap<String, CategoryId> SHORT_NAMES = 
			ImmutableBiMap.<String, CategoryId>builder()
			.put("action", Categories.ACTION)
			.put("enviroment", Categories.ENVIRONMENT)
			.put("resource", Categories.RESOURCE)
			.put("subject", Categories.SUBJECT_ACCESS)
			.put("codebase", Categories.SUBJECT_CODEBASE)
			.put("intermediary-subject", Categories.SUBJECT_INTERMEDIARY)
			.put("recipient-subject", Categories.SUBJECT_RECIPIENT)
			.put("requesting-machine", Categories.SUBJECT_REQUESTING_MACHINE)
			.build();
	
	@Override
	public Category deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		try {
			JsonObject o = json.getAsJsonObject();
			Collection<Attribute> attr = context.deserialize(o.getAsJsonArray(JsonProperties.ATTRIBUTE_PROPERTY),
					new TypeToken<Collection<Attribute>>() {
					}.getType());
			String categoryId = GsonUtil.getAsString(o, JsonProperties.CATEGORY_PROPERTY, null);
			CategoryId category = SHORT_NAMES.get(categoryId);
			category =  (category == null)?Categories.parse(categoryId):category;
			Node content = DOMUtil.stringToNode(GsonUtil.getAsString(o, JsonProperties.CONTENT_PROPERTY, null));
			String id = GsonUtil.getAsString(o, JsonProperties.ID_PROPERTY, null);
			return Category.builder(category)
					.id(id)
					.entity(Entity
							.builder() 
							.attributes(attr)
							.content(content)
							.build())
					.build();
		} catch (XacmlSyntaxException e) {
			throw new JsonParseException(e);
		}
	}
	
	@Override
	public JsonElement serialize(Category src, Type typeOfSrc, 
			JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		if (src.getId() != null) {
			o.addProperty(JsonProperties.ID_PROPERTY, src.getId());
		}
		Entity e = src.getEntity();
		String categoryId = SHORT_NAMES.inverse().get(src.getCategoryId());
		o.addProperty(JsonProperties.CATEGORY_PROPERTY, (categoryId == null)?src.getCategoryId().getId():categoryId);
		o.addProperty(JsonProperties.CONTENT_PROPERTY, DOMUtil.nodeToString(e.getContent()));
		o.add(JsonProperties.ATTRIBUTE_PROPERTY, context.serialize(e.getAttributes()));
		return o;
	}
}
