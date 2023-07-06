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

import static com.google.common.base.Preconditions.checkArgument;
import static org.xacml4j.v30.marshal.json.JsonProperties.ATTRIBUTE_ID_PROPERTY;
import static org.xacml4j.v30.marshal.json.JsonProperties.CATEGORY_PROPERTY;
import static org.xacml4j.v30.marshal.json.JsonProperties.DATA_TYPE_PROPERTY;
import static org.xacml4j.v30.marshal.json.JsonProperties.ISSUER_PROPERTY;
import static org.xacml4j.v30.marshal.json.JsonProperties.VALUE_PROPERTY;

import java.lang.reflect.Type;

import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.types.Value;

import com.google.common.base.Strings;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class AttributeAssignmentDeserializer implements JsonDeserializer<AttributeAssignment> {

	@Override
	public AttributeAssignment deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		String attrId = GsonUtil.getAsString(o, ATTRIBUTE_ID_PROPERTY, null);
		checkArgument(attrId != null, "Property '%s' is mandatory.", ATTRIBUTE_ID_PROPERTY);
		Value value = deserializeValue(context, o);
		String categoryId = GsonUtil.getAsString(o, CATEGORY_PROPERTY, null);
		CategoryId parsedCategoryId = Strings.isNullOrEmpty(categoryId)?null: CategoryId.parse(categoryId).get();
		String issuer = GsonUtil.getAsString(o, ISSUER_PROPERTY, null);
		return AttributeAssignment
				.builder()
				.attributeId(attrId)
				.category(parsedCategoryId)
				.issuer(issuer)
				.value(value)
				.build();
	}

	private Value deserializeValue(JsonDeserializationContext context, JsonObject o) {
		String dataTypeId = GsonUtil.getAsString(o, DATA_TYPE_PROPERTY, null);
		JsonElement jsonValue = o.get(VALUE_PROPERTY);
		// TODO: do a proper type coercion
		Value value = deserializeValue(dataTypeId, jsonValue, context);
		checkArgument(value != null, "Property '%s' is mandatory.", VALUE_PROPERTY);
		return value;
	}

	private Value deserializeValue(String dataTypeId, JsonElement jsonValue, JsonDeserializationContext ctx) {
		TypeToGSon toGson = TypeToGSon.forType(dataTypeId).orElse(TypeToGSon.Types.STRING);
		return toGson.fromJson(jsonValue, ctx);
	}

}
