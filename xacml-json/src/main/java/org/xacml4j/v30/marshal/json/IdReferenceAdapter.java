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

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Type;

import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.pdp.PolicyIDReference;
import org.xacml4j.v30.pdp.PolicySetIDReference;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class IdReferenceAdapter implements JsonDeserializer<CompositeDecisionRuleIDReference>,
		JsonSerializer<CompositeDecisionRuleIDReference> {

	private static final String ID_PROPERTY = "Id";
	private static final String VERSION_PROPERTY = "Version";

	@Override
	public CompositeDecisionRuleIDReference deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		String id = checkNotNull(GsonUtil.getAsString(o, ID_PROPERTY, null));
		String version = GsonUtil.getAsString(o, VERSION_PROPERTY, null);
		if (typeOfT == PolicyIDReference.class) {
            return PolicyIDReference.builder().id(id).versionAsString(version).build();
		}
		if (typeOfT == PolicySetIDReference.class) {
            return PolicySetIDReference.builder().id(id).versionAsString(version).build();
        }
        throw new IllegalArgumentException(String.format("Invalid type: %s", typeOfT));
	}


	@Override
	public JsonElement serialize(CompositeDecisionRuleIDReference src, Type typeOfSrc,
			JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		o.addProperty(ID_PROPERTY, src.getId());
        o.addProperty(VERSION_PROPERTY, src.getVersion().getPattern());
		return o;
	}

}
