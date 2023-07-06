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
import java.util.Optional;

import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCode;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class StatusAdapter implements JsonSerializer<Status>, JsonDeserializer<Status> {

	private static final String STATUS_MESSAGE_PROPERTY = "StatusMessage";
	private static final String STATUS_CODE_PROPERTY = "StatusCode";

	@Override
	public Status deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		String statusMessage = GsonUtil.getAsString(o, STATUS_MESSAGE_PROPERTY, null);
		StatusCode code = context.deserialize(o.get(STATUS_CODE_PROPERTY), StatusCode.class);
		return Status
				.builder(code)
				.message(statusMessage)
				.build();
	}

	@Override
	public JsonElement serialize(Status src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		Optional<String> m = src.getMessage();
		if(m.isPresent()){
			o.addProperty(STATUS_MESSAGE_PROPERTY, m.get());
		}
		// TODO: serialize status detail
		o.add(STATUS_CODE_PROPERTY, context.serialize(src.getStatusCode()));
		return o;
	}

}
