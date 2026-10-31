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

import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

public class ResponseContextAdapter implements JsonDeserializer<ResponseContext>, JsonSerializer<ResponseContext> {
	private static final String RESULT_PROPERTY = "Result";

	private static final Type RESULTS_TYPE = new TypeToken<Collection<Result>>() {
	}.getType();

	@Override
	public ResponseContext deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		Collection<Result> r = context.deserialize(o.get(RESULT_PROPERTY), RESULTS_TYPE);
		return ResponseContext.builder().results(r).build();
	}

	@Override
	public JsonElement serialize(ResponseContext src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		o.add(RESULT_PROPERTY, context.serialize(src.getResults()));
		return o;
	}

}
