package org.xacml4j.v30.marshal.json;

/*
 * #%L
 * Artagon XACML 3.0 Gson integration
 * %%
 * Copyright (C) 2009 - 2014 Artagon
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

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Type;
import java.util.Collection;

import org.xacml4j.v30.Advice;
import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.BaseDecisionRuleResponse;
import org.xacml4j.v30.Obligation;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

public class ObligationOrAdviceAdapter implements JsonSerializer<BaseDecisionRuleResponse>,
		JsonDeserializer<BaseDecisionRuleResponse> {

	private static final String ID_PROPERTY = "Id";
	private static final String ATTRIBUTE_ASSIGNMENTS_PROPERTY = "AttributeAssignment";

	private static final Type ATTRIBUTE_ASSIGNMENTS_TYPE = new TypeToken<Collection<AttributeAssignment>>() {
	}.getType();

	@Override
	public BaseDecisionRuleResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		String id = checkNotNull(GsonUtil.getAsString(o, ID_PROPERTY, null));

		Collection<AttributeAssignment> attributeAssignments = context.deserialize(
				o.get(ATTRIBUTE_ASSIGNMENTS_PROPERTY), ATTRIBUTE_ASSIGNMENTS_TYPE);

		if (typeOfT == Obligation.class) {
			Obligation.Builder builder = Obligation.builder(id);
			return attributeAssignments != null ? builder.attributes(attributeAssignments).build() : builder.build();
		} else if (typeOfT == Advice.class) {
			Advice.Builder builder = Advice.builder(id);
			return attributeAssignments != null ? builder.attributes(attributeAssignments).build() : builder.build();
		} else {
			throw new IllegalArgumentException(String.format("Invalid type: %s", typeOfT));
		}
	}

	@Override
	public JsonElement serialize(BaseDecisionRuleResponse src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		o.addProperty(ID_PROPERTY, src.getId());
		Collection<AttributeAssignment> attributeAssignments = src.getAttributes();
		if (attributeAssignments != null && !attributeAssignments.isEmpty()) {
			o.add(ATTRIBUTE_ASSIGNMENTS_PROPERTY, context.serialize(attributeAssignments, ATTRIBUTE_ASSIGNMENTS_TYPE));
		}

		return o;
	}

}
