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

import static com.google.common.base.Preconditions.checkArgument;
import static org.xacml4j.v30.marshal.json.JsonProperties.ATTRIBUTE_ID_PROPERTY;
import static org.xacml4j.v30.marshal.json.JsonProperties.DATA_TYPE_PROPERTY;
import static org.xacml4j.v30.marshal.json.JsonProperties.INCLUDE_IN_RESULT_PROPERTY;
import static org.xacml4j.v30.marshal.json.JsonProperties.ISSUER_PROPERTY;
import static org.xacml4j.v30.marshal.json.JsonProperties.VALUE_PROPERTY;

import java.lang.reflect.Type;
import java.util.Collection;

import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

class AttributeDeserializer implements JsonDeserializer<Attribute> 
{

	@Override
	public Attribute deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		String attrId = GsonUtil.getAsString(o, ATTRIBUTE_ID_PROPERTY, null);
		checkArgument(attrId != null, "Property '%s' is mandatory.", ATTRIBUTE_ID_PROPERTY);
		String issuer = GsonUtil.getAsString(o, ISSUER_PROPERTY, null);
		boolean inclInRes = GsonUtil.getAsBoolean(o, INCLUDE_IN_RESULT_PROPERTY, false);
		return Attribute
				.builder(attrId)
				.issuer(issuer)
				.includeInResult(inclInRes)
				.values(deserializeValue(context, o))
				.build();
	}

	private Collection<AttributeExp> deserializeValue(JsonDeserializationContext context, JsonObject o) {
		AttributeExpType type = getDataType(o);
		JsonElement jsonValue = o.get(VALUE_PROPERTY);
		checkArgument(jsonValue != null, "Property '%s' is mandatory.", 
				VALUE_PROPERTY);
		Collection<AttributeExp> values = null;
		if (jsonValue.isJsonArray()) {
			JsonArray jsonArray = jsonValue.getAsJsonArray();
			ImmutableList.Builder<AttributeExp> valuesBuilder = ImmutableList.builder();
			for (int i = 0; i < jsonArray.size(); i++) {
				valuesBuilder.add(deserializeValue(type, jsonArray.get(i), context));
			}
			values = valuesBuilder.build();
		} else {
			values = ImmutableList.of(deserializeValue(type, jsonValue, context));
		}
		checkArgument(values != null && !values.isEmpty(), "Property '%s' is mandatory.", 
				VALUE_PROPERTY);
		return values;
	}

	private AttributeExp deserializeValue(AttributeExpType type, JsonElement jsonValue, 
			JsonDeserializationContext ctx) {
		Optional<TypeToGSon> toGson = TypeToGSon.Types.getIndex().get(type);
		Preconditions.checkState(toGson.isPresent());
		return toGson.get().fromJson(jsonValue, ctx);
	}
	
	private AttributeExpType getDataType(JsonObject o){
		String dataTypeId = GsonUtil.getAsString(o, DATA_TYPE_PROPERTY, null);
		if (dataTypeId == null) {
			// TODO: properly infer data type
			dataTypeId = XacmlTypes.STRING.getDataTypeId();
		}
		Optional<AttributeExpType> type = XacmlTypes.getType(dataTypeId);
		Preconditions.checkState(type.isPresent());
		return type.get();
	}

}
