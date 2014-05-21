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

import org.xacml4j.v30.StatusCode;
import org.xacml4j.v30.StatusCodeIds;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class StatusCodeAdapter implements JsonSerializer<StatusCode>, JsonDeserializer<StatusCode> {

	private static final String VALUE_PROPERTY = "Value";
	private static final String STATUS_CODE_PROPERTY = "StatusCode";

	@Override
	public StatusCode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		String value = GsonUtil.getAsString(o, VALUE_PROPERTY, null);
		checkNotNull(value);
		StatusCode.Builder builder = StatusCode.builder(StatusCodeIds.parse(value));

		StatusCode embeddedStatusCode = context.deserialize(o.getAsJsonObject(STATUS_CODE_PROPERTY), StatusCode.class);
		if (embeddedStatusCode != null) {
			builder.minorStatus(embeddedStatusCode);
		}

		return builder.build();
	}

	@Override
	public JsonElement serialize(StatusCode src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		o.addProperty(VALUE_PROPERTY, src.getValue().toString());
		if (src.getMinorStatus() != null) {
			o.add(STATUS_CODE_PROPERTY, context.serialize(src.getMinorStatus()));
		}

		return o;
	}

}
