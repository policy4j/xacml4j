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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.util.DOMUtil;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.RequestReference;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

final class RequestContextAdapter implements JsonDeserializer<RequestContext>, JsonSerializer<RequestContext>
{
    private final static Logger log = LoggerFactory.getLogger(RequestContextAdapter.class);

    private static final String REQUEST_PROPERTY = "Request";
	private static final String REQUEST_REFERENCE_PROPERTY = "RequestReference";
	private static final String RETURN_POLICY_ID_LIST_PROPERTY = "ReturnPolicyIdList";
	private static final String COMBINED_DECISION_PROPERTY = "CombinedDecision";
	private static final String MULTI_REQUESTS_PROPERTY = "MultipleRequests";
    private static final String CATEGORY_ARRAY_NAME = "Category";

	@Override
	public RequestContext deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject reqJson1 = json.getAsJsonObject();
        JsonObject reqJson = reqJson1.getAsJsonObject(REQUEST_PROPERTY);
        if(reqJson == null){
            throw new JsonParseException(
                    "Failed to locate Request property");
        }
        Boolean returnPolicyIdList = GsonUtil.getAsBoolean(reqJson, RETURN_POLICY_ID_LIST_PROPERTY, null);
		Boolean combinedDecision = GsonUtil.getAsBoolean(reqJson, COMBINED_DECISION_PROPERTY, null);
		JsonArray array = reqJson.getAsJsonArray(CATEGORY_ARRAY_NAME);
        ImmutableList.Builder<Category> builder = ImmutableList.builder();
        if(array != null){
            for(int i = 0; i < array.size(); i++) {
                JsonObject categoryObject = array.get(i).getAsJsonObject();
                CategoryId categoryId = CategoryAdapter.getCategoryId(categoryObject);
                builder.add(CategoryAdapter.deserializeCategory(categoryId, categoryObject, context));
            }
        }
        for(String categoryId : Categories.getCategoryShortNames()){
            JsonObject categoryJson = reqJson.getAsJsonObject(categoryId);
            if(categoryJson != null){
                builder.add(CategoryAdapter.deserializeCategory(
                        Categories.parse(categoryId),
                        categoryJson, context));
            }
        }
        Collection<Category> categories = builder.build();
		Collection<RequestReference> reqRefs = ImmutableList.of();
		JsonObject multiRequests = reqJson.getAsJsonObject(MULTI_REQUESTS_PROPERTY);
		if(multiRequests != null) {
			reqRefs = context.deserialize(multiRequests.getAsJsonArray(REQUEST_REFERENCE_PROPERTY),
					new TypeToken<Collection<RequestReference>>() {
					}.getType());
		}
		return RequestContext.builder()
				.returnPolicyIdList(returnPolicyIdList)
				.combineDecision(combinedDecision)
				.attributes(categories)
				.reference(reqRefs)
				.build();
	}

	@Override
	public JsonElement serialize(RequestContext src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject o = new JsonObject();
        JsonObject reqJson = new JsonObject();
		reqJson.addProperty(RETURN_POLICY_ID_LIST_PROPERTY, src.isReturnPolicyIdList());
		reqJson.addProperty(COMBINED_DECISION_PROPERTY, src.isCombinedDecision());
        Collection<Category> defaultCategories = Categories.getDefaultCategories(src.getAttributes());
        Collection<Category> customCategories = Categories.getCustomCategories(src.getAttributes());
        // serialize custom categories to array
        if(customCategories != null &&
                !customCategories.isEmpty()){
            reqJson.add(JsonProperties.CATEGORY_ARRAY_NAME,
                    context.serialize(customCategories));
        }
        // serialize default categories
        for(Category category : defaultCategories){
            reqJson.add(category.getCategoryId().getShortName(),
                    CategoryAdapter.serializeDefaultCategory(category, context));
        }
		Collection<RequestReference> requestReferences = src.getRequestReferences();
		if (requestReferences != null &&
                !requestReferences.isEmpty()) {
			JsonObject multiRequests = new JsonObject();
			multiRequests.add(REQUEST_REFERENCE_PROPERTY, context.serialize(requestReferences));
			reqJson.add(MULTI_REQUESTS_PROPERTY, multiRequests);
		}
		o.add(REQUEST_PROPERTY, reqJson);
        return o;
	}


    private JsonObject serialize(Category src,
                                JsonSerializationContext context) {
        JsonObject o = new JsonObject();
        if (src.getId() != null) {
            o.addProperty(JsonProperties.ID_PROPERTY, src.getId());
        }
        o.addProperty(JsonProperties.CATEGORY_ID_PROPERTY, src.getCategoryId().getShortName());
        o.addProperty(JsonProperties.CONTENT_PROPERTY, DOMUtil.nodeToString(src.getEntity().getContent()));
        o.add(JsonProperties.ATTRIBUTE_PROPERTY, context.serialize(src.getEntity().getAttributes()));
        return o;
    }

}
