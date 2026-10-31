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

import org.xacml4j.v30.Category;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.RequestReference;

import com.google.common.base.Preconditions;
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

final class RequestContextAdapter implements JsonDeserializer<RequestContext>, JsonSerializer<RequestContext> {

	private static final String REQUEST_REFERENCE_PROPERTY = "RequestReference";
	private static final String RETURN_POLICY_ID_LIST_PROPERTY = "ReturnPolicyIdList";
	private static final String COMBINED_DECISION_PROPERTY = "CombinedDecision";
	private static final String MULTI_REQUESTS_PROPERTY = "MultiRequests";

	@Override
	public RequestContext deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		boolean returnPolicyIdList = GsonUtil.getAsBoolean(o, RETURN_POLICY_ID_LIST_PROPERTY, false);
		boolean combinedDecision = GsonUtil.getAsBoolean(o, COMBINED_DECISION_PROPERTY, false);
		JsonArray array = o.getAsJsonArray(JsonProperties.CATEGORY_ARRAY_NAME);
		Preconditions.checkState(array != null);
		Collection<Category> attributes = context.deserialize(array,
				new TypeToken<Collection<Category>>() {
				}.getType());

		Collection<RequestReference> reqRefs = ImmutableList.of();
		JsonObject multiRequests = o.getAsJsonObject(MULTI_REQUESTS_PROPERTY);
		if (multiRequests != null) {
			reqRefs = context.deserialize(multiRequests.getAsJsonArray(REQUEST_REFERENCE_PROPERTY),
					new TypeToken<Collection<RequestReference>>() {
					}.getType());
		}

		return RequestContext.builder()
				.returnPolicyIdList(returnPolicyIdList)
				.combineDecision(combinedDecision)
				.attributes(attributes)
				.reference(reqRefs)
				.build();
	}

	@Override
	public JsonElement serialize(RequestContext src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		o.addProperty(RETURN_POLICY_ID_LIST_PROPERTY, src.isReturnPolicyIdList());
		o.addProperty(COMBINED_DECISION_PROPERTY, src.isCombinedDecision());
		// TODO: add support for predefined Attributes objects: Subject, Action, Resource, Environment
		o.add(JsonProperties.CATEGORY_ARRAY_NAME, context.serialize(src.getCategories()));
		// SPEC: There must be at least one RequestReference object inside the MultiRequests object
		Collection<RequestReference> requestReferences = src.getRequestReferences();
		if (requestReferences != null && !requestReferences.isEmpty()) {
			JsonObject multiRequests = new JsonObject();
			multiRequests.add(REQUEST_REFERENCE_PROPERTY, context.serialize(requestReferences));
			o.add(MULTI_REQUESTS_PROPERTY, multiRequests);
		}
		return o;
	}

}
