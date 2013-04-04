package org.xacml4j.v30.marshal.json;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Type;

import org.xacml4j.v30.pdp.BaseCompositeDecisionRuleIDReference;
import org.xacml4j.v30.pdp.BaseCompositeDecisionRuleIDReference.BaseCompositeDecisionRuleIDReferenceBuilder;
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

	@Override
	public JsonElement serialize(BaseCompositeDecisionRuleIDReference src, Type typeOfSrc,
			JsonSerializationContext context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseCompositeDecisionRuleIDReference deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		String id = checkNotNull(GsonUtil.getAsString(o, "Id", null));
		String version = GsonUtil.getAsString(o, "Version", null);
		String earliestVersion = GsonUtil.getAsString(o, "EarliestVersion", null);
		String latestVersion = GsonUtil.getAsString(o, "LatestVersion", null);

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

	private <T extends BaseCompositeDecisionRuleIDReferenceBuilder<T>> T setVersions(T builder, String version,
			String earliestVersion, String latestVersion) {
		if (version != null) {
			builder.version(version);
		}
		if (earliestVersion != null) {
			builder.earliest(earliestVersion);
		}
		if (latestVersion != null) {
			builder.latest(latestVersion);
		}

		return builder;
	}

}
