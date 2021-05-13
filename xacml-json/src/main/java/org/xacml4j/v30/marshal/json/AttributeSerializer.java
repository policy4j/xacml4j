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
import static com.google.common.base.Preconditions.checkState;
import static org.xacml4j.v30.marshal.json.JsonProperties.ATTRIBUTE_ID_PROPERTY;
import static org.xacml4j.v30.marshal.json.JsonProperties.DATA_TYPE_PROPERTY;
import static org.xacml4j.v30.marshal.json.JsonProperties.INCLUDE_IN_RESULT_PROPERTY;
import static org.xacml4j.v30.marshal.json.JsonProperties.ISSUER_PROPERTY;
import static org.xacml4j.v30.marshal.json.JsonProperties.VALUE_PROPERTY;

import java.lang.reflect.Type;
import java.util.Collection;

import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeExp;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

class AttributeSerializer implements JsonSerializer<Attribute>
{
	@Override
	public JsonElement serialize(Attribute src, Type typeOfSrc,
			JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		o.addProperty(ATTRIBUTE_ID_PROPERTY, src.getAttributeId());
		if (src.getIssuer() != null) {
			o.addProperty(ISSUER_PROPERTY, src.getIssuer());
		}
		Collection<AttributeExp> values = src.getValues();
		serializeValue(context, o, values);
		// OMIT property if value is "false"
		if(src.isIncludeInResult()){
			o.addProperty(INCLUDE_IN_RESULT_PROPERTY, src.isIncludeInResult());
		}
		return o;
	}

	private void serializeValue(JsonSerializationContext context, JsonObject o,
			Collection<AttributeExp> values) {
		AttributeExp firstValue = Iterables.getFirst(values, null);
        if(firstValue  == null){
            return;
        }
		o.addProperty(DATA_TYPE_PROPERTY, firstValue.getType().getShortDataTypeId());
		Optional<TypeToGSon> toGson = TypeToGSon.Types.getIndex().get(firstValue.getType());
		checkState(toGson.isPresent());
		if(values.size() == 1){
			o.add(VALUE_PROPERTY, toGson.get().toJson(firstValue, context));
			return;
		}
		JsonArray array = new JsonArray();
		for(AttributeExp a : values){
			checkArgument(firstValue.getType().equals(a.getType()));
			array.add(toGson.get().toJson(a, context));
		}
		o.add(VALUE_PROPERTY, array);
	}
}
