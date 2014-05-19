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

import org.xacml4j.v30.pdp.BaseCompositeDecisionRuleIDReference;
import org.xacml4j.v30.pdp.PolicyIDReference;
import org.xacml4j.v30.pdp.PolicySetIDReference;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class IdReferenceAdapter implements JsonDeserializer<BaseCompositeDecisionRuleIDReference>,
		JsonSerializer<BaseCompositeDecisionRuleIDReference> {

	private static final String ID_PROPERTY = "Id";
	private static final String VERSION_PROPERTY = "Version";
	private static final String EARLIEST_VERSION_PROPERTY = "EarliestVersion";
	private static final String LATEST_VERSION_PROPERTY = "LatestVersion";

	@Override
	public BaseCompositeDecisionRuleIDReference deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		String id = checkNotNull(GsonUtil.getAsString(o, ID_PROPERTY, null));
		String version = GsonUtil.getAsString(o, VERSION_PROPERTY, null);
		String earliestVersion = GsonUtil.getAsString(o, EARLIEST_VERSION_PROPERTY, null);
		String latestVersion = GsonUtil.getAsString(o, LATEST_VERSION_PROPERTY, null);

		if (typeOfT == PolicyIDReference.class) {
			PolicyIDReference.Builder builder = PolicyIDReference.builder(id);
			return setVersions(builder, version, earliestVersion, latestVersion).build();
		}
		if (typeOfT == PolicySetIDReference.class) {
			PolicySetIDReference.Builder builder = PolicySetIDReference.builder(id);
			return setVersions(builder, version, earliestVersion, latestVersion).build();
		} else {
			throw new IllegalArgumentException(String.format("Invalid type: %s", typeOfT));
		}
	}

	private <T extends BaseCompositeDecisionRuleIDReference.Builder<T>> T setVersions(T builder, String version,
			String earliestVersion, String latestVersion) {
		if (version != null) {
			builder.versionAsString(version);
		}
		if (earliestVersion != null) {
			builder.earliest(earliestVersion);
		}
		if (latestVersion != null) {
			builder.latest(latestVersion);
		}

		return builder;
	}

	@Override
	public JsonElement serialize(BaseCompositeDecisionRuleIDReference src, Type typeOfSrc,
			JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		o.addProperty(ID_PROPERTY, src.getId());

		if (src.getVersion() != null) {
			o.addProperty(VERSION_PROPERTY, src.getVersion().getPattern());
		}
		if (src.getEarliestVersion() != null) {
			o.addProperty(EARLIEST_VERSION_PROPERTY, src.getEarliestVersion().getPattern());
		}
		if (src.getLatestVersion() != null) {
			o.addProperty(LATEST_VERSION_PROPERTY, src.getLatestVersion().getPattern());
		}

		return o;
	}

}
