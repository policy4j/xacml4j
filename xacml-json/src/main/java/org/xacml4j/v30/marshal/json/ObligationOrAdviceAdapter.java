package org.xacml4j.v30.marshal.json;

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

	@Override
	public BaseDecisionRuleResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject o = json.getAsJsonObject();
		String id = GsonUtil.getAsString(o, "Id", null);
		Collection<AttributeAssignment> attributeAssignments = context.deserialize(o.get("AttributeAssignment"),
				new TypeToken<Collection<AttributeAssignment>>() {
				}.getType());

		if (typeOfT == Obligation.class) {
			Obligation.Builder builder = Obligation.builder(id);
			return attributeAssignments != null ? builder.attributes(attributeAssignments).build() : builder.build();
		} else if (typeOfT == Advice.class) {
			Advice.Builder builder = Advice.builder(id);
			return attributeAssignments != null ? builder.attributes(attributeAssignments).create() : builder.create();
		} else {
			throw new IllegalArgumentException(String.format("Invalid type: %s", typeOfT));
		}
	}

	@Override
	public JsonElement serialize(BaseDecisionRuleResponse src, Type typeOfSrc, JsonSerializationContext context) {
		// TODO Auto-generated method stub
		return null;
	}

}
