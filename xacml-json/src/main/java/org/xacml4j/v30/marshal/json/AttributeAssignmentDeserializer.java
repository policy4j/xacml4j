package org.xacml4j.v30.marshal.json;

import static com.google.common.base.Preconditions.checkArgument;
import static org.xacml4j.v30.marshal.json.AttributeProperties.ATTRIBUTE_ID_PROPERTY;
import static org.xacml4j.v30.marshal.json.AttributeProperties.DATA_TYPE_PROPERTY;
import static org.xacml4j.v30.marshal.json.AttributeProperties.ISSUER_PROPERTY;
import static org.xacml4j.v30.marshal.json.AttributeProperties.VALUE_PROPERTY;

import java.lang.reflect.Type;

import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.AttributeCategory;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.types.StringType;
import org.xacml4j.v30.types.Types;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class AttributeAssignmentDeserializer implements JsonDeserializer<AttributeAssignment> {

	private final Types typesRegistry;

	public AttributeAssignmentDeserializer(Types typesRegistry) {
		this.typesRegistry = typesRegistry;
	}

	@Override
	public AttributeAssignment deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject o = json.getAsJsonObject();

		String attrId = GsonUtil.getAsString(o, ATTRIBUTE_ID_PROPERTY, null);
		checkArgument(attrId != null, "Property '%s' is mandatory.", ATTRIBUTE_ID_PROPERTY);

		AttributeExp value = deserializeValue(context, o);
		AttributeCategory category = AttributeCategories.parse(GsonUtil.getAsString(o, "Category", null));
		String issuer = GsonUtil.getAsString(o, ISSUER_PROPERTY, null);

		return new AttributeAssignment(attrId, category, issuer, value);
	}

	private AttributeExp deserializeValue(JsonDeserializationContext context, JsonObject o) {
		String dataTypeId = GsonUtil.getAsString(o, DATA_TYPE_PROPERTY, null);
		if (dataTypeId == null) {
			// TODO: properly infer data type
			dataTypeId = StringType.STRING.getDataTypeId();
		}
		AttributeExpType type = typesRegistry.getType(dataTypeId);

		JsonElement jsonValue = o.get(VALUE_PROPERTY);
		// TODO: do a proper type coersion
		AttributeExp value = deserializePrimitiveValue(type, jsonValue);

		checkArgument(value != null, "Property '%s' is mandatory.", VALUE_PROPERTY);
		return value;
	}

	private AttributeExp deserializePrimitiveValue(AttributeExpType type, JsonElement jsonValue) {
		return type.fromXacmlString(jsonValue.getAsString());
	}

}
