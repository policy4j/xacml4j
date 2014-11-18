package org.xacml4j.v30.marshal.json;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.w3c.dom.Node;
import org.xacml4j.util.DOMUtil;
import org.xacml4j.v30.*;

import java.lang.reflect.Type;
import java.util.Collection;


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
        if (src.getReferenceId() != null) {
            o.addProperty(JsonProperties.ID_PROPERTY, src.getReferenceId());
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
        if (src.getReferenceId() != null) {
            o.addProperty(JsonProperties.ID_PROPERTY, src.getReferenceId());
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
