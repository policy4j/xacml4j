package org.xacml4j.v30.marshal.json;

/*
 * #%L
 * Xacml4J Gson Integration
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.lang.reflect.Type;
import java.util.Collection;

import org.xacml4j.v30.*;
import org.xacml4j.v30.Entity;

import com.google.common.base.Preconditions;
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
	@Override
	public Category deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		try {
			JsonObject o = json.getAsJsonObject();
			String categoryId = GsonUtil.getAsString(o, JsonProperties.CATEGORY_ID_PROPERTY, null);
			Preconditions.checkState(categoryId != null);
			CategoryId category = CategoryId.of(categoryId);
			String id = GsonUtil.getAsString(o, JsonProperties.ID_PROPERTY, null);
			Collection<Attribute> attr = context.deserialize(o.getAsJsonArray(JsonProperties.ATTRIBUTE_PROPERTY),
					new TypeToken<Collection<Attribute>>() {
					}.getType());
			Entity.Builder entityBuilder = Entity
					.builder()
					.attributes(attr);
			String content = GsonUtil.getAsString(o,
					JsonProperties.CONTENT_PROPERTY, null);
			if(content != null){
				entityBuilder.content(content);
			}
			return Category.builder(category)
					.id(id)
					.entity(entityBuilder.build())
					.build();
		} catch (SyntaxException e) {
			throw new JsonParseException(e);
		}
	}

	@Override
	public JsonElement serialize(Category src, Type typeOfSrc,
			JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		if (src.getReferenceId() != null) {
			o.addProperty(JsonProperties.ID_PROPERTY, src.getReferenceId().orElse(null));
		}
		Entity e = src.getEntity();
		o.addProperty(JsonProperties.CATEGORY_ID_PROPERTY, src.getCategoryId().getAbbreviatedId());
		o.addProperty(JsonProperties.CONTENT_PROPERTY, e.getContent()
				.map(v->v.asString()).orElse(null));
		o.add(JsonProperties.ATTRIBUTE_PROPERTY, context.serialize(e.getAttributes()));
		return o;
	}
}
