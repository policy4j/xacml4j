package org.xacml4j.v30.marshal.json;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Type;
import java.util.Collection;

import org.xacml4j.v30.Advice;
import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.BaseDecisionRuleResponse;
import org.xacml4j.v30.Obligation;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

public class ObligationOrAdviceAdapter implements JsonSerializer<BaseDecisionRuleResponse>,
		JsonDeserializer<BaseDecisionRuleResponse> {

	private static final String ID_PROPERTY = "Id";
	private static final String ATTRIBUTE_ASSIGNMENTS_PROPERTY = "AttributeAssignment";

	private static final Type ATTRIBUTE_ASSIGNMENTS_TYPE = new TypeToken<Collection<AttributeAssignment>>() {
	}.getType();

	@Override
	public BaseDecisionRuleResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		String id = checkNotNull(GsonUtil.getAsString(o, ID_PROPERTY, null));

		Collection<AttributeAssignment> attributeAssignments = context.deserialize(
				o.get(ATTRIBUTE_ASSIGNMENTS_PROPERTY), ATTRIBUTE_ASSIGNMENTS_TYPE);

		if (typeOfT == Obligation.class) {
			Obligation.Builder builder = Obligation.builder(id);
			return attributeAssignments != null ? builder.attributes(attributeAssignments).build() : builder.build();
		} else if (typeOfT == Advice.class) {
			Advice.Builder builder = Advice.builder(id);
			return attributeAssignments != null ? builder.attributes(attributeAssignments).build() : builder.build();
		} else {
			throw new IllegalArgumentException(String.format("Invalid type: %s", typeOfT));
		}
	}

	@Override
	public JsonElement serialize(BaseDecisionRuleResponse src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject o = new JsonObject();
		o.addProperty(ID_PROPERTY, src.getId());
		Collection<AttributeAssignment> attributeAssignments = src.getAttributes();
		if (attributeAssignments != null && !attributeAssignments.isEmpty()) {
			o.add(ATTRIBUTE_ASSIGNMENTS_PROPERTY, context.serialize(attributeAssignments, ATTRIBUTE_ASSIGNMENTS_TYPE));
		}

		return o;
	}

}
