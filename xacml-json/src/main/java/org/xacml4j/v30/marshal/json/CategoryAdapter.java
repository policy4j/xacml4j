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

import org.w3c.dom.Node;
import org.xacml4j.util.DOMUtil;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.XacmlSyntaxException;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;


public class CategoryAdapter implements JsonDeserializer<Category>, JsonSerializer<Category>
{

    @Override
    public Category deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        return deserializeCategory(getCategoryId(json), json, context);
    }

    @Override
    public JsonElement serialize(Category src, Type typeOfSrc, JsonSerializationContext context) {
        return serialize(src, context);
    }

    public static JsonObject serialize(Category src,
                                 JsonSerializationContext context) {
        JsonObject o = new JsonObject();
        if (src.getId() != null) {
            o.addProperty(JsonProperties.ID_PROPERTY, src.getId());
        }
        Entity e = src.getEntity();
        o.addProperty(JsonProperties.CATEGORY_ID_PROPERTY, src.getCategoryId().getShortName());
        o.addProperty(JsonProperties.CONTENT_PROPERTY, DOMUtil.nodeToString(e.getContent()));
        o.add(JsonProperties.ATTRIBUTE_PROPERTY, context.serialize(e.getAttributes()));
        return o;
    }

    public static JsonObject serializeDefaultCategory(Category src,
                                       JsonSerializationContext context) {
        JsonObject o = new JsonObject();
        if (src.getId() != null) {
            o.addProperty(JsonProperties.ID_PROPERTY, src.getId());
        }
        Entity e = src.getEntity();
        o.addProperty(JsonProperties.CONTENT_PROPERTY, DOMUtil.nodeToString(e.getContent()));
        o.add(JsonProperties.ATTRIBUTE_PROPERTY, context.serialize(e.getAttributes()));
        return o;
    }

    public static Category deserializeCategory(CategoryId category,
                                        JsonElement json, JsonDeserializationContext context)
            throws JsonParseException {
        try {
            Preconditions.checkNotNull(category);
            JsonObject o = json.getAsJsonObject();
            String id = GsonUtil.getAsString(o, JsonProperties.ID_PROPERTY, null);
            Collection<Attribute> attr = context.deserialize(o.getAsJsonArray(JsonProperties.ATTRIBUTE_PROPERTY),
                    new TypeToken<Collection<Attribute>>() {
                    }.getType());
            Node content = DOMUtil.stringToNode(GsonUtil.getAsString(o, JsonProperties.CONTENT_PROPERTY, null));
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

    public static CategoryId getCategoryId(final JsonElement e){
        JsonObject o = e.getAsJsonObject();
        return Categories.parse(new Supplier<String>() {
            @Override
            public String get() {
                JsonObject o = e.getAsJsonObject();
                Preconditions.checkState(o != null);
                return GsonUtil.getAsString(o,
                        JsonProperties.CATEGORY_ID_PROPERTY, null);
            }
        });
    }
}
