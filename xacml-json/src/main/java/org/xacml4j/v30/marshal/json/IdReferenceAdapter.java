package org.xacml4j.v30.marshal.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.xacml4j.v30.pdp.BaseCompositeDecisionRuleIDReference;
import org.xacml4j.v30.pdp.BaseCompositeDecisionRuleIDReference.BaseCompositeDecisionRuleIDReferenceBuilder;
import org.xacml4j.v30.pdp.PolicyIDReference;
import org.xacml4j.v30.pdp.PolicySetIDReference;

import java.lang.reflect.Type;

import static com.google.common.base.Preconditions.checkNotNull;

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

	private <T extends BaseCompositeDecisionRuleIDReferenceBuilder<T>> T setVersions(T builder, String version,
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
