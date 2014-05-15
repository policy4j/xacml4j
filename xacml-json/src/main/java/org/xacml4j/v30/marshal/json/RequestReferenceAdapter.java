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

import java.lang.reflect.Type;
import java.util.Collection;

import org.xacml4j.v30.CategoryReference;
import org.xacml4j.v30.RequestReference;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

final class RequestReferenceAdapter implements JsonDeserializer<RequestReference>, JsonSerializer<RequestReference> {
	private static final String REFERENCE_ID_PROPERTY = "ReferenceId";

	@Override
	public RequestReference deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		Collection<CategoryReference> refs = context.deserialize(o.getAsJsonArray(REFERENCE_ID_PROPERTY),
				new TypeToken<Collection<CategoryReference>>() {
				}.getType());
		return RequestReference.builder().reference(refs).build();
	}

	@Override
	public JsonElement serialize(RequestReference src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		o.add(REFERENCE_ID_PROPERTY, context.serialize(src.getReferencedCategories()));
		return o;
	}

}
